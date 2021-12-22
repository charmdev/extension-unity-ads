package com.unityads;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.Log;

import org.haxe.extension.Extension;
import org.haxe.lime.HaxeObject;
import android.opengl.GLSurfaceView;

import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAdsShowOptions;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;


public class UnityAdsEx extends Extension implements IUnityAdsInitializationListener {

	private static UnityAdsEx _self = null;
	protected static HaxeObject unityadsCallback;

	private static String appId = null;
	private static String adUnitId = null;
	private static Boolean canShow = false;
	protected static UnityAds.UnityAdsShowCompletionState resultFinishState;


	static public void init(HaxeObject cb, final String appId, final String adUnitId, final boolean testMode, final boolean debugMode) {
		
		unityadsCallback = cb;
		UnityAdsEx.appId = appId;
		UnityAdsEx.adUnitId = adUnitId;
		
		if (Extension.mainView == null) return;
		GLSurfaceView view = (GLSurfaceView) Extension.mainView;
		view.queueEvent(new Runnable() {
			public void run() {

				Log.d("UnityAdsEx","Init UnityAds appId:" + appId);
				UnityAds.setDebugMode(debugMode);
				UnityAds.initialize(mainActivity, appId, testMode, _self);
			}
		});
	}

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		_self = this;
	}

	public static boolean canShowUnityAds(final String placementId) {

		if (!canShow)
		{
			_self.onInitializationComplete();
		}

		return canShow;
	}

	@Override
	public void onInitializationComplete() {

		Log.d("UnityAdsEx","UnityAds.load");

		UnityAds.load(adUnitId, loadListener);
	}

	@Override
	public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
		
	}

	private IUnityAdsLoadListener loadListener = new IUnityAdsLoadListener()
	{
		@Override
		public void onUnityAdsAdLoaded(String placementId) {
			canShow = true;
			Log.d("UnityAdsEx","onUnityAdsAdLoaded");
		}

		@Override
		public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
			canShow = false;
			Log.d("UnityAdsEx","onUnityAdsFailedToLoad");

			_self.onInitializationComplete();

			GLSurfaceView view = (GLSurfaceView) Extension.mainView;
			view.queueEvent(new Runnable() {
				public void run() {
					unityadsCallback.call("onVideoSkipped", new Object[] {});
				}
			});
		}
	};
	



	static public void showRewarded(final String rewardPlacementId, final String title, final String msg)
	{
		Log.d("UnityAdsEx","Show Rewarded Begin " + rewardPlacementId);
		if (rewardPlacementId == "") return;

		Extension.mainActivity.runOnUiThread(new Runnable() {
			public void run() {
					UnityAds.show(mainActivity, rewardPlacementId, new UnityAdsShowOptions(), showListener);
				}
		});
	}

	static private IUnityAdsShowListener showListener = new IUnityAdsShowListener()
	{
		@Override
		public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
			Log.d("UnityAdsEx","onUnityAdsShowFailure");
			canShow = false;

			_self.onInitializationComplete();
			
			GLSurfaceView view = (GLSurfaceView) Extension.mainView;
			view.queueEvent(new Runnable() {
				public void run() {
					unityadsCallback.call("onVideoSkipped", new Object[] {});
				}
			});
		};

		@Override
		public void onUnityAdsShowStart(String placementId) {
			Log.d("UnityAdsEx","onUnityAdsShowStart");

			if (Extension.mainView == null) return;
			GLSurfaceView view = (GLSurfaceView) Extension.mainView;
			view.queueEvent(new Runnable() {
				public void run() {
					if (unityadsCallback != null)
						unityadsCallback.call("onRewardedDisplaying", new Object[] {});
				}
			});
		};

		@Override
		public void onUnityAdsShowClick(String placementId) {
			Log.d("UnityAdsEx","onUnityAdsShowClick");

			if (Extension.mainView == null) return;
			GLSurfaceView view = (GLSurfaceView) Extension.mainView;
			view.queueEvent(new Runnable() {
				public void run() {
					if (unityadsCallback != null)
						unityadsCallback.call("onRewardedClick", new Object[] {});
				}
			});
		};

		@Override
		public void onUnityAdsShowComplete(String placementId, final UnityAds.UnityAdsShowCompletionState state) {
			Log.v("UnityAdsExample", "onUnityAdsShowComplete: " + placementId);

			if (Extension.mainView == null || unityadsCallback == null) return;

			resultFinishState = state;

			GLSurfaceView view = (GLSurfaceView) Extension.mainView;
			view.queueEvent(new Runnable() {
				public void run() {
					if (state.equals(UnityAds.UnityAdsShowCompletionState.COMPLETED)) {
						unityadsCallback.call("onRewardedCompleted", new Object[] {});
					}
					else {
						unityadsCallback.call("onVideoSkipped", new Object[] {});
					}
				}
			});
		}
		
	};

}
