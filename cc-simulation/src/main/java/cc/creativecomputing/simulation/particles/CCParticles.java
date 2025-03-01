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
package cc.creativecomputing.simulation.particles;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cc.creativecomputing.app.modules.CCAnimator;
import cc.creativecomputing.core.CCProperty;
import cc.creativecomputing.core.logging.CCLog;
import cc.creativecomputing.core.util.CCCollectionUtil;
import cc.creativecomputing.graphics.CCDrawMode;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.CCVBOMesh;
import cc.creativecomputing.graphics.shader.CCGLProgram;
import cc.creativecomputing.graphics.shader.CCGLWriteDataShader;
import cc.creativecomputing.graphics.shader.CCGLProgram.CCGLTextureUniform;
import cc.creativecomputing.graphics.shader.CCShaderBuffer;
import cc.creativecomputing.graphics.texture.CCGLSwapBuffer;
import cc.creativecomputing.graphics.texture.CCTexture2D;
import cc.creativecomputing.graphics.util.CCStopWatchGraph;
import cc.creativecomputing.io.CCNIOUtil;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCVector3;
import cc.creativecomputing.math.CCVector4;
import cc.creativecomputing.simulation.particles.blends.CCBlend;
import cc.creativecomputing.simulation.particles.constraints.CCConstraint;
import cc.creativecomputing.simulation.particles.emit.CCIParticleEmitter;
import cc.creativecomputing.simulation.particles.emit.CCParticleCPUGroupEmitter;
import cc.creativecomputing.simulation.particles.forces.CCForce;
import cc.creativecomputing.simulation.particles.impulses.CCImpulse;
import cc.creativecomputing.simulation.particles.render.CCIndexedParticleRenderer;
import cc.creativecomputing.simulation.particles.render.CCParticlePointRenderer;
import cc.creativecomputing.simulation.particles.render.CCParticleRenderer;

/**
 * This particle system renders particles as points. You can add different forces
 * and constraints to change the behavior of the particles.
 * 
 * The data of the particles is stored in textures. Implementation wise the data is written into a framebuffer
 * with 4 attachments. Holding the information in the following layout:
 * <ul>
 * <li>attachment0: positions as xyz</li>
 * <li>attachment1: infos as age / lifetime / state</li>
 * <li>attachment2: velocities as xyz</li>
 * <li>attachment3: colors as rgba</li>
 * </ul>
 * You can use this data and overwrite them.
 * @author info
 * @demo cc.creativecomputing.gpu.particles.demo.CCParticlesNoiseFlowFieldTest
 * @see CCQuadParticles
 */
@SuppressWarnings("unused")
public class CCParticles{
	
	protected Map<Integer, CCVector3> _myPositionUpdates = new HashMap<Integer, CCVector3>();
	protected List<CCParticle> _myLifetimeUpdates = new ArrayList<CCParticle>();
	
	@CCProperty(name = "emitter")
	private List<CCIParticleEmitter> _myEmitter = new ArrayList<>();

	protected List<CCParticleRenderer> _myRenderer = new ArrayList<>();
	protected List<CCForce> _myForces = new ArrayList<>();
	protected List<CCBlend> _myBlends = new ArrayList<>();
	protected List<CCConstraint> _myConstraints = new ArrayList<>();
	protected List<CCImpulse> _myImpulses = new ArrayList<>();
	
	protected final int _myWidth;
	protected final int _myHeight;
	
	@CCProperty(name = "update shader")
	protected CCParticlesUpdateShader _myUpdateShader;
	
	protected CCGLWriteDataShader _mySetDataShader;
	
	protected CCVBOMesh _myResetMesh;
	
	protected CCGLSwapBuffer _mySwapTexture;
	
	protected CCParticleEnvelopeData _myEnvelopeData;
	protected CCShaderBuffer _myGroupData;
	
	protected double _myCurrentTime = 0;
	
	protected FloatBuffer _myPositionBuffer;
	protected FloatBuffer _myVelocityBuffer;

	@CCProperty(name = "renderer")
	private Map<String,CCParticleRenderer> _myRendererMap = new LinkedHashMap<>();
	@CCProperty(name = "forces")
	private Map<String, CCForce> _myForceMap = new LinkedHashMap<>();
	@CCProperty(name = "blends")
	private Map<String, CCBlend> _myBlendMap = new LinkedHashMap<>();
	@CCProperty(name = "contraints")
	private Map<String, CCConstraint> _myConstraintMap = new LinkedHashMap<>();
	
