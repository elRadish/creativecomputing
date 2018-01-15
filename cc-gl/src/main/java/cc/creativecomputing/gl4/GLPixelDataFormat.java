package cc.creativecomputing.gl4;

import cc.creativecomputing.image.CCPixelFormat;

import com.jogamp.opengl.GL4;

public enum GLPixelDataFormat {
	DEPTH_COMPONENT(GL4.GL_DEPTH_COMPONENT),
	DEPTH_STENCIL(GL4.GL_DEPTH_STENCIL),
	STENCIL_INDEX(GL4.GL_STENCIL_INDEX),
	RED(GL4.GL_RED),
	GREEN(GL4.GL_GREEN),
	BLUE(GL4.GL_BLUE),
	RG(GL4.GL_RG),
	RGB(GL4.GL_RGB),
	RGBA(GL4.GL_RGBA),
	BGR(GL4.GL_BGR),
	BGRA(GL4.GL_BGRA),
	RED_INTEGER(GL4.GL_RED_INTEGER),
	GREEN_INTEGER(GL4.GL_GREEN_INTEGER),
	BLUE_INTEGER(GL4.GL_BLUE_INTEGER),
	RG_INTEGER(GL4.GL_RG_INTEGER),
	RGB_INTEGER(GL4.GL_RGB_INTEGER),
	RGBA_INTEGER(GL4.GL_RGBA_INTEGER),
	BGR_INTEGER(GL4.GL_BGR_INTEGER),
	BGRA_INTEGER(GL4.GL_BGRA_INTEGER);
	
	private int _myGLID;
	
	GLPixelDataFormat(int theGLID){
		_myGLID = theGLID;
	}
	
	public int glID(){
		return _myGLID;
	}
	
	public static GLPixelDataFormat fromCC(CCPixelFormat theFormat){
		switch(theFormat){
		case DEPTH_COMPONENT:return DEPTH_COMPONENT;
		case DEPTH_STENCIL:return DEPTH_STENCIL;
		case STENCIL_INDEX:return STENCIL_INDEX;
		case RED:return RED;
		case RED_INTEGER:return RED_INTEGER;
		case GREEN:return GREEN;
		case GREEN_INTEGER:return GREEN_INTEGER;
		case BLUE:return BLUE;
		case BLUE_INTEGER:return BLUE_INTEGER;
		case RG:return RG;
		case RG_INTEGER:return RG_INTEGER;
		case RGB:return RGB;
		case RGBA:return RGBA;
		case BGR:return BGR;
		case BGR_INTEGER:return BGR_INTEGER;
		case BGRA:return BGRA;
		case BGRA_INTEGER:return BGRA_INTEGER;
		case RGB_INTEGER:return RGB_INTEGER;
		case RGBA_INTEGER:return RGBA_INTEGER;
		}
		return null;
	}
	
	public static GLPixelDataFormat fromGLID(int theGLID){
		switch(theGLID){
		case GL4.GL_DEPTH_COMPONENT:return DEPTH_COMPONENT;
		case GL4.GL_DEPTH_STENCIL:return DEPTH_STENCIL;
		case GL4.GL_STENCIL_INDEX:return STENCIL_INDEX;
		case GL4.GL_RED:return RED;
		case GL4.GL_GREEN:return GREEN;
		case GL4.GL_BLUE:return BLUE;
		case GL4.GL_RG:return RG;
		case GL4.GL_RGB:return RGB;
		case GL4.GL_RGBA:return RGBA;
		case GL4.GL_BGR:return BGR;
		case GL4.GL_BGRA:return BGRA;
		case GL4.GL_RED_INTEGER:return RED_INTEGER;
		case GL4.GL_GREEN_INTEGER:return GREEN_INTEGER;
		case GL4.GL_BLUE_INTEGER:return BLUE_INTEGER;
		case GL4.GL_RG_INTEGER:return RG_INTEGER;
		case GL4.GL_RGB_INTEGER:return RGB_INTEGER;
		case GL4.GL_RGBA_INTEGER:return RGBA_INTEGER;
		case GL4.GL_BGR_INTEGER:return BGR_INTEGER;
		case GL4.GL_BGRA_INTEGER:return BGRA_INTEGER;
		}
		return null;
	}
}

