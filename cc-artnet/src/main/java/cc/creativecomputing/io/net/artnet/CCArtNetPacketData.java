
package cc.creativecomputing.io.net.artnet;

import cc.creativecomputing.artnet.packets.ByteUtils;

public class CCArtNetPacketData {

    /**
     * Converts the byte into an unsigned int.
     * 
     * @param b
     * @return
     */
    public static final int byteToUint(byte b) {
        return (b < 0 ? 256 + b : b);
    }

    public static final String hex(int value, int digits) {
        String stuff = Integer.toHexString(value).toUpperCase();
        int length = stuff.length();
        if (length > digits) {
            return stuff.substring(length - digits);

        } else if (length < digits) {
            return "00000000".substring(8 - (digits - length)) + stuff;
        }
        return stuff;
    }

    protected byte[] data;

    public int length;

    public CCArtNetPacketData(byte[] theData) {
        data = theData;
        length = theData.length;
    }
    
    public CCArtNetPacketData(){
    	
    }
    
    /**
     * @param _myData
     *            the data to set
     */
    public void setData(byte[] theData) {
        data = theData;
        length = theData.length;
    }

    public void setData(byte[] raw, int maxLength) {
        if (raw.length > maxLength) {
            byte[] raw2 = new byte[maxLength];
            System.arraycopy(raw, 0, raw2, 0, maxLength);
            raw = raw2;
        }
        setData(raw);
    }

    public boolean compareBytes(byte[] other, int offset, int length) {
        boolean isEqual = (offset + length) < data.length;
        for (int i = 0; i < length && isEqual; i++) {
            isEqual = data[offset++] == other[i];
        }
        return isEqual;
    }

    public final byte[] getByteChunk(byte[] buffer, int offset, int len) {
        if (buffer == null) {
            buffer = new byte[len];
        }
        System.arraycopy(data, offset, buffer, 0, len);
        return buffer;
    }

    public byte[] getBytes() {
        return data;
    }

    /**
     * Gets a 16bit int (Big Endian, MSB first) from the buffer at the given
     * offset.
     * 
     * @param offset
     * @return
     */
    public final int getInt16(int offset) {
        return (byteToUint(data[offset]) << 8) | byteToUint(data[offset + 1]);
    }

    /**
     * Gets a 16bit int (Little Endian, LSB first) from the buffer at the given
     * offset.
     * 
     * @param offset
     * @return
     */
    public final int getInt16LE(int offset) {
        return (byteToUint(data[offset + 1]) << 8) | byteToUint(data[offset]);
    }

    /**
     * Gets an 8bit int from the buffer at the given offset.
     * 
     * @param offset
     * @return
     */
    public final int getInt8(int offset) {
        return byteToUint(data[offset]);
    }

    public final int getLength() {
        return data.length;
    }

    public final void setByteChunk(byte[] buffer, int offset, int len) {
        System.arraycopy(buffer, 0, data, offset, len);
    }

    public final void setInt16(int val, int offset) {
        data[offset] = (byte) (val >> 8 & 0xff);
        data[offset + 1] = (byte) (val & 0xff);
    }

    public final void setInt16LE(int val, int offset) {
        data[offset] = (byte) (val & 0xff);
        data[offset + 1] = (byte) (val >> 8 & 0xff);
    }

    public final void setInt8(int val, int offset) {
        data[offset] = (byte) (val & 0xff);
    }

    public final String toHex(int length) {
        String result = "";
        String ascii = "";
        for (int i = 0; i < length; i++) {
            if (0 == i % 16) {
                result += hex(i, 4) + ": ";
                ascii = " ";
            }
            result += hex(byteToUint(data[i]), 2);
            ascii += data[i] > 0x1f && data[i] < 0x7f ? (char) data[i] : ".";
            if (7 == i % 8) {
                result += " ";
                ascii += " ";
            }
            result += (15 == i % 16 ? ascii + "\n" : " ");
        }
        return result;
    }
}
