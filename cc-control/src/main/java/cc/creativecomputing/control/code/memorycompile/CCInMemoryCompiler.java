package cc.creativecomputing.control.code.memorycompile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import cc.creativecomputing.core.logging.CCLog;
import cc.creativecomputing.io.CCNIOUtil;

/**
 * MASSIVELY based on http://javapracs.blogspot.de/2011/06/dynamic-in-memory-compilation-using.html by Rekha Kumari
 * (June 2011)
 */
public class CCInMemoryCompiler {
    
    private final List<CCInMemoryCompilerSourceCode> _myClassSourceCodes;
    private final CCInMemoryFileManager _myFileManager;
    private final JavaCompiler _myCompiler;
    
    
    public CCInMemoryCompiler(){
    	_myCompiler = ToolProvider.getSystemJavaCompiler();
        if (_myCompiler == null) {
            throw new RuntimeException("ToolProvider.getSystemJavaCompiler() returned null! This program needs to be run on a system with an installed JDK.");
        }
    	_myFileManager = new CCInMemoryFileManager(_myCompiler);
        _myClassSourceCodes = new ArrayList<>();
    }

    public CCInMemoryCompiler(final List<CCInMemoryCompilerSourceCode> classSourceCodes) {
    	this();
        _myClassSourceCodes.addAll(classSourceCodes);
    }
    
    private String _myMainClassName = null;
    private CCInMemoryCompilerSourceCode _myMainSource = null;
    
    private void scanSources(Path theFolder){
    	CCLog.info("SCAN");
    	for(Path myPath:CCNIOUtil.list(theFolder)){
        	if(Files.isDirectory(myPath)){
        		scanSources(myPath);
        	}else{
        		if(CCNIOUtil.fileExtension(myPath).equals("java")){
        			_myClassSourceCodes.add(new CCInMemoryCompilerSourceCode(myPath));
        		}
        	}
        	CCLog.info(myPath);
        }
    }
    
    private static String[] SOURCE_FOLDER_LOCATIONS = new String[] {
    		"src/main/java",
    		"src/demo",
    		"src"
    };
    
    private String findSourceFolder(Class<?> theClass) {
    		String myClassName = theClass.getName();
		String myPackageName = myClassName.substring(0, myClassName.lastIndexOf("."));
		String classSimpleName = myClassName.substring(myClassName.lastIndexOf(".") + 1);
		for(String mySourceFolder:SOURCE_FOLDER_LOCATIONS) {
			Path sourcePath = Paths.get(mySourceFolder, myPackageName.split("\\.")).resolve(classSimpleName + ".java");
			if(CCNIOUtil.exists(sourcePath))return mySourceFolder;
		}
		return "";
    }
    
    public CCInMemoryCompiler(Class<?> theMainClass) {
    		this();
    		
    		String mySourceFolder = findSourceFolder(theMainClass);
    		
    		//"src/main/java", 
    		_myMainSource = new CCInMemoryCompilerSourceCode(mySourceFolder,theMainClass);
        scanSources(_myMainSource.sourcePath.getParent());
        _myMainClassName = _myMainSource.className;
    }
    
    public static void main(String[] args) {
		new CCInMemoryCompiler(CCInMemoryCompiler.class);
	}
    
    public boolean needsUpdated(){
    		for (CCInMemoryCompilerSourceCode classSourceCode : _myClassSourceCodes) {
            if(classSourceCode.needsUpdate())return true;
        }
    		return false;
    }

    public CCInMemoryCompilerFeedback compile() {
    	
	    	if(_myMainSource != null){
	    		_myClassSourceCodes.clear();
	            scanSources(_myMainSource.sourcePath.getParent());
	    	}
	    	_myFileManager.reset();
    	
        final List<JavaFileObject> myFiles = new ArrayList<>();
        for (CCInMemoryCompilerSourceCode classSourceCode : _myClassSourceCodes) {
            JavaFileObject myFileObject = classSourceCode.fileObject();
            myFiles.add(myFileObject);
        }

        if (myFiles.size() <= 0) return null;

        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        
        final JavaCompiler.CompilationTask myCompileTask = _myCompiler.getTask(null, _myFileManager, diagnostics, null, null, myFiles);
        return new CCInMemoryCompilerFeedback(myCompileTask.call(), diagnostics);
       
    }
    
    public Class<?> getCompiledMainClass() throws ClassNotFoundException{
	    	if(_myMainClassName == null)return null;
	    	return getCompiledClass(_myMainClassName);
    }

    public Class<?> getCompiledClass(final String theClassName) throws ClassNotFoundException {
        final Class<?> myResult =  _myFileManager.getClassLoader(null).loadClass(theClassName);
        if (myResult == null) {
        		CCLog.info(theClassName);
            throw new ClassNotFoundException("Class returned by ClassLoader was null!");
        }
        return myResult;
    }
}