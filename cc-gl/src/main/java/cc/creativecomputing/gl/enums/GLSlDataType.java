package cc.creativecomputing.gl.enums;

import com.jogamp.opengl.GL4;

public enum GLSlDataType {
	FLOAT(GL4.GL_FLOAT),
	FLOAT_VEC2(GL4.GL_FLOAT_VEC2),
	FLOAT_VEC3(GL4.GL_FLOAT_VEC3),
	FLOAT_VEC4(GL4.GL_FLOAT_VEC4),
	DOUBLE(GL4.GL_DOUBLE),
	DOUBLE_VEC2(GL4.GL_DOUBLE_VEC2),
	DOUBLE_VEC3(GL4.GL_DOUBLE_VEC3),
	DOUBLE_VEC4(GL4.GL_DOUBLE_VEC4),
	INT(GL4.GL_INT),
	INT_VEC2(GL4.GL_INT_VEC2),
	INT_VEC3(GL4.GL_INT_VEC3),
	INT_VEC4(GL4.GL_INT_VEC4),
	UNSIGNED_INT(GL4.GL_UNSIGNED_INT),
	UNSIGNED_INT_VEC2(GL4.GL_UNSIGNED_INT_VEC2),
	UNSIGNED_INT_VEC3(GL4.GL_UNSIGNED_INT_VEC3),
	UNSIGNED_INT_VEC4(GL4.GL_UNSIGNED_INT_VEC4),
	BOOL(GL4.GL_BOOL),
	BOOL_VEC2(GL4.GL_BOOL_VEC2),
	BOOL_VEC3(GL4.GL_BOOL_VEC3),
	BOOL_VEC4(GL4.GL_BOOL_VEC4),
	FLOAT_MAT2(GL4.GL_FLOAT_MAT2),
	FLOAT_MAT3(GL4.GL_FLOAT_MAT3),
	FLOAT_MAT4(GL4.GL_FLOAT_MAT4),
	FLOAT_MAT2x3(GL4.GL_FLOAT_MAT2x3),
	FLOAT_MAT2x4(GL4.GL_FLOAT_MAT2x4),
	FLOAT_MAT3x2(GL4.GL_FLOAT_MAT3x2),
	FLOAT_MAT3x4(GL4.GL_FLOAT_MAT3x4),
	FLOAT_MAT4x2(GL4.GL_FLOAT_MAT4x2),
	FLOAT_MAT4x3(GL4.GL_FLOAT_MAT4x3),
	DOUBLE_MAT2(GL4.GL_DOUBLE_MAT2),
	DOUBLE_MAT3(GL4.GL_DOUBLE_MAT3),
	DOUBLE_MAT4(GL4.GL_DOUBLE_MAT4),
	DOUBLE_MAT2x3(GL4.GL_DOUBLE_MAT2x3),
	DOUBLE_MAT2x4(GL4.GL_DOUBLE_MAT2x4),
	DOUBLE_MAT3x2(GL4.GL_DOUBLE_MAT3x2),
	DOUBLE_MAT3x4(GL4.GL_DOUBLE_MAT3x4),
	DOUBLE_MAT4x2(GL4.GL_DOUBLE_MAT4x2),
	DOUBLE_MAT4x3(GL4.GL_DOUBLE_MAT4x3),
	SAMPLER_1D(GL4.GL_SAMPLER_1D),
	SAMPLER_2D(GL4.GL_SAMPLER_2D),
	SAMPLER_3D(GL4.GL_SAMPLER_3D),
	SAMPLER_CUBE(GL4.GL_SAMPLER_CUBE),
	SAMPLER_1D_SHADOW(GL4.GL_SAMPLER_1D_SHADOW),
	SAMPLER_2D_SHADOW(GL4.GL_SAMPLER_2D_SHADOW),
	SAMPLER_1D_ARRAY(GL4.GL_SAMPLER_1D_ARRAY),
	SAMPLER_2D_ARRAY(GL4.GL_SAMPLER_2D_ARRAY),
	SAMPLER_CUBE_MAP_ARRAY(GL4.GL_SAMPLER_CUBE_MAP_ARRAY),
	SAMPLER_1D_ARRAY_SHADOW(GL4.GL_SAMPLER_1D_ARRAY_SHADOW),
	SAMPLER_2D_ARRAY_SHADOW(GL4.GL_SAMPLER_2D_ARRAY_SHADOW),
	SAMPLER_2D_MULTISAMPLE(GL4.GL_SAMPLER_2D_MULTISAMPLE),
	SAMPLER_2D_MULTISAMPLE_ARRAY(GL4.GL_SAMPLER_2D_MULTISAMPLE_ARRAY),
	SAMPLER_CUBE_SHADOW(GL4.GL_SAMPLER_CUBE_SHADOW),
	SAMPLER_CUBE_MAP_ARRAY_SHADOW(GL4.GL_SAMPLER_CUBE_MAP_ARRAY_SHADOW),
	SAMPLER_BUFFER(GL4.GL_SAMPLER_BUFFER),
	SAMPLER_2D_RECT(GL4.GL_SAMPLER_2D_RECT),
	SAMPLER_2D_RECT_SHADOW(GL4.GL_SAMPLER_2D_RECT_SHADOW),
	INT_SAMPLER_1D(GL4.GL_INT_SAMPLER_1D),
	INT_SAMPLER_2D(GL4.GL_INT_SAMPLER_2D),
	INT_SAMPLER_3D(GL4.GL_INT_SAMPLER_3D),
	INT_SAMPLER_CUBE(GL4.GL_INT_SAMPLER_CUBE),
	INT_SAMPLER_1D_ARRAY(GL4.GL_INT_SAMPLER_1D_ARRAY),
	INT_SAMPLER_2D_ARRAY(GL4.GL_INT_SAMPLER_2D_ARRAY),
	INT_SAMPLER_CUBE_MAP_ARRAY(GL4.GL_INT_SAMPLER_CUBE_MAP_ARRAY),
	INT_SAMPLER_2D_MULTISAMPLE(GL4.GL_INT_SAMPLER_2D_MULTISAMPLE),
	INT_SAMPLER_2D_MULTISAMPLE_ARRAY(GL4.GL_INT_SAMPLER_2D_MULTISAMPLE_ARRAY),
	INT_SAMPLER_BUFFER(GL4.GL_INT_SAMPLER_BUFFER),
	INT_SAMPLER_2D_RECT(GL4.GL_INT_SAMPLER_2D_RECT),
	UNSIGNED_INT_SAMPLER_1D(GL4.GL_UNSIGNED_INT_SAMPLER_1D),
	UNSIGNED_INT_SAMPLER_2D(GL4.GL_UNSIGNED_INT_SAMPLER_2D),
	UNSIGNED_INT_SAMPLER_3D(GL4.GL_UNSIGNED_INT_SAMPLER_3D),
	UNSIGNED_INT_SAMPLER_CUBE(GL4.GL_UNSIGNED_INT_SAMPLER_CUBE),
	UNSIGNED_INT_SAMPLER_1D_ARRAY(GL4.GL_UNSIGNED_INT_SAMPLER_1D_ARRAY),
	UNSIGNED_INT_SAMPLER_2D_ARRAY(GL4.GL_UNSIGNED_INT_SAMPLER_2D_ARRAY),
	UNSIGNED_INT_SAMPLER_CUBE_MAP_ARRAY(GL4.GL_UNSIGNED_INT_SAMPLER_CUBE_MAP_ARRAY),
	UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE(GL4.GL_UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE),
	UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE_ARRAY(GL4.GL_UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE_ARRAY),
	UNSIGNED_INT_SAMPLER_BUFFER(GL4.GL_UNSIGNED_INT_SAMPLER_BUFFER),
	UNSIGNED_INT_SAMPLER_2D_RECT(GL4.GL_UNSIGNED_INT_SAMPLER_2D_RECT),
	IMAGE_1D(GL4.GL_IMAGE_1D),
	IMAGE_2D(GL4.GL_IMAGE_2D),
	IMAGE_3D(GL4.GL_IMAGE_3D),
	IMAGE_2D_RECT(GL4.GL_IMAGE_2D_RECT),
	IMAGE_CUBE(GL4.GL_IMAGE_CUBE),
	IMAGE_BUFFER(GL4.GL_IMAGE_BUFFER),
	IMAGE_1D_ARRAY(GL4.GL_IMAGE_1D_ARRAY),
	IMAGE_2D_ARRAY(GL4.GL_IMAGE_2D_ARRAY),
	IMAGE_2D_MULTISAMPLE(GL4.GL_IMAGE_2D_MULTISAMPLE),
	IMAGE_2D_MULTISAMPLE_ARRAY(GL4.GL_IMAGE_2D_MULTISAMPLE_ARRAY),
	INT_IMAGE_1D(GL4.GL_INT_IMAGE_1D),
	INT_IMAGE_2D(GL4.GL_INT_IMAGE_2D),
	INT_IMAGE_3D(GL4.GL_INT_IMAGE_3D),
	INT_IMAGE_2D_RECT(GL4.GL_INT_IMAGE_2D_RECT),
	INT_IMAGE_CUBE(GL4.GL_INT_IMAGE_CUBE),
	INT_IMAGE_BUFFER(GL4.GL_INT_IMAGE_BUFFER),
	INT_IMAGE_1D_ARRAY(GL4.GL_INT_IMAGE_1D_ARRAY),
	INT_IMAGE_2D_ARRAY(GL4.GL_INT_IMAGE_2D_ARRAY),
	INT_IMAGE_2D_MULTISAMPLE(GL4.GL_INT_IMAGE_2D_MULTISAMPLE),
	INT_IMAGE_2D_MULTISAMPLE_ARRAY(GL4.GL_INT_IMAGE_2D_MULTISAMPLE_ARRAY),
	UNSIGNED_INT_IMAGE_1D(GL4.GL_UNSIGNED_INT_IMAGE_1D),
	UNSIGNED_INT_IMAGE_2D(GL4.GL_UNSIGNED_INT_IMAGE_2D),
	UNSIGNED_INT_IMAGE_3D(GL4.GL_UNSIGNED_INT_IMAGE_3D),
	UNSIGNED_INT_IMAGE_2D_RECT(GL4.GL_UNSIGNED_INT_IMAGE_2D_RECT),
	UNSIGNED_INT_IMAGE_CUBE(GL4.GL_UNSIGNED_INT_IMAGE_CUBE),
	UNSIGNED_INT_IMAGE_BUFFER(GL4.GL_UNSIGNED_INT_IMAGE_BUFFER),
	UNSIGNED_INT_IMAGE_1D_ARRAY(GL4.GL_UNSIGNED_INT_IMAGE_1D_ARRAY),
	UNSIGNED_INT_IMAGE_2D_ARRAY(GL4.GL_UNSIGNED_INT_IMAGE_2D_ARRAY),
	UNSIGNED_INT_IMAGE_2D_MULTISAMPLE(GL4.GL_UNSIGNED_INT_IMAGE_2D_MULTISAMPLE),
	UNSIGNED_INT_IMAGE_2D_MULTISAMPLE_ARRAY(GL4.GL_UNSIGNED_INT_IMAGE_2D_MULTISAMPLE_ARRAY),
	UNSIGNED_INT_ATOMIC_COUNTER(GL4.GL_UNSIGNED_INT_ATOMIC_COUNTER),
	NONE(GL4.GL_NONE);
	