	/**
	 * <p>
	 * Creates a new particle system. To create a new particle system you have to
	 * pass the CCGraphics instance and a list with forces. You can also pass
	 * a list of constraints that act as boundary so that the particles bounce at
	 * collision.
	 * </p>
	 * <p>
	 * The number of particles you can create depends on the size of the texture
	 * that holds the particle data on the gpu. You can define this size by passing
	 * a width and height value. The number of particles you can allocate is 
	 * width * height.
	 * </p>
	 * <p>
	 * How the particles are drawn is defined by a shader. You can pass a custom
	 * shader to the particle system to define how the particles are drawn. To
	 * create your own shader you need to extend the CCDisplayShader and write your
	 * own cg shader.
	 * </p>
	 * 
	 * @param g graphics object used to initialize shaders and meshes for drawing
	 * @param theForces list with the forces applied to the particles
	 * @param theBlends TODO
	 * @param theConstraints list with constraints applied to the particles
	 * @param theWidth width of particle system texture
	 * @param theHeight height of the particle system texture
	 * @param theDisplayShader custom shader for displaying the particles
	 */
	public CCParticles(
		final CCGraphics g,
		final List<CCParticleRenderer> theRenderer,
		final List<CCForce> theForces, 
		final List<CCBlend> theBlends, 
		final List<CCConstraint> theConstraints, 
		final List<CCImpulse> theImpulse, 
		final int theWidth, 
		final int theHeight
	){
		_myWidth = theWidth;
		_myHeight = theHeight;

		_mySetDataShader = new CCGLWriteDataShader();
		_myEnvelopeData = new CCParticleEnvelopeData(100, 100);
		_mySwapTexture = new CCGLSwapBuffer(g, 32, 4, 4,_myWidth,_myHeight);
		_myGroupData = new CCShaderBuffer(32, 4, 1, CCParticleCPUGroupEmitter.GROUP_WIDH,CCParticleCPUGroupEmitter.GROUP_WIDH);
		
		_myResetMesh = new CCVBOMesh(CCDrawMode.POINTS, _myWidth * _myHeight);
	
		for (int i = 0; i < _myWidth * _myHeight; i++){
			_myResetMesh.addVertex(i % _myWidth,i / _myWidth);
			_myResetMesh.addTextureCoords(0,Float.MAX_VALUE,Float.MAX_VALUE,Float.MIN_VALUE,0);
			_myResetMesh.addTextureCoords(1, 1, 1, 1);
			_myResetMesh.addTextureCoords(2, 0, 0, 0);
			_myResetMesh.addTextureCoords(3, 1, 1, 1);
		}
		_myRenderer = theRenderer;
		_myForces = theForces;
		_myBlends = theBlends;
		_myConstraints = theConstraints;
		_myImpulses = theImpulse;
		
		
		init(g);
	}
	
	public interface CCParticleSetup{
		public void setup(CCParticles theParticles);
	}
	
	public CCParticles(final CCGraphics g, int theWidth, int theHeight, CCParticleSetup theSetup) {
		
		this(g, theWidth, theHeight);
		setup(g,theSetup);
	}
	
	protected CCParticles(final CCGraphics g, int theWidth, int theHeight) {
		_myWidth = theWidth;
		_myHeight = theHeight;

		_mySetDataShader = new CCGLWriteDataShader();
		_myEnvelopeData = new CCParticleEnvelopeData(100, 100);
		
		_mySwapTexture = new CCGLSwapBuffer(g, 32, 4, 4,_myWidth,_myHeight);
		_myGroupData = new CCShaderBuffer(32, 4, 1, CCParticleCPUGroupEmitter.GROUP_WIDH,CCParticleCPUGroupEmitter.GROUP_WIDH);
		
		_myResetMesh = new CCVBOMesh(CCDrawMode.POINTS, _myWidth * _myHeight);
		
		for (int i = 0; i < _myWidth * _myHeight; i++){
			_myResetMesh.addVertex(i % _myWidth,i / _myWidth);
			_myResetMesh.addTextureCoords(0,Float.MAX_VALUE,Float.MAX_VALUE,Float.MIN_VALUE,0);
			_myResetMesh.addTextureCoords(1, 1, 1, 1);
			_myResetMesh.addTextureCoords(2, 0, 0, 0);
			_myResetMesh.addTextureCoords(3, 1, 1, 1);
		}
	}
	
