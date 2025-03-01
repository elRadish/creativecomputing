package cc.creativecomputing.demo.effectables;

import java.util.ArrayList;
import java.util.List;

import cc.creativecomputing.app.modules.CCAnimator;
import cc.creativecomputing.core.CCProperty;
import cc.creativecomputing.effects.CCEffectManager;
import cc.creativecomputing.effects.CCEffectable;
import cc.creativecomputing.effects.CCOffsetEffect;
import cc.creativecomputing.effects.CCSignalEffect;
import cc.creativecomputing.effects.modulation.CCColumnRowRingSource;
import cc.creativecomputing.effects.modulation.CCColumnRowSpiralSource;
import cc.creativecomputing.effects.modulation.CCPositionSource;
import cc.creativecomputing.effects.modulation.CCXYEuclidianDistanceSource;
import cc.creativecomputing.effects.modulation.CCXYManhattanDistanceSource;
import cc.creativecomputing.effects.modulation.CCXYRadialSource;
import cc.creativecomputing.graphics.CCDrawAttributes;
import cc.creativecomputing.graphics.CCDrawMode;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.CCShapeMode;
import cc.creativecomputing.graphics.app.CCGL2Adapter;
import cc.creativecomputing.graphics.app.CCGL2Application;
import cc.creativecomputing.graphics.camera.CCCameraController;
import cc.creativecomputing.graphics.export.CCScreenCaptureController;
import cc.creativecomputing.graphics.primitives.CCSphereMesh;
import cc.creativecomputing.graphics.texture.CCTexture2D;
import cc.creativecomputing.image.CCImageIO;
import cc.creativecomputing.io.CCNIOUtil;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCVector2;

public class CCEffectableQuads extends CCGL2Adapter {
	
	private class CCQuadEffectable extends CCEffectable{
		
		private double _myAlpha = 1;

		public CCQuadEffectable(int theId, double theX, double theY) {
			super(theId);
			position().x = theX;
			position().z = theY;
		}
		
		@Override
		public void apply(double...theValues) {
			position().y = theValues[0] * _cHeight;
		}
		
	}

	@CCProperty(name = "effects")
	private CCEffectManager<CCQuadEffectable> _myEffectManager;
	private List<CCQuadEffectable> _myCubes = new ArrayList<>();

	@CCProperty(name = "height", min = 0, max = 300)
	private double _cHeight = 100;
	@CCProperty(name = "camera")
	private CCCameraController _cCameraController;
	
	@CCProperty(name = "attributes")
	private CCDrawAttributes _cAttributes = new CCDrawAttributes();
	
	@CCProperty(name = "screen capture")
	private CCScreenCaptureController _cScreenCapture;
	
	private CCTexture2D _myPointTexture;
	
	private CCSphereMesh _mySphere;

	@Override
	public void init(CCGraphics g, CCAnimator theAnimator) {
		
		_cCameraController = new CCCameraController(this, g, 100);
		
		_mySphere = new CCSphereMesh(100, 100);
		
		_myPointTexture = new CCTexture2D(CCImageIO.newImage(CCNIOUtil.dataPath("textures/Visualisierung_Perspektive von oben_ohne Kronleuchter.jpg")));
		
		double a = 30;
		double height = a / 2 * CCMath.SQRT3;
		
		
		int i = 0;
		for(int r = 0; r < 13; r++){
			for(int c = 0; c < r; c++){
				CCQuadEffectable myCube = new CCQuadEffectable(i, c * a - r * a / 2, r * height);
				myCube.column(c);
				myCube.row(r);
				_myCubes.add(myCube);
				i++;
			}
		}
		
		g.rectMode(CCShapeMode.CENTER);
		
		_myEffectManager = new CCEffectManager<CCQuadEffectable>(_myCubes, "a");
		_myEffectManager.addIdSources(CCEffectable.COLUMN_SOURCE, CCEffectable.ROW_SOURCE);
		_myEffectManager.addRelativeSources(
			new CCColumnRowRingSource(),
			new CCColumnRowSpiralSource(),
			new CCPositionSource("position"),
			new CCXYEuclidianDistanceSource("euclidian", 200, new CCVector2()),
			new CCXYManhattanDistanceSource("manhattan", 200, 200, new CCVector2()),
			new CCXYRadialSource("radial", new CCVector2())
		);
		_myEffectManager.put("offset", new CCOffsetEffect());
		_myEffectManager.put("signal", new CCSignalEffect());
		
		_cScreenCapture = new CCScreenCaptureController(this);
		
	}

	@Override
	public void update(CCAnimator theAnimator) {
		_myEffectManager.update(theAnimator);
	}

	@Override
	public void display(CCGraphics g) {
//		g.clearColor(255);
		g.clear();
		
		g.color(255);
		g.image(_myPointTexture, -g.width()/2, -g.height() /2, g.width(), g.height());
		g.clearDepthBuffer();
		_cCameraController.camera().draw(g);
		
		_cAttributes.start(g);
//		g.pointSprite(_myPointTexture);
		g.beginShape(CCDrawMode.LINES);
		for(CCQuadEffectable myCube:_myCubes){
			g.vertex(myCube.position());
			g.vertex(myCube.position().x, -100, myCube.position().z);
//			g.pushMatrix();
//			g.translate(myCube.position());
//			_mySphere.draw(g);
//			g.popMatrix();
		}
		g.endShape();
		g.color(255);
		for(CCQuadEffectable myCube:_myCubes){
			g.pushMatrix();
			g.translate(myCube.position());
			g.box(10);
			g.popMatrix();
		}
//		g.noPointSprite();
		_cAttributes.end(g);
	}

	public static void main(String[] args) {

		CCEffectableQuads demo = new CCEffectableQuads();

		CCGL2Application myAppManager = new CCGL2Application(demo);
		myAppManager.glcontext().size(1200, 600);
		myAppManager.animator().framerate = 30;
		myAppManager.animator().animationMode = CCAnimator.CCAnimationMode.FRAMERATE_PRECISE;
		myAppManager.start();
	}
}
