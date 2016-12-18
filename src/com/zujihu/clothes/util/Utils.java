package com.zujihu.clothes.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.widget.Toast;

import com.zujihu.clothes.R;
import com.zujihu.clothes.data.PictureModel;
import com.zujihu.opencv.ImgProc;

public class Utils {

	public static Bitmap getBitmap(Context context, int drawableId) {
		Drawable drawable = context.getResources().getDrawable(drawableId);
		BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;

		Bitmap bitmaps = bitmapDrawable.getBitmap();
		return bitmaps;
	}

	public static Drawable getDrawable(Context context, Bitmap bitmap) {
		Drawable drawable = new BitmapDrawable(bitmap);
		return drawable;
	}

	public static List<PictureModel> getSdcardFileImage(String path) {
		List<PictureModel> list = new ArrayList<PictureModel>();
		try {
			File imgDir = new File(path);
			File[] imgFiles = imgDir.listFiles();
			for (File file : imgFiles) {
				PictureModel imageData = new PictureModel();
				imageData.url = file.getAbsolutePath();
				imageData.bitmap = getSdcardImage(imageData.url);
				list.add(imageData);
			}
			return list;
		}
		catch (Exception e) {
			return null;
		}
	}

	public static Bitmap getSdcardImage(String path) {
		Bitmap imageBitmap = BitmapFactory.decodeFile(path);
		return imageBitmap;
	}

	public static void createSdcardFile(Context context, String path) {
		if (!sdcardEnabled()) {
			showToastInfo(context, R.string.sdcard_not_available);
			return;
		}

		File dir = Environment.getExternalStorageDirectory();
		File imageDir = new File(dir.getAbsolutePath() + path);

		if (!imageDir.exists()) {
			imageDir.mkdir();
		}
	}

	public static boolean sdcardEnabled() {
		return (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED));
	}

	public static void showToastInfo(Context context, int messageId) {
		Toast.makeText(context, messageId, Toast.LENGTH_SHORT).show();
	}

	public static void deleteSdcardFile(String path) {
		if (path != null) {
			File file = new File(path);
			boolean deleted = file.delete();
			if (deleted) {
				// file success
			}
			else {
				// file fail
			}

		}
	}

	public static byte[] gzipCompress(byte[] data) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		GZIPOutputStream zos = new GZIPOutputStream(bos);
		try {
			zos.write(data);
		}
		finally {
			zos.close();
		}
		return bos.toByteArray();
	}

	public static byte[] gzipUncompress(byte[] data) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		GZIPInputStream zis = new GZIPInputStream(bis);
		byte[] buf = new byte[512];
		try {
			int count;
			while ((count = zis.read(buf)) != -1)
				bos.write(buf, 0, count);
		}
		finally {
			zis.close();
		}
		return bos.toByteArray();
	}

	public static void saveMaskToSdCard(byte[] mask, String path) {
		File dir = Environment.getExternalStorageDirectory();
		File file = new File(dir.getAbsolutePath() + path);
		try {
			FileOutputStream out = new FileOutputStream(file);
			out.write(mask);
			out.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static byte[] readMaskBySdcard(String file) {
		byte[] buffer = null;
		try {
			FileInputStream in = new FileInputStream(file);
			int length;
			length = in.available();

			buffer = new byte[length];
			in.read(buffer);
			System.out.println("==buffer===" + buffer);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return buffer;
	}

	/**
	 * Mask从Bytes转换为Bits，再用GZip压缩
	 * 
	 * @param mask
	 * @param count
	 * @return
	 * @throws IOException
	 */
	public static byte[] zipMask(byte[] mask, int count) throws IOException {
		byte[] maskBits = new byte[(count + 7) / 8];
		for (int i = 0, k = 0; i < count; k++) {
			byte t = 0;
			for (int j = 0; j < 8 && i < count; j++, i++) {
				t <<= 1;
				t |= (mask[i] == 0 ? 0 : 1) & 1;
			}
			maskBits[k] = t;
		}
		return Utils.gzipCompress(maskBits);
	}

	/**
	 * Mask先用GZip解压缩，然后从Bits转换为Bytes
	 * 
	 * @param zippedMask
	 * @param pixelCount
	 * @return
	 * @throws IOException
	 */
	public static byte[] unzipMask(byte[] zippedMask, int pixelCount) throws IOException {
		byte[] maskBits = Utils.gzipUncompress(zippedMask);
		byte[] alphaBytes = new byte[pixelCount];
		System.out.println("Mask compressed length: " + zippedMask.length
				+ ", uncompressed length: " + maskBits.length + ", pixel count: " + pixelCount);
		for (int i = 0, k = 0; i < pixelCount; k++) {
			byte t = maskBits[k];
			for (int j = 0; j < 8 && i < pixelCount; j++, i++) {
				alphaBytes[i] = (t & 0x80) == 0 ? 0 : (byte) -1;
				t <<= 1;
			}
		}
		return alphaBytes;
	}

	/**
	 * 从Bitmap和Mask重新创建带透明度的图片
	 * 
	 * @param bmp
	 * @param maskGZip
	 * @return
	 * @throws IOException
	 */
	public static Bitmap maskBitmap(Bitmap bmp, byte[] mask) throws IOException {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		int count = width * height;
		int[] pixels = new int[count];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		ImgProc.applyAlphaToArgb(mask, pixels, 0, count);
		return Bitmap.createBitmap(pixels, width, height, Config.ARGB_8888);
	}
}
