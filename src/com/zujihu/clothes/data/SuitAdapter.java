package com.zujihu.clothes.data;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.zujihu.clothes.R;
import com.zujihu.clothes.activity.SuitActivity;

public class SuitAdapter extends BaseAdapter {

	public static final String	SUITUEDITORADD	= "SUITUEDITORADD";
	private Activity			mActivity;
	ImageView					imageView;
	private List<PictureModel>	mList;

	public SuitAdapter(Activity c, List<PictureModel> list) {
		mActivity = c;
		mList = list;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			imageView = new ImageView(mActivity);
			imageView.setClickable(true);
			imageView.setLayoutParams(new GridView.LayoutParams(100, 100));
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		}
		else {
			imageView = (ImageView) convertView;
		}

		if (mList.get(position).bitmap != null) {
			imageView.setImageBitmap(mList.get(position).bitmap);
		}

		imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent it = new Intent(mActivity, SuitActivity.class);
				it.putExtra(SUITUEDITORADD, true);
				it.putExtra("_id", mList.get(position)._id);
				it.putExtra("url", mList.get(position).url.split("\\|")[0]);
				it.putExtra("data", mList.get(position).url.split("\\|")[1]);
				mActivity.startActivityForResult(it, SAVE_RESULT_ID);
				mActivity.overridePendingTransition(R.anim.scale_in, R.anim.scale_out);
			}
		});
		return imageView;
	}

	public static final int	SAVE_RESULT_ID	= 12;
}
