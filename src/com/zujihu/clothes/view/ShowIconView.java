package com.zujihu.clothes.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ShowIconView extends View {

	Bitmap	mBitmap	= null;

	Paint	mPaint	= null;

	int		mPosX	= 0;
	int		mPosY	= 0;

	public ShowIconView(Context context) {
		super(context);
	}

	public ShowIconView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ShowIconView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setImage(int posX, int posY, Bitmap bitmap) {
		this.mPosX = posX;
		this.mPosY = posY;
		mBitmap = bitmap;
		mPaint = new Paint();
		mPaint.setColor(Color.WHITE);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mBitmap == null) {
			return;
		}
		canvas.drawBitmap(mBitmap, mPosX, mPosY, mPaint);
		super.onDraw(canvas);
	}

}
