package cc.creativecomputing.realsense;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;

import org.librealsense.Config;
import org.librealsense.Context;
import org.librealsense.Device;
import org.librealsense.DeviceList;
import org.librealsense.Frame;
import org.librealsense.FrameList;
import org.librealsense.Intrinsics;
import org.librealsense.Native;
import org.librealsense.Native.Option;
import org.librealsense.Pipeline;
import org.librealsense.PipelineProfile;

import cc.creativecomputing.app.modules.CCAnimator;
import cc.creativecomputing.core.CCProperty;
import cc.creativecomputing.core.logging.CCLog;
import cc.creativecomputing.image.CCImage;
import cc.creativecomputing.image.CCPixelFormat;
import cc.creativecomputing.image.CCPixelInternalFormat;
import cc.creativecomputing.image.CCPixelType;
import cc.creativecomputing.io.CCFileInputChannel;
import cc.creativecomputing.io.CCFileOutputChannel;
import cc.creativecomputing.io.CCNIOUtil;
import cc.creativecomputing.math.CCVector2;

/**
 * Intel RealSense Camera
 */
public class CCRealSense {

	// realsense
	private Context context;
	private Pipeline pipeline;

	private final int depthStreamIndex = 0;

	// camera
	volatile private boolean running = false;

	public int width;
	public int height;
	private int fps;

	private CCImage depthImage;
	private CCImage lastdepthImage;
	
	private Thread _myThread;
	
	/**
	 * Focal length of the image plane, as a multiple of pixel width and height
	 */
	public CCVector2 depthFocalLength = new CCVector2();
	
	/**
	 * Coordinate of the principal point of the image, as a pixel offset from the left top edge
	 */
	public CCVector2 depthOffset = new CCVector2();
	
	public double depthScale = 0.001;

	public boolean savePointCloud = false;

	/**
	 * Create a new Intel RealSense camera.
	 * 
	 * @param parent Parent processing sketch.
	 */
	public CCRealSense() {

		// load native libs
		// loadNativeLibraries();

		// create context
		context = Context.create();

		_myThread = new Thread(()->{
			while(running) {
				readFrames();
			}
		}); 
	}
	
	private CCFileInputChannel _myInputChannel = null;
	
	private long _myFrameSize;
	private double _myFrame;
	@CCProperty(name = "speed")
	private double _mySpeed = 30;
	private long _myNumberOfFrames;
	
	public CCRealSense(Path thePath, int theWidth, int theHeight) {
		_myInputChannel = new CCFileInputChannel(thePath);
		width = theWidth;
		height = theHeight;

		// create images
		depthImage = new CCImage(width, height, CCPixelInternalFormat.LUMINANCE16, CCPixelFormat.LUMINANCE, CCPixelType.UNSIGNED_SHORT);
		lastdepthImage = new CCImage(width, height, CCPixelInternalFormat.LUMINANCE16, CCPixelFormat.LUMINANCE, CCPixelType.UNSIGNED_SHORT);
		
		_myFrameSize = width * height * 2;
		
		_myFrame = 0;
		_myNumberOfFrames = _myInputChannel.size() / _myFrameSize;
		
		
	}
	
	public void start() {
		start(1280,720,30);
	}

	/**
	 * Start the camera.
	 * 
	 * @param width             Width of the camera image.
	 * @param height            Height of the camera image.
	 * @param fps               Frames per second to capture.
	 * @param enableDepthStream True if depth stream should be enabled.
	 * @param enableColorStream True if color stream should be enabled.
	 */
	public void start(int width, int height, int fps) {
		
		DeviceList deviceList = context.queryDevices();
		List<Device> devices = deviceList.getDevices();
		for (Device device : devices) {
		
			CCLog.info(device.name());
		}
		if (devices.isEmpty())
			CCLog.info("RealSense: No device found!");
		start(devices.get(0), width, height, fps);
		
	}
	
	public CCImage depthImage() {
		return depthImage;
	}
	public CCImage lastDepthImage() {
		return lastdepthImage;
	}

