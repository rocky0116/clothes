package com.zujihu.clothes.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import com.zujihu.clothes.data.MultiTouchController;
import com.zujihu.clothes.data.MultiTouchController.MultiTouchObjectCanvas;
import com.zujihu.clothes.data.MultiTouchController.PointInfo;
import com.zujihu.clothes.data.MultiTouchController.PositionAndScale;
import com.zujihu.clothes.data.PictureModel;
import com.zujihu.clothes.util.Utils;

public class SuitEditView extends View implements MultiTouchObjectCanvas<SuitEditView.Img> {

	private ArrayList<Img>				mImages						= new ArrayList<Img>();

	// --

	private MultiTouchController<Img>	multiTouchController		= new MultiTouchController<Img>(
																		this);

	// --

	private PointInfo					currTouchPoint				= new PointInfo();

	private boolean						mShowDebugInfo				= true;

	private static final int			UI_MODE_ROTATE				= 1,
			UI_MODE_ANISOTROPIC_SCALE = 2;

	private int							mUIMode						= UI_MODE_ROTATE;

	// --

	private Paint						mLinePaintTouchPointCircle	= new Paint();

	// ---------------------------------------------------------------------------------------------------

	public SuitEditView(Context context, List<String> list, List<Float[]> floats) {
		this(context, null, list, floats);
	}

	public SuitEditView(Context context, AttributeSet attrs, List<String> list, List<Float[]> floats) {
		this(context, attrs, 0, list, floats);
	}

	public SuitEditView(Context context, AttributeSet attrs, int defStyle, List<String> list,
		List<Float[]> floats) {
		super(context, attrs, defStyle);
		init(context, list, floats);
	}

	private void init(Context context, List<String> lists, List<Float[]> floats) {
		Resources res = context.getResources();

		int n = lists.size();
		if (n > 0) {
			for (int i = 0; i < n; i++) {
				String path = lists.get(i);
				Bitmap bm = Utils.getSdcardImage(path);
				if (floats != null) {
					Float[] data = floats.get(i);
					mImages.add(new Img(bm, res, path, data));
				}
				else {
					mImages.add(new Img(bm, res, path, null));
				}
			}
		}

		mLinePaintTouchPointCircle.setColor(Color.YELLOW);
		mLinePaintTouchPointCircle.setStrokeWidth(5);
		mLinePaintTouchPointCircle.setStyle(Style.STROKE);
		mLinePaintTouchPointCircle.setAntiAlias(true);
		setBackgroundColor(Color.BLACK);
	}

	/** Called by activity's onResume() method to load the images */
	public void loadImages(Context context) {
		Resources res = context.getResources();
		int n = mImages.size();
		for (int i = 0; i < n; i++) {
			mImages.get(i).load(res);
		}
	}

	/** Called by activity's onPause() method to free memory used for loading the images */
	public void unloadImages() {
		int n = mImages.size();
		for (int i = 0; i < n; i++)
			mImages.get(i).unload();
	}

