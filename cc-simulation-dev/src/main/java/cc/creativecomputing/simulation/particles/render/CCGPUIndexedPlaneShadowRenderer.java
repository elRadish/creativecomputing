/*
 * Copyright (c) 2013 christianr.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * Contributors:
 *     christianr - initial API and implementation
 */
package cc.creativecomputing.simulation.particles.render;

import javax.media.opengl.GL2;

import cc.creativecomputing.graphics.CCDrawMode;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.CCVBOMesh;
import cc.creativecomputing.graphics.shader.CCGLSLShader;
import cc.creativecomputing.graphics.shader.CCShaderBuffer;
import cc.creativecomputing.io.CCIOUtil;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.simulation.particles.CCParticles;

/**
 * @author christianriekoff
 *
 */
public class CCGPUIndexedPlaneShadowRenderer extends CCGPUParticleRenderer{

	protected CCVBOMesh _myMesh;
	
	protected CCParticles _myParticles;
	
	protected CCShaderBuffer _myIndexTexture;
	
	protected CCGLSLShader _myShader;
	
	protected CCGraphics g;
	
	protected float _myPointsize = 5;
	
	public CCGPUIndexedPlaneShadowRenderer(CCGraphics theGraphics, String theVertexShader, String theFragmentShader) {
		_myShader = new CCGLSLShader(theVertexShader, theFragmentShader);
		_myShader.load();
		g = theGraphics;
	}
	
	public CCGPUIndexedPlaneShadowRenderer(CCGraphics theGraphics) {
		this(
			theGraphics,
			CCIOUtil.classPath(CCGPUDisplayShader.class, "shader/indexed/indexed_display_vertex.glsl"),
			CCIOUtil.classPath(CCGPUDisplayShader.class, "shader/indexed/indexed_display_fragment.glsl")
		);
	}
	
	public void setup(CCParticles theParticles) {
		_myParticles = theParticles;
		_myMesh = new CCVBOMesh(CCDrawMode.POINTS, _myParticles.size());
		_myMesh.prepareVertexData(3);
		_myIndexTexture = new CCShaderBuffer(_myParticles.width(), _myParticles.height());
		
		_myIndexTexture.beginDraw();
		g.clear();
		_myParticles.initValueShader().start();
		g.beginShape(CCDrawMode.POINTS);
		for(int y = 0; y < _myParticles.height();y++) {
			for(int x = 0; x < _myParticles.width();x++) {
				g.textureCoords(0, (float)x, (float)y);
				g.vertex(x,y);
				_myMesh.addVertex(x,y,0);
			}
		}
		g.endShape();
		
		_myParticles.initValueShader().end();
		_myIndexTexture.endDraw();
		
		
		
	}
	
	public void pointSize(float thePointSize){
		_myPointsize = thePointSize;
	}
	
	public void update(final float theDeltaTime) {
//		_myMesh.vertices(_myParticles.positions());
	}

	public void draw(CCGraphics g){
		g.gl.glEnable(GL2.GL_VERTEX_PROGRAM_POINT_SIZE);
		_myShader.start();
		g.texture(0, _myParticles.dataBuffer().attachment(0));
		g.texture(1, _myParticles.dataBuffer().attachment(1));
		_myShader.uniform1i("positions", 0);
		_myShader.uniform1i("infos", 1);
		_myShader.uniform1f("tanHalfFOV", CCMath.tan(g.camera().fov()) * g.height);
		_myMesh.draw(g);
		g.noTexture();
		_myShader.end();
		g.gl.glDisable(GL2.GL_VERTEX_PROGRAM_POINT_SIZE) ;
	}
	
	public CCVBOMesh mesh(){
		return _myMesh;
	}
	
	public void pointSizeClamp(final float theMinPointSize, final float theMaxPointSize) {
//		if(_myDisplayShader == null)_myDisplayShader = new CCGPUDisplayShader();
//		_myDisplayShader.minPointSize(theMinPointSize);
//		_myDisplayShader.maxPointSize(theMinPointSize);
	}
}
