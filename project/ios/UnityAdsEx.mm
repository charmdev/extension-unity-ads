#include <hx/CFFI.h>
#include <UnityAdsEx.h>
#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <UnityAds/UnityAds.h>
#import <UnityAds/UADSMetaData.h>


using namespace unityads;

extern "C" void sendUnityAdsEvent(char* event);

@interface UnityAdsController : NSObject <UnityAdsDelegate>

- (id)initWithID:(NSString*)appID testModeOn:(BOOL)testMode debugModeOn:(BOOL)debugMode;
- (void)showRewardedAdWithPlacementID:(NSString*)videoPlacementId andTitle:(NSString*)title withMsg:(NSString*)msg;
- (BOOL)canShowUnityAds:(NSString*)placementId;

@end

@implementation UnityAdsController

- (id)initWithID:(NSString*)ID testModeOn:(BOOL)testMode debugModeOn:(BOOL)debugMode
{
	NSLog(@"UnityAds Init");
	self = [super init];
	if (!self) return nil;
	
	[UnityAds setDebugMode:debugMode];
	[UnityAds initialize:ID delegate:self testMode:testMode];
	
	return self;
}

- (void)showRewardedAdWithPlacementID:(NSString*)rewardPlacementId andTitle:(NSString*)title withMsg:(NSString*)msg
{
	if ([UnityAds isReady:rewardPlacementId])
	{
		NSLog(@"UnityAds show start ");

		UIViewController *viewController = [[[UIApplication sharedApplication] keyWindow] rootViewController];

		[UnityAds show:viewController placementId:rewardPlacementId];
	}
}

- (BOOL)canShowUnityAds:(NSString*)placementId
{
	return [UnityAds isReady: placementId];
}


#pragma mark - UnityAdsSDK Delegate

- (void)unityAdsReady:(NSString *)placementId {
	NSLog(@"unityAdsReady");
}

- (void)unityAdsDidError:(UnityAdsError)error withMessage:(NSString *)message {
	NSLog(@"UnityAds ERROR: %ld - %@",(long)error, message);
}

- (void)unityAdsDidStart:(NSString *)placementId {
	NSLog(@"unityAdsDidShow");
}

- (void)unityAdsDidFinish:(NSString *)placementId withFinishState:(UnityAdsFinishState)state {
	NSLog(@"unityAdsDidFinish");
	
	switch (state) {
		case kUnityAdsFinishStateError:
			sendUnityAdsEvent("unity_videoisskipped");
			break;
		case kUnityAdsFinishStateSkipped:
			sendUnityAdsEvent("unity_videoisskipped");
			break;
		case kUnityAdsFinishStateCompleted:
			sendUnityAdsEvent("unity_rewardedcompleted");
			break;
		default:
			break;
	}
}

@end

namespace unityads {
	
	static UnityAdsController *unityAdsController;
	
	void init(const char *__appID, bool testMode, bool debugMode)
	{
		if (unityAdsController == NULL) {
			unityAdsController = [[UnityAdsController alloc] init];
		}
		
		NSString *appID = [NSString stringWithUTF8String:__appID];
		[unityAdsController initWithID:appID testModeOn:(BOOL)testMode debugModeOn:(BOOL)debugMode];
	}
	
	void showRewarded(const char *__rewardPlacementId,const char *__title,const char *__msg)
	{
		NSString *rewardPlacementId = [NSString stringWithUTF8String:__rewardPlacementId];
		NSString *title = [NSString stringWithUTF8String:__title];
		NSString *msg = [NSString stringWithUTF8String:__msg];
		
		if (unityAdsController != NULL)
			[unityAdsController showRewardedAdWithPlacementID:rewardPlacementId andTitle:title withMsg:msg];
	}
	
	bool unityCanShow(const char *__placementId)
	{
		if (unityAdsController == NULL) return false;
		NSString *placementId = [NSString stringWithUTF8String:__placementId];
		return [unityAdsController canShowUnityAds:placementId];
	}
}
