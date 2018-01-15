package cc.creativecomputing.math.interpolate;

public interface CCInterpolator {

	double interpolate(double theV0, double theV1, double theV2, double theV3, double theBlend, double... theparam);
	
}
