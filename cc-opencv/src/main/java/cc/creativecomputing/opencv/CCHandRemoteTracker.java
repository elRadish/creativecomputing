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


public class CCHandRemoteTracker implements CCIHandTracker  {

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

	@CCProperty(name = "calibrate")
	private boolean _cCalibrate = false;

	@CCProperty(name = "hscale", min = 1, max = 3000, readBack = true)
	protected double _cHScale = 2000;//1.42;// 1.9;
	@CCProperty(name = "vscale", min = 1, max = 3000, readBack = true)
	protected double _cVScale = 1000;//1.75;//1.72;

	@CCProperty(name = "xoff", min = -1000, max = 1000, readBack = true)
	protected int xOffset = -347;
	@CCProperty(name = "yoff", min = -1000, max = 1000, readBack = true)
	protected int yOffset = -14;

	private boolean _myIsInDebug = false;
	private boolean _myIsInConfig = false;
	public CCListenerManager<CCFixedTipEvent> fixedTipEvents = CCListenerManager.create(CCFixedTipEvent.class);

	double rawTipX;
	double rawTipY;

	private int currentCalibrationPoint = 0;
	private CCVector2 calibrationPoints[] = new CCVector2[4];

	private double markerScale = 0.7;

	private int tableAppX = 0;
	private int tableAppY = 0;
	private int tableAppW = 1920;
	private int tableAppH = 1200;

	private int tableAppX1Scaled = (int) (tableAppX + tableAppW * (1-markerScale));
	private int tableAppY1Scaled = (int) (tableAppY + tableAppH * (1-markerScale));
	private int tableAppX2Scaled = (int) (tableAppW - tableAppX1Scaled);
	private int tableAppY2Scaled = (int) (tableAppH - tableAppY1Scaled);

	private CCTransform _myTransform = new CCTransform();

	private CCHandInfo theLastHand = new CCHandInfo();

	protected ArrayList<CCHandInfo> _myHands  = new ArrayList<CCHandInfo>();
	private String[] fingerNames = new String[] {"THUMB","INDEX_FINGER", "MIDDLE_FINGER", "RING_FINGER", "PINKY"};

