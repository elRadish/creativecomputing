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
package cc.creativecomputing.simulation.particles.constraints;

import cc.creativecomputing.graphics.texture.CCTexture3D;
import cc.creativecomputing.math.CCVector3;


/**
 * This force creates a texture based force field, every pixel of the
 * texture holds a direction which acts as a force on the particle. 
 * To read out the force from the texture it can be placed and scaled
 * on the xy plane. The xy coords of the particles will than be used to
 * read the force from the texture.
 * @author christian riekoff
 *
 */
public class CCGPUTexture3DConstraint extends CCConstraint{
	
	protected CCTexture3D _myTexture;
	private CCVector3 _myTextureScale;
	private CCVector3 _myTextureOffset;

	private CCVector3 _myMinCut = new CCVector3(0,0,0);
	private CCVector3 _myMaxCut = new CCVector3(1,1,1);
	
	private float _myMinForce = 0;
	
	private String _myTextureParameter;
	private String _myTextureScaleParameter;
	private String _myTextureOffsetParameter;
	
	private String _myMinCutParameter;
	private String _myMaxCutParameter;
	private String _myMinForceParameter;
	
	public CCGPUTexture3DConstraint(
		final CCTexture3D theTexture,
		final CCVector3 theTextureScale,
		final CCVector3 theTextureOffset
	){
		super("Texture3DConstraint");
		_myTexture = theTexture;
		_myTextureScale = theTextureScale;
		_myTextureOffset = theTextureOffset;

		_myTextureParameter = parameter("texture");
		_myTextureScaleParameter = parameter("textureScale");
		_myTextureOffsetParameter = parameter("textureOffset");
		_myMinCutParameter = parameter("minCut");
		_myMaxCutParameter = parameter("maxCut");
		_myMinForceParameter = parameter("minForce");
	}
	
//	@Override
//	public void setupParameter(int theWidth, int theHeight){
//		
//		texture(_myTexture);
//	}
//
//	@Override
//	public void update(final float theDeltaTime) {
//		super.update(theDeltaTime);
//		_myVelocityShader.texture(_myTextureParameter, _myTexture.id());
//		_myVelocityShader.parameter(_myTextureScaleParameter, _myTextureScale);
//		_myVelocityShader.parameter(_myTextureOffsetParameter, _myTextureOffset);
//		_myVelocityShader.parameter(_myMinCutParameter, _myMinCut);
//		_myVelocityShader.parameter(_myMaxCutParameter, _myMaxCut);
//		_myVelocityShader.parameter(_myMinForceParameter, _myMinForce);
//		
//	}
//	
//	public boolean addToForceArray(){
//		return true;
//	}
//	
//	public void texture(final CCTexture3D theTexture){
//		_myTexture = theTexture;
//	}
//	
//	public CCVector3 textureScale() {
//		return _myTextureScale;
//	}
//	
//	public CCVector3 textureOffset() {
//		return _myTextureOffset;
//	}
//	
//	public CCVector3 minCut(){
//		return _myMinCut;
//	}
//	
//	public CCVector3 maxCut(){
//		return _myMaxCut;
//	}
//	
//	public void minForce(float theMinForce){
//		_myMinForce = theMinForce;
//	}
}
