/*
   The PeasyCam Processing library, which provides an easy-peasy
   camera for 3D sketching.
  
   Copyright 2008 Jonathan Feinberg

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package cc.creativecomputing.graphics.camera;

import cc.creativecomputing.animation.CCAnimation;
import cc.creativecomputing.animation.CCAnimationManager;
import cc.creativecomputing.app.modules.CCAnimator;
import cc.creativecomputing.app.modules.CCAnimatorAdapter;
import cc.creativecomputing.core.CCProperty;
import cc.creativecomputing.core.logging.CCLog;
import cc.creativecomputing.gl.app.CCGLListener;
import cc.creativecomputing.gl.app.events.CCKeyAdapter;
import cc.creativecomputing.gl.app.events.CCKeyEvent;
import cc.creativecomputing.gl.app.events.CCMouseAdapter;
import cc.creativecomputing.gl.app.events.CCMouseEvent;
import cc.creativecomputing.gl.app.events.CCMouseEvent.CCMouseButton;
import cc.creativecomputing.gl.app.events.CCMouseWheelEvent;
import cc.creativecomputing.gl.app.events.CCMouseWheelListener;
import cc.creativecomputing.graphics.CCCamera;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.CCViewport;
import cc.creativecomputing.graphics.app.CCGL2Adapter;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCQuaternion;
import cc.creativecomputing.math.CCVector3;
import cc.creativecomputing.math.interpolate.CCInterpolator;

/**
 * 
 * @author Jonathan Feinberg
 */
public class CCCameraController {
	
	private interface CCCameraMouseDragHandler {
		void handleDrag(final double theMoveX, final double theMoveY, double theMouseX, double theMouseY);
	}
	
	private abstract class CCDampedAction extends CCAnimatorAdapter{
		private double _myVelocity;

		public CCDampedAction() {
			this(0.16);
		}

		public CCDampedAction(final double theFriction) {
			_myVelocity = 0;
			_myApp.animator().listener().add(this);
		}

		public void impulse(final double theImpulse) {
			_myVelocity += theImpulse;
		}

		public void update(CCAnimator theAnimator) {
			if (_myVelocity == 0) {
				return;
			}
			behave(_myVelocity);
//			feed();
			_myVelocity *= 1 - _myFriction;
			if (CCMath.abs(_myVelocity) < .001f) {
				_myVelocity = 0;
			}
		}

		public void stop() {
			_myVelocity = 0;
		}

		abstract protected void behave(final double theVelocity);
	}
	
	public static class CCCameraState  {
		@CCProperty(name = "rotation", readBack = true)
		private CCQuaternion _myRotation;
		@CCProperty(name = "center", readBack = true)
		private CCVector3 _myCenter;
		@CCProperty(name = "distance", readBack = true)
		private double _myDistance;

		public CCCameraState(final CCQuaternion theRotation, final CCVector3 theCenter, final double theDistance) {
			_myRotation = theRotation;
			_myCenter = theCenter;
			_myDistance = theDistance;
		}
		
		public CCCameraState() {
			_myCenter = new CCVector3();
			_myDistance = 100;
			_myRotation = new CCQuaternion();
		}
		
		public void set(CCCameraState theState) {
			_myRotation.set(theState._myRotation);
			_myCenter.set(theState._myCenter);
			_myDistance = theState._myDistance;
		}

		public void set(CCCameraController theCameraController) {
			_myRotation.set(theCameraController._myRotation);
			_myCenter.set(theCameraController._myCenter);
			_myDistance = theCameraController._myDistance;
		}
		
		public double distance() {
			return _myDistance;
		}
		
		public void distance(double theDistance) {
			 _myDistance = theDistance;
		}
		
		public CCVector3 center() {
			return _myCenter;
		}
		
		public CCQuaternion rotation() {
			return _myRotation;
		}
	}
	
	private static final CCVector3 LOOK = CCVector3.UNIT_Z;
	private static final CCVector3 UP = CCVector3.UNIT_Y;

	private final CCGraphics g;
	private final CCGL2Adapter _myApp;

