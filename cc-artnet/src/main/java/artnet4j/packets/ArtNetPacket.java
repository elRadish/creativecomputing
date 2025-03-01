/*
 * This file is part of artnet4j.
 * 
 * Copyright 2009 Karsten Schmidt (PostSpectacular Ltd.)
 * 
 * artnet4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * artnet4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with artnet4j. If not, see <http://www.gnu.org/licenses/>.
 */

package artnet4j.packets;

import java.util.logging.Logger;

public abstract class ArtNetPacket {

    public static final byte[] HEADER = "Art-Net\0".getBytes();

    public static final int PROTOCOL_VERSION = 14;

    public static final Logger logger =
            Logger.getLogger(ArtNetPacket.class.getClass().getName());

    protected ByteUtils _myData;
    protected final PacketType _myType;

    public ArtNetPacket(PacketType type) {
        _myType = type;
    }

    /**
     * @return the data
     */
    public byte[] getData() {
        return _myData.getBytes();
    }

    /**
     * Returns the actually used length of the data buffer.
     * 
     * @return
     */
    public int getLength() {
        return _myData.length;
    }

    /**
     * Returns the type of this packet.
     * 
     * @return the type
     */
    public PacketType getType() {
        return _myType;
    }

    /**
     * Parses the given byte array into semantic values and populates type
     * specific fields for each packet type. Implementing classes do not need to
     * check the packet header anymore since this has already been done at this
     * stage.
     * 
     * @param raw
     * @return true, if there were no parse errors
     */
    public abstract boolean parse(byte[] raw);

    /**
     * @param data
     *            the data to set
     */
    public void setData(byte[] data) {
        _myData = new ByteUtils(data);
    }

    public void setData(byte[] raw, int maxLength) {
        if (raw.length > maxLength) {
            byte[] raw2 = new byte[maxLength];
            System.arraycopy(raw, 0, raw2, 0, maxLength);
            raw = raw2;
        }
        setData(raw);
    }

    /**
     * Sets the header bytes of the packet consisting of {@link #HEADER} and the
     * type's OpCode.
     */
    protected void header() {
        _myData.setByteChunk(HEADER, 0, 8);
        _myData.setInt16LE(_myType.getOpCode(), 8);
    }

    protected void protocol() {
        _myData.setInt16(PROTOCOL_VERSION, 10);
    }

    @Override
    public String toString() {
        return _myData.toHex(getLength());
    }
}