	protected void setup(final CCGraphics g, CCParticleSetup theSetup) {
		theSetup.setup(this);
		init(g);
	}
	
	protected void init(CCGraphics g) {
		for(CCParticleRenderer myRenderer:_myRenderer) {
			myRenderer.setup(this);
			_myRendererMap.put(myRenderer.name(), myRenderer);
		}
		
		for(CCForce myForce:_myForces) {
			myForce.index(_myEnvelopeData.add(myForce.lifetimeBlend()));
			myForce.setSize(g, _myWidth, _myHeight);
			_myForceMap.put(myForce.append(), myForce);
		}
		
		for(CCBlend myBlend:_myBlends) {
			myBlend.setSize(g, _myWidth, _myHeight);
			_myBlendMap.put(myBlend.append(), myBlend);
		}
		
		for(CCConstraint myConstraint:_myConstraints) {
			myConstraint.setSize(g, _myWidth, _myHeight);
			_myConstraintMap.put(myConstraint.append(), myConstraint);
		}
		
		_myUpdateShader = new CCParticlesUpdateShader(this, g, _myForces, _myBlends, _myConstraints, _myImpulses,_myWidth,_myHeight);
		
		reset(g);
		
		_myUpdateShader.setTextureUniform("positionTexture", positionData());
		_myUpdateShader.setTextureUniform("infoTexture", infoData());
		_myUpdateShader.setTextureUniform("velocityTexture", velocityData());
		_myUpdateShader.setTextureUniform("colorTexture", colorData());
		_myUpdateShader.setTextureUniform("staticPositions", null);
		_myUpdateShader.setTextureUniform("lifeTimeBlends", _myEnvelopeData.texture());
		_myUpdateShader.setTextureUniform("groupInfoTexture", _myGroupData.attachment(0));
	}
	
	public CCParticles(
		final CCGraphics g,
		List<CCParticleRenderer> theRender, 
		List<CCForce> theForces, 
		List<CCBlend> theBlends, 
		List<CCConstraint> theConstraints, 
		int theWidth, 
		int theHeight
	) {
		this(g, theRender, theForces, theBlends, theConstraints, new ArrayList<CCImpulse>(), theWidth, theHeight);
	}
	
	public CCParticles(
		final CCGraphics g,
		CCParticleRenderer theRender, 
		List<CCForce> theForces, 
		List<CCBlend> theBlends, 
		List<CCConstraint> theConstraints, 
		int theWidth, 
		int theHeight
	) {
		this(g, CCCollectionUtil.createList(theRender), theForces, theBlends, theConstraints, new ArrayList<CCImpulse>(), theWidth, theHeight);
	}

	public CCParticles(
		final CCGraphics g, 
		List<CCForce> theForces, 
		List<CCBlend> theBlends, 
		List<CCConstraint> theConstraints, 
		int theWidth, int theHeight
	) {
		this(g, CCCollectionUtil.createList(new CCParticlePointRenderer()), theForces, theBlends, theConstraints, theWidth, theHeight);
	}

	public CCParticles(final CCGraphics g, List<CCForce> theForces, List<CCConstraint> theConstraints) {
		this(g, theForces, new ArrayList<>(),theConstraints,200, 200);
	}

	public CCParticles(final CCGraphics g,List<CCForce> theForces) {
		this(g, theForces, new ArrayList<CCConstraint>());
	}
	
	public void addEmitter(CCIParticleEmitter theEmitter) {
		_myEmitter.add(theEmitter);
	}
	
	public CCGLProgram initValueShader() {
		return _mySetDataShader;
	}
	
	public CCTexture2D groupTexture() {
		return _myGroupData.attachment(0);
	}
	
	public CCShaderBuffer groupData() {
		return _myGroupData;
	}
	
	public CCTexture2D envelopeTexture() {
		return _myEnvelopeData.texture();
	}
	
	public CCParticleEnvelopeData envelopeData() {
		return _myEnvelopeData;
	}
	