	private final double _myStartDistance;
	private final CCVector3 _myStartCenter;

	@CCProperty(name = "reset on double click")
	private boolean _myResetOnDoubleClick = true;
	private double _myMaximumDistance = Double.MAX_VALUE;

	private final CCDampedAction _myRotateXAction;
	private final CCDampedAction _myRotateYAction;
	private final CCDampedAction _myRotateZAction;
	
	private final CCDampedAction _myDampedZoom;
	private final CCDampedAction _myDampedPanX;
	private final CCDampedAction _myDampedPanY;

	@CCProperty(name = "distance", min = 1, max = 20000, readBack = true)
	private double _myDistance;
	@CCProperty(name = "center", min = -10000, max = 10000, readBack = true)
	private CCVector3 _myCenter;
	@CCProperty(name = "rotation", min = -1, max = 1, readBack = true)
	private CCQuaternion _myRotation;
	@CCProperty(name = "fov", min = 0, max = 90, readBack = true)
	private double _myFoV = 45;
	
	@CCProperty(name = "friction", min = 0, max = 1)
	private double _myFriction = 0.16;

	private CCCameraRotationMode _myDragConstraint = CCCameraRotationMode.FREE;
	

	public static enum CCCameraRotationMode {
		/**
		 * Permit arbitrary rotation. (Default mode.)
		 */
		FREE, 
		/**
		 * Only permit yaw.
		 */
		YAW, 
		/**
		 * Only permit pitch.
		 */
		PITCH, 
		/**
		 * Only permit roll.
		 */
		ROLL, 
		/**
		 * Only suppress roll.
		 */
		SUPPRESS_ROLL
	}
	
	@CCProperty(name = "rotation mode", readBack = true)
	private CCCameraRotationMode _cRotationMode = CCCameraRotationMode.FREE;
	@CCProperty(name = "pan", readBack = true)
	private boolean _cPan = true;
	@CCProperty(name = "zoom", readBack = true)
	private boolean _cZoom = true;
	@CCProperty(name = "invert")
	private boolean _cInvert = false;
	@CCProperty(name = "always rotate")
	private boolean _cAlwaysRotate = false;
	
	public void rotationMode(CCCameraRotationMode theRotationMode) {
		_cRotationMode = theRotationMode;
	}
	public void pan(boolean thePan) {
		_cPan = thePan;
	}
	public void zoom(boolean theZoom) {
		_cZoom = theZoom;
	}

	private final CCAnimationManager _myAnimationManager = new CCAnimationManager();
	

	private final CCCameraMouseDragHandler _myPanHandler = new CCCameraMouseDragHandler() {
		public void handleDrag(final double theMoveX, final double theMoveY, double theMouseX, double theMouseY) {
			_myDampedPanX.impulse(theMoveX / 8f);
			_myDampedPanY.impulse(theMoveY / 8f);
		}
	};
	
	private CCCameraMouseDragHandler _myCenterDragHandler = _myPanHandler;

	private final CCCameraMouseDragHandler _myRotateHandler = new CCCameraMouseDragHandler() {
		public void handleDrag(final double theMoveX, final double theMoveY, double theMouseX, double theMouseY) {
			mouseRotate(-theMoveX, theMoveY, theMouseX, theMouseY);
		}
	};
	private CCCameraMouseDragHandler _myLeftDragHandler = _myRotateHandler;

	private final CCCameraMouseDragHandler _myZoomHandler = new CCCameraMouseDragHandler() {
		public void handleDrag(final double theMoveX, final double theMoveY, double theMouseX, double theMouseY) {
			_myDampedZoom.impulse(theMoveY / 10f);
		}
	};
	private CCCameraMouseDragHandler _myRightDraghandler = _myZoomHandler;

	private final CCMouseWheelListener _myWheelHandler = new CCMouseWheelListener() {

		@Override
		public void mouseWheelMoved(CCMouseWheelEvent theThe) {
			if(_cZoom)_myDampedZoom.impulse(_myWheelScale * theThe.rotation());
		}
	};
	
