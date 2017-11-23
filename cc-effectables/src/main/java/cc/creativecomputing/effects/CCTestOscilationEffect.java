package cc.creativecomputing.effects;

import cc.creativecomputing.core.CCProperty;
import cc.creativecomputing.math.CCMath;

public class CCTestOscilationEffect extends CCEffect{

	@CCProperty(name = "x amount", min = 0, max = 1)
	private double _cXAmount = 0;
	@CCProperty(name = "y amount", min = 0, max = 1)
	private double _cYAmount = 0;

	@CCProperty(name = "time decrease", min = 0, max = 10)
	private double _cTimeDecrease = 0;
	
	@CCProperty(name = "start time", min = 0, max = 360)
	private double _cStartTime = 0;
	
	@CCProperty(name = "min time", min = 0, max = 360)
	private double _cMinTime = 0;
	
	@CCProperty(name = "break time", min = 0, max = 60)
	private double _cBreakTime = 0;
	
	

	private double _myPhase = 0;
	private double _mySpeed = 0;
	
	private double _myTime = 0;
	
	private boolean _myBreak = false;
	private double _myBreakTime = 0;

	public void update(final double theDeltaTime) {
		if(_myBreak){
			_myBreakTime += theDeltaTime;
			if(_myBreakTime < _cBreakTime){
				return;
			}
			_myBreak = false;
		}
		
		double myOldPhase = _myPhase;
		_myPhase += theDeltaTime * _mySpeed;
		
		
		
		if(_myPhase % CCMath.PI < myOldPhase % CCMath.PI){
			_myTime -= _cTimeDecrease;
			if(_myTime < _cMinTime)_myTime = _cStartTime;
			_mySpeed = CCMath.PI / _myTime;
			_myBreak = true;
			_myBreakTime = 0;
		}
	}

	@Override
	public double[] applyTo(CCEffectable theElement) {
		double myX = CCMath.cos(_myPhase) * _cXAmount;
		double myY = CCMath.cos(_myPhase) * _cYAmount;

		double myBlend = elementBlend(theElement);
		return new double[]{myX * myBlend, myY * myBlend};
	}
	
	@CCProperty(name = "restart")
	private void restart(){
		start();
	}

	public void start() {
		_myPhase = 0;
		_myTime = _cStartTime;
		_mySpeed = CCMath.PI / _myTime;
		_myBreak = false;
	}

	public void end() {
		// TODO Auto-generated method stub
		
	}
	
	
}
