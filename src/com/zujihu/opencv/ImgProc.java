package com.zujihu.opencv;

public class ImgProc {

	public static final int	GC_BGD				= 0;	// !< background
	public static final int	GC_FGD				= 1;	// !< foreground
	public static final int	GC_PR_BGD			= 2;	// !< most probably background
	public static final int	GC_PR_FGD			= 3;	// !< most probably foreground

	public static final int	GC_INIT_WITH_RECT	= 0;
	public static final int	GC_INIT_WITH_MASK	= 1;
	public static final int	GC_EVAL				= 2;

	/**
	 * 调用OpenCV中的背景剔除算法grabCut
	 * 
	 * @param image
	 *            需要剔除背景的图片，必需是不含Alpha通道的RGB 3通道像素
	 * @param width
	 *            图片宽度
	 * @param height
	 *            图片高度
	 * @param mask
	 *            [输入/输出]8bit图片掩码，1-前景，3-可能的前景，0-背景，2-可能的背景。
	 * @param x0
	 *            矩形区域左边坐标
	 * @param y0
	 *            矩形区域上边坐标
	 * @param x1
	 *            矩形区域右边坐标
	 * @param y1
	 *            矩形区域下边坐标
	 * @param iterCount
	 *            迭代次数
	 * @param mode
	 *            掩码初始化模式： 0-矩形区域初始化，不能是整个图像区域，1-使用传入的掩码本身，掩码本身不需同时包含前景和背景，2-继续上一次的操作
	 */
	public static native void grabCut(byte image[], int width, int height, byte[] mask, int x0,
			int y0, int x1, int y1, int iterCount, int mode);

	public static native void floodFillMaskOnly(byte image[], int width, int height, byte[] mask,
			int seedX, int seedY, int newColor, int loDiff, int upDiff);

	static {
		System.loadLibrary("native_sample");
	}

	/**
	 * Convert ARGB byte array to RGB byte array
	 * 
	 * @param argb
	 * @param start
	 * @param count
	 * @param rgb
	 */
	public static void argbToRgb(byte[] argb, int start, int count, byte rgb[]) {
		int i = start * 3;
		int j = start * 4;
		int end = (start + count) * 3;
		for (; i < end; i += 3, j += 4) {
			rgb[i] = argb[j];
			rgb[i + 1] = argb[j + 1];
			rgb[i + 2] = argb[j + 2];
		}
	}

	/**
	 * Convert RGB byte array to ARGB byte array
	 * 
	 * @param rgb
	 * @param start
	 * @param count
	 * @param argb
	 */
	public static void rgbToArgb(byte[] rgb, int start, int count, byte argb[]) {
		// Convert from RGB to ARGB
		int i = (start + count - 1) * 4;
		int j = (start + count - 1) * 3;
		int end = start * 4;
		for (; i >= end; i -= 4, j -= 3) {
			argb[i + 3] = -1;
			argb[i + 2] = rgb[j + 2];
			argb[i + 1] = rgb[j + 1];
			argb[i] = rgb[j];
		}
	}

	/**
	 * Convert RGB byte array to ARGB byte array, fill Alpha value with the given alpha byte array
	 * 
	 * @param rgb
	 * @param alpha
	 * @param start
	 * @param count
	 * @param argb
	 */
	public static void rgbToArgbWithAlpha(byte[] rgb, byte[] alpha, int start, int count,
			byte argb[]) {
		// Convert from RGB to ARGB
		int i = start + count - 1;
		int k = (start + count - 1) * 4;
		int j = (start + count - 1) * 3;
		for (; i >= start; k -= 4, j -= 3, i--) {
			argb[k + 3] = alpha[i];
			argb[k + 2] = rgb[j + 2];
			argb[k + 1] = rgb[j + 1];
			argb[k] = rgb[j];
			// if (alpha[k] == 0) {
			// argb[i + 2] = 0;
			// argb[i + 1] = 0;
			// argb[i] = 0;
			// }
		}
	}

	public static void rgbToArgbWithAlpha(byte[] rgb, byte[] alpha, int start, int count,
			int argb[]) {
		// Convert from RGB to ARGB
		int i = start + count - 1;
		int j = (start + count - 1) * 3;
		for (; i >= start; j -= 3, i--) {
			argb[i] = (alpha[i] << 24) | (rgb[j] << 16 & 0xFF0000) | (rgb[j + 1] << 8 & 0xFF00)
					| (rgb[j + 2] & 0xFF);
		}
	}

	public static void applyAlphaToArgb(byte[] alpha, int argb[], int start, int count) {
		for (count += start; start < count; start++)
			argb[start] = (argb[start] & 0xFFFFFF) | (alpha[start] << 24);
	}

	public static void grayToArgb(byte[] gray, int start, int count, byte argb[]) {
		// Convert from GRAY to ARGB
		int i = start + count - 1;
		int j = (start + count - 1) * 4;
		for (; i >= start; j -= 4, i--) {
			argb[j + 3] = -1;
			argb[j + 2] = argb[j + 1] = argb[j] = gray[i];
		}
	}
}
