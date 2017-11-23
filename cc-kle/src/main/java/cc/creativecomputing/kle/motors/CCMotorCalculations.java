package cc.creativecomputing.kle.motors;

import java.util.List;

import cc.creativecomputing.core.CCProperty;
import cc.creativecomputing.kle.CCKleEffectable;

public abstract class CCMotorCalculations<SetupType extends CCMotorSetup> {

	protected double _myElementRadiusScale = 1;
	protected double _myTopDistance = 300;
	protected double _myBottomDistance = 1600;
	
	protected List<CCKleEffectable> _myElements;
	
	@CCProperty(name = "max velocity")
	private double _cMaxVelocity = 0;
	@CCProperty(name = "max acceleration")
	private double _cMaxAcceleration = 0;
	@CCProperty(name = "max jerk")
	private double _cMaxJerk = 0;
	@CCProperty(name = "apply filter")
	private boolean _cFilter = false;
	
	private double _myDeltaTime;
	
	public void setElements(List<CCKleEffectable> theElements){
		_myElements = theElements;
	}
	
	public boolean applyFilter(){
		return _cFilter;
	}
	
	public void update(double theDeltaTime){
		_myDeltaTime = theDeltaTime;
	}
	
	public double deltaTime(){
		return _myDeltaTime;
	}
	
	public double maxVelocity(){
		return _cMaxVelocity;
	}
	
	public double maxAcceleration(){
		return _cMaxAcceleration;
	}
	
	public double maxJerk(){
		return _cMaxJerk;
	}
	
	public abstract void updateBounds(SetupType mySetup);
	
	public abstract CCMotorSetupTypes type();
	
	@SuppressWarnings("unchecked")
	public void updateBounds(){
		if(_myElements == null)return;
		
		for(CCKleEffectable myElement:_myElements){
			CCMotorSetup mySetup = myElement.motorSetup();
			try{
				updateBounds((SetupType)mySetup);
			}catch(Exception e){
				
			}
		}
	}
	
	@CCProperty(name = "top distance", min = 0, max = 2000, defaultValue = 0)
	public void topDistance(double theTopDistance){
		_myTopDistance = theTopDistance;
		updateBounds();
	}
	
	public double topDistance(){
		return _myTopDistance;
	}
	
	@CCProperty(name = "bottom distance", min = 0, max = 2000, defaultValue = 2000)
	public void bottomDistance(double theBottomDistance){
		_myBottomDistance = theBottomDistance;
		updateBounds();
	}
	
	public double bottomDistance(){
		return _myBottomDistance;
	}
}