	private double _myWheelScale = 1f;

	private final CCCameraMouseListener _myMouseListener = new CCCameraMouseListener();
	private final CCCameraKeyListener _myKeyListener = new CCCameraKeyListener();
	private boolean _myIsActive = false;
	
	private double _myRelX;
	private double _myRelY;
	private double _myRelWidth;
	private double _myRelHeight;
	
	@CCProperty(name = "camera")
	private CCCamera _myCamera;
	@CCProperty(name = "rotate only")
	public boolean rotateOnly = false;

	public CCCameraController(final CCGL2Adapter theApp, final CCGraphics theG, final double theDistance) {
		this(theApp, theG, 0,0,1,1,0, 0, 0, theDistance);
	}

	public CCCameraController(
		final CCGL2Adapter theApp, 
		final CCGraphics theG,  
		final double theRelX, final double theRelY, final double theRelWidth, final double theRelHeight, 
		final double theLookAtX, final double theLookAtY, final double theLookAtZ,
		final double theDistance
	) {
		_myApp = theApp;
		
		_myApp.animator().updateEvents().add(a -> {
			_myAnimationManager.update(a);
			feed();
		});
		
		_myRelX = theRelX;
		_myRelY = theRelY;
		_myRelWidth = theRelWidth;
		_myRelHeight = theRelHeight;
		
		_myCamera = new CCCamera(theG);
		_myCamera.viewport(
			new CCViewport(
				(int)(theG.width() * _myRelX), 
				(int)(theG.height() * _myRelY), 
				(int)(theG.width() * _myRelWidth), 
				(int)(theG.height() * _myRelHeight)
			)
		);
		
		
		
		_myApp.glContext().listener().add(new CCGLListener<CCGraphics>() {

			@Override
			public void reshape(CCGraphics theContext) {
				_myCamera = new CCCamera(theContext);
				_myCamera.viewport(
					new CCViewport(
						(int)(theContext.width() * _myRelX), 
						(int)(theContext.height() * _myRelY), 
						(int)(theContext.width() * _myRelWidth), 
						(int)(theContext.height() * _myRelHeight)
					)
				);
			}

			@Override
			public void init(CCGraphics theContext) {}

			@Override
			public void dispose(CCGraphics theContext) {}

			@Override
			public void display(CCGraphics theContext) {}
		});
		g  = theG;
		_myStartCenter = new CCVector3(theLookAtX, theLookAtY, theLookAtZ);
		_myCenter = new CCVector3(theLookAtX, theLookAtY, theLookAtZ);
		_myStartDistance = _myDistance = theDistance;
		_myRotation = new CCQuaternion();
		
		_myCamera = new CCCamera(theG);

		feed();

		_myRotateXAction = new CCDampedAction() {
			@Override
			protected void behave(final double theVelocity) {
				_myRotation.multiplyLocal(CCQuaternion.createFromAngleAxis(theVelocity, CCVector3.UNIT_X ));
			}
		};

		_myRotateYAction = new CCDampedAction() {
			@Override
			protected void behave(final double theVelocity) {
				_myRotation.multiplyLocal(CCQuaternion.createFromAngleAxis(theVelocity, CCVector3.UNIT_Y));
			}
		};

		_myRotateZAction = new CCDampedAction() {
			@Override
			protected void behave(final double theVelocity) {
				_myRotation.multiplyLocal(CCQuaternion.createFromAngleAxis(theVelocity, CCVector3.UNIT_Z));
			}
		};

		_myDampedZoom = new CCDampedAction() {
			@Override
			protected void behave(final double theVelocity) {
				mouseZoom(theVelocity);
			}
		};

		_myDampedPanX = new CCDampedAction() {
			@Override
			protected void behave(final double theVelocity) {
				mousePan(theVelocity, 0);
			}
		};

		_myDampedPanY = new CCDampedAction() {
			@Override
			protected void behave(final double theVelocity) {
				mousePan(0, theVelocity);
			}
		};

		setActive(true);
	}
	
