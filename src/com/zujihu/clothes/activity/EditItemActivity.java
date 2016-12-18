package com.zujihu.clothes.activity;

import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.zujihu.clothes.R;
import com.zujihu.clothes.common.AnimationFactory;
import com.zujihu.clothes.common.Constant;
import com.zujihu.clothes.data.DatabaseHelper;
import com.zujihu.clothes.data.PictureModel;
import com.zujihu.clothes.util.Utils;

public class EditItemActivity extends Activity implements OnClickListener {

	private View		mWardView;
	private View		mShowMenuView;
	private View		mEditMenuView;
	private View		mEditView;
	private View		mCutView;
	private ImageView	mShowImageView;
	private int			mItemId	= 0;
	private Bitmap		mBitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_item_view);
		initComplete();
	}

	private void initComplete() {
		initView();
		setClick();
		initData();
	}

	private void initView() {
		mWardView = findViewById(R.id.wardrob_btn);
		mShowMenuView = findViewById(R.id.show_menu_view);
		mEditMenuView = findViewById(R.id.edit_item_menu_layout);
		mEditView = findViewById(R.id.edit_view);
		mCutView = findViewById(R.id.cut_view);

		mShowImageView = (ImageView) findViewById(R.id.show_item_view);
	}

	private void setClick() {
		mWardView.setOnClickListener(this);
		mShowMenuView.setOnClickListener(this);
		mEditView.setOnClickListener(this);
		mCutView.setOnClickListener(this);
		mShowImageView.setOnClickListener(this);
	}

	private void initData() {
		mItemId = getIntent().getIntExtra(Constant.ITEM_DATA_ID, 0);
		DatabaseHelper dbHelper = new DatabaseHelper(this);
		PictureModel pModel = dbHelper.searchItemById(mItemId);
		mBitmap = Utils.getSdcardImage(pModel.url);
		if (pModel.mask_path != null) {
			String pathString = Environment.getExternalStorageDirectory() + pModel.mask_path;
			byte[] zippedMask = Utils.readMaskBySdcard(pathString);
			int width = mBitmap.getWidth();
			int height = mBitmap.getHeight();
			int pixelCount = width * height;
			try {
				byte[] mask = Utils.unzipMask(zippedMask, pixelCount);
				Bitmap bitmap = Utils.maskBitmap(mBitmap, mask);
				mShowImageView.setImageBitmap(bitmap);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			mShowImageView.setImageBitmap(mBitmap);
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.wardrob_btn:
				finish();
				overridePendingTransition(0, R.anim.scale_out);
				break;
			case R.id.show_menu_view:
				if (mEditMenuView.getVisibility() == View.GONE) {
					showMenu(mEditMenuView);
				}
				else {
					hiddenMenu(mEditMenuView);
				}
				break;
			case R.id.edit_view:
				break;
			case R.id.cut_view:
				break;
			case R.id.show_item_view:
				break;
			default:
				break;
		}

	}

	private void showMenu(View view) {
		Animation loadAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_down);
		view.setVisibility(View.VISIBLE);
		view.startAnimation(loadAnimation);
	}

	private void hiddenMenu(final View view) {
		Animation loadAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_up);
		loadAnimation.setAnimationListener(new AnimationFactory.ZujiHuAnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				view.setVisibility(View.GONE);
			}
		});
		view.startAnimation(loadAnimation);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mBitmap!=null&&!mBitmap.isRecycled()) {
			mBitmap.recycle();
		}
	}

}
