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
package cc.creativecomputing.modbus.msg;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import cc.creativecomputing.modbus.CCModbusExceptionCode;
import cc.creativecomputing.modbus.CCModbusFunctionCode;
import cc.creativecomputing.modbus.Modbus;
import cc.creativecomputing.modbus.ModbusCoupler;
import cc.creativecomputing.modbus.procimg.IllegalAddressException;
import cc.creativecomputing.modbus.procimg.ProcessImage;
import cc.creativecomputing.modbus.procimg.Register;
import cc.creativecomputing.modbus.procimg.SimpleRegister;

/**
 * Class implementing a <tt>WriteSingleRegisterRequest</tt>. The implementation
 * directly correlates with the class 0 function <i>write single register (FC
 * 6)</i>. It encapsulates the corresponding request message.
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 */
public final class WriteSingleRegisterRequest extends CCAbstractModbusRequest {

	// instance attributes
	private int m_Reference;
	private Register m_Register;

	/**
	 * Constructs a new <tt>WriteSingleRegisterRequest</tt> instance.
	 */
	public WriteSingleRegisterRequest() {
		super();

		functionCode(CCModbusFunctionCode.WRITE_SINGLE_REGISTER);
		dataLength(4);
	}

	/**
	 * Constructs a new <tt>WriteSingleRegisterRequest</tt> instance with a
	 * given reference and value to be written.
	 * 
	 * @param ref
	 *            the reference number of the register to read from.
	 * @param reg
	 *            the register containing the data to be written.
	 */
	public WriteSingleRegisterRequest(int ref, Register reg) {
		super();

		functionCode(CCModbusFunctionCode.WRITE_SINGLE_REGISTER);
		dataLength(4);

		m_Reference = ref;
		m_Register = reg;
	}

	public CCAbstractModbusResponse response() {
		WriteSingleRegisterResponse response = new WriteSingleRegisterResponse();

		response.setHeadless(isHeadless());
		if (!isHeadless()) {
			response.protocolID(protocolID());
			response.transactionID(transactionID());
		}
		response.functionCode(functionCode());
		response.unitID(unitID());

		return response;
	}

	public CCAbstractModbusResponse createResponse() {
		WriteSingleRegisterResponse response = null;
		Register reg = null;

		// 1. get process image
		ProcessImage procimg = ModbusCoupler.getReference().getProcessImage();
		// 2. get register
		try {
			reg = procimg.getRegister(m_Reference);
			// 3. set Register
			reg.setValue(m_Register.toBytes());
		} catch (IllegalAddressException iaex) {
			return createExceptionResponse(CCModbusExceptionCode.ILLEGAL_ADDRESS_EXCEPTION);
		}
		response = (WriteSingleRegisterResponse) response();

		return response;
	}

	/**
	 * Sets the reference of the register to be written to with this
	 * <tt>WriteSingleRegisterRequest</tt>.
	 * 
	 * @param ref
	 *            the reference of the register to be written to.
	 */
	public void setReference(int ref) {
		m_Reference = ref;
	}

	/**
	 * Returns the reference of the register to be written to with this
	 * <tt>WriteSingleRegisterRequest</tt>.
	 * 
	 * @return the reference of the register to be written to.
	 */
	public int getReference() {
		return m_Reference;
	}

	/**
	 * Sets the value that should be written to the register with this
	 * <tt>WriteSingleRegisterRequest</tt>.
	 * 
	 * @param reg
	 *            the register to be written.
	 */
	public void setRegister(Register reg) {
		m_Register = reg;
	}

	/**
	 * Returns the register to be written with this
	 * <tt>WriteSingleRegisterRequest</tt>.
	 * 
	 * @return the value to be written to the register.
	 */
	public Register getRegister() {
		return m_Register;
	}

	public void writeData(DataOutput dout) throws IOException {
		dout.writeShort(m_Reference);
		dout.write(m_Register.toBytes());
	}

	public void readData(DataInput din) throws IOException {
		m_Reference = din.readUnsignedShort();
		m_Register = new SimpleRegister(din.readByte(), din.readByte());
	}

	public byte[] message() {
		byte result[] = new byte[4];

		result[0] = (byte) ((m_Reference >> 8) & 0xff);
		result[1] = (byte) (m_Reference & 0xff);
		result[2] = (byte) ((m_Register.getValue() >> 8) & 0xff);
		result[3] = (byte) (m_Register.getValue() & 0xff);

		return result;
	}
}