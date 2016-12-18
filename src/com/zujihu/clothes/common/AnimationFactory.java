package com.zujihu.clothes.common;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public class AnimationFactory {

	public static class ZujiHuAnimationListener implements
		android.view.animation.Animation.AnimationListener {

		@Override
		public void onAnimationEnd(Animation animation) {

		}

		@Override
		public void onAnimationRepeat(Animation animation) {

		}

		@Override
		public void onAnimationStart(Animation animation) {

		}
	}

	public static void translateScaleView(int[] startXY, int[] stopXY, View moveView,
			AnimationListener animationListener) {
		if (moveView == null) {
			return;
		}

		if (startXY == null || startXY.length < 2) {
			startXY = new int[] { 0, 0 };
		}

		if (stopXY == null || stopXY.length < 2) {
			stopXY = new int[] { 0, 0 };
		}

		ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.5f, 1f, 0.5f);
		TranslateAnimation translateAnimation = new TranslateAnimation(startXY[0], stopXY[0],
			startXY[1], stopXY[1]);

		AnimationSet animationSet = new AnimationSet(false);
		animationSet.addAnimation(scaleAnimation);
		animationSet.addAnimation(translateAnimation);
		animationSet.setAnimationListener(animationListener);
		scaleAnimation.setDuration(1000);
		scaleAnimation.setRepeatCount(0);
		translateAnimation.setInterpolator(new DecelerateInterpolator());
		translateAnimation.setDuration(1000);
		translateAnimation.setRepeatCount(0);
		moveView.startAnimation(animationSet);
	}

}
