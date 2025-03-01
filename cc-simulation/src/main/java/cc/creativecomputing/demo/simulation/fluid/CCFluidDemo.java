/*******************************************************************************
 * Copyright (C) 2018 christianr
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package cc.creativecomputing.demo.simulation.fluid;

import cc.creativecomputing.app.modules.CCAnimator;
import cc.creativecomputing.core.CCProperty;
import cc.creativecomputing.gl.app.events.CCMouseSimpleInfo;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.app.CCGL2Adapter;
import cc.creativecomputing.graphics.app.CCGL2Application;
import cc.creativecomputing.math.CCColor;
import cc.creativecomputing.math.CCVector2;
import cc.creativecomputing.simulation.fluid.CCFluidGrid;
import cc.creativecomputing.simulation.fluid.CCFluidSolver;

public class CCFluidDemo extends CCGL2Adapter {
	
	private CCFluidGrid _myGrid = new CCFluidGrid();
	
	@CCProperty(name = "solver")
	private CCFluidSolver _mySolver;
	
	@CCProperty(name = "noise speed x", min = -1, max = 1)
	private double _cSpeedX = 0;
	@CCProperty(name = "noise speed y", min = -1, max = 1)
	private double _cSpeedY = 0;
	@CCProperty(name = "noise speed z", min = -1, max = 1)
	private double _cSpeedZ = 0;
	

	@CCProperty(name = "mouse impulse", min = 0, max = 1)
	private double _cMouseImpulse = 0;
	@CCProperty(name = "mouse temperature", min = 0, max = 1)
	private double _cMouseTemperature = 0;
	
	private CCMouseSimpleInfo _myMouse = new CCMouseSimpleInfo();
	
	@CCProperty(name = "color speed", min = 0, max = 1)
	private double _cColorSpeed = 0.1;

	@Override
	public void init(CCGraphics g, CCAnimator theAnimator) {
		_myGrid.size.set(g.width() / 2, g.height() / 2);
		_mySolver = new CCFluidSolver(g, _myGrid, new CCVector2(g.width(), g.height()));
		
		
		mouseListener().add(_myMouse);
		mouseMotionListener().add(_myMouse);
	}

	@Override
	public void update(CCAnimator theAnimator) {
		_mySolver.noiseOffset().x = theAnimator.time() * _cSpeedX;
		_mySolver.noiseOffset().y = theAnimator.time() * _cSpeedY;
		_mySolver.noiseOffset().z = theAnimator.time() * _cSpeedZ;
	}
	
	
	
	public void addForces(CCGraphics g) {
		_mySolver.addColor(g, _myMouse.position, CCColor.createFromHSB((animator().time() * _cColorSpeed) % 1,1d,1d));
		_mySolver.addForce(g, _myMouse.position, new CCVector2( _myMouse.motion.x, - _myMouse.motion.y).multiplyLocal(_cMouseImpulse));
		_mySolver.addTemperature(g, _myMouse.position, new CCVector2( _myMouse.motion.x, - _myMouse.motion.y).length() * (_cMouseTemperature));
	}
	
	@CCProperty(name = "speed impulse scale")
	private double _cSpeedImpulseScale = 1;

	@Override
	public void display(CCGraphics g) {
		
		addForces(g);
//			_mySolver.density.beginDraw();
//			g.color(CCColor.createFromHSB(animator().time() * 0.1, 1d, 1d));
//			g.rect(animator().time() * 100 % _myGrid.size.x,100, 10, 10);
//			_mySolver.density.endDraw();
//			_mySolver.velocity.beginDraw();
//			g.color(255,0,0);
//			g.rect(animator().time() * 100 % _myGrid.size.x,100, 10, 10);
//			_mySolver.velocity.endDraw();
//			_mySolver.clearBounds(g);
//			_mySolver.bounds().beginDraw(g);

//			_myWriteDataShader.start();
//			double _myY = (CCMath.sin(animator().time() * _cLineSpeed) + 1) / 2 * _myGrid.size.y;
//			double _myY2 = (CCMath.sin((animator().time() + animator().deltaTime()) * _cLineSpeed ) + 1) / 2 * _myGrid.size.y;
//			
//			double _myDir = (_myY2 - _myY) * _cSpeedImpulseScale;
//			g.textureCoords3D(0, 1, 0, _myDir);
//			g.rect(0.5, _myY, _myGrid.size.x, 20);
//			
//			g.ellipse(_myGrid.size.x / 2,_myY, 200);
//			_mySolver.bounds().endDraw(g);
//			_myWriteDataShader.end();
			
		_mySolver.step(g);


		 g.clear();

		 _mySolver.display(g);
	}

	public static void main(String[] args) {

		CCFluidDemo demo = new CCFluidDemo();

		CCGL2Application myAppManager = new CCGL2Application(demo);
		myAppManager.glcontext().size(1280, 720);
		myAppManager.animator().framerate = 30;
		myAppManager.animator().animationMode = CCAnimator.CCAnimationMode.FRAMERATE_PRECISE;
		myAppManager.start();
	}
}