	/**
	 * Start the camera.
	 * 
	 * @param theDevice            Intel RealSense device to start.
	 * @param theWidth             Width of the camera image.
	 * @param theHeight            Height of the camera image.
	 * @param theFPS               Frames per second to capture.
	 * @param enableDepthStream True if depth stream should be enabled.
	 * @param enableColorStream True if color stream should be enabled.
	 */
	public void start(Device theDevice, int theWidth, int theHeight, int theFPS) {
		if (running)
			return;

		// set configuration
		width = theWidth;
		height = theHeight;
		fps = theFPS;

		// create images
		depthImage = new CCImage(width, height, CCPixelInternalFormat.LUMINANCE16, CCPixelFormat.LUMINANCE, CCPixelType.UNSIGNED_SHORT);
		lastdepthImage = new CCImage(width, height, CCPixelInternalFormat.LUMINANCE16, CCPixelFormat.LUMINANCE, CCPixelType.UNSIGNED_SHORT);

		// create pipeline
		pipeline = context.createPipeline();

		Config config = Config.create();
		
		config.enableDevice(theDevice);
		
		config.enableStream(
			Native.Stream.RS2_STREAM_DEPTH, 
			depthStreamIndex, 
			width, 
			height,
			Native.Format.RS2_FORMAT_Z16, 
			fps
		);

		// start pipeline
		PipelineProfile myProfile = pipeline.startWithConfig(config);
		Intrinsics myIntrinsics = myProfile.getStreams().get(0).getVideoStreamIntrinsics();
		
		depthFocalLength.x = myIntrinsics.fx;
		depthFocalLength.y = myIntrinsics.fy;
		
		depthOffset.x = myIntrinsics.ppx;
		depthOffset.y = myIntrinsics.ppy;
		
		CCLog.info(depthFocalLength, depthOffset);
		
		running = true;
		_myThread.start();
	}
	
	public void update(CCAnimator theAnimator) {
		if(_myInputChannel == null)return;
		ByteBuffer myReadBuffer = ByteBuffer.allocateDirect((int)_myFrameSize);
		_myInputChannel.read((long)_myFrame * _myFrameSize, myReadBuffer); 
		myReadBuffer.rewind();
		
		CCImage tmp = depthImage;
		depthImage = lastdepthImage;
		lastdepthImage = tmp;
		depthImage.buffer(myReadBuffer.asShortBuffer());
		
		_myFrame += theAnimator.deltaTime() * _mySpeed;
		_myFrame %= _myNumberOfFrames;
	}

	/**
	 * Read the camera frame buffers for all active streams.
	 */
	public void readFrames() {
		FrameList frames = pipeline.waitForFrames(5000);

		for (int i = 0; i < frames.frameCount(); i++) {
			Frame frame = frames.frame(i);
			readDepthBuffer(frame);
			frame.release();
		}

		frames.release();
	}

	/**
	 * Returns true if a device is available.
	 * 
	 * @return True if device is available.
	 */
	public boolean isCameraAvailable() {
		DeviceList deviceList = context.queryDevices();
		return deviceList.getDeviceCount() > 0;
	}

	/**
	 * Stop the camera.
	 */
	public void stop() {

		if (!running)
			return;

		// clean up

		// set states
		running = false;
		
		
	}

	/**
	 * Creates grayscale depth image from depth buffer (accessible through
	 * getDepthImage()).
	 * 
	 * @param minDepth Minimum depth value which translates to white.
	 * @param maxDepth Maximum depth value which translates to black.
	 */
	public void createDepthImage(int minDepth, int maxDepth) {
		// depthImage.loadPixels();

//        for (int i = 0; i < width * height; i++)
//        {
//            int grayScale = (int) PApplet.map(depthBuffer[i] & 0xFFFF, minDepth, maxDepth, 255, 0);
//            grayScale = PApplet.constrain(grayScale, MIN_DEPTH, MAX_DEPTH);
//
//            if (depthBuffer[i] > 0)
//                depthImage.pixels[i] = toColor(grayScale);
//            else
//                depthImage.pixels[i] = toColor(0);
//        }
//
//        depthImage.updatePixels();
	}
	

	private CCFileOutputChannel _myFileChannel = null;
	
	@CCProperty(name = "recording Path")
	private String _myRecordingPath = "exports/realsense01.byt";
	
	
	@CCProperty(name = "start record")
	public void startRecord() {
		_myFileChannel = new CCFileOutputChannel(CCNIOUtil.appPath(_myRecordingPath));
	}
	
	@CCProperty(name = "end record")
	public void endRecord() {
		_myFileChannel.close();
		_myFileChannel = null;
	}

	private void readDepthBuffer(Frame frame) {
		CCImage tmp = depthImage;
		depthImage = lastdepthImage;
		lastdepthImage = tmp;
		
		depthImage.buffer(frame.getFrameData().asShortBuffer());

		if(_myFileChannel != null)_myFileChannel.write(frame.getFrameData());
	}

	

	

//    private void loadNativeLibraries()
//    {
//        String os = System.getProperty("os.name").toLowerCase();
//        String libPath = ProcessingUtils.getLibPath(parent);
//
//        if (os.contains("win")) {
//            Native.loadNativeLibraries(Paths.get(libPath,"native/windows-x64").toString());
//        } else if (os.contains("mac")) {
//            Native.loadNativeLibraries(Paths.get(libPath,"native/osx-x64").toString());
//        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
//            Native.loadNativeLibraries(Paths.get(libPath,"native/linux-64").toString());
//        } else {
//            // Operating System not supported!
//            PApplet.println("RealSense: Load the native libraries by your own.");
//        }
//    }

	/**
	 * Check if the camera is already running.
	 * 
	 * @return True if the camera is already running.
	 */
	public boolean isRunning() {
		return running;
	}

	public static void main(String[] args) {
		new CCRealSense();
	}
}