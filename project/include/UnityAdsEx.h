#ifndef UNITYADSEX_H
#define UNITYADSEX_H

namespace unityads {
	void init(const char *__appID, bool testMode, bool debugMode);
	void showRewarded(const char *__rewardPlacementId,const char *__title,const char *__msg);
	bool unityCanShow(const char *__placementId);
}

#endif
