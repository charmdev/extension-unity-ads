package com.unityads;

import android.app.Activity;
import android.app.*;
import android.content.*;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.*;
import android.util.Log;
import android.content.ActivityNotFoundException;

import android.view.animation.Animation;
import android.view.animation.AlphaAnimation;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.view.ViewGroup;


import org.haxe.extension.Extension;
import org.haxe.lime.HaxeObject;

import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.IUnityAdsListener;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.ads.metadata.MediationMetaData;
import com.unity3d.ads.metadata.MetaData;
import com.unity3d.ads.metadata.PlayerMetaData;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.webview.WebView;

import android.opengl.GLSurfaceView;

public class UnityAdsEx extends Extension implements IUnityAdsListener {

	private static UnityAdsEx _self = null;
	protected static HaxeObject unityadsCallback;
	protected static UnityAds.FinishState resultFinishState;
	
	private LinearLayout layout;

	private static String appId = null;
	private static MetaData gdprMetaData = null;

	static public void init(HaxeObject cb, final String appId, final boolean testMode, final boolean debugMode){
		
		unityadsCallback = cb;
		UnityAdsEx.appId = appId;
		
		if (Extension.mainView == null) return;
		GLSurfaceView view = (GLSurfaceView) Extension.mainView;
		view.queueEvent(new Runnable() {
			public void run() {

				Log.d("UnityAdsEx","Init UnityAds appId:" + appId);
				UnityAds.setDebugMode(debugMode);
				UnityAds.initialize(mainActivity, appId, _self, testMode);
			}
		});
	}
	
	static public void showRewarded(final String rewardPlacementId, final String title, final String msg)
	{
		Log.d("UnityAdsEx","Show Rewarded Begin");
		if (appId=="") return;

		Extension.mainActivity.runOnUiThread(new Runnable() {
			public void run() {
					UnityAds.show(mainActivity, rewardPlacementId, mUnityShowListener);
				}
		});
		Log.d("UnityAdsEx","Show Rewarded End ");
	}
	
	public static boolean canShowUnityAds(final String placementId) {
		return UnityAds.isReady(placementId);
	}
	
	public static boolean isSupportedUnityAds() {
		return UnityAds.isSupported();
	}

	static private IUnityAdsShowListener mUnityShowListener = new IUnityAdsShowListener()
	{
		@Override
		public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message)
		{

		}

		@Override
		public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state)
		{
			
		}

		@Override
		public void onUnityAdsShowStart(String placementId)
		{
			Log.d("UnityAdsEx","onUnityAdsShowStart");

			if (Extension.mainView == null) return;
			GLSurfaceView view = (GLSurfaceView) Extension.mainView;
			view.queueEvent(new Runnable() {
				public void run() {
					if (unityadsCallback != null)
						unityadsCallback.call("onRewardedDisplaying", new Object[] {});
				}
			});
		}

		@Override	
		public void onUnityAdsShowClick(String placementId)
		{
			Log.d("UnityAdsEx","onUnityAdsShowClick");

			if (Extension.mainView == null) return;
			GLSurfaceView view = (GLSurfaceView) Extension.mainView;
			view.queueEvent(new Runnable() {
				public void run() {
					if (unityadsCallback != null)
						unityadsCallback.call("onRewardedClick", new Object[] {});
				}
			});
		}
		
	};

	@Override
	public void onUnityAdsReady(final String zoneId) {
		Log.d("UnityAdsEx","Fetch Completed ");
	}
	
	@Override
	public void onUnityAdsError(UnityAds.UnityAdsError error, String message) {
		Log.d("UnityAdsEx","Fetch Failed ");
	}
	
	@Override
	public void onUnityAdsStart(String zoneId) {
		Log.d("UnityAdsEx onUnityAdsStart", zoneId);
	}
	
	@Override
	public void onUnityAdsFinish(String zoneId, UnityAds.FinishState result) {
		
		resultFinishState = result;

		if (Extension.mainView == null) return;
		GLSurfaceView view = (GLSurfaceView) Extension.mainView;
		view.queueEvent(new Runnable() {
			public void run() {

				if (unityadsCallback != null)
				{
					switch(resultFinishState) {
						case ERROR:
							unityadsCallback.call("onVideoSkipped", new Object[] {});
							break;
						case SKIPPED:
							unityadsCallback.call("onVideoSkipped", new Object[] {});
							break;
						case COMPLETED:
							unityadsCallback.call("onRewardedCompleted", new Object[] {});
							break;
					}
				}
			}
		});
	}

	public void onCreate ( Bundle savedInstanceState )
	{
		super.onCreate(savedInstanceState);
		_self = this;
	}
	
	public void onResume () {
		super.onResume();
	}

}
