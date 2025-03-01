package cc.creativecomputing.gl.enums;

import com.jogamp.opengl.GL4;

public enum GLMemoryBarrierBit {
	VERTEX_ATTRIB_ARRAY_BARRIER_BIT(GL4.GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT),
	ELEMENT_ARRAY_BARRIER_BIT(GL4.GL_ELEMENT_ARRAY_BARRIER_BIT),
	UNIFORM_BARRIER_BIT(GL4.GL_UNIFORM_BARRIER_BIT),
	TEXTURE_FETCH_BARRIER_BIT(GL4.GL_TEXTURE_FETCH_BARRIER_BIT),
	SHADER_IMAGE_ACCESS_BARRIER_BIT(GL4.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT),
	COMMAND_BARRIER_BIT(GL4.GL_COMMAND_BARRIER_BIT),
	PIXEL_BUFFER_BARRIER_BIT(GL4.GL_PIXEL_BUFFER_BARRIER_BIT),
	TEXTURE_UPDATE_BARRIER_BIT(GL4.GL_TEXTURE_UPDATE_BARRIER_BIT),
	BUFFER_UPDATE_BARRIER_BIT(GL4.GL_BUFFER_UPDATE_BARRIER_BIT),
	FRAMEBUFFER_BARRIER_BIT(GL4.GL_FRAMEBUFFER_BARRIER_BIT),
	TRANSFORM_FEEDBACK_BARRIER_BIT(GL4.GL_TRANSFORM_FEEDBACK_BARRIER_BIT),
	ATOMIC_COUNTER_BARRIER_BIT(GL4.GL_ATOMIC_COUNTER_BARRIER_BIT),
	SHADER_STORAGE_BARRIER_BIT(GL4.GL_SHADER_STORAGE_BARRIER_BIT),
	ALL_BARRIER_BITS((int)GL4.GL_ALL_BARRIER_BITS);
	
	private int _myGLID;
	
	GLMemoryBarrierBit(int theGLID){
		_myGLID = theGLID;
	}
	
	public int glID(){
		return _myGLID;
	}
	
	public static GLMemoryBarrierBit fromGLID(int theGLID){
		switch(theGLID){
		case GL4.GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT:return VERTEX_ATTRIB_ARRAY_BARRIER_BIT;
		case GL4.GL_ELEMENT_ARRAY_BARRIER_BIT:return ELEMENT_ARRAY_BARRIER_BIT;
		case GL4.GL_UNIFORM_BARRIER_BIT:return UNIFORM_BARRIER_BIT;
		case GL4.GL_TEXTURE_FETCH_BARRIER_BIT:return TEXTURE_FETCH_BARRIER_BIT;
		case GL4.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT:return SHADER_IMAGE_ACCESS_BARRIER_BIT;
		case GL4.GL_COMMAND_BARRIER_BIT:return COMMAND_BARRIER_BIT;
		case GL4.GL_PIXEL_BUFFER_BARRIER_BIT:return PIXEL_BUFFER_BARRIER_BIT;
		case GL4.GL_TEXTURE_UPDATE_BARRIER_BIT:return TEXTURE_UPDATE_BARRIER_BIT;
		case GL4.GL_BUFFER_UPDATE_BARRIER_BIT:return BUFFER_UPDATE_BARRIER_BIT;
		case GL4.GL_FRAMEBUFFER_BARRIER_BIT:return FRAMEBUFFER_BARRIER_BIT;
		case GL4.GL_TRANSFORM_FEEDBACK_BARRIER_BIT:return TRANSFORM_FEEDBACK_BARRIER_BIT;
		case GL4.GL_ATOMIC_COUNTER_BARRIER_BIT:return ATOMIC_COUNTER_BARRIER_BIT;
		case GL4.GL_SHADER_STORAGE_BARRIER_BIT:return SHADER_STORAGE_BARRIER_BIT;
		case (int)GL4.GL_ALL_BARRIER_BITS:return ALL_BARRIER_BITS;
		}
		return null;
	}
}

