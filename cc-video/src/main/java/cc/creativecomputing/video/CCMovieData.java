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
package cc.creativecomputing.video;

import cc.creativecomputing.app.modules.CCAnimator;
import cc.creativecomputing.core.events.CCListenerManager;

/**
 * This class is representing video data so the content of the object
 * is updated permanently on playback. It also implements the movie
 * interface for control of the play back.
 * @author christian riekoff
 *
 */
public abstract class CCMovieData extends CCVideo implements CCMovie{
	
	public static interface CCMovieEvent{
		public void event();
	}
	
	/**
	 * set this true for looping
	 */
	protected boolean _myDoRepeat = false;
	
	/**
	 * indicates if the movie is running
	 */
	protected boolean _myIsRunning = false;
	
	protected boolean _myIsPaused = false;
	
	public CCListenerManager<CCMovieEvent> playEvents = CCListenerManager.create(CCMovieEvent.class);
	public CCListenerManager<CCMovieEvent> stopEvents = CCListenerManager.create(CCMovieEvent.class);
	public CCListenerManager<CCMovieEvent> pauseEvents = CCListenerManager.create(CCMovieEvent.class);
	public CCListenerManager<CCMovieEvent> endEvents = CCListenerManager.create(CCMovieEvent.class);

	/**
	 * Creates a new instance, without setting any parameters.
	 * @param theAnimator
	 */
	public CCMovieData(final CCAnimator theAnimator) {
		super(theAnimator);
	}

	/* (non-Javadoc)
	 * @see cc.creativecomputing.texture_new.video.CCMovie#isRunning()
	 */
	public boolean isRunning() {
		return _myIsRunning;
	}
	public boolean isPaused() {
		return _myIsPaused;
	}

	/* (non-Javadoc)
	 * @see cc.creativecomputing.texture_new.video.CCMovie#loop()
	 */
	public void loop() {
		_myDoRepeat = true;
		try {
			play();
			_myIsRunning = true;
		} catch (Exception e) {
			e.printStackTrace();
			_myIsRunning = false;
		}
	}

	/* (non-Javadoc)
	 * @see cc.creativecomputing.texture_new.video.CCMovie#loop(boolean)
	 */
	public void loop(boolean theDoLoop) {
		_myDoRepeat = theDoLoop;
	}

	/* (non-Javadoc)
	 * @see cc.creativecomputing.texture_new.video.CCMovie#progress()
	 */
	public double progress() {
		return time() / duration();
	}

	public void play() {
		play(false);
	}
	
	public void play(boolean theDoRestart) {
		_myIsRunning = true;
		_myIsPaused = false;

		if (theDoRestart)
			goToBeginning();
		playEvents.proxy().event();
	}

	public void stop() {
		if (_myIsRunning) {
			goToBeginning();
			_myIsRunning = false;
		}
		_myIsPaused = false;
		stopEvents.proxy().event();
	}
	
	public void pause() {
		_myIsRunning = false;
		_myIsPaused = true;
		pauseEvents.proxy().event();
	}

	/* (non-Javadoc)
	 * @see cc.creativecomputing.texture_new.video.CCMovie#volume()
	 */
	public double volume() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see cc.creativecomputing.texture_new.video.CCMovie#volume(float)
	 */
	public void volume(double theVolume) {
		
	}
	
	/**
	 * Set the time of the movie in seconds
	 * @param theTime
	 */
	@Override
	public void time(double theTime) {
		
	}
	
	/**
	 * 
	 */
	public double time() {
		return 0;
	}
}