	public CCHandRemoteTracker(int uDPPort) {
		Runnable receiver = new Runnable() {
			boolean running = true;
			JsonParser parser = new JsonParser();
			byte[] receiveBuffer = new byte[65535];

			DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);

			@Override
			public void run() {
				DatagramSocket ds;
				try {
					ds = new DatagramSocket(uDPPort);

					while(running) {
						try {
							ds.receive(packet);
							JsonObject handData = parser.parse(new String(packet.getData()).substring(packet.getOffset(), packet.getLength()).trim().strip()).getAsJsonObject();

							CCHandInfo hand = new CCHandInfo();
							hand.isHand = true;
							rawTipX = handData.get("INDEX_FINGER_TIP").getAsJsonArray().get(0).getAsDouble();
							rawTipY = handData.get("INDEX_FINGER_TIP").getAsJsonArray().get(1).getAsDouble();

							hand.tip.x = mapX(handData.get("INDEX_FINGER_TIP").getAsJsonArray().get(0).getAsDouble());
							hand.tip.y = mapY(handData.get("INDEX_FINGER_TIP").getAsJsonArray().get(1).getAsDouble());

							hand.center.x =  mapX(handData.get("WRIST").getAsJsonArray().get(0).getAsDouble());
							hand.center.y =  mapY(handData.get("WRIST").getAsJsonArray().get(0).getAsDouble());

							hand.fingerTips = new ArrayList<CCVector3>();
							for (String finger : fingerNames) {
								double x = handData.get(finger+"_TIP").getAsJsonArray().get(0).getAsDouble();
								double y = handData.get(finger+"_TIP").getAsJsonArray().get(1).getAsDouble();
								double z = handData.get(finger+"_TIP").getAsJsonArray().get(2).getAsDouble();
								hand.fingerTips.add(new CCVector3(mapX(x),mapY(y),z).rotate(0, 0, 1, _cRotAngle));							
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

		Thread receiverThread = new Thread(receiver); 
		receiverThread.start();
	}

	private double mapX(double x) {
		return _cHFlip ? tableAppW-xOffset - (x * _cHScale) : x * _cHScale + xOffset ;
	}

	private double mapY(double y) {
		return _cVFlip ? tableAppH-yOffset - (y * _cVScale) : y * _cVScale + yOffset;
	}


	private void drawFingerTips(CCHandInfo myInfo, CCGraphics g) {
		//if(!_cDrawFingerTip)return;
		g.color(CCColor.MAGENTA, 0.5);
		myInfo.fingerTips.forEach(myTip -> g.ellipse(new CCVector2(myTip.x, myTip.y),_cTipRadius,_cTipRadius, false));
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

			if (_cCalibrate) {
				if (currentCalibrationPoint < 4) {				
					calibrationPoints[currentCalibrationPoint++] = new CCVector2(rawTipX, rawTipY);
					if (currentCalibrationPoint == 4) {
						calculateCalibration();
					}
				} 
			} 
		}
	}

	private void calculateCalibration() {
		_cVScale =  (tableAppH*0.4 / (calibrationPoints[0].y - calibrationPoints[1].y));
		_cHScale =  (tableAppW*0.4 / (calibrationPoints[2].x - calibrationPoints[1].x));
		double offX = calibrationPoints[0].x * _cHScale - (0.3 * tableAppW);
		double offY = calibrationPoints[1].y * _cVScale - (0.3 * tableAppH);
		xOffset = (int) -offX;
		yOffset = (int) -offY;
	}


	@Override
	public void drawDebug(CCGraphics g) {
		g.applyTransform(transform());
		g.color(255);
		for(CCHandInfo myInfo:_myHands) {
			drawFingerTips(myInfo, g);
		}

		if(_cDrawSelection)drawSelection(g);
	}

	@Override
	public List<CCHandInfo> hands(){
		return _myHands;
	}


	@Override
	public void drawSelection(CCGraphics g) {
		g.applyTransform(transform());

		g.popMatrix();
		if (_cCalibrate) {
			g.text(currentCalibrationPoint < 4 ? "Calibrate":"Done", new CCVector2(tableAppW/2,tableAppH/2));

			g.color(new CCColor(0, 240, 0), 1.0);
			g.ellipse(new CCVector2(tableAppX1Scaled,tableAppY1Scaled),  10,  10, false);
			if (currentCalibrationPoint == 0) {
				g.ellipse(new CCVector2(tableAppX1Scaled,tableAppY1Scaled), 20 , 20, true);
			}
			g.ellipse(new CCVector2(tableAppX1Scaled,tableAppY2Scaled), 10,10, false);
			if (currentCalibrationPoint == 1) {
				g.ellipse(new CCVector2(tableAppX1Scaled,tableAppY2Scaled), 20 , 20, true);
			}
			g.ellipse(new CCVector2(tableAppX2Scaled,tableAppY2Scaled), 10,10 , false);
			if (currentCalibrationPoint == 2) {
				g.ellipse(new CCVector2(tableAppX2Scaled,tableAppY2Scaled), 20 , 20, true);
			}
			g.ellipse(new CCVector2(tableAppX2Scaled,tableAppY1Scaled), 10,10 , false);
			if (currentCalibrationPoint == 3) {
				g.ellipse(new CCVector2(tableAppX2Scaled,tableAppY1Scaled), 20 , 20, true);
			}
			if (_cDrawFingerTip) {
				for(CCHandInfo myInfo:_myHands) {
					drawFingerTips(myInfo, g);
				}
			}
		} else {
			currentCalibrationPoint = 0;
		}
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

	@Override
	public void isInDebug(boolean isInDebug) {
		_myIsInDebug = isInDebug;
	}

	@Override
	public void isInConfig(boolean isInConfig) {
		_myIsInConfig = isInConfig;
	}

	@Override
	public void active(boolean _myIsTrackingActive) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateDebugTexture(boolean b) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(CCAnimator theAnimator) {
		// TODO Auto-generated method stub

	}

	@Override
	public CCTexture debugTexture() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void preDisplay(CCGraphics g) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addFixedTipevent(CCFixedTipEvent event) {
		fixedTipEvents.add(event);
	}
}