	public CCCamera camera(){
		return _myCamera;
	}

	/**
	 * <_myApp>
	 * Turn on or off default mouse-handling behavior:
	 * 
	 * <_myApp>
	 * <table>
	 * <tr>
	 * <td><b>left-drag</b></td>
	 * <td>rotate camera around look-at point</td>
	 * <tr>
	 * <tr>
	 * <td><b>center-drag</b></td>
	 * <td>pan camera (change look-at point)</td>
	 * <tr>
	 * <tr>
	 * <td><b>right-drag</b></td>
	 * <td>zoom</td>
	 * <tr>
	 * <tr>
	 * <td><b>wheel</b></td>
	 * <td>zoom</td>
	 * <tr>
	 * </table>
	 * 
	 * @param isMouseControlled
	 */
	@CCProperty(name = "active")
	public void setActive(final boolean theIsActive) {
		if (theIsActive == _myIsActive) {
			return;
		}
		_myIsActive = theIsActive;
		if (_myIsActive) {
			_myApp.mouseListener().add(_myMouseListener);
			_myApp.mouseWheelListener().add(_myWheelHandler);
			_myApp.mouseMotionListener().add(_myMouseListener);
			_myApp.keyListener().add(_myKeyListener);
		} else {
			_myApp.mouseListener().remove(_myMouseListener);
			_myApp.mouseWheelListener().remove(_myWheelHandler);
			_myApp.mouseMotionListener().remove(_myMouseListener);
			_myApp.keyListener().remove(_myKeyListener);
		}
	}

	public boolean isActive() {
		return _myIsActive;
	}

	public double wheelScale() {
		return _myWheelScale;
	}

	public void wheelScale(final double theWheelScale) {
		_myWheelScale = theWheelScale;
	}
	
	protected class CCCameraMouseListener extends CCMouseAdapter{
		
		private double _myPMouseX;
		private double _myPMouseY;
		
		private boolean _myIsActive = false;
		
		@Override
		public void mouseReleased(CCMouseEvent theEvent) {
			CCLog.info("mouseReleased");;
			_myDragConstraint = null;
			_myIsPressed = false;
		}
		
		public boolean _myIsPressed = false;
		
		@Override
		public void mousePressed(CCMouseEvent theEvent) {
			_myIsActive = _myCamera.viewport().pointInside(theEvent.x(), g.height() - theEvent.y());
			if(!_myIsActive)return;
			
			_myPMouseX = theEvent.x();
			_myPMouseY = g.height() - theEvent.y();
			
			_myIsPressed = true;
		}
		
		@Override
		public void mouseClicked(CCMouseEvent theEvent) {
			if(!_myIsActive)return;
			
			if (_myResetOnDoubleClick && 2 == theEvent.clickCount()) {
				reset();
			}
		}
		
		@Override
		public void mouseDragged(CCMouseEvent theEvent) {
			if(!_myIsActive)return;
			
			double theMoveX = theEvent.x() - _myPMouseX;
			double theMoveY = _myPMouseY - (g.height() - theEvent.y());
			_myPMouseX = theEvent.x();
			_myPMouseY = g.height() - theEvent.y();

			if(_cInvert) {
				theMoveX = -theMoveX;
				theMoveY = -theMoveY;
			}
			if (theEvent.isShiftDown()) {
				if (_myDragConstraint == null && Math.abs(theMoveX - theMoveY) > 1) {
					_myDragConstraint = Math.abs(theMoveX) > Math.abs(theMoveY) ? CCCameraRotationMode.YAW : CCCameraRotationMode.PITCH;
				}
			} else if (_cRotationMode != CCCameraRotationMode.FREE) {
				_myDragConstraint = _cRotationMode;
			} else {
				_myDragConstraint = CCCameraRotationMode.FREE;
			}
			
			if(rotateOnly) {
				_myRotateHandler.handleDrag(theMoveX, theMoveY, theEvent.x(), g.height() - theEvent.y());
			}

			final CCMouseButton b = theEvent.button();
			if (_myCenterDragHandler != null && (b == CCMouseButton.CENTER || (b == CCMouseButton.LEFT && theEvent.isMetaDown()))) {
				if(_cPan)_myCenterDragHandler.handleDrag(theMoveX, theMoveY, theEvent.x(), g.height() - theEvent.y());
			} 
			if (_myLeftDragHandler != null && (b == CCMouseButton.LEFT || _cAlwaysRotate && !_cPan && !_cZoom)) {
				_myLeftDragHandler.handleDrag(theMoveX, theMoveY, theEvent.x(), g.height() - theEvent.y());
			} 
			if (_myRightDraghandler != null && b == CCMouseButton.RIGHT) {
				if(_cZoom)_myRightDraghandler.handleDrag(theMoveX, theMoveY, theEvent.x(), g.height() - theEvent.y());
			}
		}
	}
	
