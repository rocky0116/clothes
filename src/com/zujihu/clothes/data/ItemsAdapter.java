package com.zujihu.clothes.data;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.zujihu.clothes.R;

public class ItemsAdapter extends BaseAdapter {
	private Context				mContext;
	private List<PictureModel>	mImageIds;
	private ArrayList<Integer>	filter	= new ArrayList<Integer>();
	private ViewHolder			holder;
	private List<ViewHolder>	vList	= new ArrayList<ItemsAdapter.ViewHolder>();

	public ItemsAdapter(Context c, ArrayList<Integer> itemlAL, List<PictureModel> imageIds) {
		mContext = c;
		filter = itemlAL;
		mImageIds = imageIds;
	}

	@Override
	public int getCount() {
		return mImageIds.size();
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
			holder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.item_view, null);
			holder.imageView = (ImageView) convertView.findViewById(R.id.item_image);
			holder.seleteImageView = (ImageView) convertView.findViewById(R.id.select_image);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (filter != null) {
			if (filter.get(position) == 0) {
				holder.imageView.setImageBitmap(mImageIds.get(position).bitmap);
			}
			else if (filter.get(position) != 0) {
				holder.imageView.setImageBitmap(mImageIds.get(position).bitmap);
				holder.imageView.setBackgroundResource(R.drawable.layout_border_select);
			}
		}
		else {
			holder.imageView.setImageBitmap(mImageIds.get(position).bitmap);
		}
		vList.add(holder);

		return convertView;
	}

	class ViewHolder {
		public ImageView	imageView		= null;
		public ImageView	seleteImageView	= null;
	}

	public void showSelectView(boolean isShowDeleteView) {
		for (int i = 0; i < vList.size(); i++) {
			if (isShowDeleteView) {
				vList.get(i).seleteImageView.setVisibility(View.GONE);
			}
			else {
				vList.get(i).seleteImageView.setVisibility(View.VISIBLE);
			}

		}
	}

}
