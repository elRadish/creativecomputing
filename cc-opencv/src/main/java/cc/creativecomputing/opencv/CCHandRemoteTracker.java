package cc.creativecomputing.opencv;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.file.Path;
import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cc.creativecomputing.core.CCProperty;
import cc.creativecomputing.core.logging.CCLog;
import cc.creativecomputing.graphics.CCDrawMode;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.math.CCColor;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCVector2;
import cc.creativecomputing.math.CCVector3;


public class CCHandRemoteTracker extends CCHandTracker {
	
	@CCProperty(name = "rotangle", min = 0, max = 3.14)
	protected double _cRotAngle = 0;
	
	private CCHandInfo theLastHand = new CCHandInfo();
	DatagramSocket ds; 
	byte[] receiveBuffer = new byte[65535];
    boolean running = true;
    int w = 1440;
    int h = 1080;
	JsonParser parser = new JsonParser();
	protected ArrayList<CCHandInfo> _myHands  = new ArrayList<CCHandInfo>();
	String[] fingerNames = new String[] {"THUMB","INDEX_FINGER", "MIDDLE_FINGER", "RING_FINGER", "PINKY"};
    Runnable receiver = new Runnable() {
		DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
 	
		@Override
		public void run() {
			
			while(running) {
			try {
				
				ds.receive(packet);
				String d = new String(packet.getData()).substring(packet.getOffset(), packet.getLength()).trim().strip();
			
				JsonObject handData = parser.parse(d).getAsJsonObject();
				
				System.out.println(handData.toString());
		
				double indX = handData.get("INDEX_FINGER_TIP").getAsJsonArray().get(0).getAsDouble();
				double indY = handData.get("INDEX_FINGER_TIP").getAsJsonArray().get(1).getAsDouble();
				indX = indX * w;
				indY = indY * h;
				
				CCHandInfo hand = new CCHandInfo();
				hand.center.x = handData.get("WRIST").getAsJsonArray().get(0).getAsDouble();;
				hand.center.y = handData.get("WRIST").getAsJsonArray().get(0).getAsDouble();;
				
				hand.tip.x = indX;
				hand.tip.y = indY;
				hand.isHand = true;
				hand.fingerTips = new ArrayList<CCVector3>();
				for (String finger : fingerNames) {
					double x = handData.get(finger+"_TIP").getAsJsonArray().get(0).getAsDouble();
					double y = handData.get(finger+"_TIP").getAsJsonArray().get(1).getAsDouble();
					double z = handData.get(finger+"_TIP").getAsJsonArray().get(2).getAsDouble();
					hand.fingerTips.add(new CCVector3(w*x,h*y,z).rotate(0, 0, 1, _cRotAngle));							
				}
				//hand.validFrames = 10;
				_myHands.clear();
				updateHandInfo(theLastHand, hand);
				_myHands.add(theLastHand);
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}
	};
    		
	Thread receiverThread = new Thread(receiver); 
    
	public CCHandRemoteTracker(CCCVVideoIn theVideoIn, Path theMaskTexture) {
		super(theVideoIn, theMaskTexture);
		try {
			ds = new DatagramSocket(1234);
		
		} catch (SocketException e) {
			e.printStackTrace();
		}

		receiverThread.start();
	}

	@CCProperty(name = "draw finger tips")
	private boolean _cDrawFingerTip = true;
	@CCProperty(name = "tip radius")
	private double _cTipRadius = 10;
	
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
    	
		// update cursor with new position
		theLastHand.handContour = theNewHand.handContour;
		theLastHand.simpleContour = theNewHand.simpleContour;
		theLastHand.fingerTips = theNewHand.fingerTips;
		theLastHand.startAngle = theNewHand.startAngle;
			
		theLastHand.center.x = theNewHand.center.x * (1 - _cTipSmooth) + theLastHand.center.x * _cTipSmooth;
		theLastHand.center.y = theNewHand.center.y * (1 - _cTipSmooth) + theLastHand.center.y * _cTipSmooth;

		double myJitter = theNewHand.tip.distance(theLastHand.tip);
		
		if(theNewHand.tip.isZero()) {
//			CCLog.info(theNewHand.tip.isZero(),theLastHand.tip);
		//	return;
		}else {
			theLastHand.tip.x = theNewHand.tip.x * (1 - _cTipSmooth) + theLastHand.tip.x * _cTipSmooth;
			theLastHand.tip.y = theNewHand.tip.y * (1 - _cTipSmooth) + theLastHand.tip.y * _cTipSmooth;
		}
			
		theLastHand.jitter = myJitter * (1 - _cJitterSmooth) + theLastHand.jitter * _cJitterSmooth;
		theLastHand.validFrames++;
				
		double myLastRestTime = theLastHand.restFrames;
		System.out.println(theLastHand.jitter);
		if(theLastHand.jitter < _cMaxJitter && !theLastHand.tip.isZero()) {
		
			theLastHand.restFrames++;
		}
		else {
			theLastHand.restFrames = 0;
		}
				
		theLastHand.progress = CCMath.saturate(theLastHand.restFrames / _cMinRestFrames);
		System.out.println(theLastHand.progress);
		if(myLastRestTime < _cMinRestFrames && theLastHand.restFrames >= _cMinRestFrames) {
//			if(_myBoundingPolygon== null || _myBoundingPolygon.vertices().size() < 3 || _myBoundingPolygon.isInShape(theLastHand.tip)) {
				fixedTipEvents.proxy().event(theLastHand.tip);
//			}
		}
	}
	
	public void drawDebug(CCGraphics g) {
		g.color(255);
		g.pushMatrix();
		g.applyTransform(transform());
		g.popMatrix();
		for(CCHandInfo myInfo:_myHands) {
			drawFingerTips(myInfo, g);
		}

		//if(_cDrawSelection)drawSelection(g);
	}
	
	public void drawSelection(CCGraphics g) {

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
			System.out.println("P "+myInfo.progress);
			for(double a = 0; a < _cProgressAngle * myInfo.progress;a++) {
				double myAngle = CCMath.radians(a) - startAngle - CCMath.radians(_cProgressAngle / 2);// - CCMath.HALF_PI;
				g.vertex(CCVector2.circlePoint(myAngle, _cSelectionRadius - _cProgressStrength / 2, myInfo.tip.x, myInfo.tip.y));
				g.vertex(CCVector2.circlePoint(myAngle, _cSelectionRadius + _cProgressStrength / 2, myInfo.tip.x, myInfo.tip.y));
			}
			g.endShape();
		}
		g.color(1d);
	}
	
	

}