	protected class CCCameraKeyListener extends CCKeyAdapter{
		@Override
		public void keyReleased(CCKeyEvent theKeyEvent) {
			if (theKeyEvent.isShiftDown())
				_myDragConstraint = null;
		}
	}

	private void mouseZoom(final double delta) {

//		CCLog.info(_myDistance + ":" + delta + ":" + Math.log1p(CCMath.abs(_myDistance)) + ":" + CCMath.sign(_myDistance));
		safeSetDistance(_myDistance + delta * Math.log1p(CCMath.abs(_myDistance)) * CCMath.sign(_myDistance));
	}

	private void mousePan(final double dxMouse, final double dyMouse) {
		final double panScale = CCMath.sqrt(CCMath.abs(_myDistance) * .005);
		pan(
			_myDragConstraint == CCCameraRotationMode.PITCH ? 0 : -dxMouse * panScale,
			_myDragConstraint == CCCameraRotationMode.YAW ? 0 : dyMouse * panScale
		);
	}
	
	@CCProperty(name = "rotation speed", min = 0, max = 0.2)
	private double _cMouseRotateSpeed = 0.1;

	private void mouseRotate(final double theMoveX, final double theMoveY, double mouseX, double mouseY) {
		final CCVector3 u = LOOK.multiply(100 + .6f * _myStartDistance).negate();

		final int xSign = theMoveX > 0 ? -1 : 1;
		final int ySign = theMoveY < 0 ? -1 : 1;

		mouseY -= _myCamera.viewport().y();
		mouseX -= _myCamera.viewport().x();
		final double eccentricity = CCMath.abs((_myCamera.viewport().height() / 2f) - mouseY) / (_myCamera.viewport().height() / 2f);
		final double rho = CCMath.abs((_myCamera.viewport().width() / 2f) - mouseX) / (_myCamera.viewport().width() / 2f);

		if (
			_myDragConstraint == CCCameraRotationMode.FREE || 
			_myDragConstraint == CCCameraRotationMode.YAW || 
			_myDragConstraint == CCCameraRotationMode.SUPPRESS_ROLL
		) {
			final double adx = Math.abs(theMoveX) * (1 - eccentricity);
			final CCVector3 vx = u.add(new CCVector3(adx, 0, 0));
			_myRotateYAction.impulse(CCVector3.angle(u, vx) * xSign * _cMouseRotateSpeed);
		}
		if (
			_myDragConstraint == CCCameraRotationMode.FREE || 
			_myDragConstraint == CCCameraRotationMode.PITCH || 
			_myDragConstraint == CCCameraRotationMode.SUPPRESS_ROLL
		) {
			final double ady = Math.abs(theMoveY) * (1 - rho);
			final CCVector3 vy = u.add(new CCVector3(0, ady, 0));
			_myRotateXAction.impulse(CCVector3.angle(u, vy) * ySign * _cMouseRotateSpeed);
		}
		if (
			_myDragConstraint == CCCameraRotationMode.FREE || 
			_myDragConstraint == CCCameraRotationMode.PITCH
		) {
			{
				final double adz = Math.abs(theMoveY) * rho;
				final CCVector3 vz = u.add(new CCVector3(0, adz, 0));
				_myRotateZAction.impulse(CCVector3.angle(u, vz) * ySign * (mouseX < _myCamera.viewport().width() / 2 ? -1 : 1) * _cMouseRotateSpeed);
			}
			{
				final double adz = Math.abs(theMoveX) * eccentricity;
				final CCVector3 vz = u.add(new CCVector3(0, adz, 0));
				_myRotateZAction.impulse(CCVector3.angle(u, vz) * -xSign * (mouseY > _myCamera.viewport().height() / 2 ? -1 : 1) * _cMouseRotateSpeed);
			}
		}
	}