	public double currentTime() {
		return _myCurrentTime;
	}
	
	public CCParticlesUpdateShader updateShader() {
		return _myUpdateShader;
	}
	
	public void reset(CCGraphics g){

		for(CCIParticleEmitter myEmitter:_myEmitter) {
			myEmitter.reset();
		}
		
		_mySwapTexture.clear(g);
		
		_mySwapTexture.beginDrawCurrent(g);
		_mySetDataShader.start();
		_myResetMesh.draw(g);
		_mySetDataShader.end();
		_mySwapTexture.endDrawCurrent(g);
		
		for(CCForce myForce:_myForces) {
			myForce.reset(g);
		}
	}
	
	/**
	 * Returns the width of the texture containing the particle data
	 * @return width of the particle texture
	 */
	public int width() {
		return _myWidth;
	}
	
	/**
	 * Returns the height of the texture containing the particle data
	 * @return height of the particle texture
	 */
	public int height() {
		return _myHeight;
	}
	
	public int size() {
		return _myWidth * _myHeight;
	}
	
	public int x(int theIndex) {
		return theIndex % _myWidth;
	}
	
	public int y(int theIndex) {
		return theIndex / _myWidth;
	}
	
	/**
	 * Returns the texture with the current positions of the particles.
	 * @return texture containing the positions of the particles
	 */
	public CCShaderBuffer dataBuffer() {
		return _mySwapTexture.currentBuffer();
	}
	
	public CCTexture2D positionData() {
		return _mySwapTexture.attachment(0);
	}
	
	public CCTexture2D infoData() {
		return _mySwapTexture.attachment(1);
	}
	
	public CCTexture2D velocityData() {
		return _mySwapTexture.attachment(2);
	}
	
	public CCTexture2D colorData() {
		return _mySwapTexture.attachment(3);
	}
	
	/**
	 * Returns the position of the particle. This is useful as particle data is stored on the gpu
	 * and there for not accessible on the cpu side. Be aware that is time consuming and should only
	 * be used for a couple of particles.
	 * @param theParticle the particle to query
	 * @return the position of the given particle
	 */
	public CCVector3 position(CCParticle theParticle) {
		return position(theParticle, new CCVector3());
	}
	
	/**
	 * 
	 * @param theParticle the particle t query
	 * @param theVector vector to store the position
	 * @return the position of the particle as vector
	 */
	public CCVector3 position(CCParticle theParticle, CCVector3 theVector){
		FloatBuffer myResult = _mySwapTexture.getData(theParticle.x(), theParticle.y(), 1, 1);
		theVector.x = myResult.get();
		theVector.y = myResult.get();
		theVector.z = myResult.get();
		return theVector;
	}

	public CCShaderBuffer destinationDataTexture() {
		return _mySwapTexture.destinationBuffer();
	}
	
	/**
	 * Set the absolute position of the particle referenced by theIndex.
	 * @param theIndex index of the target particle
	 * @param thePosition target position of the particle
	 */
	public void setPosition(int theIndex, CCVector3 thePosition) {
		_myPositionUpdates.put(theIndex, thePosition);
	}
	
	private void updateManualPositionChanges(CCGraphics g) {
		
		if (_myPositionUpdates.size() == 0) {
			return;
		}
		
		// Render manually changed positions into the texture.
		_mySwapTexture.beginDrawCurrent(g,0);
		_mySetDataShader.start();
		
		g.beginShape(CCDrawMode.POINTS);
	
		Iterator<Entry<Integer, CCVector3>> it = _myPositionUpdates.entrySet().iterator();
		
	    while (it.hasNext()) {
	        Map.Entry<Integer, CCVector3> pairs = it.next();
	        
	        g.textureCoords3D(0, pairs.getValue());
			g.vertex(pairs.getKey() % _myWidth, pairs.getKey() / _myWidth);
	    }
	    
		g.endShape();
		
		_mySetDataShader.end();
		_mySwapTexture.endDrawCurrent(g);
		
		_myPositionUpdates.clear();
	}
	
	/**
	 * Update the lifetime of the given particle to what is specified in 
	 * the particle instance.
	 * @param theParticle particle instance containing new lifetime data
	 */
	public void updateLifecyle(CCParticle theParticle) {
		_myLifetimeUpdates.add(theParticle);
	}
	
	
	
	
	
