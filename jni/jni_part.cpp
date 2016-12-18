#include <jni.h>
#include <opencv2/imgproc/imgproc.hpp>
#include <android/log.h>

using namespace std;
using namespace cv;

extern "C" {

JNIEXPORT void JNICALL Java_com_zujihu_opencv_ImgProc_grabCut(JNIEnv* env,
	jclass cls, jbyteArray image, jint width, jint height, jbyteArray mask,
	jint x0, jint y0, jint x1, jint y1, jint iterCount, jint mode)
{
    try {

	    jbyte* _image  = env->GetByteArrayElements(image, 0);
	    jbyte*  _mask = env->GetByteArrayElements(mask, 0);

	    Mat matImageBGR(height, width, CV_8UC3, (unsigned char *)_image);
	    Mat matMask(height, width, CV_8UC1, (unsigned char *)_mask);
	    Rect rect = Rect(Point(x0, y0), Point(x1, y1));
		Mat bgdModel, fgdModel;

		grabCut(matImageBGR, matMask, rect, bgdModel, fgdModel, iterCount, mode);

	    env->ReleaseByteArrayElements(image, _image, 0);
	    env->ReleaseByteArrayElements(mask, _mask, 0);

    } catch(cv::Exception e) {
        jclass je = env->FindClass("com/zujihu/opencv/OpenCvException");
        if(!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {ImgProc.grabCut}");
        return;
    }
}

JNIEXPORT void JNICALL Java_com_zujihu_opencv_ImgProc_floodFillMaskOnly(JNIEnv* env,
	jclass cls, jbyteArray image, jint width, jint height, jbyteArray mask,
	jint seedX, jint seedY, jint newColor, jint loDiff, jint upDiff)
{
    try {

	    jbyte* _image  = env->GetByteArrayElements(image, 0);
	    jbyte*  _mask = env->GetByteArrayElements(mask, 0);

		// Create a image with 1 pixel border in order to meet floodFill requirements for masks
	    Mat matImageBGR(height-2, width-2, CV_8UC3, (unsigned char *)_image+(width+1)*3, width*3);
	    Mat matMask(height, width, CV_8UC1, (unsigned char *)_mask);
	    Point seedPoint(seedX-1, seedY-1);
	    Scalar newColorS(newColor&0xFF, (newColor>>8 & 0xFF), (newColor>>16 & 0xFF));
	    Scalar loDiffS(loDiff&0xFF, (loDiff>>8 & 0xFF), (loDiff>>16 & 0xFF));
	    Scalar upDiffS(upDiff&0xFF, (upDiff>>8 & 0xFF), (upDiff>>16 & 0xFF));

		floodFill(matImageBGR, matMask, seedPoint, newColorS, 0, loDiffS, upDiffS,
		                            FLOODFILL_FIXED_RANGE|FLOODFILL_MASK_ONLY);

	    env->ReleaseByteArrayElements(image, _image, 0);
	    env->ReleaseByteArrayElements(mask, _mask, 0);

    } catch(cv::Exception e) {
        jclass je = env->FindClass("com/zujihu/opencv/OpenCvException");
        if(!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {ImgProc.grabCut}");
        return;
    }
}

}