	public double distance() {
		return _myDistance;
	}

	public void distance(final double theNewDistance) {
		distance(theNewDistance, 0.3f);
	}

	public void distance(final double theNewDistance, final double theAnimationTime) {
		if(_myDistanceAnimation != null)_myDistanceAnimation.cancel();
		_myAnimationManager.play(new CCDistanceAnimation(theAnimationTime,theNewDistance));
	}

	public CCVector3 lookAt() {
		return _myCenter;
	}

	public void lookAt(final double x, final double y, final double z) {
		if(_myCenterAnimation != null)_myCenterAnimation.cancel();
		_myAnimationManager.play(new CCCenterAnimation(0.3, new CCVector3(x, y, z)));
	}

	public void lookAt(final double x, final double y, final double z, final double theDistance) {
		lookAt(x, y, z);
		distance(theDistance);
	}

	public void lookAt(
		final double x, final double y, final double z,
		final double theDistance, final double theAnimationTime
	) {
		setState(new CCCameraState(_myRotation, new CCVector3(x, y, z), theDistance), theAnimationTime);
	}

	private void safeSetDistance(final double theDistance) {
		_myDistance = CCMath.constrain(theDistance, 1, _myMaximumDistance);
	}

	public void feed() {
		apply(_myCenter, _myRotation, _myDistance);
	}

	public void apply(final CCVector3 center, final CCQuaternion rotation, final double theDistance) {
		final CCVector3 pos = rotation.apply(LOOK).multiply(theDistance).add(center);
		final CCVector3 rup = rotation.apply(UP);
		
		_myCamera.position(pos.x, pos.y, pos.z);
		_myCamera.target(center.x, center.y, center.z);
		_myCamera.up(rup.x, rup.y, rup.z);
//		_myCamera.fov(_myFoV * CCMath.DEG_TO_RAD);
	}

	/**
	 * Where is the camera in world space?
	 * 
	 * @return camera position
	 */
	public CCVector3 position() {
		return _myRotation.apply(LOOK).multiply(_myDistance).add(_myCenter);
	}

	@CCProperty(name = "reset")
	public void reset() {
		reset(0.3);
	}

	public void reset(final double theAnimationTime) {
		setState(new CCCameraState(new CCQuaternion(), _myStartCenter, _myStartDistance), theAnimationTime);
	}

	public void pan(final double theMoveX, final double theMoveY) {
		_myCenter.addLocal(_myRotation.apply(new CCVector3(theMoveX, theMoveY, 0)));
//		feed();
	}
	
	public CCAnimation pan(final double theMoveX, final double theMoveY, final double theDuration) {
		if(_myCenterAnimation != null)_myCenterAnimation.cancel();
		
		CCAnimation myAnimation = new CCCenterAnimation(theDuration, _myCenter.add(_myRotation.apply(new CCVector3(theMoveX, theMoveY, 0))));
		_myAnimationManager.play(myAnimation);
		return myAnimation;
	}

	public void rotateX(final double angle) {
		_myRotation.multiplyLocal(CCQuaternion.createFromAngleAxis(angle, CCVector3.UNIT_X));
	}

	public void rotateY(final double angle) {
		_myRotation.multiplyLocal(CCQuaternion.createFromAngleAxis(angle, CCVector3.UNIT_Y));
	}

	public void rotateZ(final double angle) {
		_myRotation.multiplyLocal(CCQuaternion.createFromAngleAxis(angle, CCVector3.UNIT_Z));
	}

