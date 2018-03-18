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

import cc.creativecomputing.core.CCProperty;
import cc.creativecomputing.graphics.shader.CCGLProgram;
import cc.creativecomputing.graphics.texture.CCTexture2D;
import cc.creativecomputing.math.CCVector3;

/**
 * Use the height
 * @author Christian Riekoff
 *
 */
public class CCTerrainConstraint extends CCConstraint{
	
	private CCTexture2D _myTexture;
	private CCVector3 _myScale;
	private CCVector3 _myOffset;
	
	@CCProperty(name = "exponent", min = 0, max = 10)
	private double _cExponent = 1;
	
	private String _myTextureParameter;
	private String _myScaleParameter;
	private String _myOffsetParameter;
	private String _myExponentParameter;
	
	public CCTerrainConstraint(
		final CCTexture2D theTexture, final CCVector3 theScale, final CCVector3 theOffset,
		final float theResilience, final float theFriction, final float theMinimalVelocity
	) {
		super("terrainConstraint", theResilience, theFriction, theMinimalVelocity);
		
		_myTexture = theTexture;
		_myScale = new CCVector3(theScale);
		_myOffset = new CCVector3(theOffset);

		_myTextureParameter = parameter("texture");
		_myScaleParameter = parameter("scale");
		_myOffsetParameter = parameter("offset");
		_myExponentParameter = parameter("exponent");
	}
	
	@Override
	public void setShader(CCGLProgram theProgram) {
		super.setShader(theProgram);
		_myShader.setTextureUniform(_myTextureParameter, _myTexture);
	}
	
	@Override
	public void setUniforms() {
		super.setUniforms();
		_myShader.uniform3f(_myScaleParameter, _myScale);
		_myShader.uniform3f(_myOffsetParameter, _myOffset);
		_myShader.uniform1f(_myExponentParameter, _cExponent);
	}
	
	public void texture(final CCTexture2D theTexture) {
		_myTexture = theTexture;
	}
	
	public CCVector3 textureScale() {
		return _myScale;
	}
	
	public CCVector3 textureOffset() {
		return _myOffset;
	}
	
	/**
	 * Sets the exponent to change the values of the height map. All pixel values
	 * are read as brightness between 0 and 1. Where 0 is low and 1 is high. Setting 
	 * the exponent you can have different increases in height. The default value is 1.
	 * @param theExponent exponent to control the height increase
	 */
	public void exponent(final float theExponent){
		_cExponent = theExponent;
	}
}
