package com.zujihu.clothes.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.GridView;

import com.zujihu.clothes.R;
import com.zujihu.clothes.common.Constant;

public class ShelvesView extends GridView {

	private Bitmap	bitmap;

	public ShelvesView(Context context, AttributeSet attrs) {
		super(context, attrs);
		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_4_2x);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		int count = getChildCount();
		int top = count > 0 ? getChildAt(0).getTop() + Constant.PLANK_TOP_HEIGHT : 0;
		int backgroundWidth = bitmap.getWidth();
		int backgroundHeight = bitmap.getHeight() + Constant.PLANK_MOVE_HEIGHT;
		int width = getWidth();
		int height = getHeight();

		for (int y = top; y < height; y += backgroundHeight) {
			for (int x = 0; x < width; x += backgroundWidth) {
				canvas.drawBitmap(bitmap, x, y, null);
			}
		}
		super.dispatchDraw(canvas);
	}

	public void recycleBitmap() {
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
		}
	}
}
