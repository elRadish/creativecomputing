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
package cc.creativecomputing.demo.simulation.particles.contraints;

import java.util.ArrayList;
import java.util.List;

import cc.creativecomputing.app.modules.CCAnimator;
import cc.creativecomputing.core.CCProperty;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.CCGraphics.CCBlendMode;
import cc.creativecomputing.graphics.app.CCGL2Adapter;
import cc.creativecomputing.graphics.app.CCGL2Application;
import cc.creativecomputing.graphics.camera.CCCameraController;
import cc.creativecomputing.graphics.texture.CCTexture.CCTextureTarget;
import cc.creativecomputing.graphics.texture.CCTexture2D;
import cc.creativecomputing.image.CCImageIO;
import cc.creativecomputing.io.CCNIOUtil;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCVector3;
import cc.creativecomputing.simulation.particles.CCParticles;
import cc.creativecomputing.simulation.particles.constraints.CCConstraint;
import cc.creativecomputing.simulation.particles.constraints.CCTerrainConstraint;
import cc.creativecomputing.simulation.particles.emit.CCParticlesIndexParticleEmitter;
import cc.creativecomputing.simulation.particles.forces.CCForce;
import cc.creativecomputing.simulation.particles.forces.CCGravity;
import cc.creativecomputing.simulation.particles.forces.CCViscousDrag;

public class CCTerrainConstraintDemo extends CCGL2Adapter {
	
	@CCProperty(name = "particles")
	private CCParticles _myParticles;
	private CCParticlesIndexParticleEmitter _myEmitter;
	
	@CCProperty(name = "camera")
	private CCCameraController _cCameraController;
	

	@Override
	public void init(CCGraphics g, CCAnimator theAnimator) {
		final List<CCForce> myForces = new ArrayList<>();
		myForces.add(new CCGravity(new CCVector3(0.1,0,0)));
		myForces.add(new CCViscousDrag(0.3f));
		
		final List<CCConstraint> myConstraints = new ArrayList<>();
		myConstraints.add(new CCTerrainConstraint(
			new CCTexture2D(CCImageIO.newImage(CCNIOUtil.dataPath("heightmap.png")), CCTextureTarget.TEXTURE_RECT),
			new CCVector3(4f,300,4f), 
			new CCVector3(200, 00, 100)
		));
		
		_myParticles = new CCParticles(g, myForces, new ArrayList<>(), myConstraints,700, 700);
		_myParticles.addEmitter(_myEmitter = new CCParticlesIndexParticleEmitter(_myParticles));
		
		_cCameraController = new CCCameraController(this, g, 100);
	}
	
	float angle = 0;
	
	double height = 100;
	
	@Override
	public void update(final CCAnimator theAnimator){
	
		angle += theAnimator.deltaTime() * 30;
		for(int i = 0; i < 100; i++){
			_myEmitter.emit(
				new CCVector3(CCMath.random(-400,400), height/2,CCMath.random(-400,400)),
				new CCVector3().randomize(20),
				10
			);
		}
		_myParticles.update(theAnimator);
	}

	public void display(CCGraphics g) {
		_myParticles.preDisplay(g);
		
		height = g.height();
		g.noDepthTest();
		g.clear();
		g.color(255,50);
		
		g.pushMatrix();
		_cCameraController.camera().draw(g);
		g.blend(CCBlendMode.ADD);
		g.color(255,25);
		_myParticles.display(g);
		

		g.popMatrix();
		
		g.blend();
	}
	
	public static void main(String[] args) {

		CCTerrainConstraintDemo demo = new CCTerrainConstraintDemo();

		CCGL2Application myAppManager = new CCGL2Application(demo);
		myAppManager.glcontext().size(1200, 600);
		myAppManager.animator().framerate = 30;
		myAppManager.animator().animationMode = CCAnimator.CCAnimationMode.FRAMERATE_PRECISE;
		myAppManager.start();
	}

}
