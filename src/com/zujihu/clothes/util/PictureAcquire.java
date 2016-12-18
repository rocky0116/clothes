package com.zujihu.clothes.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.zujihu.clothes.common.Constant;

public class PictureAcquire {

	private static final String	TAG				= "PictureAcquire";

	public static final int		CAMERA			= 1;
	public static final int		GALLERY			= 2;
	public static final int		TAKE_PICTURE	= 3;
	public static final int		SELECT_PICTURE	= 4;
	public static final int		SHOW_PICTURE	= 5;
	public static final int		SAVE_PICTURE	= 6;

	public static final int		COMPRESSED_SIZE	= 640;

	public static String		image_path		= "gallery.jpg";

	public static String		IMAGE_PATH;

	public static Bitmap loadImage(String filePath, int maxSize, Bitmap.Config preferredConfig) {

		Bitmap bitmap = null;

		File temp = new File(filePath);

		if (!temp.exists())
			return null;
		// First, get the dimensions of the image
		Options options = new Options();
		filePath = temp.getAbsolutePath();

		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options); // Just
		// get image info without actual decoding.

		int maxWHSize = Math.max(options.outHeight, options.outWidth);

		int inSampleSize = maxSize <= 0 ? 1 : (maxWHSize + maxSize - 1) / maxSize;

		try {
			options.inPreferredConfig = preferredConfig;
			options.inSampleSize = inSampleSize;
			// Do the actual decoding
			options.inJustDecodeBounds = false;
			bitmap = BitmapFactory.decodeFile(filePath, options);
			int outSize = Math.max(options.outWidth, options.outHeight);
			if (outSize > maxSize) {
				// The output image may larger than the maxSize if inSampleSize is not the power of
				// 2
				Bitmap finalBitmap = Bitmap.createScaledBitmap(bitmap, options.outWidth * maxSize
						/ outSize, options.outHeight * maxSize / outSize, false);
				bitmap.recycle();
				bitmap = finalBitmap;
			}
		}
		catch (Throwable ex) {
		}
		return bitmap;
	}

	public static Bitmap loadImageWithRotation(String filePath, int maxSize,
			Bitmap.Config preferredConfig) {

		Log.d("Test", "file=" + filePath);

		int degree = PictureAcquire.getExifOrientation(filePath);

		Bitmap bmp = null;
		bmp = loadImage(filePath, maxSize, preferredConfig);

		if (bmp != null && degree != 0) {
			Bitmap tempBmp = PictureAcquire.rotate(bmp, degree);
			if (!bmp.isRecycled()) {
				bmp.recycle();
			}
			bmp = tempBmp;
		}

		return bmp;
	}

	public static void saveImage(Bitmap bitmapImage, String filePath, int quality)
		throws IOException {

		if (bitmapImage == null)
			return;

		FileOutputStream out = null;
		out = new FileOutputStream(filePath);
		bitmapImage.compress(Bitmap.CompressFormat.JPEG, quality, out);
		out.close();
	}

	public static int getExifOrientation(String filepath) {
		int degree = 0;
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(filepath);
		}
		catch (IOException ex) {
			Log.e(TAG, "cannot read exif", ex);
		}
		if (exif != null) {
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
			if (orientation != -1) {
				// We only recognize a subset of orientation tag values.
				switch (orientation) {
					case ExifInterface.ORIENTATION_ROTATE_90:
						degree = 90;
						break;
					case ExifInterface.ORIENTATION_ROTATE_180:
						degree = 180;
						break;
					case ExifInterface.ORIENTATION_ROTATE_270:
						degree = 270;
						break;
				}
			}
		}
		return degree;
	}

	public static Bitmap rotate(Bitmap b, int degrees) {
		if (degrees != 0 && b != null) {
			Matrix m = new Matrix();
			m.setRotate(degrees, (float) b.getWidth() / 2, (float) b.getHeight() / 2);
			try {
				Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
				if (b != b2) {
					// TODO do not recycle here!
					b = b2;
				}
			}
			catch (OutOfMemoryError ex) {
				Log.e(TAG, "Out of memory!");
			}
		}
		return b;
	}

	public static String urlToFilePath(Activity context, String contentUrl) {
		String filePath = contentUrl;
		Uri uri = Uri.parse(contentUrl);
		if (contentUrl.indexOf("file:") == 0) {
			filePath = uri.getPath();
		}
		else {
			String[] proj = { MediaStore.Images.Media.DATA };
			Cursor cursor = context.managedQuery(uri, proj, null, null, null);
			if (cursor != null) {
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				filePath = cursor.getString(column_index);
				cursor.close();
			}
		}
		return filePath;
	}

	public static String getSelectorGalleryFilePath(Activity context, String data) {
		Uri uri = Uri.parse(data);
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = context.managedQuery(uri, proj, null, null, null);
		if (cursor != null) {
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			String path = cursor.getString(column_index);
			cursor.close();
			return path;
		}

		return null;
	}

	public static void getImageFromLocal(Activity activity) {
		// resourceType = GALLERY;
		image_path = Constant.ITEM_PATH + "/t_" + System.currentTimeMillis() + ".jpg";
		File temp = new File(Environment.getExternalStorageDirectory(), image_path);
		if (temp.exists())
			temp.delete();

		try {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			activity.startActivityForResult(intent, SELECT_PICTURE);
		}
		catch (ActivityNotFoundException e) { // album not support
			showToastInfo(activity, "您的设备不支持相册功能");
		}

	}

	public static void getImageFromCamera(Activity activity) {
		image_path = Constant.ITEM_PATH + "/t_" + System.currentTimeMillis() + ".jpg";
		File temp = new File(Environment.getExternalStorageDirectory(), image_path);
		if (temp.exists())
			temp.delete();
		try {
			Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
			intent.putExtra(MediaStore.EXTRA_OUTPUT,
				Uri.fromFile(new File(Environment.getExternalStorageDirectory(), image_path)));
			intent.putExtra("outputFormat", "JPEG");

			activity.startActivityForResult(intent, TAKE_PICTURE);

		}
		catch (ActivityNotFoundException e) { // camera not support
			showToastInfo(activity, "您的设备不支持相机功能");
		}

	}

	public static Bitmap loadCameraFile(int maxSize, Bitmap.Config preferredConfig) {
		File cameraFile = new File(Environment.getExternalStorageDirectory(), image_path);
		return loadImageWithRotation(cameraFile.getAbsolutePath(), maxSize, preferredConfig);
	}

	public static void showToastInfo(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
}
