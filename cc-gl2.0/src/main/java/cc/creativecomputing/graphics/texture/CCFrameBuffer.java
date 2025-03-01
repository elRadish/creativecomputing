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
package cc.creativecomputing.graphics.texture;

import java.nio.FloatBuffer;

import cc.creativecomputing.graphics.CCGraphics;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2GL3;

/**
 * 
 FramebufferObject Class. This class encapsulates the FramebufferObject (FBO) OpenGL spec. See the official spec at:
 * http://oss.sgi.com/projects/ogl-sample/registry/EXT/framebuffer_object.txt
 * 
 * for details.
 * <p>
 * A framebuffer object (FBO) is conceptually a structure containing pointers to GPU memory. The memory pointed to is
 * either an OpenGL texture or an OpenGL RenderBuffer. FBOs can be used to render to one or more textures, share depth
 * buffers between multiple sets of color buffers/textures and are a complete replacement for pbuffers.
 * <p>
 * Performance Notes:
 * <ol>
 * <li>It is more efficient (but not required) to call Bind() on an FBO before making multiple method calls. For
 * example:
 * 
 * <pre>
 * FramebufferObject fbo;
 * fbo.Bind();
 * fbo.AttachTexture(GL_TEXTURE_2D, texId0, GL_COLOR_ATTACHMENT0);
 * fbo.AttachTexture(GL_TEXTURE_2D, texId1, GL_COLOR_ATTACHMENT1);
 * fbo.IsValid();
 * </pre>
 * 
 * To provide a complete encapsulation, the following usage pattern works correctly but is less efficient:
 * 
 * <pre>
 * FramebufferObject fbo;
 * // NOTE : No Bind() call
 * fbo.AttachTexture(GL_TEXTURE_2D, texId0, GL_COLOR_ATTACHMENT0);
 * fbo.AttachTexture(GL_TEXTURE_2D, texId1, GL_COLOR_ATTACHMENT1);
 * fbo.IsValid();
 * </pre>
 * 
 * The first usage pattern binds the FBO only once, whereas the second usage binds/unbinds the FBO for each method call.
 * </li>
 * <p>
 * <li>Use FramebufferObject::Disable() sparingly. We have intentionally left out an "Unbind()" method because it is
 * largely unnecessary and encourages rendundant Bind/Unbind coding. Binding an FBO is usually much faster than
 * enabling/disabling a pbuffer, but is still a costly operation. When switching between multiple FBOs and a visible
 * OpenGL framebuffer, the following usage pattern is recommended:
 * 
 * <pre>
 * FramebufferObject fbo1, fbo2;
 * fbo1.Bind();
 * ... Render ...
 * // NOTE : No Unbind/Disable here...
 * 
 * fbo2.Bind();
 * ... Render ...
 * 
 * // Disable FBO rendering and return to visible window
 * // OpenGL framebuffer.
 * framebufferObject::Disable();
 * </pre>
 * 
 * </li>
 * </ol>
 * 
 * @author christianriekoff
 * 
 */
class CCFrameBuffer {

	private int[] _myID;
	private int[] _mySavedId;

	// / Ctor/Dtor
	public CCFrameBuffer() {
		_myID = generateFboId();
		_mySavedId = new int[] { 0 };

		// Bind this FBO so that it actually gets created now
		guardedBind();
		guardedUnbind();
	}

	@Override
	public void finalize() {
		CCGraphics.currentGL().glDeleteFramebuffers(1, _myID, 0);
	}

	/**
	 * Bind this FBO as current render target
	 */
	public void bind() {
		CCGraphics.currentGL().glBindFramebuffer(GL2GL3.GL_FRAMEBUFFER, _myID[0]);
	}

	/**
	 * Bind a texture to the "attachment" point of this FBO
	 * @param texTarget
	 * @param texId
	 * @param attachment
	 * @param mipLevel
	 * @param zSlice
	 */
	public void attachTexture(int texTarget, int texId, int attachment, int mipLevel, int zSlice) {
		guardedBind();
		framebufferTextureND(attachment, texTarget, texId, mipLevel, zSlice);
		guardedUnbind();
	}

	public void attachTexture(int texTarget, int texId, int attachment, int mipLevel) {
		attachTexture(texTarget, texId, attachment, mipLevel, 0);
	}

	public void attachTexture(int texTarget, int texId, int attachment) {
		attachTexture(texTarget, texId, attachment, 0);
	}

	public void attachTexture(int texTarget, int texId) {
		attachTexture(texTarget, texId, GL2.GL_COLOR_ATTACHMENT0);
	}
	
