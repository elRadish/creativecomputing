package cc.creativecomputing.opencv;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cc.creativecomputing.app.modules.CCAnimator;
import cc.creativecomputing.core.CCProperty;
import cc.creativecomputing.core.events.CCListenerManager;
import cc.creativecomputing.core.logging.CCLog;
import cc.creativecomputing.graphics.CCDrawMode;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.texture.CCTexture;
import cc.creativecomputing.math.CCColor;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCTransform;
import cc.creativecomputing.math.CCVector2;
import cc.creativecomputing.math.CCVector3;
import cc.creativecomputing.opencv.CCHandTracker.CCFixedTipEvent;
import cc.creativecomputing.opencv.CCHandTracker.CCHandInfo;
import cc.creativecomputing.opencv.filtering.CCFlip;


public class CCHandRemoteTracker {

	@CCProperty(name = "rotangle", min = 0, max = 3.14)
	protected double _cRotAngle = 0;
	
	@CCProperty(name = "tip smooth", min = 0, max = 1)
	protected double _cTipSmooth = 0;
	@CCProperty(name = "jitter smooth", min = 0, max = 1)
	protected double _cJitterSmooth = 0;

	@CCProperty(name = "min rest time", min = 0, max = 200)
	protected double _cMinRestFrames = 30;
	@CCProperty(name = "min size")
	private double _cMinSize = 2;
	@CCProperty(name = "max jitter", min = 0, max = 100)
	protected double _cMaxJitter = 30;
	@CCProperty(name = "min valid frames", min = 0, max = 100)
	protected double _cMinValidFrames = 20;

	@CCProperty(name = "progress angle", min = 0, max = 360)
	protected double _cProgressAngle = 0;

	@CCProperty(name = "selection radius", min = 0, max = 50)
	protected double _cSelectionRadius = 20;
	@CCProperty(name = "line strength", min = 0, max = 50)
	protected double _cLineStrength = 0;
	@CCProperty(name = "line color")
	protected CCColor _cLineColor = new CCColor();
	@CCProperty(name = "progress strength", min = 0, max = 50)
	protected double _cProgressStrength = 0;
	@CCProperty(name = "progress color")
	protected CCColor _cProgressColor = new CCColor();

	@CCProperty(name = "draw selection")
	private boolean _cDrawSelection = false;

	@CCProperty(name = "draw finger tips")
	private boolean _cDrawFingerTip = true;
	@CCProperty(name = "tip radius")
	private double _cTipRadius = 10;
	
	@CCProperty(name = "vflip")
	private boolean _cVFlip = false;
	
	@CCProperty(name = "hflip")
	private boolean _cHFlip = false;
	
	private boolean _myIsInDebug = false;
	private boolean _myIsInConfig = false;
	
	int w = 1440;
	int h = 1080;
	final int PORT = 1234;
	
	private CCTransform _myTransform = new CCTransform();

	
	public CCListenerManager<CCFixedTipEvent> fixedTipEvents = CCListenerManager.create(CCFixedTipEvent.class);
	private CCHandInfo theLastHand = new CCHandInfo();

	protected ArrayList<CCHandInfo> _myHands  = new ArrayList<CCHandInfo>();
	String[] fingerNames = new String[] {"THUMB","INDEX_FINGER", "MIDDLE_FINGER", "RING_FINGER", "PINKY"};
//	String[] fingerNames = new String[] {  "PINKY", "RING_FINGER", "MIDDLE_FINGER", "INDEX_FINGER", "THUMB"};

	
	Runnable receiver = new Runnable() {
		boolean running = true;
		JsonParser parser = new JsonParser();
		byte[] receiveBuffer = new byte[65535];

		DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);

