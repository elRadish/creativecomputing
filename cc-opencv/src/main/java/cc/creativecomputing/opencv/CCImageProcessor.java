package cc.creativecomputing.opencv;

import static org.bytedeco.javacpp.opencv_core.*;

import org.bytedeco.javacpp.opencv_core.Mat;

import cc.creativecomputing.core.CCProperty;
import cc.creativecomputing.graphics.CCGraphics;

public abstract class CCImageProcessor {
	
	
	
	@CCProperty(name = "bypass")
	protected boolean _cBypass = true;
	
	public abstract Mat implementation(Mat...theSources);
	
	public Mat process(Mat...theSources) {
		if(_cBypass)return theSources[0];
		
		
		return implementation(theSources);
	}
	
	public void preDisplay(CCGraphics g) {
		
	}
}
