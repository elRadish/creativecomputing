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
package cc.creativecomputing.io.net.codec.osc;

import java.util.ArrayList;
import java.util.List;

//import java.util.Map;

/**
 * Implementation of the OSC-Bundle which assembles several OSC-Packets under the same timetag. See <A
 * HREF="http://opensoundcontrol.org/spec-1_0">opensoundcontrol.org/spec-1_0</A> for the specification of the bundle
 * format.
 * <p>
 * The bundle time can be calculated in different ways: specifying a <code>long</code> which represents the milliseconds
 * since 1 jan 1970 as returned by <code>System.currentTimeMillis()</code>, produces the required network time tag as
 * required by the OSC specification. Alternatively, using a <code>float</code> for a relative time offset in seconds
 * can be used when running SuperCollider in offline mode. Third, there is a version that calculates a sample accurate
 * time tag. However, scsynth doesn't process bundles with this accuracy, so it's kind of useless.
 * <p>
 * To assemble a bundle, you create a new instance of <code>OSCBundle</code>, call <code>addPacket</code> one or several
 * times, then write the contents of the bundle to a <code>ByteBuffer</code> using the method <code>encode</code>. The
 * byte buffer can then be written to a <code>DatagramChannel</code>. Here is an example:
 * 
 * <pre>
 * OSCBundle bndl;
 * DatagramChannel dch = null;
 * 
 * final ByteBuffer buf = ByteBuffer.allocateDirect(1024);
 * final SocketAddress addr = new InetSocketAddress(&quot;localhost&quot;, 57110);
 * final long serverLatency = 50;
 * final long now = System.currentTimeMillis() + serverLatency;
 * 
 * try {
 * 	dch = DatagramChannel.open();
 * 	dch.configureBlocking(true);
 * 	bndl = new OSCBundle(now);
 * 	bndl.addPacket(new OSCMessage(&quot;/s_new&quot;, new Object[] { &quot;default&quot;, new Integer(1001), new Integer(1), new Integer(0), &quot;out&quot;, new Integer(0), &quot;freq&quot;, new Float(666), &quot;amp&quot;,
 * 			new Float(0.1f) }));
 * 	bndl.encode(buf);
 * 	buf.flip();
 * 	dch.send(buf, addr);
 * 	buf.clear();
 * 	bndl = new OSCBundle(now + 2000); // two seconds later
 * 	bndl.addPacket(new OSCMessage(&quot;/n_free&quot;, new Object[] { new Integer(1001) }));
 * 	bndl.encode(buf);
 * 	buf.flip();
 * 	dch.send(buf, addr);
 * } catch (IOException e2) {
 * 	System.err.println(e2.getLocalizedMessage());
 * } finally {
 * 	if (dch != null) {
 * 		try {
 * 			dch.close();
 * 		} catch (IOException e4) {
 * 		}
 * 		;
 * 	}
 * }
 * </pre>
 * 
 * Note that this example uses the old way of sending messages. A easier way is to create an <code>OSCTransmitter</code>
 * which handles the byte buffer for you. See the <code>OSCReceiver</code> doc for an example using a dedicated
 * transmitter.
 * 
 * @author Hanns Holger Rutz
 * @version 0.33, 28-Apr-07
 * 
 * @see CCOSCIn
 */
public class CCOSCBundle extends CCOSCPacket {
	/**
	 * This is the initial string of an OSC bundle datagram
	 */
	public static final String TAG = "#bundle";

	/**
	 * The special timetag value to indicate that the bundle be processed as soon as possible
	 */
	public static final long NOW = 1;

	private static final long SECONDS_FROM_1900_TO_1970 = 2208988800L;

	// private static final byte[] cmd1 = { 0x23, 0x62, 0x75, 0x6E, 0x64, 0x6C, 0x65, 0x00 }; // "#bundle" (4-aligned)
	private long timetag; // 64 bit fixed point seconds since 1 jan 1900
	protected final List<CCOSCPacket> _myPackets = new ArrayList<CCOSCPacket>();

	/**
	 * Creates a new empty OSCBundle with timetag set to &quot;immediately&quot;. SuperCollider recognizes this special
	 * timetime to process the bundle just when it arrives.
	 */
	public CCOSCBundle() {
		super();

		timetag = NOW; // special code
	}
	
	public List<CCOSCPacket> packets(){
		return _myPackets;
	}

	/**
	 * Creates a new empty OSCBundle with timetag specified by 'when' which is milliseconds since 1 jan 1970 as returned
	 * by System.currentTimeMillis(). This is converted into an absolute time offset since 1 jan 1900 as required by the
	 * OSC specs.
	 * 
	 * @param when absolute time tag for the bundle
	 * @see java.lang.System#currentTimeMillis()
	 */
	public CCOSCBundle(long when) {
		super();
		setTimeTagAbsMillis(when);
	}

