package cc.creativecomputing.control;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import cc.creativecomputing.control.timeline.point.TimedEventPoint;
import cc.creativecomputing.core.CCProperty;
import cc.creativecomputing.core.events.CCListenerManager;

@SuppressWarnings({"rawtypes","unchecked"})
public abstract class CCAsset <AssetType>{
	
	public interface CCAssetListener<AssetType>{
		void onChange(AssetType theAsset);
	}

	protected AssetType _myAsset ;
	
	protected Path _myAssetPath;
	
	protected Map<Path, AssetType> _myAssetMap = new HashMap<>();
	
	protected CCListenerManager<CCAssetListener> _myEvents = CCListenerManager.create(CCAssetListener.class);
	
	public CCAsset(){
		
	}
	
	public AssetType value(){
		return _myAsset;
	}
	
	public abstract AssetType loadAsset(Path thePath);
	
	public AssetType checkLoadAsset(Path theFilePath){
		if(_myAssetMap.containsKey(theFilePath)){
			return _myAssetMap.get(theFilePath);
		}else{
			try{
				
				AssetType myData = loadAsset(theFilePath);
				_myAssetMap.put(theFilePath, myData);
				
				return myData;
			}catch(Exception e){
				e.printStackTrace();
				return null;
			}
		}
	}
	
	public void onChangePath(Path thePath){
		if(thePath == null){
			_myAsset = null;
			return;
		}
		if(_myAssetMap.containsKey(thePath)){
			_myAsset = _myAssetMap.get(thePath);
			return;
		}else{
			_myAsset = loadAsset(thePath);
			_myAssetMap.put(thePath, _myAsset);
		}
	}
	
	@CCProperty(name = "path")
	public final void path(Path thePath){
		if(thePath == _myAssetPath || (thePath != null && thePath.equals(_myAssetPath)))return;
		_myAssetPath = thePath;
		onChangePath(thePath);
		_myEvents.proxy().onChange(_myAsset);
	}
	
	public void mute(boolean theMute){
		
	}
	
	public String[] extensions(){
		return null;
	}
	
	public CCListenerManager<CCAssetListener> events(){
		return _myEvents;
	}
	
	public Path path(){
		return _myAssetPath;
	}
	
	public void time(double theGlobalTime, double theEventTime, double theContentOffset){}
	
	public void renderTimedEvent(TimedEventPoint theTimedEvent, Point2D theLower, Point2D theUpper, double lowerTime, double UpperTime, Graphics2D theG2d) {
		if(theTimedEvent.content() == null || theTimedEvent.content().value() == null) {
			return;
		}
		
//		Point2D myPos = theView.controller().curveToViewSpace(new ControlPoint(theTimedEvent.time(),1));
//		Point2D myEndPos = theView.controller().curveToViewSpace(new ControlPoint(theTimedEvent.endTime(),1));
//		double width = myEndPos.getX() - myPos.getX();
//		theG2d.setColor(new Color(0,0,0,100));
//		
//		FontMetrics myMetrix = theG2d.getFontMetrics();
//		String myString = theTimedEvent.content().value().toString();
//		int myIndex = myString.length() - 1;
//		StringBuffer myText = new StringBuffer();
//		while(myIndex >= 0 && myMetrix.stringWidth(myText.toString() + myString.charAt(myIndex)) < width - 5){
//			myText.insert(0, myString.charAt(myIndex));
//			myIndex--;
//		}
//		theG2d.drawString(myText.toString(), (int) myPos.getX() + 5, (int) myPos.getY() + 15);
	}
	
	public void reset(TimedEventPoint theTimedEvent){
		theTimedEvent.contentOffset(0);
	}
	
	public void out(){}
	
	public void play(){}
	
	public void stop(){}
}