	private int _myGLID;
	
	GLSlDataType(int theGLID){
		_myGLID = theGLID;
	}
	
	public int glID(){
		return _myGLID;
	}
	
	public static GLSlDataType fromGLID(int theGLID){
		switch(theGLID){
		case GL4.GL_FLOAT:return FLOAT;
		case GL4.GL_FLOAT_VEC2:return FLOAT_VEC2;
		case GL4.GL_FLOAT_VEC3:return FLOAT_VEC3;
		case GL4.GL_FLOAT_VEC4:return FLOAT_VEC4;
		case GL4.GL_DOUBLE:return DOUBLE;
		case GL4.GL_DOUBLE_VEC2:return DOUBLE_VEC2;
		case GL4.GL_DOUBLE_VEC3:return DOUBLE_VEC3;
		case GL4.GL_DOUBLE_VEC4:return DOUBLE_VEC4;
		case GL4.GL_INT:return INT;
		case GL4.GL_INT_VEC2:return INT_VEC2;
		case GL4.GL_INT_VEC3:return INT_VEC3;
		case GL4.GL_INT_VEC4:return INT_VEC4;
		case GL4.GL_UNSIGNED_INT:return UNSIGNED_INT;
		case GL4.GL_UNSIGNED_INT_VEC2:return UNSIGNED_INT_VEC2;
		case GL4.GL_UNSIGNED_INT_VEC3:return UNSIGNED_INT_VEC3;
		case GL4.GL_UNSIGNED_INT_VEC4:return UNSIGNED_INT_VEC4;
		case GL4.GL_BOOL:return BOOL;
		case GL4.GL_BOOL_VEC2:return BOOL_VEC2;
		case GL4.GL_BOOL_VEC3:return BOOL_VEC3;
		case GL4.GL_BOOL_VEC4:return BOOL_VEC4;
		case GL4.GL_FLOAT_MAT2:return FLOAT_MAT2;
		case GL4.GL_FLOAT_MAT3:return FLOAT_MAT3;
		case GL4.GL_FLOAT_MAT4:return FLOAT_MAT4;
		case GL4.GL_FLOAT_MAT2x3:return FLOAT_MAT2x3;
		case GL4.GL_FLOAT_MAT2x4:return FLOAT_MAT2x4;
		case GL4.GL_FLOAT_MAT3x2:return FLOAT_MAT3x2;
		case GL4.GL_FLOAT_MAT3x4:return FLOAT_MAT3x4;
		case GL4.GL_FLOAT_MAT4x2:return FLOAT_MAT4x2;
		case GL4.GL_FLOAT_MAT4x3:return FLOAT_MAT4x3;
		case GL4.GL_DOUBLE_MAT2:return DOUBLE_MAT2;
		case GL4.GL_DOUBLE_MAT3:return DOUBLE_MAT3;
		case GL4.GL_DOUBLE_MAT4:return DOUBLE_MAT4;
		case GL4.GL_DOUBLE_MAT2x3:return DOUBLE_MAT2x3;
		case GL4.GL_DOUBLE_MAT2x4:return DOUBLE_MAT2x4;
		case GL4.GL_DOUBLE_MAT3x2:return DOUBLE_MAT3x2;
		case GL4.GL_DOUBLE_MAT3x4:return DOUBLE_MAT3x4;
		case GL4.GL_DOUBLE_MAT4x2:return DOUBLE_MAT4x2;
		case GL4.GL_DOUBLE_MAT4x3:return DOUBLE_MAT4x3;
		case GL4.GL_SAMPLER_1D:return SAMPLER_1D;
		case GL4.GL_SAMPLER_2D:return SAMPLER_2D;
		case GL4.GL_SAMPLER_3D:return SAMPLER_3D;
		case GL4.GL_SAMPLER_CUBE:return SAMPLER_CUBE;
		case GL4.GL_SAMPLER_1D_SHADOW:return SAMPLER_1D_SHADOW;
		case GL4.GL_SAMPLER_2D_SHADOW:return SAMPLER_2D_SHADOW;
		case GL4.GL_SAMPLER_1D_ARRAY:return SAMPLER_1D_ARRAY;
		case GL4.GL_SAMPLER_2D_ARRAY:return SAMPLER_2D_ARRAY;
		case GL4.GL_SAMPLER_CUBE_MAP_ARRAY:return SAMPLER_CUBE_MAP_ARRAY;
		case GL4.GL_SAMPLER_1D_ARRAY_SHADOW:return SAMPLER_1D_ARRAY_SHADOW;
		case GL4.GL_SAMPLER_2D_ARRAY_SHADOW:return SAMPLER_2D_ARRAY_SHADOW;
		case GL4.GL_SAMPLER_2D_MULTISAMPLE:return SAMPLER_2D_MULTISAMPLE;
		case GL4.GL_SAMPLER_2D_MULTISAMPLE_ARRAY:return SAMPLER_2D_MULTISAMPLE_ARRAY;
		case GL4.GL_SAMPLER_CUBE_SHADOW:return SAMPLER_CUBE_SHADOW;
		case GL4.GL_SAMPLER_CUBE_MAP_ARRAY_SHADOW:return SAMPLER_CUBE_MAP_ARRAY_SHADOW;
		case GL4.GL_SAMPLER_BUFFER:return SAMPLER_BUFFER;
		case GL4.GL_SAMPLER_2D_RECT:return SAMPLER_2D_RECT;
		case GL4.GL_SAMPLER_2D_RECT_SHADOW:return SAMPLER_2D_RECT_SHADOW;
		case GL4.GL_INT_SAMPLER_1D:return INT_SAMPLER_1D;
		case GL4.GL_INT_SAMPLER_2D:return INT_SAMPLER_2D;
		case GL4.GL_INT_SAMPLER_3D:return INT_SAMPLER_3D;
		case GL4.GL_INT_SAMPLER_CUBE:return INT_SAMPLER_CUBE;
		case GL4.GL_INT_SAMPLER_1D_ARRAY:return INT_SAMPLER_1D_ARRAY;
		case GL4.GL_INT_SAMPLER_2D_ARRAY:return INT_SAMPLER_2D_ARRAY;
		case GL4.GL_INT_SAMPLER_CUBE_MAP_ARRAY:return INT_SAMPLER_CUBE_MAP_ARRAY;
		case GL4.GL_INT_SAMPLER_2D_MULTISAMPLE:return INT_SAMPLER_2D_MULTISAMPLE;
		case GL4.GL_INT_SAMPLER_2D_MULTISAMPLE_ARRAY:return INT_SAMPLER_2D_MULTISAMPLE_ARRAY;
		case GL4.GL_INT_SAMPLER_BUFFER:return INT_SAMPLER_BUFFER;
		case GL4.GL_INT_SAMPLER_2D_RECT:return INT_SAMPLER_2D_RECT;
		case GL4.GL_UNSIGNED_INT_SAMPLER_1D:return UNSIGNED_INT_SAMPLER_1D;
		case GL4.GL_UNSIGNED_INT_SAMPLER_2D:return UNSIGNED_INT_SAMPLER_2D;
		case GL4.GL_UNSIGNED_INT_SAMPLER_3D:return UNSIGNED_INT_SAMPLER_3D;
		case GL4.GL_UNSIGNED_INT_SAMPLER_CUBE:return UNSIGNED_INT_SAMPLER_CUBE;
		case GL4.GL_UNSIGNED_INT_SAMPLER_1D_ARRAY:return UNSIGNED_INT_SAMPLER_1D_ARRAY;
		case GL4.GL_UNSIGNED_INT_SAMPLER_2D_ARRAY:return UNSIGNED_INT_SAMPLER_2D_ARRAY;
		case GL4.GL_UNSIGNED_INT_SAMPLER_CUBE_MAP_ARRAY:return UNSIGNED_INT_SAMPLER_CUBE_MAP_ARRAY;
		case GL4.GL_UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE:return UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE;
		case GL4.GL_UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE_ARRAY:return UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE_ARRAY;
		case GL4.GL_UNSIGNED_INT_SAMPLER_BUFFER:return UNSIGNED_INT_SAMPLER_BUFFER;
		case GL4.GL_UNSIGNED_INT_SAMPLER_2D_RECT:return UNSIGNED_INT_SAMPLER_2D_RECT;
		case GL4.GL_IMAGE_1D:return IMAGE_1D;
		case GL4.GL_IMAGE_2D:return IMAGE_2D;
		case GL4.GL_IMAGE_3D:return IMAGE_3D;
		case GL4.GL_IMAGE_2D_RECT:return IMAGE_2D_RECT;
		case GL4.GL_IMAGE_CUBE:return IMAGE_CUBE;
		case GL4.GL_IMAGE_BUFFER:return IMAGE_BUFFER;
		case GL4.GL_IMAGE_1D_ARRAY:return IMAGE_1D_ARRAY;
		case GL4.GL_IMAGE_2D_ARRAY:return IMAGE_2D_ARRAY;
		case GL4.GL_IMAGE_2D_MULTISAMPLE:return IMAGE_2D_MULTISAMPLE;
		case GL4.GL_IMAGE_2D_MULTISAMPLE_ARRAY:return IMAGE_2D_MULTISAMPLE_ARRAY;
		case GL4.GL_INT_IMAGE_1D:return INT_IMAGE_1D;
		case GL4.GL_INT_IMAGE_2D:return INT_IMAGE_2D;
		case GL4.GL_INT_IMAGE_3D:return INT_IMAGE_3D;
		case GL4.GL_INT_IMAGE_2D_RECT:return INT_IMAGE_2D_RECT;
		case GL4.GL_INT_IMAGE_CUBE:return INT_IMAGE_CUBE;
		case GL4.GL_INT_IMAGE_BUFFER:return INT_IMAGE_BUFFER;
		case GL4.GL_INT_IMAGE_1D_ARRAY:return INT_IMAGE_1D_ARRAY;
		case GL4.GL_INT_IMAGE_2D_ARRAY:return INT_IMAGE_2D_ARRAY;
		case GL4.GL_INT_IMAGE_2D_MULTISAMPLE:return INT_IMAGE_2D_MULTISAMPLE;
		case GL4.GL_INT_IMAGE_2D_MULTISAMPLE_ARRAY:return INT_IMAGE_2D_MULTISAMPLE_ARRAY;
		case GL4.GL_UNSIGNED_INT_IMAGE_1D:return UNSIGNED_INT_IMAGE_1D;
		case GL4.GL_UNSIGNED_INT_IMAGE_2D:return UNSIGNED_INT_IMAGE_2D;
		case GL4.GL_UNSIGNED_INT_IMAGE_3D:return UNSIGNED_INT_IMAGE_3D;
		case GL4.GL_UNSIGNED_INT_IMAGE_2D_RECT:return UNSIGNED_INT_IMAGE_2D_RECT;
		case GL4.GL_UNSIGNED_INT_IMAGE_CUBE:return UNSIGNED_INT_IMAGE_CUBE;
		case GL4.GL_UNSIGNED_INT_IMAGE_BUFFER:return UNSIGNED_INT_IMAGE_BUFFER;
		case GL4.GL_UNSIGNED_INT_IMAGE_1D_ARRAY:return UNSIGNED_INT_IMAGE_1D_ARRAY;
		case GL4.GL_UNSIGNED_INT_IMAGE_2D_ARRAY:return UNSIGNED_INT_IMAGE_2D_ARRAY;
		case GL4.GL_UNSIGNED_INT_IMAGE_2D_MULTISAMPLE:return UNSIGNED_INT_IMAGE_2D_MULTISAMPLE;
		case GL4.GL_UNSIGNED_INT_IMAGE_2D_MULTISAMPLE_ARRAY:return UNSIGNED_INT_IMAGE_2D_MULTISAMPLE_ARRAY;
		case GL4.GL_UNSIGNED_INT_ATOMIC_COUNTER:return UNSIGNED_INT_ATOMIC_COUNTER;
		case GL4.GL_NONE:return NONE;
		}
		return null;
	}
}