		@Override
		public void run() {
			DatagramSocket ds;
			try {
				ds = new DatagramSocket(PORT);

				while(running) {
					try {
						ds.receive(packet);
						String d = new String(packet.getData()).substring(packet.getOffset(), packet.getLength()).trim().strip();

						JsonObject handData = parser.parse(d).getAsJsonObject();

						System.out.println(handData.toString());
					
						CCHandInfo hand = new CCHandInfo();
						hand.isHand = true;
						
						hand.tip.x = mapX(handData.get("INDEX_FINGER_TIP").getAsJsonArray().get(0).getAsDouble());
						hand.tip.y = mapY(handData.get("INDEX_FINGER_TIP").getAsJsonArray().get(1).getAsDouble());
					
						hand.center.x = handData.get("WRIST").getAsJsonArray().get(0).getAsDouble() * w;
						hand.center.y = handData.get("WRIST").getAsJsonArray().get(0).getAsDouble() * h;
						
						hand.fingerTips = new ArrayList<CCVector3>();
						for (String finger : fingerNames) {
							double x = handData.get(finger+"_TIP").getAsJsonArray().get(0).getAsDouble();
							double y = handData.get(finger+"_TIP").getAsJsonArray().get(1).getAsDouble();
							double z = handData.get(finger+"_TIP").getAsJsonArray().get(2).getAsDouble();
							hand.fingerTips.add(new CCVector3(w*x,h*y,z).rotate(0, 0, 1, _cRotAngle));							
						}
						
						_myHands.clear();
						updateHandInfo(theLastHand, hand);
						_myHands.add(theLastHand);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (SocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		};
	};

	private double mapX(double x) {
		return _cHFlip ? w-(x * w) : x*w ;
	}
	
	private double mapY(double y) {
		return _cVFlip ? h-(y * h) : y*h ;
	}
	
	Thread receiverThread = new Thread(receiver); 
	public CCHandRemoteTracker(CCCVVideoIn theVideoIn, Path theMaskTexture) {
		receiverThread.start();
	}



	private void drawFingerTips(CCHandInfo myInfo, CCGraphics g) {
		//if(!_cDrawFingerTip)return;
		g.color(CCColor.MAGENTA, 0.5);
		myInfo.fingerTips.forEach(myTip -> g.ellipse(myTip.xy(),_cTipRadius,_cTipRadius, false));
		if(myInfo.tip.isZero())return;
		g.color(CCColor.CYAN, 0.5);
		g.ellipse(myInfo.tip,_cTipRadius,_cTipRadius, false);
	}

	private void updateHandInfo(CCHandInfo theLastHand, CCHandInfo theNewHand) {
		theLastHand.isCorrelated = true;

		theLastHand.handContour = theNewHand.handContour;
		theLastHand.simpleContour = theNewHand.simpleContour;
		theLastHand.fingerTips = theNewHand.fingerTips;
		theLastHand.startAngle = theNewHand.startAngle;

		theLastHand.center.x = theNewHand.center.x * (1 - _cTipSmooth) + theLastHand.center.x * _cTipSmooth;
		theLastHand.center.y = theNewHand.center.y * (1 - _cTipSmooth) + theLastHand.center.y * _cTipSmooth;

		double myJitter = theNewHand.tip.distance(theLastHand.tip);

		if(!theNewHand.tip.isZero()) {
			theLastHand.tip.x = theNewHand.tip.x * (1 - _cTipSmooth) + theLastHand.tip.x * _cTipSmooth;
			theLastHand.tip.y = theNewHand.tip.y * (1 - _cTipSmooth) + theLastHand.tip.y * _cTipSmooth;
		}

		theLastHand.jitter = myJitter * (1 - _cJitterSmooth) + theLastHand.jitter * _cJitterSmooth;
		theLastHand.validFrames++;

		double myLastRestTime = theLastHand.restFrames;
		if(theLastHand.jitter < _cMaxJitter && !theLastHand.tip.isZero()) {
			theLastHand.restFrames++;
		} else {
			theLastHand.restFrames = 0;
		}

		theLastHand.progress = CCMath.saturate(theLastHand.restFrames / _cMinRestFrames);
		if(myLastRestTime < _cMinRestFrames && theLastHand.restFrames >= _cMinRestFrames) {
			fixedTipEvents.proxy().event(theLastHand.tip);
		}
	}

	public void drawDebug(CCGraphics g) {
		g.applyTransform(transform());
		g.popMatrix();
		g.color(CCColor.YELLOW, 0.5);
		g.ellipse(new CCVector2(0,0),14,14, true);
		
		g.ellipse(new CCVector2(1,1200),10,10, true);
		g.ellipse(new CCVector2(1920,1),10,10, false);
		g.ellipse(new CCVector2(1920,1200),10,10, false);
		g.pushMatrix();

		g.color(255);
		for(CCHandInfo myInfo:_myHands) {
			drawFingerTips(myInfo, g);
		}

		if(_cDrawSelection)drawSelection(g);

	}
	
	public List<CCHandInfo> hands(){
		return _myHands;
	}

	public void drawSelection(CCGraphics g) {
		g.applyTransform(transform());
		g.popMatrix();
		g.color(CCColor.YELLOW, 0.5);
		g.ellipse(new CCVector2(0,0),14,14, true);
		
		g.ellipse(new CCVector2(1,1080),10,10, true);
		g.ellipse(new CCVector2(1440,1),10,10, false);
		g.ellipse(new CCVector2(1440,1080),10,10, false);
		g.pushMatrix();
		
		for(CCHandInfo myInfo:_myHands) {
			if(myInfo.tip.isZero())continue;
			if(myInfo.validFrames < _cMinValidFrames) {
				CCLog.info(myInfo.validFrames);
				continue;
			}
			g.color(1d);

			CCVector2 myDirection = myInfo.tip.subtract(myInfo.center).normalizeLocal();
			double startAngle = CCMath.atan2(-myDirection.y, myDirection.x);

			g.color(_cLineColor);
			g.beginShape(CCDrawMode.TRIANGLE_STRIP);
			for(double a = _cProgressAngle * myInfo.progress; a < _cProgressAngle;a++) {
				double myAngle = CCMath.radians(a) - startAngle - CCMath.radians(_cProgressAngle / 2);// - CCMath.HALF_PI;
				g.vertex(CCVector2.circlePoint(myAngle, _cSelectionRadius - _cLineStrength / 2, myInfo.tip.x, myInfo.tip.y));
				g.vertex(CCVector2.circlePoint(myAngle, _cSelectionRadius + _cLineStrength / 2, myInfo.tip.x, myInfo.tip.y));
			}
			g.endShape();

			g.color(_cProgressColor);
			if(myInfo.progress>= 1)g.color(CCColor.CYAN);
			g.beginShape(CCDrawMode.TRIANGLE_STRIP);
			for(double a = 0; a < _cProgressAngle * myInfo.progress;a++) {
				double myAngle = CCMath.radians(a) - startAngle - CCMath.radians(_cProgressAngle / 2);// - CCMath.HALF_PI;
				g.vertex(CCVector2.circlePoint(myAngle, _cSelectionRadius - _cProgressStrength / 2, myInfo.tip.x, myInfo.tip.y));
				g.vertex(CCVector2.circlePoint(myAngle, _cSelectionRadius + _cProgressStrength / 2, myInfo.tip.x, myInfo.tip.y));
			}
			g.endShape();
		}
		g.color(1d);
	}
	
	
	private CCTransform transform() {
		return _myTransform;
	}

	public void isInDebug(boolean isInDebug) {
		_myIsInDebug = isInDebug;
	}
	
	public void isInConfig(boolean isInConfig) {
		_myIsInConfig = isInConfig;
	}

	public void active(boolean _myIsTrackingActive) {
		// TODO Auto-generated method stub
		
	}

	public void updateDebugTexture(boolean b) {
		// TODO Auto-generated method stub
		
	}

	public void update(CCAnimator theAnimator) {
		// TODO Auto-generated method stub
		
	}

	public CCTexture debugTexture() {
		// TODO Auto-generated method stub
		return null;
	}

	public void preDisplay(CCGraphics g) {
		// TODO Auto-generated method stub
		
	}
}
