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
package cc.creativecomputing.protocol.midi.messages;

import javax.sound.midi.ShortMessage;

public class CCMidiNoteOff extends CCNote{

	public CCMidiNoteOff(int thePitch) {
		super(ShortMessage.NOTE_OFF, thePitch, 0);
	}

	public CCMidiNoteOff(ShortMessage theShortMessage) {
		super(theShortMessage);
	}
}