	public void data(FloatBuffer theData, CCTexture theTexture, int attachment, int mipLevel, int zSlice) {
		guardedBind();
		framebufferTextureND(attachment, theTexture.target().glID, theTexture.id(), mipLevel, zSlice);
		CCGraphics.currentGL().glReadPixels(0, 0, theTexture.width(), theTexture.height(), theTexture.format().glID,  GL2.GL_FLOAT, theData); 
		guardedUnbind();
	}
	
	public FloatBuffer data(CCTexture theTexture, int attachment, int mipLevel, int zSlice) {
		FloatBuffer myResult = FloatBuffer.allocate(
				theTexture.width() * 
				theTexture.height() * 
				theTexture.format().numberOfChannels
		);
		data(myResult, theTexture, attachment, mipLevel, zSlice);
		myResult.rewind();
		return myResult;
	}

	/**
	 * Bind an array of textures to multiple "attachment" points of this FBO.
	 * By default, the first 'numTextures' attachments are used, starting with GL_COLOR_ATTACHMENT0
	 * @param numTextures
	 * @param texTarget
	 * @param texId
	 * @param attachment
	 * @param mipLevel
	 * @param zSlice
	 */
	public void attachTextures(int numTextures, int[] texTarget, int[] texId, int[] attachment, int[] mipLevel, int[] zSlice) {
		for (int i = 0; i < numTextures; ++i) {
			attachTexture(texTarget[i], texId[i], attachment != null ? attachment[i] : (GL2.GL_COLOR_ATTACHMENT0 + i), mipLevel != null ? mipLevel[i] : 0,
					zSlice != null ? zSlice[i] : 0);
		}
	}

	public void attachTextures(int numTextures, int texTarget[], int texId[], int attachment[], int mipLevel[]) {
		attachTextures(numTextures, texTarget, texId, attachment, mipLevel, null);
	}

	public void attachTextures(int numTextures, int texTarget[], int texId[], int attachment[]) {
		attachTextures(numTextures, texTarget, texId, attachment, null);
	}

	public void attachTextures(int numTextures, int texTarget[], int texId[]) {
		attachTextures(numTextures, texTarget, texId, null);
	}

	/**
	 * Bind a render buffer to the attachment point of this FBO
	 * @param theBufferID
	 * @param theAttachment
	 */
	public void AttachRenderBuffer(int theBufferID, int theAttachment) {
		guardedBind();

		CCGraphics.currentGL().glFramebufferRenderbuffer(GL2.GL_FRAMEBUFFER, theAttachment, GL2.GL_RENDERBUFFER, theBufferID);

		guardedUnbind();
	}

	public void attachRenderBuffer(int buffId) {
		AttachRenderBuffer(buffId, GL2.GL_COLOR_ATTACHMENT0);
	}
	
	/**
	 * Bind an array of render buffers to corresponding attachment points of this FBO.
	 * By default, the first 'numBuffers' attachments are used, starting with GL_COLOR_ATTACHMENT0
	 * @param numBuffers
	 * @param theBufferId
	 * @param theAttachments
	 */
	public void attachRenderBuffers(int numBuffers, int theBufferId[], int theAttachments[]) {
		for (int i = 0; i < numBuffers; ++i) {
			AttachRenderBuffer(theBufferId[i], theAttachments != null ? theAttachments[i] : (GL2.GL_COLOR_ATTACHMENT0 + i));
		}
	}

	public void attachRenderBuffers(int numBuffers, int buffId[]) {
		attachRenderBuffers(numBuffers, buffId, null);
	}

	/**
	 * Free any resource bound to the "attachment" point of this FBO
	 * @param theAttachment
	 */
	public void unattach(int theAttachment) {
		guardedBind();
		int type = attachedType(theAttachment);

		switch (type) {
		case GL2.GL_NONE:
			break;
		case GL2.GL_RENDERBUFFER:
			AttachRenderBuffer(0, theAttachment);
			break;
		case GL2.GL_TEXTURE:
			attachTexture(GL2.GL_TEXTURE_2D, 0, theAttachment);
			break;
		default:
			// cerr << "FramebufferObject::unbind_attachment ERROR: Unknown attached resource type\n";
		}
		guardedUnbind();
	}

	/**
	 * Free any resources bound to any attachment points of this FBO
	 */
	public void unattachAll() {
		int numAttachments = maxColorAttachments();
		for (int i = 0; i < numAttachments; ++i) {
			unattach(GL2.GL_COLOR_ATTACHMENT0 + i);
		}
	}

	/**
	 * Get the FBO ID
	 * @return
	 */
	public int id() {
		return _myID[0];
	}

	/**
	 * Is attached type GL_RENDERBUFFER or GL_TEXTURE? Returns GL_RENDERBUFFER or GL_TEXTURE
	 * 
	 * @param attachment
	 * @return
	 */
	public int attachedType(int attachment) {
		guardedBind();
		int[] type = new int[] { 0 };
		CCGraphics.currentGL().glGetFramebufferAttachmentParameteriv(GL2.GL_FRAMEBUFFER, attachment, GL2.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE, type, 0);
		guardedUnbind();
		return type[0];
	}

