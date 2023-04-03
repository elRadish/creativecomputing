package cc.creativecomputing.opencv;

import java.util.List;

import cc.creativecomputing.app.modules.CCAnimator;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.texture.CCTexture;
import cc.creativecomputing.opencv.CCHandTracker.CCFixedTipEvent;
import cc.creativecomputing.opencv.CCHandTracker.CCHandInfo;

public interface CCIHandTracker {

	void drawDebug(CCGraphics g);

	List<CCHandInfo> hands();

	void drawSelection(CCGraphics g);

	void isInDebug(boolean isInDebug);

	void isInConfig(boolean isInConfig);

	void active(boolean _myIsTrackingActive);

	void updateDebugTexture(boolean b);

	void update(CCAnimator theAnimator);
	
	void addFixedTipevent(CCFixedTipEvent event);

	CCTexture debugTexture();

	void preDisplay(CCGraphics g);

}