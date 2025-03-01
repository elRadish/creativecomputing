package cc.creativecomputing.gl4;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

import cc.creativecomputing.io.CCBufferUtil;
import cc.creativecomputing.io.CCNIOUtil;

import com.jogamp.opengl.GL4;

/**
 * A shader object is used to maintain the source code strings that define a shader.
 * @author christianr
 *
 */
public class GLShaderObject {
	
	/**
	 * shaderType indicates the type of shader to be created. Five types of shader are supported. 
	 * @author christianr
	 *
	 */
	public enum GLShaderType{
		/**
		 * A shader of type VERTEX is a shader that is intended to run on the programmable vertex processor.
		 */
		VERTEX(GL4.GL_VERTEX_SHADER),
		/**
		 * A shader of type TESS_CONTROL is a shader that is intended to run on the programmable tessellation processor in the control stage.
		 */
		TESS_CONTROL(GL4.GL_TESS_CONTROL_SHADER),
		/**
		 * A shader of type TESS_EVALUATION is a shader that is intended to run on the programmable tessellation processor in the evaluation stage.
		 */
		TESS_EVALUATION(GL4.GL_TESS_EVALUATION_SHADER),
		/**
		 * A shader of type GEOMETRY is a shader that is intended to run on the programmable geometry processor. 
		 */
		GEOMETRY(GL4.GL_GEOMETRY_SHADER),
		/**
		 * A shader of type GL_FRAGMENT_SHADER is a shader that is intended to run on the programmable fragment processor.
		 */
		FRAGMENT(GL4.GL_FRAGMENT_SHADER);
		
		int glID;
		
		GLShaderType(int theGLID){
			glID = theGLID;
		}
		
		public static GLShaderType fromGLID(int theGLID){
			switch(theGLID){
			case GL4.GL_VERTEX_SHADER:return VERTEX;
			case GL4.GL_TESS_CONTROL_SHADER:return TESS_CONTROL;
			case GL4.GL_TESS_EVALUATION_SHADER:return TESS_EVALUATION;
			case GL4.GL_GEOMETRY_SHADER:return GEOMETRY;
			case GL4.GL_FRAGMENT_SHADER:return FRAGMENT;
			}
			return null;
		}
		
		public int glID(){
			return glID;
		}
	}
	
	private int _myID;
	private Path[] _mySourcePaths;
	private String[] _mySources;
	private GLShaderType _myType;

	/**
	 * Create a shader object of the given type from the given source files
	 * @param theType type of the shader
	 * @param theSourceFiles sourcefiles to compile
	 */
	public GLShaderObject(GLShaderType theType, Path...theSourcePaths){
		GL4 gl = GLGraphics.currentGL();
		_myID = gl.glCreateShader(theType.glID);
		_myType = theType;
		sourceFiles(theSourcePaths);
		if(!compile()){
			throw new GLException("Error in " + _myType +" shader:" + infoLog());
		}
	}
	
	public GLShaderType type(){
		return _myType;
	}
	
	public int id(){
		return _myID;
	}
	
	private int shaderInfo(int theInfo){
		GL4 gl = GLGraphics.currentGL();
		IntBuffer myResult = IntBuffer.allocate(1);
		gl.glGetShaderiv(_myID, theInfo, myResult);
		return myResult.get(0);
	}
	
	/**
	 * Takes the given files and merges them to one String. 
	 * This method is used to combine the different shader sources and get rid of the includes
	 * inside the shader files.
	 * @param theFiles
	 * @return
	 */
	private String buildSource(final Path...thePaths) {
		StringBuffer myBuffer = new StringBuffer();
		
		for(Path myPath:thePaths) {
			myBuffer.append(CCNIOUtil.loadString(myPath));
			myBuffer.append("\n");
		}
		
		return myBuffer.toString();
	}
	
	/**
	 * Returns the files the sahder sources for this object are loaded from
	 * @return shader files
	 */
	public Path[] sourcePaths(){
		return _mySourcePaths;
	}
	
	/**
	 * Returns the source code this object is build from
	 * @return the source code this object is build from
	 */
	public String[] sources(){
		return _mySources;
	}
	
	/**
	 * Sets the source code in shader to the source code in the array of strings specified by theSources. 
	 * Any source code previously stored in the shader object is completely replaced. OpenGL copies the 
	 * shader source code strings when glShaderSource is called, so an application may free its copy of 
	 * the source code strings immediately after the function returns.
	 * @param theSourceFiles Specifies an array of Files paths containing the source code to be loaded into the shader.
	 */
	public void source(String...theSources){
		_mySources = theSources;
		
		GL4 gl = GLGraphics.currentGL();
		gl.glShaderSource(_myID, theSources.length, theSources, null);
	}
	
	/**
	 * Sets the source code in shader to the source code in the array of strings specified by theSources. 
	 * Any source code previously stored in the shader object is completely replaced. OpenGL copies the 
	 * shader source code strings when glShaderSource is called, so an application may free its copy of 
	 * the source code strings immediately after the function returns.
	 * @param theSourcePaths Specifies an array of Files paths containing the source code to be loaded into the shader.
	 */
	public void sourceFiles(Path...theSourcePaths){
		_mySourcePaths = theSourcePaths;
		String[] mySource = new String[]{buildSource(theSourcePaths)};
		source(mySource);
	}
	
	/**
	 * Compiles the source code strings that have been stored in the shader object specified by shader.
	 * <p>
	 * The compilation status will be stored as part of the shader object's state. This value will be set to 
	 * <code>true</code> if the shader was compiled without errors and is ready for use, and <code>false</code> 
	 * otherwise. It can be queried by calling glGetShader with arguments shader and GL_COMPILE_STATUS.
	 * <p>
	 * Compilation of a shader can fail for a number of reasons as specified by the OpenGL Shading Language 
	 * Specification. Whether or not the compilation was successful, information about the compilation can be 
	 * obtained from the shader object's information log by calling infoLog.
	 */
	public boolean compile(){
		GL4 gl = GLGraphics.currentGL();
		gl.glCompileShader(_myID);
		return shaderInfo(GL4.GL_COMPILE_STATUS) == GL4.GL_TRUE;
	}
	
	/**
	 * Returns the information log for the specified shader object. The information log for a shader object is 
	 * modified when the shader is compiled. The string that is returned will be null terminated.
	 * <p>
	 * The information log for a shader object is a string that may contain diagnostic messages, warning messages, 
	 * and other information about the last compile operation. When a shader object is created, its information log 
	 * will be a string of length 0.
	 * <p>
	 * The information log for a shader object is the OpenGL implementer's primary mechanism for conveying information 
	 * about the compilation process. Therefore, the information log can be helpful to application developers during 
	 * the development process, even when compilation is successful. Application developers should not expect 
	 * different OpenGL implementations to produce identical information logs.
	 * @return information log for a shader object
	 */
	public String infoLog(){
		GL4 gl = GLGraphics.currentGL();

		int length = shaderInfo(GL4.GL_INFO_LOG_LENGTH);
		if (length <= 0) {
			return "";
		}
		ByteBuffer infoLog = CCBufferUtil.newByteBuffer(length);
		gl.glGetShaderInfoLog(_myID, length, null, infoLog);
		byte[] infoBytes = new byte[length];
		infoLog.get(infoBytes);
		return new String(infoBytes);
	}
	
	@Override
	protected void finalize() {
		GL4 gl = GLGraphics.currentGL();
		gl.glDeleteShader(_myID);
	}
}