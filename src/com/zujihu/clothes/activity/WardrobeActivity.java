package com.zujihu.clothes.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;

import com.zujihu.clothes.R;
import com.zujihu.clothes.common.Constant;
import com.zujihu.clothes.data.DatabaseHelper;
import com.zujihu.clothes.data.ItemsAdapter;
import com.zujihu.clothes.data.PictureModel;
import com.zujihu.clothes.data.SuitAdapter;
import com.zujihu.clothes.util.Utils;

public class WardrobeActivity extends Activity {

	private GridView					itemsGrid;
	private Button						okButton;
	private Button						resetButton;

	private GridView					showItemGridView;
	private AlertDialog					showSingleProductDialog;
	private View						showSingleProductDialogView;

	public static ArrayList<Integer>	singleProductSelectArr	= new ArrayList<Integer>();

	public List<PictureModel>			mImageIds				= new ArrayList<PictureModel>();
	public List<PictureModel>			mAddItems				= new ArrayList<PictureModel>();
	private ArrayList<String>			mSelectImagesIdList		= new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wardrobe_view);
		initComplete();
		Utils.createSdcardFile(this, Constant.SUIT_PATH);
	}

	private void initComplete() {
		getDataPic();
		findView();
		initItemGridView();
	}

	private void initSelectedItemsArrayList() {
		for (int i = 0; i < mAddItems.size(); i++) {
			singleProductSelectArr.add(i, 0);
		}
	}

	private void findView() {
		showItemGridView = (GridView) findViewById(R.id.wardrobe_gridview);
	}

	private void initItemGridView() {
		showItemGridView.setAdapter(new SuitAdapter(WardrobeActivity.this, mImageIds));
	}

	// collection
	public void showDialog() {
		showSingleProductDialog = new AlertDialog.Builder(WardrobeActivity.this).create();
		showSingleProductDialogView = View.inflate(WardrobeActivity.this,
			R.layout.item_grid_dialog, null);
		itemsGrid = (GridView) (showSingleProductDialogView).findViewById(R.id.single_product_grid);
		okButton = (Button) (showSingleProductDialogView).findViewById(R.id.Ok_Button);
		resetButton = (Button) (showSingleProductDialogView).findViewById(R.id.Reset_Button);

		getSdcardItemFileImage();
		itemsGrid.setAdapter(new ItemsAdapter(WardrobeActivity.this, singleProductSelectArr,
			mAddItems));
		showSingleProductDialog.setView(showSingleProductDialogView);
		showSingleProductDialog.show();

		itemsGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
				if (singleProductSelectArr.get(position) == 0) {
					view.setBackgroundResource(R.drawable.layout_border_select);
					singleProductSelectArr.set(position, position + 1);
					if (!mSelectImagesIdList.contains(mAddItems.get(position).url)) {
						mSelectImagesIdList.add(0, mAddItems.get(position).url);
					}
				}
				else {
					view.setBackgroundDrawable(null);
					singleProductSelectArr.set(position, 0);
					mSelectImagesIdList.remove(mSelectImagesIdList.indexOf(mAddItems.get(position).url));
				}
			}
		});

		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mSelectImagesIdList.size() > 0) {
					recycleNoSelectedImage();
					Intent intent = new Intent(WardrobeActivity.this, SuitActivity.class);
					intent.putStringArrayListExtra("lists", mSelectImagesIdList);
					startActivityForResult(intent, SuitAdapter.SAVE_RESULT_ID);
					overridePendingTransition(R.anim.scale_in, R.anim.scale_out);
				}
				showSingleProductDialog.dismiss();
			}
		});

		resetButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				singleProductSelectArr.clear();
				initSelectedItemsArrayList();
				mSelectImagesIdList.clear();
				itemsGrid.setAdapter(new ItemsAdapter(WardrobeActivity.this,
					singleProductSelectArr, mAddItems));
			}
		});

	}

	private void getDataPic() {
		DatabaseHelper db = new DatabaseHelper(this);
		mImageIds = db.getAllSuit();
	}

	private void getSdcardItemFileImage() {
		mAddItems.clear();
		singleProductSelectArr.clear();
		mSelectImagesIdList.clear();
		recycleItem();
		String imgDirPathString = Environment.getExternalStorageDirectory() + File.separator
				+ "vask/items";
		mAddItems = Utils.getSdcardFileImage(imgDirPathString);
		initSelectedItemsArrayList();
	}

	public void refresh() {
		recycle();
		getDataPic();
		initItemGridView();
	}

	private void recycleNoSelectedImage() {
		int n = mAddItems.size();
		if (n > 0) {
			for (int i = 0; i < n; i++) {
				if (singleProductSelectArr.get(i) == 0) {
					if (mAddItems.get(i).bitmap != null && !mAddItems.get(i).bitmap.isRecycled()) {
						mAddItems.get(i).bitmap.recycle();
					}
				}
			}
		}
	}

	private void recycle() {
		if (mImageIds.size() > 0) {
			for (PictureModel imageData : mImageIds) {
				if (imageData.bitmap != null && !imageData.bitmap.isRecycled()) {
					imageData.bitmap.recycle();
				}
			}
		}
	}

	private void recycleItem() {
		if (mAddItems.size() > 0) {
			for (PictureModel imageData : mAddItems) {
				if (imageData.bitmap != null && !imageData.bitmap.isRecycled()) {
					imageData.bitmap.recycle();
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		recycle();
		recycleItem();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SuitAdapter.SAVE_RESULT_ID) {
			refresh();
		}
	}

}