	/**
	 * Creates a new empty OSCBundle with timetag specified by 'when' which is seconds relative to start of session.
	 * This relative time offset with origin of zero is understood by SuperCollider in NonRealTime mode. (see example in
	 * Non-Realtime-Synthesis.rtf).
	 * 
	 * @param when relative time tag for the bundle
	 */
	public CCOSCBundle(double when) {
		super();
		setTimeTagRelSecs(when);
	}

	/**
	 * Creates a new empty OSCBundle with timetag specified by a sample frame offset and an absolute time in
	 * milliseconds since 1 jan 1970 as returned by <code>System.currentTimeMillis()</code>
	 * 
	 * @param absMillisOffset time offset as returned by <code>System.currentTimeMillis</code>
	 * @param sampleFrames this offset is added to the milli second offset.
	 * @param sampleRate used in conjunction with <code>sampleFrames</code> to calculate the time offset.
	 */
	public CCOSCBundle(long absMillisOffset, long sampleFrames, int sampleRate) {
		super();
		setTimeTagSamples(absMillisOffset, sampleFrames, sampleRate);
	}

	/**
	 * Adds a new <code>OSCPacket</code> to the tail of the bundle. Passing <code>null</code> is allowed in which case
	 * no action is performed.
	 * 
	 * @param p the packet to add to the tail of the bundle
	 */
	public void addPacket(CCOSCPacket p) {
		if (p != null) {
			synchronized (_myPackets) {
				_myPackets.add(p);
			}
		}
	}

	/**
	 * Gets the <code>OSCPacket</code> at the provided index which must be between 0 inclusive and
	 * <code>getPacketCount()</code> exclusive. If bundles are nested, each nested bundle will count as one packet of
	 * course.
	 * 
	 * @param idx index of the packet to get
	 * @return packet at index <code>idx</code>
	 */
	public CCOSCPacket getPacket(int idx) {
		synchronized (_myPackets) {
			return _myPackets.get(idx);
		}
	}

	/**
	 * Returns the number of packets currently assembled in this bundle. If bundles are nested, each nested bundle will
	 * count as one packet of course.
	 * 
	 * @return number of packets assembled in this bundle
	 */
	public int getPacketCount() {
		synchronized (_myPackets) {
			return _myPackets.size();
		}
	}

	/**
	 * Removes the specified packet
	 * 
	 * @param idx the index of the packet to remove
	 */
	public void removePacket(int idx) {
		synchronized (_myPackets) {
			_myPackets.remove(idx);
		}
	}

	/**
	 * Sets the bundle's timetag specified by a long which is milliseconds since 1 jan 1970 as returned by
	 * <code>System.currentTimeMillis()</code>. This is converted into an absolute time offset since 1 jan 1900 as
	 * required by the OSC specs.
	 * 
	 * @param when absolute time tag for the bundle
	 * @see java.lang.System#currentTimeMillis()
	 */
	public void setTimeTagAbsMillis(long when) {
		final long secsSince1900 = when / 1000 + SECONDS_FROM_1900_TO_1970;
		final long secsFractional = ((when % 1000) << 32) / 1000;
		timetag = (secsSince1900 << 32) | secsFractional;
	}

	public void setTimeTagRaw(long raw) {
		timetag = raw;
	}

	/**
	 * Sets the bundle's timetag specified by a double which is seconds relative to start of session. This relative time
	 * offset with origin of zero is understood by SuperCollider in NonRealTime mode. (see example in
	 * Non-Realtime-Synthesis.rtf).
	 * 
	 * @param when relative time tag for the bundle
	 */
	public void setTimeTagRelSecs(double when) {
		// timetag = ((long) when << 32) | (long) ((when % 1.0) * 0x100000000L + 0.5);
		timetag = ((long) when << 32) + (long) ((when % 1.0) * 0x100000000L + 0.5);
	}

	/**
	 * Sets the bundle's timetag as a combination of system absolute time and sample offset. note that this is not too
	 * useful, because supercollider will execute OSC bundles not with audiorate but controlrate precision!!
	 * 
	 * @param absMillisOffset time offset as returned by <code>System.currentTimeMillis</code>
	 * @param sampleFrames this offset is added to the milli second offset.
	 * @param sampleRate used in conjunction with <code>sampleFrames</code> to calculate the time offset.
	 */
	public void setTimeTagSamples(long absMillisOffset, long sampleFrames, int sampleRate) {
		final double seconds = (double) sampleFrames / (double) sampleRate + (double) absMillisOffset / 1000;
		timetag = (((long) seconds + SECONDS_FROM_1900_TO_1970) << 32) + (long) ((seconds % 1.0) * 0x100000000L + 0.5);
	}

	/**
	 * Returns the raw format time tag of the bundle
	 * 
	 * @return the bundle's timetag in OSC format
	 * 
	 * @todo a utility method to convert this to a more useful value
	 */
	public long getTimeTag() {
		return timetag;
	}
}