	public CCCameraState getState() {
		return new CCCameraState(_myRotation, _myCenter, _myDistance);
	}

	public void setMaximumDistance(final double maximumDistance) {
		_myMaximumDistance = maximumDistance;
		safeSetDistance(_myDistance);
	}

	public void setResetOnDoubleClick(final boolean resetOnDoubleClick) {
		_myResetOnDoubleClick = resetOnDoubleClick;
	}
	

	
	private static class CCCameraInterpolator implements CCInterpolator{

		@Override
		public double interpolate(double theV0, double theV1, double theV2, double theV3, double theBlend, double... theparam) {
			final double smooth = (theBlend * theBlend * (3 - 2 * theBlend));
			return (theV2 * smooth) + (theV1 * (1 - smooth));
		}
		
	}
	
	private CCInterpolator _myInterpolator = new CCCameraInterpolator();
	
	public void interpolator(CCInterpolator theInterpolator) {
		_myInterpolator = theInterpolator;
	}
	
	public double interpolate(double theA, double theB, double theT) {
		return _myInterpolator.interpolate(theA, theA, theB, theB, theT);
	}

	class CCDistanceAnimation extends CCAnimation{
		private final double _myStartDistance;
		private final double _myEndDistance;

		public CCDistanceAnimation(double theDuration, final double endDistance) {
			super(theDuration);
			_myStartDistance = _myDistance;
			_myEndDistance = CCMath.constrain(endDistance, -_myMaximumDistance, _myMaximumDistance);
			
			progressEvents.add(a -> {
				_myDistance = interpolate(_myStartDistance, _myEndDistance, a.progress());
			});
		}

		protected void progress(final double t) {
			
		}
	}

	class CCCenterAnimation extends CCAnimation{
		private final CCVector3 startCenter;
		private final CCVector3 endCenter;

		public CCCenterAnimation(double theDuration, final CCVector3 theEndCenter) {
			super(theDuration);
			startCenter = new CCVector3(_myCenter);
			endCenter = theEndCenter;
			
			progressEvents.add(a -> {
				_myCenter.set(
					interpolate(startCenter.x, endCenter.x, a.progress()), 
					interpolate(startCenter.y, endCenter.y, a.progress()), 
					interpolate(startCenter.z, endCenter.z, a.progress())
				);
			});
		}
	}

	class CCRotationAnimation extends CCAnimation{
		final CCQuaternion _myStartRotation;
		final CCQuaternion _myEndRotation;

		public CCRotationAnimation(double theDuration, final CCQuaternion endRotation) {
			super(theDuration);
			_myStartRotation = new CCQuaternion(_myRotation);
			_myEndRotation = endRotation;
			
			playEvents.add(a -> {
				_myRotateXAction.stop();
				_myRotateYAction.stop();
				_myRotateZAction.stop();
			});
			
			progressEvents.add(a -> {
				_myRotation.set(CCQuaternion.slerp(_myStartRotation, _myEndRotation, interpolate(0, 1, a.progress())));
			});
		}
	}

	public void setState(final CCCameraState state) {
		_myRotation.set(state._myRotation);
		_myCenter.set(state._myCenter);
		_myDistance = state._myDistance;
	}
	
	private CCRotationAnimation _myRotationAnimation;
	private CCCenterAnimation _myCenterAnimation;
	private CCDistanceAnimation _myDistanceAnimation;

	public CCAnimation setState(final CCCameraState state, final double theTime) {
		CCAnimation myResult;
		
		if(_myRotationAnimation != null) _myRotationAnimation.cancel();
		if(_myCenterAnimation != null) _myCenterAnimation.cancel();
		if(_myDistanceAnimation != null) _myDistanceAnimation.cancel();
			
		_myAnimationManager.play(new CCRotationAnimation(theTime, state._myRotation));
		_myAnimationManager.play(new CCCenterAnimation(theTime, state._myCenter));
		_myAnimationManager.play(myResult = new CCDistanceAnimation(theTime, state._myDistance));
		
		return myResult;
	}
}
