package com.zujihu.clothes.activity;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.zujihu.clothes.R;
import com.zujihu.clothes.common.AnimationFactory;
import com.zujihu.clothes.common.Constant;
import com.zujihu.clothes.data.SuitAdapter;
import com.zujihu.clothes.util.PictureAcquire;
import com.zujihu.clothes.util.Utils;

public class ClothesTabActivity extends TabActivity implements OnClickListener {
	private final int			SINGLEPRODUCT_TAB_INDEX	= 0;
	private final int			COLLOCATION_TAB_INDEX	= 1;
	public static final String	ADD_URL_STRING			= "ADD_BITMAP_STRING";
	private ImageView			mSingleProductView;
	private ImageView			mCollocationView;
	private ImageView			mDeleteView;
	private View				mAddView;
	private TabHost				mTabHost;
	private TextView			mTitleTextView;
	private ViewFlipper			mFlipper;
	private View				mAddCameraView;
	private View				mAddFileView;
	private View				mParentView;

	public static final int		ADD_ITEM_REQUEST_CODE	= 13;
	private boolean				mIsDelete				= false;

	// private View mTabLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.clothes_tab);
		Utils.createSdcardFile(this, Constant.DB_PATH);
		initComponents();
		Intent intent = this.getIntent();
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				if (bundle.getBoolean("singleProduct") == true) {
					switchTab(false);
				}
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	private void initTabHost() {
		mTabHost = getTabHost();
		mTabHost.setup(this.getLocalActivityManager());
		mTabHost.addTab(mTabHost.newTabSpec("singleProduct")
			.setIndicator("", null)
			.setContent(new Intent(this, ItemsActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("collocation")
			.setIndicator("", null)
			.setContent(new Intent(this, WardrobeActivity.class)));
		mTabHost.setCurrentTab(SINGLEPRODUCT_TAB_INDEX);

	}

	private void initComponents() {
		findViews();
		setListener();
		initTabHost();
	}

	private void findViews() {
		mTitleTextView = (TextView) findViewById(R.id.title_txtView);
		mSingleProductView = (ImageView) findViewById(R.id.singleProduct_button);
		mCollocationView = (ImageView) findViewById(R.id.collocation_button);
		mDeleteView = (ImageView) findViewById(R.id.delete_view);
		mAddView = findViewById(R.id.add_view);

		mFlipper = (ViewFlipper) findViewById(R.id.flipper);
		mAddCameraView = findViewById(R.id.add_camera_image);
		mAddFileView = findViewById(R.id.add_file_image);

		mParentView = findViewById(R.id.tab_parent_view);
	}

	private void setListener() {
		mSingleProductView.setOnClickListener(this);
		mCollocationView.setOnClickListener(this);
		mDeleteView.setOnClickListener(this);
		mAddView.setOnClickListener(this);

		mAddCameraView.setOnClickListener(this);
		mAddFileView.setOnClickListener(this);

		mParentView.setOnClickListener(this);
	}

	private void changeTabImage(boolean bl) {
		if (bl) {
			mSingleProductView.setImageResource(R.drawable.bt_1_on_2x);
			mCollocationView.setImageResource(R.drawable.bt_2_2x);
		}
		else {
			mSingleProductView.setImageResource(R.drawable.bt_1_2x);
			mCollocationView.setImageResource(R.drawable.bt_2_on_2x);
		}
	}

	public void switchTab(boolean isSingleProduct) {

		if (isSingleProduct) {
			mTabHost.setCurrentTab(SINGLEPRODUCT_TAB_INDEX);
			mTitleTextView.setText(getString(R.string.items));
			changeTabImage(true);
		}
		else {
			mTabHost.setCurrentTab(COLLOCATION_TAB_INDEX);
			mTitleTextView.setText(getString(R.string.with_clothes));
			changeTabImage(false);
		}

	}

	public void addItemOrPicture(String url) {
		if (getCurrentActivity().getClass() == ItemsActivity.class) {
			// TODO add camera picture to single product
			// DisposePictureActivity.class
			Intent intent = new Intent(this, CutImageActivity.class);
			intent.putExtra(ADD_URL_STRING, url);
			startActivityForResult(intent, ADD_ITEM_REQUEST_CODE);
			overridePendingTransition(R.anim.scale_in, R.anim.scale_out);
		}
		else {
			WardrobeActivity collocationActivity = (WardrobeActivity) getCurrentActivity();
			collocationActivity.showDialog();
		}
	}

	private void add() {
		if (getCurrentActivity().getClass() == ItemsActivity.class) {
			showOrHiddenFlipper();
		}
		else {
			WardrobeActivity collocationActivity = (WardrobeActivity) getCurrentActivity();
			collocationActivity.showDialog();
		}
	}

	private void showOrHiddenFlipper() {
		if (mFlipper.getVisibility() == View.GONE) {
			showViewFilpper(mFlipper);
		}
		else {
			hiddenType(mFlipper);
		}
	}

	private void refresh() {
		Activity currentActivity = getCurrentActivity();
		if (currentActivity instanceof ItemsActivity) {
			ItemsActivity itemsActivity = (ItemsActivity) getCurrentActivity();
			itemsActivity.addItem(null);
		}
		else {
			WardrobeActivity collocationActivity = (WardrobeActivity) currentActivity;
			collocationActivity.refresh();
		}
	}

	private void delete() {
		Activity currentActivity = getCurrentActivity();
		if (currentActivity instanceof ItemsActivity) {
			ItemsActivity itemsActivity = (ItemsActivity) getCurrentActivity();
			itemsActivity.deleteItem(mIsDelete);
			if (mIsDelete) {
				mDeleteView.setImageResource(R.drawable.icon_4_2x);
				mIsDelete = false;
			}
			else {
				mDeleteView.setImageResource(R.drawable.icon_21_2x);
				mIsDelete = true;
			}
		}
		else {
			WardrobeActivity collocationActivity = (WardrobeActivity) currentActivity;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.singleProduct_button:
				switchTab(true);
				break;

			case R.id.collocation_button:
				isShowAddView();
				switchTab(false);
				break;

			case R.id.delete_view:
				// TODO delete item or suit
				isShowAddView();
				delete();
				break;
			case R.id.add_view:
				add();
				break;

			case R.id.add_camera_image:
				hiddenType(mFlipper);
				PictureAcquire.getImageFromCamera(this);
				break;
			case R.id.add_file_image:
				hiddenType(mFlipper);
				PictureAcquire.getImageFromLocal(this);
				break;
			case R.id.tab_parent_view:
				isShowAddView();
				break;
			default:
				break;
		}
	}

	public void isShowAddView() {
		if (mFlipper.getVisibility() == View.VISIBLE) {
			hiddenType(mFlipper);
		}
	}

	private void showViewFilpper(View view) {
		Animation loadAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		view.setVisibility(View.VISIBLE);
		view.startAnimation(loadAnimation);
	}

	private void hiddenType(final View view) {
		Animation loadAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		loadAnimation.setAnimationListener(new AnimationFactory.ZujiHuAnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				view.setVisibility(View.GONE);
			}
		});
		view.startAnimation(loadAnimation);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK)
			return;
		switch (requestCode) {
			case PictureAcquire.TAKE_PICTURE: // camera after
				// String url = Environment.getExternalStorageDirectory() + "/"
				// + PictureAcquire.PORTRAIT_IN_CAMERA;
				String url = Environment.getExternalStorageDirectory() + PictureAcquire.image_path;
				Bitmap newBitmap = PictureAcquire.loadCameraFile(480, Bitmap.Config.ARGB_8888);
				saveImage(url, newBitmap);
				addItemOrPicture(url);
				break;
			case PictureAcquire.SELECT_PICTURE: // choice local file image --> after
				String fileUrl = null;
				if (data.getDataString() != null) {
					fileUrl = data.getDataString();
				}
				else if (data.getAction() != null) {
					fileUrl = data.getAction();
				}

				url = PictureAcquire.urlToFilePath(this, fileUrl);
				if (url == null)
					return;
				newBitmap = PictureAcquire.loadImageWithRotation(
					PictureAcquire.urlToFilePath(this, fileUrl), 480, Bitmap.Config.ARGB_8888);
				saveImage(url, newBitmap);
				addItemOrPicture(url);
				break;
			case ADD_ITEM_REQUEST_CODE:
			case SuitAdapter.SAVE_RESULT_ID:
				refresh();
				break;
		}
	}

	public void showTitle(int type) {
		String titleText = getResources().getString(R.string.my_wardrobe);
		switch (type) {
			case Constant.UPPER_OUTER_TYPE_NO:
				titleText = titleText + getResources().getString(R.string.upper_outer);
				break;
			case Constant.PANTS_TYPE_NO:
				titleText = titleText + getResources().getString(R.string.pants);
				break;
			case Constant.SKIRT_TYPE_NO:
				titleText = titleText + getResources().getString(R.string.skirt);
				break;
			case Constant.SHOES_TYPE_NO:
				titleText = titleText + getResources().getString(R.string.shoes);
				break;
			case Constant.BAGS_TYPE_NO:
				titleText = titleText + getResources().getString(R.string.bags);
				break;
			case Constant.DECORATIONS_TYPE_NO:
				titleText = titleText + getResources().getString(R.string.decorations);
				break;

			default:
				titleText = titleText + getResources().getString(R.string.upper_outer);
				break;
		}
		mTitleTextView.setText(titleText);
	}

	private void saveImage(String url, Bitmap bitmap) {
		File file = new File(url);

		try {
			file.createNewFile();
			FileOutputStream ostream = new FileOutputStream(file);
			bitmap.compress(CompressFormat.JPEG, 100, ostream);
			ostream.flush();
			ostream.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