	/**
	 * What is the Id of Renderbuffer/texture currently attached to attachment?
	 * @param theAttachment
	 * @return
	 */
	public int attachedId(int theAttachment) {
		guardedBind();
		int[] id = new int[] { 0 };
		CCGraphics.currentGL().glGetFramebufferAttachmentParameteriv(GL2.GL_FRAMEBUFFER, theAttachment, GL2.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME, id, 0);
		guardedUnbind();
		return id[0];
	}

	/**
	 * Which mipmap level is currently attached to attachment?
	 * @param theAttachment
	 * @return
	 */
	public int GetAttachedMipLevel(int theAttachment) {
		guardedBind();
		int[] level = new int[] { 0 };
		CCGraphics.currentGL().glGetFramebufferAttachmentParameteriv(GL2.GL_FRAMEBUFFER, theAttachment, GL2.GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL, level, 0);
		guardedUnbind();
		return level[0];
	}

	/**
	 * Which cube face is currently attached to attachment?
	 * @param theAttachment
	 * @return
	 */
	public int attachedCubeFace(int theAttachment) {
		guardedBind();
		int[] level = new int[] { 0 };
		CCGraphics.currentGL().glGetFramebufferAttachmentParameteriv(GL2.GL_FRAMEBUFFER, theAttachment, GL2.GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE, level, 0);
		guardedUnbind();
		return level[0];
	}

	// / Which z-slice is currently attached to "attachment?"
	// public int GetAttachedZSlice( int attachment ) {
	// _GuardedBind();
	// int[] slice = new int[] {0};
	// CCGraphics.currentGL().glGetFramebufferAttachmentParameterivEXT(GL2.GL_FRAMEBUFFER, attachment,
	// GL2ES2.GL_FRAMEBUFFER_ATTACHMENT,
	// slice,0);
	// _GuardedUnbind();
	// return slice;
	// }

	public static int maxColorAttachments() {
		int[] maxAttach = new int[] { 0 };
		CCGraphics.currentGL().glGetIntegerv(GL2.GL_MAX_COLOR_ATTACHMENTS, maxAttach, 0);
		return maxAttach[0];
	}

	/**
	 * Disable all FBO rendering and return to traditional, windowing-system controlled framebuffer 
	 * <p>
	 * NOTE: This is NOT an
	 * "unbind" for this specific FBO, but rather disables all FBO rendering. This call is intentionally "static" and
	 * named "Disable" instead of "Unbind" for this reason. The motivation for this strange semantic is performance.
	 * Providing "Unbind" would likely lead to a large number of unnecessary FBO enabling / disabling.
	 */
	public static void disable() {
		CCGraphics.currentGL().glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
	}

	/**
	 * Only binds if m_fboId is different than the currently bound FBO
	 */
	protected void guardedBind() {

		CCGraphics.currentGL().glGetIntegerv(GL2.GL_FRAMEBUFFER_BINDING, _mySavedId, 0);
		if (_myID[0] != _mySavedId[0]) {
			CCGraphics.currentGL().glBindFramebuffer(GL2.GL_FRAMEBUFFER, _myID[0]);
		}
	}

	/**
	 * Returns FBO binding to the previously enabled FBO
	 */
	protected void guardedUnbind() {
		if (_myID[0] != _mySavedId[0]) {
			CCGraphics.currentGL().glBindFramebuffer(GL2.GL_FRAMEBUFFER, _mySavedId[0]);
		}
	}

	protected void framebufferTextureND(int theAttachment, int texTarget, int texId, int mipLevel, int zSlice) {
		if (texTarget == GL2.GL_TEXTURE_1D) {
			CCGraphics.currentGL().glFramebufferTexture1D(GL2.GL_FRAMEBUFFER, theAttachment, GL2.GL_TEXTURE_1D, texId, mipLevel);
		} else if (texTarget == GL2.GL_TEXTURE_3D) {
			CCGraphics.currentGL().glFramebufferTexture3D(GL2.GL_FRAMEBUFFER, theAttachment, GL2.GL_TEXTURE_3D, texId, mipLevel, zSlice);
		} else {
			// Default is GL_TEXTURE_2D, GL_TEXTURE_RECTANGLE_ARB, or cube faces
			CCGraphics.currentGL().glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, theAttachment, texTarget, texId, mipLevel);
		}
	}

	protected static int[] generateFboId() {
		int[] id = new int[] {0};
		CCGraphics.currentGL().glGenFramebuffers(1, id, 0);
		return id;
	}

}
