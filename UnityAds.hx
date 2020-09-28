import nme.Lib;
import nme.JNI;

class UnityAds {

	private static var initialized:Bool=false;

#if ios
	private static var __init:String->Bool->Bool->Void = function(appId:String,testMode:Bool, debugMode:Bool){};
	private static var __unityads_set_event_handle = Lib.load("unityads","unityads_set_event_handle", 1);
#end
#if android
	private static var __init:Dynamic;
#end

	private static var __showRewarded:String->String->String->Void = function(rewardPlacementId:String,alertTitle:String,alertMSG:String){};
	private static var __canShowAds:String->Bool = function(placementId:String):Bool {return false;};
	
	private static var completeCB:Void->Void;
	private static var skipCB:Void->Void;

	public static var onRewardedEvent:String->Void = null;

	public static function init(appId:String, testMode:Bool, debugMode:Bool) {
#if ios
		if(initialized) return;
		initialized = true;
		try{
			__init = cpp.Lib.load("unityads","unityads_init",3);
			__showRewarded = cpp.Lib.load("unityads","unityads_rewarded_show",3);
			__canShowAds = cpp.Lib.load("unityads","unityads_canshow",1);

			__init(appId, testMode, debugMode);
			__unityads_set_event_handle(notifyListeners);
		}catch(e:Dynamic){
			trace("iOS INIT Exception: "+e);
			initialized = false;
		}
#end

#if android
		if (initialized) return;
		initialized = true;
		try {
			__showRewarded = JNI.createStaticMethod("com/unityads/UnityAdsEx", "showRewarded", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
			__canShowAds = JNI.createStaticMethod("com/unityads/UnityAdsEx", "canShowUnityAds", "(Ljava/lang/String;)Z");
			
			if(__init == null) {
				__init = JNI.createStaticMethod("com/unityads/UnityAdsEx", "init", "(Lorg/haxe/lime/HaxeObject;Ljava/lang/String;ZZ)V", true);
			}

			var args = new Array<Dynamic>();
			args.push(new UnityAds());
			args.push(appId);
			args.push(testMode);
			args.push(debugMode);
			__init(args);
		}catch(e:Dynamic){
			trace("Android INIT Exception: "+e);
		}
#end
	}

	public static function showRewarded(rewardPlacementId:String,alertTitle:String,alertMSG:String, cb, skip)
	{
		completeCB = cb;
		skipCB = skip;

		try {
			__showRewarded(rewardPlacementId,alertTitle,alertMSG);
		} catch(e:Dynamic) {
			trace("ShowRewarded Exception: "+e);
		}
	}

	public static function canShowAds(placementID:String):Bool
	{
		return __canShowAds(placementID);
	}

#if ios
	private static function notifyListeners(inEvent:Dynamic)
	{
		var event:String = Std.string(Reflect.field(inEvent, "type"));

		if (event == "rewardedcompleted")
		{
			trace("UnityAds REWARDED COMPLETED");
			dispatchEventIfPossible("CLOSED");
			if (completeCB != null) completeCB();
		}
		else if (event == "videoisskipped")
		{
			trace("UnityAds REWARDED SKIPPED");
			dispatchEventIfPossible("CLOSED");
			if (skipCB != null) skipCB();
		}
	}
#end

#if android
	private function new() {}
	
	public function onRewardedCompleted()
	{
		trace("UnityAds onRewardedCompleted");
		dispatchEventIfPossible("CLOSED");
		if (completeCB != null) completeCB();
	}
	public function onVideoSkipped()
	{
		trace("UnityAds onVideoSkipped");
		dispatchEventIfPossible("CLOSED");
		if (skipCB != null) skipCB();
	}
#end

	private static function dispatchEventIfPossible(e:String):Void
	{
		if (onRewardedEvent != null)
		{
			onRewardedEvent(e);
		}
		else
		{
			trace('no event handler');
		}
	}

}
