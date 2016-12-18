package com.zujihu.clothes.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Environment;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.zujihu.clothes.R;
import com.zujihu.clothes.common.Constant;
import com.zujihu.clothes.data.DatabaseHelper;
import com.zujihu.clothes.data.PictureModel;
import com.zujihu.clothes.util.PictureAcquire;
import com.zujihu.clothes.util.Utils;
import com.zujihu.opencv.ImgProc;

public class MaskingImageView extends ImageView {

	public static final int	MODE_RECT			= 1;
	public static final int	MODE_LINE_CUT		= 2;
	public static final int	MODE_POINT_CLEAR	= 3;

	int						bitmapWidth;
	int						bitmapHeight;
	private byte[]			imageRgb;
	int[]					imageArgb;
	private byte[]			lastMaskPixels;
	private byte[]			currentMaskPixels;

	private List<byte[]>	maskHistory			= new ArrayList<byte[]>();

	private Bitmap			bitmap;
	public Matrix			bitmapMatrix		= new Matrix();
	private Matrix			bitmapMatrixInverse	= new Matrix();
	private Context			mContext;

	public MaskingImageView(Context context) {
		super(context);
	}

	public MaskingImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MaskingImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
	}

	public void setBitmap(Bitmap bmp, byte[] mask) {
		if (bmp == null) {
			return;
		}
		bitmapWidth = bmp.getWidth();
		bitmapHeight = bmp.getHeight();
		int count = bitmapWidth * bitmapHeight;

		if (bmp.getConfig() != Config.ARGB_8888) {
			Bitmap b = bmp.copy(Config.ARGB_8888, false);
			bmp.recycle();
			bmp = b;
		}

		imageRgb = new byte[count * 4];
		ByteBuffer pixelBuffer = ByteBuffer.wrap(imageRgb);
		bmp.copyPixelsToBuffer(pixelBuffer);
		ImgProc.argbToRgb(imageRgb, 0, count, imageRgb);
		imageArgb = new int[count];
		currentMaskPixels = new byte[count];
		if (mask != null) {
			currentMaskPixels = mask;
		}
		else {
			for (int i = 0; i < count; i++)
				currentMaskPixels[i] = -1;
		}
		calculateFitMatrix();
		updateDrawingBitmap();
		postInvalidate();
	}

	public void grubCut() {
		if (currentMaskPixels == null)
			return;

		int count = bitmapWidth * bitmapHeight;

		ImgProc.grabCut(imageRgb, bitmapWidth, bitmapHeight, currentMaskPixels, 2, 2,
			bitmapWidth - 4, bitmapHeight - 4, 3, ImgProc.GC_INIT_WITH_RECT);

		for (int i = 0; i < count; i++) {
			// GC_FGD/GC_PR_FGD -> 255, GC_BGD/GC_PR_BGD ->0
			currentMaskPixels[i] = (byte) -(currentMaskPixels[i] & 1);
		}

		updateDrawingBitmap();
		postInvalidate();
	}

	public void floodFill(int x, int y, int range) {
		if (currentMaskPixels == null)
			return;
		float[] pts = new float[] { x, y };
		bitmapMatrixInverse.mapPoints(pts);
		x = (int) pts[0];
		y = (int) pts[1];

		if (x < 1 || x >= bitmapWidth - 1 || y < 1 || y >= bitmapHeight - 1)
			return;

		int count = bitmapWidth * bitmapHeight;
		byte[] stepMaskPixels = new byte[count];
		range = Math.min(range, 0xFF);
		range = range | (range << 8) | (range << 16);
		ImgProc.floodFillMaskOnly(imageRgb, bitmapWidth, bitmapHeight, stepMaskPixels, x, y, 0,
			range, range);
		for (int i = 0; i < count; i++) {
			// 1-> 255, 0 ->0
			stepMaskPixels[i] = (byte) -((stepMaskPixels[i] & 1) ^ 1);
		}
		if (lastMaskPixels == null) {
			System.arraycopy(stepMaskPixels, 0, currentMaskPixels, 0, stepMaskPixels.length);
		}
		else {
			for (int i = 0; i < count; i++) {
				currentMaskPixels[i] = (byte) (lastMaskPixels[i] & stepMaskPixels[i]);
			}
		}
		if (lastMaskPixels == null)
			currentMaskPixels = stepMaskPixels;
		else
			for (int i = 0; i < count; i++)
				currentMaskPixels[i] = (byte) (lastMaskPixels[i] & stepMaskPixels[i]);
		updateDrawingBitmap();
	}

	public void erase(int x, int y, int radius) {
		if (currentMaskPixels == null)
			return;
		float[] pts = new float[] { x, y };
		bitmapMatrixInverse.mapPoints(pts);
		x = (int) pts[0];
		y = (int) pts[1];

		Bitmap maskBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Config.ALPHA_8);
		ByteBuffer buf = ByteBuffer.wrap(currentMaskPixels);
		maskBitmap.copyPixelsFromBuffer(buf);
		Canvas canvas = new Canvas(maskBitmap);
		Paint paint = new Paint();
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
		paint.setColor(0);
		canvas.drawCircle(x, y, radius, paint);
		buf.rewind();
		maskBitmap.copyPixelsToBuffer(buf);
		updateDrawingBitmap();
	}

	public void eraseJ(int x, int y, int radius) {
		if (currentMaskPixels == null)
			return;
		float[] pts = new float[] { x, y };
		bitmapMatrixInverse.mapPoints(pts);
		x = (int) pts[0];
		y = (int) pts[1];

		Bitmap maskBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Config.ALPHA_8);
		ByteBuffer buf = ByteBuffer.wrap(currentMaskPixels);
		maskBitmap.copyPixelsFromBuffer(buf);
		Canvas canvas = new Canvas(maskBitmap);
		Paint paint = new Paint();
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
		paint.setColor(Color.BLUE);
		canvas.drawCircle(x, y, radius, paint);
		buf.rewind();
		maskBitmap.copyPixelsToBuffer(buf);
		updateDrawingBitmap();
	}

	public void saveState() {
		if (currentMaskPixels == null)
			return;
		// Save mask of last step to final mask
		if (lastMaskPixels != null)
			maskHistory.add(lastMaskPixels);
		lastMaskPixels = currentMaskPixels;
		currentMaskPixels = new byte[lastMaskPixels.length];
		System.arraycopy(lastMaskPixels, 0, currentMaskPixels, 0, lastMaskPixels.length);
	}

	public void restoreState() {
		if (lastMaskPixels == null)
			return;
		currentMaskPixels = lastMaskPixels;
		if (maskHistory.isEmpty())
			lastMaskPixels = null;
		else
			lastMaskPixels = maskHistory.remove(maskHistory.size() - 1);
		updateDrawingBitmap();
		postInvalidate();
	}

	public byte[] getGZipMaskData() throws IOException {
		int count = currentMaskPixels.length;
		return Utils.zipMask(currentMaskPixels, count);
	}

	private void calculateFitMatrix() {
		// Fit bitmap in center
		float zoomX = (float) getWidth() / bitmapWidth;
		float zoomY = (float) getHeight() / bitmapHeight;
		float zoom = Math.min(zoomX, zoomY);
		float x0 = 0, y0 = 0;
		if (zoomX > zoomY)
			x0 = (getWidth() - bitmapWidth * zoomY) / 2;
		else
			y0 = (getHeight() - bitmapHeight * zoomX) / 2;
		bitmapMatrix.reset();
		bitmapMatrix.postScale(zoom, zoom);
		bitmapMatrix.postTranslate(x0, y0);
		bitmapMatrix.invert(bitmapMatrixInverse);
	}

	private void updateDrawingBitmap() {
		ImgProc.rgbToArgbWithAlpha(imageRgb, currentMaskPixels, 0, bitmapWidth * bitmapHeight,
			imageArgb);
		if (bitmap != null && !bitmap.isRecycled())
			bitmap.recycle();
		bitmap = Bitmap.createBitmap(imageArgb, bitmapWidth, bitmapHeight, Config.ARGB_8888);
		setImageBitmap(bitmap);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Paint paint = new Paint();
		canvas.drawColor(getResources().getColor(R.color.dispose_color), PorterDuff.Mode.SRC);
		if (imageArgb != null) {
			canvas.drawBitmap(bitmap, bitmapMatrix, paint);
		}

		if (mIsShowMagnifier) {
			if (bitmap != null) {
				int x = px - Constant.RADIUS;
				int y = py - Constant.RADIUS;
				if (x > width) {
					x = x - Constant.MAGNIFYING_GLASS_MOVE_WIDTH;
				}
				else {
					x = x + Constant.MAGNIFYING_GLASS_MOVE_WIDTH;
				}

				if (y > height) {
					y = y - Constant.MAGNIFYING_GLASS_MOVE_HEIGHT;
				}
				else {
					y = y + Constant.MAGNIFYING_GLASS_MOVE_HEIGHT;
				}

				canvas.translate(x, y);
				canvas.clipPath(mPath);

				// Figure painting to enlarge
				if (bitmapHeight > bitmapWidth)
					canvas.translate(Constant.RADIUS - px * Constant.AMPLIFICATION_FACTOR,
						Constant.RADIUS - py * Constant.AMPLIFICATION_FACTOR);
				else
					canvas.translate(Constant.RADIUS - px * Constant.AMPLIFICATION_FACTOR,
						Constant.RADIUS - py * Constant.AMPLIFICATION_FACTOR
								+ Constant.MAGNIFYING_GLASS_MOVE_HEIGHT);

				canvas.drawBitmap(bitmap, matrix, null);
			}

		}

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		calculateFitMatrix();
	}

	public void saveImageToSdcard() {
		long tiems = System.currentTimeMillis();
		String path = Constant.DB_PATH + "m_" + tiems + ".mask";
		Utils.createSdcardFile(mContext, Constant.ITEM_PATH);
		File file = new File(Environment.getExternalStorageDirectory() + Constant.ITEM_PATH + "/t_"
				+ tiems + ".jpg");

		try {
			file.createNewFile();
			FileOutputStream ostream = new FileOutputStream(file);
			bitmap.compress(CompressFormat.JPEG, 100, ostream);
			ostream.flush();
			ostream.close();

			Utils.saveMaskToSdCard(getGZipMaskData(), path);

			saveData(file.toString(), path);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveData(String url, String path) {
		DatabaseHelper db = new DatabaseHelper(mContext);
		db.createTableItems();
		PictureModel pModel = new PictureModel();
		pModel.url = Environment.getExternalStorageDirectory() + PictureAcquire.image_path;
		pModel.mask_path = path;
		pModel.desc = "item";
		db.addItem(pModel);
	}

	private Path	mPath				= new Path();
	private Matrix	matrix				= new Matrix();
	private int		width				= 0, height = 0;
	private int		px					= 0, py = 0;
	private boolean	mIsShowMagnifier	= false;

	public void magifyingGlassEffect(int x, int y, boolean isShowMagnifier) {
		px = x;
		py = y;
		mIsShowMagnifier = isShowMagnifier;
		width = bitmapWidth / 2;
		height = bitmapHeight / 2;

		mPath.addCircle(Constant.RADIUS, Constant.RADIUS, Constant.RADIUS, Direction.CCW);
		calculateMagnifyMatrix();
	}

	private void calculateMagnifyMatrix() {
		// Fit bitmap in center
		float zoomX = (float) getWidth() / bitmapWidth;
		float zoomY = (float) getHeight() / bitmapHeight;
		float zoom = Math.min(zoomX, zoomY);
		float x0 = 0, y0 = 0;
		if (zoomX > zoomY)
			x0 = (getWidth() - bitmapWidth * zoomY) / 2;
		else
			y0 = (getHeight() - bitmapHeight * zoomX) / 2;
		matrix.reset();
		matrix.postScale(zoom * Constant.AMPLIFICATION_FACTOR, zoom * Constant.AMPLIFICATION_FACTOR);
		matrix.postTranslate(x0, y0);
	}

}
