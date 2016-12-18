package com.zujihu.clothes.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.zujihu.clothes.R;
import com.zujihu.clothes.common.Constant;
import com.zujihu.clothes.data.DatabaseHelper;
import com.zujihu.clothes.data.PictureModel;
import com.zujihu.clothes.data.SuitAdapter;
import com.zujihu.clothes.view.SuitEditView;

/**
 * 
 * @author WangZhong
 * 
 */
public class SuitActivity extends Activity implements OnClickListener {
	private SuitEditView		mSuitEditView;
	private LinearLayout		mEditItemView;
	private View				mSaveView;
	private View				mBackView;

	private ArrayList<String>	mImageUrlArrayList	= new ArrayList<String>();
	private Bitmap				bitmap				= null;
	private List<Float[]>		mDataFloats			= new ArrayList<Float[]>();
	private String				url					= null;
	private int					_id					= 0;
	private boolean				isAddOrEdit			= false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.suit_view);
		isAddOrEdit = getIntent().getBooleanExtra(SuitAdapter.SUITUEDITORADD, false);
		if (isAddOrEdit) {
			initData();
		}
		else
			mImageUrlArrayList = getIntent().getStringArrayListExtra("lists");
		initComplete();
	}

	private void initData() {
		Float[] mFloats = null;
		_id = getIntent().getIntExtra("_id", 0);
		url = getIntent().getStringExtra("url");
		String url = getIntent().getStringExtra("data");
		String[] aStrings = url.split("\\;");
		if (aStrings != null) {
			int n = aStrings.length;
			for (int i = 0; i < n; i++) {
				String[] bStrings = aStrings[i].trim().split("\\,");
				int h = bStrings.length;
				mFloats = new Float[5];
				for (int j = 1; j < h; j++) {
					mFloats[j - 1] = Float.valueOf(bStrings[j]);
				}
				mImageUrlArrayList.add(bStrings[0].trim());
				mDataFloats.add(mFloats);
			}
		}
	}

	private void initComplete() {
		findViews();
		setClick();
	}

	private void findViews() {
		mEditItemView = (LinearLayout) findViewById(R.id.edit_item_layout);
		mSaveView = findViewById(R.id.save_btn);
		mBackView = findViewById(R.id.back_btn);
		if (isAddOrEdit)
			mSuitEditView = new SuitEditView(this, mImageUrlArrayList, mDataFloats);
		else
			mSuitEditView = new SuitEditView(this, mImageUrlArrayList, null);
		mEditItemView.addView(mSuitEditView);
	}

	private void setClick() {
		mSaveView.setOnClickListener(this);
		mBackView.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSuitEditView.loadImages(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSuitEditView.unloadImages();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			mSuitEditView.trackballClicked();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.save_btn:
				saveOrUpdateItem();
				break;
			case R.id.back_btn:
				finish();
				break;
			default:
				break;
		}
	}

	private void saveOrUpdateItem() {
		mEditItemView.setDrawingCacheEnabled(true);
		bitmap = Bitmap.createBitmap(mEditItemView.getDrawingCache());
		mEditItemView.setDrawingCacheEnabled(false);
		if (url == null) {
			long tiems = System.currentTimeMillis();
			url = Environment.getExternalStorageDirectory() + Constant.SUIT_PATH + "/s_" + tiems
					+ ".jpg";
		}
		File file = new File(url);

		try {
			file.createNewFile();
			FileOutputStream ostream = new FileOutputStream(file);
			bitmap.compress(CompressFormat.JPEG, 100, ostream);
			ostream.flush();
			ostream.close();
			if (isAddOrEdit)
				updateData(file.toString());
			else
				saveData(file.toString());
			finish();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveData(String url) {
		DatabaseHelper db = new DatabaseHelper(this);
		db.createTableSuits();
		PictureModel pModel = mSuitEditView.savePicData();
		pModel.url = url + "|" + pModel.url;
		db.addSuit(pModel);
	}

	private void updateData(String url) {
		DatabaseHelper db = new DatabaseHelper(this);
		PictureModel pModel = mSuitEditView.savePicData();
		pModel._id = _id;
		pModel.url = url + "|" + pModel.url;
		db.updateSuit(pModel);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		recycle();
	}

	private void recycle() {
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
		}
	}

}
