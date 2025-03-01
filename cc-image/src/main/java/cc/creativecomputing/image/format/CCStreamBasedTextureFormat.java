/*
 * Copyright (c) 2013 christianr.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * Contributors:
 *     christianr - initial API and implementation
 */
package cc.creativecomputing.image.format;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

import cc.creativecomputing.image.CCImage;
import cc.creativecomputing.image.CCImageException;
import cc.creativecomputing.image.CCPixelFormat;
import cc.creativecomputing.image.CCPixelInternalFormat;
import cc.creativecomputing.io.CCNIOUtil;

public abstract class CCStreamBasedTextureFormat implements CCImageFormat {
	
	@Override
	public CCImage createImage(
		final Path theFile, 
		final CCPixelInternalFormat theInternalFormat, final CCPixelFormat thePixelFormat, 
		final String theFileSuffix
	) throws CCImageException {
		try {
			InputStream myInputStream = new BufferedInputStream(new FileInputStream(theFile.toFile()));
			// The SGIImage and TGAImage implementations use InputStreams
			// anyway so there isn't much point in having a separate code
			// path for files
			return createImage(myInputStream, theInternalFormat, thePixelFormat, ((theFileSuffix != null) ? theFileSuffix : CCNIOUtil.fileExtension(theFile)));
		}catch (IOException myE) {
			throw new CCImageException(myE);
		}
	}

	@Override
	public CCImage createImage(
		final URL theUrl, 
		final CCPixelInternalFormat theInternalFormat, final CCPixelFormat thePixelFormat, 
		final String theFileSuffix
	) throws CCImageException {
		try {
			InputStream myInputStream = new BufferedInputStream(theUrl.openStream());
			return createImage(myInputStream, theInternalFormat, thePixelFormat, theFileSuffix);
		}catch (IOException myE) {
			throw new CCImageException(myE);
		}
	}
}