	protected void beforeUpdate(CCGraphics g) {
		_myGroupData.beginDraw(g);
		g.pushAttribute();
		g.clearColor(0);
		g.clear();
		g.popAttribute();
		_myGroupData.endDraw(g);
		for(CCIParticleEmitter myEmitter:_myEmitter) {
			myEmitter.setData(g);
		}
		for(CCForce myForce:_myForces){
			myForce.preDisplay(g);
		}
		for(CCConstraint myConstraint:_myConstraints){
			myConstraint.preDisplay(g);
		}
		
		g.pushAttribute();
		g.noBlend();
		g.pointSize(1);

		_myEnvelopeData.preDisplay(g);

		g.popAttribute();
	}
	
	protected void afterUpdate(CCGraphics g){
		updateManualPositionChanges(g);
	}
	
	private CCTexture2D _myStaticPositionTexture = null;
	
	public void staticPositions(CCTexture2D theStaticPositions){
		_myStaticPositionTexture = theStaticPositions;
	}
	
	public void moveAll(CCVector3 theMoveAll) {
		_myUpdateShader.moveAll(theMoveAll);
	}
	
	public void update(final CCAnimator theAnimator){
		if(theAnimator.deltaTime() <= 0)return;
		
		for(CCIParticleEmitter myEmitter:_myEmitter) {
			myEmitter.update(theAnimator);
		}
		
		for(CCForce myForce:_myForces) {
			myForce.update(theAnimator);
		}
		
		for(CCConstraint myConstraint:_myConstraints) {
			myConstraint.update(theAnimator);
		}
		
		for(CCImpulse myImpulse:_myImpulses) {
			myImpulse.update(theAnimator);
		}

		_myUpdateShader.deltaTime(theAnimator.deltaTime());
		
		_myCurrentTime += theAnimator.deltaTime();
		for(CCParticleRenderer myRenderer:_myRenderer) {
			myRenderer.update(theAnimator);
		}
	}
	
	public void swapDataTextures(){
		_mySwapTexture.swap();
		_myUpdateShader.setTextureUniform("positionTexture", positionData());
		_myUpdateShader.setTextureUniform("infoTexture", infoData());
		_myUpdateShader.setTextureUniform("velocityTexture", velocityData());
		_myUpdateShader.setTextureUniform("colorTexture", colorData());
	}
	
	public void preDisplay(CCGraphics g){
		g.pushAttribute();
		g.noBlend();
		beforeUpdate(g);
		int myTextureUnit = 0;
		for(CCGLTextureUniform myTextureUniform:_myUpdateShader.textures()){
			if(myTextureUniform.texture == null ) continue;
			g.bindTexture(myTextureUnit, myTextureUniform.texture);
			myTextureUnit++;
		}
		
		_myUpdateShader.start();
		myTextureUnit = 0;
		for(CCGLTextureUniform myTextureUniform:_myUpdateShader.textures()){
			if(myTextureUniform.texture == null)continue;
			_myUpdateShader.uniform1i(myTextureUniform.parameter, myTextureUnit);
			myTextureUnit++;
		}
		_mySwapTexture.draw(g);
		_myUpdateShader.end();
		if(myTextureUnit > 0)g.unbindTextures();
		
		swapDataTextures();
		
		afterUpdate(g);
		g.popAttribute();
		for(CCParticleRenderer myRenderer:_myRenderer) {
			myRenderer.preDisplay(g);
		}
	}
	
	public void display(CCGraphics g) {
		for(CCParticleRenderer myRenderer:_myRenderer) {
			myRenderer.display(g);
		}
	}
	
	public void staticPositionBlend(float theBlend){
		_myUpdateShader.staticPositionBlend(theBlend);
	}
	
	public List<CCForce> forces(){
		return _myForces;
	}
	
	public List<CCBlend> blends(){
		return _myBlends;
	}
	
	public List<CCParticleRenderer> renderer() {
		return _myRenderer;
	}

	public void renderer(CCParticleRenderer theRenderer) {
		_myRenderer.clear();
		theRenderer.setup(this);
		_myRenderer.add(theRenderer);
		_myRendererMap.put(theRenderer.name(), theRenderer);
	}
}
