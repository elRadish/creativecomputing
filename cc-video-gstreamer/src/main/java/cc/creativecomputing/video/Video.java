/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2011-12 Ben Fry and Casey Reas

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
*/

package cc.creativecomputing.video;

import org.freedesktop.gstreamer.*;

import cc.creativecomputing.core.CCSystem;
import cc.creativecomputing.core.CCSystem.CCOS;
import cc.creativecomputing.core.logging.CCLog;

import java.io.File;
import java.nio.ByteOrder;
import java.util.List;

/**
 * This class contains some basic functions used by the rest of the classes in
 * this library.
 */
public class Video {
	// Path that the video library will use to load the GStreamer base libraries
	// and plugins from. They can be passed from the application using the
	// gstreamer.library.path and gstreamer.plugin.path system variables (see
	// comments in initImpl() below).
	protected static String gstreamerLibPath = "";
	protected static String gstreamerPluginPath = "";

	// Direct buffer pass enabled by default. With this mode enabled, no new
	// buffers are created and disposed by the GC in each frame (thanks to Octavi
	// Estape for suggesting this improvement) which should help performance in
	// most situations.
	protected static boolean passDirectBuffer = true;

	// OpenGL texture used as buffer sink by default, when the renderer is
	// GL-based. This can improve performance significantly, since the video
	// frames are automatically copied into the texture without passing through
	// the pixels arrays, as well as having the color conversion into RGBA handled
	// natively by GStreamer.
	protected static boolean useGLBufferSink = true;

	protected static boolean defaultGLibContext = false;

	protected static long INSTANCES_COUNT = 0;

	protected static int bitsJVM;
	static {
		bitsJVM = Integer.parseInt(System.getProperty("sun.arch.data.model"));
	}

	static protected void init() {
		try {
		if (INSTANCES_COUNT == 0) {
			initImpl();
		}
		}catch(Exception e) {
			e.printStackTrace();
		}
		INSTANCES_COUNT++;
	}

	static protected void restart() {
		removePlugins();
		Gst.deinit();
		initImpl();
	}
	
	public static void main(String[] args) {
		init();
	}

	static protected void initImpl() {
		// The location of the GStreamer base libraries can be passed from the
		// application to the vide library via a system variable. In Eclipse, add to
		// "VM Arguments" in "Run Configurations" the following line:
		// -Dgstreamer.library.path=path
		String libPath = GStreamerSettings.getDefaultLibraryPath();
		if (libPath != null) {
			gstreamerLibPath = libPath;

			// If the GStreamer installation referred by gstreamer.library.path is not
			// a system installation, then the path containing the plugins needs to be
			// specified separately, otherwise the plugins will be automatically
			// loaded from the default location. The system property for the plugin
			// path is "gstreamer.plugin.path"
			String pluginPath = System.getProperty("gstreamer.plugin.path");
			if (pluginPath != null) {
				gstreamerPluginPath = pluginPath;
			}
		} else {
			// Paths are build automatically from the current location of the video
			// library.
			if (CCSystem.os == CCOS.LINUX) {
				buildLinuxPaths();
			} else if (CCSystem.os == CCOS.WINDOWS) {
				buildWindowsPaths();
			} else if (CCSystem.os == CCOS.MACOSX) {
				buildMacOSXPaths();
			}
		}

		CCLog.info(gstreamerLibPath);
		if (!gstreamerLibPath.equals("")) {
			System.setProperty("jna.library.path", gstreamerLibPath);
		}
		// outputs the paths JNA is trying
		// System.setProperty("jna.debug_load", "true");

		if (CCSystem.os == CCOS.WINDOWS) {
			LibraryLoader loader = LibraryLoader.getInstance();
			if (loader == null) {
				System.err.println("Cannot load local version of GStreamer libraries.");
			}
		}

		// disable the use of gst-plugin-scanner on environments where we're
		// not using the host system's installation of GStreamer
		// the problem with gst-plugin-scanner is that the library expects it
		// to exist at a specific location determinated at build time
		if (CCSystem.os != CCOS.LINUX) {
			Environment.libc.setenv("GST_REGISTRY_FORK", "no", 1);
		}

		// prevent globally installed libraries from being used on platforms
		// where we ship GStreamer
		if (CCSystem.os == CCOS.WINDOWS || CCSystem.os == CCOS.MACOSX) {
			Environment.libc.setenv("GST_PLUGIN_SYSTEM_PATH_1_0", "", 1);
		}

		String[] args = { "" };
		Gst.setUseDefaultContext(defaultGLibContext);
		Gst.init("Processing core video", args);

		// instead of setting the plugin path via scanPath(), we could alternatively
		// also set the GST_PLUGIN_PATH_1_0 environment variable
//		addPlugins();
	}

	static protected void addPlugins() {
		if (!gstreamerPluginPath.equals("")) {
			Registry reg = Registry.get();
			boolean res;
			res = reg.scanPath(gstreamerPluginPath);
			if (!res) {
				System.err.println("Cannot load GStreamer plugins from " + gstreamerPluginPath);
			}
		}
	}

	static protected void removePlugins() {
		Registry reg = Registry.get();
		List<Plugin> list = reg.getPluginList();
		for (Plugin plg : list) {
			reg.removePlugin(plg);
		}
	}

	static protected void buildLinuxPaths() {
		// JNA automatically tries all library paths known to the host system's
		// ldconfig, so we'd even catch locations like /usr/local/lib etc
		gstreamerLibPath = "";
		gstreamerPluginPath = "";
	}

	static protected void buildWindowsPaths() {
		LibraryPath libPath = new LibraryPath();
		String path = libPath.get();
		gstreamerLibPath = buildGStreamerLibPath(path, "windows" + bitsJVM);
		gstreamerPluginPath = gstreamerLibPath + "\\gstreamer-1.0";
	}

	static protected void buildMacOSXPaths() {
		LibraryPath libPath = new LibraryPath();
		String path = libPath.get();
		gstreamerLibPath = buildGStreamerLibPath(path, "macosx" + bitsJVM);
		gstreamerPluginPath = gstreamerLibPath + "/gstreamer-1.0";
	}

	static protected String buildGStreamerLibPath(String base, String os) {
		File path = new File(base + os);
		if (path.exists()) {
			return base + os;
		} else {
			return base;
		}
	}

	static protected double nanoSecToSecFrac(double nanosec) {
		for (int i = 0; i < 3; i++)
			nanosec /= 1E3;
		return nanosec;
	}

	static protected long secToNanoLong(double sec) {
		Double f = Double.valueOf(sec * 1E9);
		return f.longValue();
	}

	/**
	 * Reorders an OpenGL pixel array (RGBA) into ARGB. The array must be of size
	 * width * height.
	 * 
	 * @param pixels int[]
	 */
	static protected void convertToARGB(int[] pixels, int width, int height) {
		int t = 0;
		int p = 0;
		if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
			// RGBA to ARGB conversion: shifting RGB 8 bits to the right,
			// and placing A 24 bits to the left.
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int pixel = pixels[p++];
					pixels[t++] = (pixel >>> 8) | ((pixel << 24) & 0xFF000000);
				}
			}
		} else {
			// We have to convert ABGR into ARGB, so R and B must be swapped,
			// A and G just brought back in.
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int pixel = pixels[p++];
					pixels[t++] = ((pixel & 0xFF) << 16) | ((pixel & 0xFF0000) >> 16) | (pixel & 0xFF00FF00);
				}
			}
		}
	}
}
