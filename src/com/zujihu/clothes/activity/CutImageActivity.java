package com.zujihu.clothes.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.zujihu.clothes.R;
import com.zujihu.clothes.util.PictureAcquire;
import com.zujihu.clothes.view.MaskingImageView;
import com.zujihu.clothes.view.ShowIconView;

public class CutImageActivity extends Activity implements OnClickListener, OnSeekBarChangeListener,
	OnTouchListener {
	private static final String	TAG				= "Sample::Activity";

	private MaskingImageView	mImageView;
	private int					mode			= 0;

	private View				mBackView;
	private View				mCameraView;
	private View				mFileView;
	private SeekBar				mSeekBar;
	private View				mNarrowView;
	private View				mExpandView;
	private ImageView			mMagicWandView;
	private ImageView			mEraserView;
	private ImageView			mUndoView;
	private ImageView			mEraserjView;
	private ImageView			mSaveView;
	private ShowIconView		mShowIconView;
	private Bitmap				mFloodBitmap;
	private Bitmap				mCutBitmap;
	private String				mUrl			= null;
	private int					mBitmapHeight	= 0;

	private float				lastX, lastY;
	private int					eraseRadius		= 20;

	// //////////////////////////////////////////////////zoom
	private Matrix				mMatrix			= new Matrix();
	private Matrix				savedMatrix		= new Matrix();

	float						minScaleR;								// 最小缩放比例
	static final float			MAX_SCALE		= 4f;					// 最大缩放比例

	PointF						prev			= new PointF();
	float						dist			= 1f;
	// Remember some things for zooming
	private PointF				mid				= new PointF();

	static final int			NONE			= 3;
	static final int			DRAG			= 4;
	static final int			ZOOM			= 5;

	private boolean				mIsChoiceMove	= false;

	public CutImageActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dispose_view);
		initComplete();

		mUrl = getIntent().getStringExtra(ClothesTabActivity.ADD_URL_STRING);
		if (mUrl != null) {
			mCutBitmap = PictureAcquire.loadImageWithRotation(
				PictureAcquire.urlToFilePath(this, mUrl), 480, Bitmap.Config.ARGB_8888);
			if (mCutBitmap != null) {
				mImageView.setBitmap(mCutBitmap, null);
			}
		}
	}

	private void initComplete() {
		findView();
		setClick();
		initData();
	}

	private void findView() {
		mImageView = (MaskingImageView) findViewById(R.id.image_view);
		mShowIconView = (ShowIconView) findViewById(R.id.icon_image);

		mBackView = findViewById(R.id.back_view);
		mCameraView = findViewById(R.id.camera_view);
		mFileView = findViewById(R.id.file_view);
		mSeekBar = (SeekBar) findViewById(R.id.player_seekbar);
		mNarrowView = findViewById(R.id.narrow_view);
		mExpandView = findViewById(R.id.expand_view);
		mMagicWandView = (ImageView) findViewById(R.id.magicWand_view);
		mEraserView = (ImageView) findViewById(R.id.eraser_view);
		mUndoView = (ImageView) findViewById(R.id.undo_view);
		mEraserjView = (ImageView) findViewById(R.id.eraserj_view);
		mSaveView = (ImageView) findViewById(R.id.save_view);
	}

	private void setClick() {
		mBackView.setOnClickListener(this);
		mCameraView.setOnClickListener(this);
		mFileView.setOnClickListener(this);
		mSeekBar.setOnSeekBarChangeListener(this);
		mNarrowView.setOnClickListener(this);
		mExpandView.setOnClickListener(this);
		mMagicWandView.setOnClickListener(this);
		mEraserView.setOnClickListener(this);
		mUndoView.setOnClickListener(this);
		mEraserjView.setOnClickListener(this);
		mSaveView.setOnClickListener(this);

		mImageView.setOnTouchListener(this);
	}

	private void initData() {
		mFloodBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_27_2x);
		mBitmapHeight = mFloodBitmap.getHeight();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				if (mIsChoiceMove) {
					mImageView.saveState();
				}
				else {
					savedMatrix.set(mMatrix);
					prev.set(event.getX(), event.getY());
					mode = DRAG;
				}
				break;

			// 副点按下
			case MotionEvent.ACTION_POINTER_DOWN:
				dist = spacing(event);
				// 如果连续两点距离大于10，则判定为多点模式
				if (dist > 10f) {
					savedMatrix.set(mMatrix);
					midPoint(mid, event);
					mode = ZOOM;
				}
				break;

			case MotionEvent.ACTION_MOVE:
				if (mIsChoiceMove) {
					lastX = event.getX();
					lastY = event.getY();
					if (mode == 0) {
						// Flood fill the current mask
						mShowIconView.setImage((int) event.getX(), (int) event.getY()
								- mBitmapHeight, mFloodBitmap);
					}
					else if (mode == 1) {
						// Erase the current mask
						mImageView.erase((int) lastX, (int) lastY,
							(int) (event.getPressure() * eraseRadius));
					}
					else {
						mImageView.eraseJ((int) lastX, (int) lastY,
							(int) (event.getPressure() * eraseRadius));
					}
					mImageView.magifyingGlassEffect((int) event.getX(), (int) event.getY(), true);
					mImageView.invalidate();
				}
				else {
					if (mode == DRAG) {
						mMatrix.set(savedMatrix);
						mMatrix.postTranslate(event.getX() - prev.x, event.getY() - prev.y);
					}
					else if (mode == ZOOM) {
						float newDist = spacing(event);
						if (newDist > 10f) {
							mMatrix.set(savedMatrix);
							float tScale = newDist / dist;
							float matrixScale = mMatrix.mapRadius(1);
							if (tScale * matrixScale > MAX_SCALE)
								tScale = MAX_SCALE / matrixScale;

							mMatrix.postScale(tScale, tScale, mid.x, mid.y);
						}
					}
				}

				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				if (mIsChoiceMove) {
					mImageView.magifyingGlassEffect((int) event.getX(), (int) event.getY(), false);
					mImageView.invalidate();
				}
				else {
					mode = NONE;
					Log.d("Motion", "Action: " + event.getAction());
				}
				break;
		}
		if (!mIsChoiceMove) {
			mImageView.bitmapMatrix = mMatrix;
			mImageView.invalidate();
			mImageView.setImageMatrix(mMatrix);
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.back_view:
				mIsChoiceMove = false;
				// finish();
				// overridePendingTransition(0, R.anim.scale_out);
				break;
			case R.id.camera_view:
				PictureAcquire.getImageFromCamera(CutImageActivity.this);
				break;
			case R.id.file_view:
				PictureAcquire.getImageFromLocal(CutImageActivity.this);
				break;
			case R.id.player_seekbar:
				break;
			case R.id.narrow_view:
				int newProgress = mSeekBar.getProgress() - 1;
				if (newProgress >= 0) {
					setFloodFill(newProgress);
				}
				break;
			case R.id.expand_view:
				newProgress = mSeekBar.getProgress() + 1;
				if (newProgress <= 100) {
					mSeekBar.setProgress(newProgress);
					setFloodFill(newProgress);
				}
				break;
			case R.id.magicWand_view:
				mIsChoiceMove = true;
				mShowIconView.setVisibility(View.VISIBLE);
				mode = 0;
				break;
			case R.id.eraserj_view:
				mShowIconView.setVisibility(View.GONE);
				mode = 2;
				break;
			case R.id.eraser_view:
				mShowIconView.setVisibility(View.GONE);
				mode = 1;
				break;
			case R.id.undo_view:
				mImageView.restoreState();
				break;
			case R.id.save_view:
				mImageView.saveImageToSdcard();
				finish();
				break;
			default:
				break;
		}

		changeViewDrawable(v);

	}

	private void setFloodFill(int progress) {
		if (mode == 0) {
			// Flood fill the current mask
			mImageView.floodFill((int) lastX, (int) lastY, progress);
		}
		else {
			eraseRadius = progress;
		}
		mImageView.invalidate();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;
		switch (requestCode) {
			case PictureAcquire.TAKE_PICTURE: {// 照相后
				Bitmap newBitmap = PictureAcquire.loadCameraFile(480, Bitmap.Config.ARGB_8888);
				mImageView.setBitmap(newBitmap, null);
				break;
			}
			case PictureAcquire.SELECT_PICTURE: {// 本地选取
				String fileUrl = null;
				if (data.getDataString() != null) {
					fileUrl = data.getDataString();
				}
				else if (data.getAction() != null) {
					fileUrl = data.getAction();
				}

				if (fileUrl == null)
					return;
				Bitmap newBitmap = PictureAcquire.loadImageWithRotation(
					PictureAcquire.urlToFilePath(this, fileUrl), 480, Bitmap.Config.ARGB_8888);
				mImageView.setBitmap(newBitmap, null);
				break;
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mFloodBitmap != null && !mFloodBitmap.isRecycled()) {
			mFloodBitmap.recycle();
		}
		if (mCutBitmap != null && !mCutBitmap.isRecycled()) {
			mCutBitmap.recycle();
		}
	}

	private void doGrabCut() {
		new AsyncTask<Object, Integer, Integer>() {
			@Override
			protected void onPreExecute() {
				CutImageActivity.this.showDialog(0);
			}

			@Override
			protected Integer doInBackground(Object... arg0) {
				mImageView.grubCut();
				return null;
			}

			@Override
			protected void onPostExecute(Integer result) {
				CutImageActivity.this.dismissDialog(0);
			}
		}.execute(this);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return new AlertDialog.Builder(CutImageActivity.this).setTitle("正在处理图像...").create();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		setFloodFill(progress);
		mImageView.invalidate();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}

	private void changeViewDrawable(View v) {
		mMagicWandView.setImageResource(R.drawable.icon_22_2x);
		mEraserjView.setImageResource(R.drawable.icon_23_2x);
		mEraserView.setImageResource(R.drawable.icon_24_2x);
		mUndoView.setImageResource(R.drawable.icon_25_2x);
		mSaveView.setImageResource(R.drawable.icon_21_2x);
		switch (v.getId()) {
			case R.id.magicWand_view:
				mMagicWandView.setImageResource(R.drawable.icon_22_on_2x);
				break;
			case R.id.eraserj_view:
				mEraserjView.setImageResource(R.drawable.icon_23_on_2x);
				break;
			case R.id.eraser_view:
				mEraserView.setImageResource(R.drawable.icon_24_on_2x);
				break;
			case R.id.undo_view:
				mUndoView.setImageResource(R.drawable.icon_26_2x);
				break;
			case R.id.save_view:
				mSaveView.setImageResource(R.drawable.icon_21_on_2x);
				break;
			default:
				break;
		}
	}

	/**
	 * 两点的距离
	 */
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/**
	 * 两点的中点
	 */
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	private void setZoom(float scale) {
		float matrixScale = mMatrix.mapRadius(1);
		mMatrix.postScale(scale / matrixScale, scale / matrixScale);
	}
}
