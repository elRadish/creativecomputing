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

import cc.creativecomputing.protocol.midi.CCMidiStatus;

public class CCAftertouch extends CCMidiMessage{

	CCAftertouch() {
		super(CCMidiStatus.AFTERTOUCH);
	}

}
