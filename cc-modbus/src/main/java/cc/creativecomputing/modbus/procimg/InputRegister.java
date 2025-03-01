//License
/***
 * Java Modbus Library (jamod)
 * Copyright (c) 2002-2004, jamod development team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the author nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS ``AS
 * IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ***/
package cc.creativecomputing.modbus.procimg;

/**
 * Interface defining an input register.
 * <p>
 * This register is read only from the slave side.
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 */
public interface InputRegister {

	/**
	 * Returns the value of this <tt>InputRegister</tt>. The value is stored as
	 * <tt>int</tt> but should be treated like a 16-bit word.
	 * 
	 * @return the value as <tt>int</tt>.
	 */
    int getValue();

	/**
	 * Returns the content of this <tt>Register</tt> as unsigned 16-bit value
	 * (unsigned short).
	 * 
	 * @return the content as unsigned short (<tt>int</tt>).
	 */
    int toUnsignedShort();

	/**
	 * Returns the content of this <tt>Register</tt> as signed 16-bit value
	 * (short).
	 * 
	 * @return the content as <tt>short</tt>.
	 */
    short toShort();

	/**
	 * Returns the content of this <tt>Register</tt> as bytes.
	 * 
	 * @return a <tt>byte[]</tt> with length 2.
	 */
    byte[] toBytes();

}