	// ---------------------------------------------------------------------------------------------------

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int n = mImages.size();
		for (int i = 0; i < n; i++)
			mImages.get(i).draw(canvas);
		if (mShowDebugInfo)
			drawMultitouchDebugMarks(canvas);
	}

	// ---------------------------------------------------------------------------------------------------

	public void trackballClicked() {
		mUIMode = (mUIMode + 1) % 3;
		invalidate();
	}

	private void drawMultitouchDebugMarks(Canvas canvas) {
		if (currTouchPoint.isDown()) {
			float[] xs = currTouchPoint.getXs();
			float[] ys = currTouchPoint.getYs();
			float[] pressures = currTouchPoint.getPressures();
			int numPoints = Math.min(currTouchPoint.getNumTouchPoints(), 2);
			for (int i = 0; i < numPoints; i++)
				canvas.drawCircle(xs[i], ys[i], 50 + pressures[i] * 80, mLinePaintTouchPointCircle);
			if (numPoints == 2)
				canvas.drawLine(xs[0], ys[0], xs[1], ys[1], mLinePaintTouchPointCircle);
		}
	}

	// ---------------------------------------------------------------------------------------------------

	/** Pass touch events to the MT controller */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return multiTouchController.onTouchEvent(event);
	}

	/**
	 * Get the image that is under the single-touch point, or return null (canceling the drag op) if
	 * none
	 */
	@Override
	public Img getDraggableObjectAtPoint(PointInfo pt) {
		float x = pt.getX(), y = pt.getY();
		int n = mImages.size();
		for (int i = n - 1; i >= 0; i--) {
			Img im = mImages.get(i);
			if (im.containsPoint(x, y))
				return im;
		}
		return null;
	}

	/**
	 * Select an object for dragging. Called whenever an object is found to be under the point
	 * (non-null is returned by getDraggableObjectAtPoint()) and a drag operation is starting.
	 * Called with null when drag op ends.
	 */
	@Override
	public void selectObject(Img img, PointInfo touchPoint) {
		currTouchPoint.set(touchPoint);
		if (img != null) {
			// Move image to the top of the stack when selected
			mImages.remove(img);
			mImages.add(img);
		}
		else {
			// Called with img == null when drag stops.
		}
		invalidate();
	}

	/**
	 * Get the current position and scale of the selected image. Called whenever a drag starts or is
	 * reset.
	 */
	@Override
	public void getPositionAndScale(Img img, PositionAndScale objPosAndScaleOut) {
		// FIXME affine-izem (and fix the fact that the anisotropic_scale part requires averaging
		// the two scale factors)
		objPosAndScaleOut.set(img.getCenterX(), img.getCenterY(),
			(mUIMode & UI_MODE_ANISOTROPIC_SCALE) == 0, (img.getScaleX() + img.getScaleY()) / 2,
			(mUIMode & UI_MODE_ANISOTROPIC_SCALE) != 0, img.getScaleX(), img.getScaleY(),
			(mUIMode & UI_MODE_ROTATE) != 0, img.getAngle());
	}

	/** Set the position and scale of the dragged/stretched image. */
	@Override
	public boolean setPositionAndScale(Img img, PositionAndScale newImgPosAndScale,
			PointInfo touchPoint) {
		currTouchPoint.set(touchPoint);
		boolean ok = img.setPos(newImgPosAndScale);
		if (ok)
			invalidate();
		return ok;
	}

	public PictureModel savePicData() {
		// TODO save picture data
		String urlString = "";
		for (int i = 0; i < mImages.size(); i++) {
			Img img = mImages.get(i);
			urlString = urlString + ";" + img.path + "," + img.centerX + ',' + img.centerY + ','
					+ img.scaleX + ',' + img.scaleY + ',' + img.angle;
		}
		PictureModel pictureModel = new PictureModel();
		pictureModel.url = urlString.substring(1);
		return pictureModel;
	}

	// ----------------------------------------------------------------------------------------------

	class Img {
		private Bitmap				bitmap;

		private Drawable			drawable;

		private boolean				firstLoad;

		private int					width, height, displayWidth, displayHeight;

		private float				centerX, centerY, scaleX, scaleY, angle;

		private float				minX, maxX, minY, maxY;
		private String				path;
		private Float[]				data;

		private static final float	SCREEN_MARGIN	= 100;

		public Img(Bitmap bitmap, Resources res, String path, Float[] data) {
			this.bitmap = bitmap;
			this.firstLoad = true;
			this.path = path;
			this.data = data;
			getMetrics(res);
		}

		private void getMetrics(Resources res) {
			DisplayMetrics metrics = res.getDisplayMetrics();
			// The DisplayMetrics don't seem to always be updated on screen rotate, so we hard code
			// a portrait
			// screen orientation for the non-rotated screen here...
			// this.displayWidth = metrics.widthPixels;
			// this.displayHeight = metrics.heightPixels;
			this.displayWidth = res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
					? Math.max(metrics.widthPixels, metrics.heightPixels) : Math.min(
						metrics.widthPixels, metrics.heightPixels);
			this.displayHeight = res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
					? Math.min(metrics.widthPixels, metrics.heightPixels) : Math.max(
						metrics.widthPixels, metrics.heightPixels);
		}

		/** Called by activity's onResume() method to load the images */
		public void load(Resources res) {
			this.drawable = new BitmapDrawable(bitmap);
			this.width = drawable.getIntrinsicWidth();
			this.height = drawable.getIntrinsicHeight();
			float cx, cy, sx, sy;
			if (firstLoad) {
				cx = SCREEN_MARGIN + (float) (Math.random() * (displayWidth - 2 * SCREEN_MARGIN));
				cy = SCREEN_MARGIN + (float) (Math.random() * (displayHeight - 2 * SCREEN_MARGIN));
				float sc = (float) (Math.max(displayWidth, displayHeight)
						/ (float) Math.max(width, height) * Math.random() * 0.3 + 0.2);
				sx = sy = sc;
				firstLoad = false;
			}
			else {
				// Reuse position and scale information if it is available
				// FIXME this doesn't actually work because the whole activity is torn down and
				// re-created on rotate
				cx = this.centerX;
				cy = this.centerY;
				sx = this.scaleX;
				sy = this.scaleY;
				// Make sure the image is not off the screen after a screen rotation
				if (this.maxX < SCREEN_MARGIN)
					cx = SCREEN_MARGIN;
				else if (this.minX > displayWidth - SCREEN_MARGIN)
					cx = displayWidth - SCREEN_MARGIN;
				if (this.maxY > SCREEN_MARGIN)
					cy = SCREEN_MARGIN;
				else if (this.minY > displayHeight - SCREEN_MARGIN)
					cy = displayHeight - SCREEN_MARGIN;
			}

			if (data != null) {
				try {
					setPos(data[0], data[1], data[2], data[3], data[4]);
				}
				catch (Exception e) {
					setPos(cx, cy, sx, sy, 0.0f);
				}
			}
			else {
				setPos(cx, cy, sx, sy, 0.0f);
			}
		}

		/** Called by activity's onPause() method to free memory used for loading the images */
		public void unload() {
			this.drawable = null;
		}

		/** Set the position and scale of an image in screen coordinates */
		public boolean setPos(PositionAndScale newImgPosAndScale) {
			return setPos(newImgPosAndScale.getXOff(), newImgPosAndScale.getYOff(),
				(mUIMode & UI_MODE_ANISOTROPIC_SCALE) != 0
						? newImgPosAndScale.getScaleX() : newImgPosAndScale.getScale(),
				(mUIMode & UI_MODE_ANISOTROPIC_SCALE) != 0
						? newImgPosAndScale.getScaleY() : newImgPosAndScale.getScale(),
				newImgPosAndScale.getAngle());
			// FIXME: anisotropic scaling jumps when axis-snapping
			// FIXME: affine-ize
			// return setPos(newImgPosAndScale.getXOff(), newImgPosAndScale.getYOff(),
			// newImgPosAndScale.getScaleAnisotropicX(),
			// newImgPosAndScale.getScaleAnisotropicY(), 0.0f);
		}

		/** Set the position and scale of an image in screen coordinates */
		private boolean setPos(float centerX, float centerY, float scaleX, float scaleY, float angle) {
			float ws = (width / 2) * scaleX, hs = (height / 2) * scaleY;
			float newMinX = centerX - ws, newMinY = centerY - hs, newMaxX = centerX + ws, newMaxY = centerY
					+ hs;
			if (newMinX > displayWidth - SCREEN_MARGIN || newMaxX < SCREEN_MARGIN
					|| newMinY > displayHeight - SCREEN_MARGIN || newMaxY < SCREEN_MARGIN)
				return false;
			this.centerX = centerX;
			this.centerY = centerY;
			this.scaleX = scaleX;
			this.scaleY = scaleY;
			this.angle = angle;
			this.minX = newMinX;
			this.minY = newMinY;
			this.maxX = newMaxX;
			this.maxY = newMaxY;

			return true;
		}

		/** Return whether or not the given screen coords are inside this image */
		public boolean containsPoint(float scrnX, float scrnY) {
			// FIXME: need to correctly account for image rotation
			return (scrnX >= minX && scrnX <= maxX && scrnY >= minY && scrnY <= maxY);
		}

		public void draw(Canvas canvas) {
			canvas.save();
			float dx = (maxX + minX) / 2;
			float dy = (maxY + minY) / 2;
			drawable.setBounds((int) minX, (int) minY, (int) maxX, (int) maxY);
			canvas.translate(dx, dy);

			// float rotate = angle * 180.0f / (float) Math.PI;

			int rotationAngleDegrees = (int) Math.toDegrees(angle);
			canvas.rotate(rotationAngleDegrees);
			canvas.translate(-dx, -dy);
			drawable.draw(canvas);
			canvas.restore();
		}

		public Drawable getDrawable() {
			return drawable;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public float getCenterX() {
			return centerX;
		}

		public float getCenterY() {
			return centerY;
		}

		public float getScaleX() {
			return scaleX;
		}

		public float getScaleY() {
			return scaleY;
		}

		public float getAngle() {
			return angle;
		}

		// FIXME: these need to be updated for rotation
		public float getMinX() {
			return minX;
		}

		public float getMaxX() {
			return maxX;
		}

		public float getMinY() {
			return minY;
		}

		public float getMaxY() {
			return maxY;
		}
	}

	public void screenshotView() {

	}
}
