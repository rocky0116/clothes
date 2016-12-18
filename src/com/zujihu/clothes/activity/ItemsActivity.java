package com.zujihu.clothes.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;

import com.zujihu.clothes.R;
import com.zujihu.clothes.common.Constant;
import com.zujihu.clothes.data.DatabaseHelper;
import com.zujihu.clothes.data.ItemsAdapter;
import com.zujihu.clothes.data.PictureModel;
import com.zujihu.clothes.util.Utils;
import com.zujihu.clothes.view.ShelvesView;

public class ItemsActivity extends Activity implements OnClickListener {

	private ShelvesView			mShowItemGridView;
	private View				mUpperOuterView;
	private View				mPantsView;
	private View				mSkirtView;
	private View				mShoesView;
	private View				mBagsView;
	private View				mDecorationsView;
	private ItemsAdapter		mItemsAdapter		= null;
	private boolean				mIsShowDeleteView	= false;

	private List<PictureModel>	mItems				= new ArrayList<PictureModel>();
	private ArrayList<Integer>	mIds				= new ArrayList<Integer>();
	private DatabaseHelper		db					= null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.clothes_view);
		db = new DatabaseHelper(this);
		initComplete();
	}

	private void initComplete() {
		initView();
		setClick();
		initData();
	}

	private void initView() {
		mShowItemGridView = (ShelvesView) findViewById(R.id.show_singleProduct_gridview);

		mUpperOuterView = findViewById(R.id.upper_outer_garment_view);
		mPantsView = findViewById(R.id.pants_view);
		mSkirtView = findViewById(R.id.skirt_view);
		mShoesView = findViewById(R.id.shoes_view);
		mBagsView = findViewById(R.id.bags_view);
		mDecorationsView = findViewById(R.id.decorations_view);
	}

	private void setClick() {
		gridClick();
		mUpperOuterView.setOnClickListener(this);
		mPantsView.setOnClickListener(this);
		mSkirtView.setOnClickListener(this);
		mShoesView.setOnClickListener(this);
		mBagsView.setOnClickListener(this);
		mDecorationsView.setOnClickListener(this);
	}

	private void initData() {
		Utils.createSdcardFile(this, Constant.ITEM_PATH);
		getItems();
		initItemGridView();
	}

	private void initItemGridView() {
		if (mItems != null) {
			mItemsAdapter = new ItemsAdapter(ItemsActivity.this, null, mItems);
			mShowItemGridView.setAdapter(mItemsAdapter);
		}
	}

	private void gridClick() {
		mShowItemGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				hiddenAddView();
				if (mIsShowDeleteView) {
					if (mItems.size() > 0) {
						ImageView iv = (ImageView) view.findViewById(R.id.select_image);
						try {
							if (!mIds.contains(mItems.get(position)._id)) {
								iv.setImageResource(R.drawable.icon_45_on_2x);
								mIds.add(mItems.get(position)._id);
							}
							else {
								iv.setImageResource(R.drawable.icon_45_2x);
								mIds.remove(mIds.indexOf(mItems.get(position)._id));
							}
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				else {
					Intent intent = new Intent(ItemsActivity.this, EditItemActivity.class);
					intent.putExtra(Constant.ITEM_DATA_ID, mItems.get(position)._id);
					startActivityForResult(intent, 2);
					overridePendingTransition(R.anim.scale_in, R.anim.scale_out);
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		hiddenAddView();
		switch (v.getId()) {
			case R.id.upper_outer_garment_view:
				showDiffType(Constant.UPPER_OUTER_TYPE_NO);
				break;
			case R.id.pants_view:
				showDiffType(Constant.PANTS_TYPE_NO);
				break;
			case R.id.skirt_view:
				showDiffType(Constant.SKIRT_TYPE_NO);
				break;
			case R.id.shoes_view:
				showDiffType(Constant.SHOES_TYPE_NO);
				break;
			case R.id.bags_view:
				showDiffType(Constant.BAGS_TYPE_NO);
				break;
			case R.id.decorations_view:
				showDiffType(Constant.DECORATIONS_TYPE_NO);
				break;

			default:
				break;
		}
	}

	private void hiddenAddView() {
		ClothesTabActivity clothesTabActivity = (ClothesTabActivity) getParent();
		if (clothesTabActivity != null) {
			clothesTabActivity.isShowAddView();
		}
	}

	private void chanageTitle(int type) {
		ClothesTabActivity clothesTabActivity = (ClothesTabActivity) getParent();
		if (clothesTabActivity != null) {
			clothesTabActivity.showTitle(type);
		}
	}

	private void showDiffType(int type) {
		mItems = db.getItemsByType(type);
		chanageTitle(type);
		initItemGridView();
	}

	public void addItem(PictureModel imageData) {
		initItemGridView();
	}

	private void getItems() {
		mItems = db.getItemsByType(Constant.UPPER_OUTER_TYPE_NO);
	}

	private void recycle() {
		if (mItems.size() > 0) {
			for (PictureModel imageData : mItems) {
				if (imageData.bitmap != null && !imageData.bitmap.isRecycled()) {
					imageData.bitmap.recycle();
				}
			}
		}
	}

	public void deleteItem(boolean isDelete) {
		if (isDelete) {
			if (mIds.size() > 0) {
				String ids = "";
				for (int i = 0; i < mIds.size(); i++) {
					ids = ids + "," + mIds.get(i);
				}
				db.deleteItemsByIds(ids.substring(0, ids.length() - 1));
			}
		}
		else {
			if (mItemsAdapter != null) {
				if (mIsShowDeleteView) {
					mItemsAdapter.showSelectView(mIsShowDeleteView);
					mIsShowDeleteView = false;
				}
				else {
					mItemsAdapter.showSelectView(mIsShowDeleteView);
					mIsShowDeleteView = true;
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		recycle();
		mShowItemGridView.recycleBitmap();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
}
