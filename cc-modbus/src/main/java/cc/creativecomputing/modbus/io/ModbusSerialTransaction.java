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
package cc.creativecomputing.modbus.io;

import cc.creativecomputing.core.logging.CCLog;
import cc.creativecomputing.modbus.Modbus;
import cc.creativecomputing.modbus.CCModbusException;
import cc.creativecomputing.modbus.ModbusIOException;
import cc.creativecomputing.modbus.CCModbusSlaveException;
import cc.creativecomputing.modbus.msg.CCExceptionResponse;
import cc.creativecomputing.modbus.msg.CCAbstractModbusRequest;
import cc.creativecomputing.modbus.msg.CCAbstractModbusResponse;
import cc.creativecomputing.modbus.net.SerialConnection;

/**
 * Class implementing the <tt>ModbusTransaction</tt> interface.
 *
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 */
public class ModbusSerialTransaction implements ModbusTransaction {

	// class attributes
	private static int c_TransactionID = Modbus.DEFAULT_TRANSACTION_ID;

	// instance attributes and associations
	private ModbusTransport m_IO;
	private CCAbstractModbusRequest m_Request;
	private CCAbstractModbusResponse m_Response;
	private boolean m_ValidityCheck = Modbus.DEFAULT_VALIDITYCHECK;
	private int m_Retries = Modbus.DEFAULT_RETRIES;
	private int m_TransDelayMS = Modbus.DEFAULT_TRANSMIT_DELAY;
	private SerialConnection m_SerialCon;

	/**
	 * Constructs a new <tt>ModbusSerialTransaction</tt> instance.
	 */
	public ModbusSerialTransaction() {
	}// constructor

	/**
	 * Constructs a new <tt>ModbusSerialTransaction</tt> instance with a given
	 * <tt>ModbusRequest</tt> to be send when the transaction is executed.
	 * <p>
	 * 
	 * @param request a <tt>ModbusRequest</tt> instance.
	 */
	public ModbusSerialTransaction(CCAbstractModbusRequest request) {
		setRequest(request);
	}// constructor

	/**
	 * Constructs a new <tt>ModbusSerialTransaction</tt> instance with a given
	 * <tt>ModbusRequest</tt> to be send when the transaction is executed.
	 * <p>
	 * 
	 * @param con a <tt>TCPMasterConnection</tt> instance.
	 */
	public ModbusSerialTransaction(SerialConnection con) {
		setSerialConnection(con);
	}// constructor

	/**
	 * Sets the port on which this <tt>ModbusTransaction</tt> should be
	 * executed.
	 * <p>
	 * <p>
	 * 
	 * @param con a <tt>SerialConnection</tt>.
	 */
	public void setSerialConnection(SerialConnection con) {
		m_SerialCon = con;
		m_IO = m_SerialCon.getModbusTransport();
	}// setConnection

	public void setTransport(ModbusSerialTransport transport) {
		m_IO = transport;
	}

	public int getTransactionID() {
		return c_TransactionID;
	}// getTransactionID

	public void setRequest(CCAbstractModbusRequest req) {
		m_Request = req;
		// m_Response = req.getResponse();
	}// setRequest

	public CCAbstractModbusRequest getRequest() {
		return m_Request;
	}// getRequest

	public CCAbstractModbusResponse getResponse() {
		return m_Response;
	}// getResponse

	public void setCheckingValidity(boolean b) {
		m_ValidityCheck = b;
	}// setCheckingValidity

	public boolean isCheckingValidity() {
		return m_ValidityCheck;
	}// isCheckingValidity

	public int getRetries() {
		return m_Retries;
	}// getRetries

	public void setRetries(int num) {
		m_Retries = num;
	}// setRetries

	/**
	 * Get the TransDelayMS value.
	 * 
	 * @return the TransDelayMS value.
	 */
	public int getTransDelayMS() {
		return m_TransDelayMS;
	}

	/**
	 * Set the TransDelayMS value.
	 * 
	 * @param newTransDelayMS The new TransDelayMS value.
	 */
	public void setTransDelayMS(int newTransDelayMS) {
		this.m_TransDelayMS = newTransDelayMS;
	}

	public void execute() throws CCModbusException {
		// 1. assert executeability
		assertExecutable();

		// 3. write request, and read response,
		// while holding the lock on the IO object
		synchronized (m_IO) {
			int tries = 0;
			boolean finished = false;
			do {
				try {
					if (m_TransDelayMS > 0) {
						try {
							Thread.sleep(m_TransDelayMS);
						} catch (InterruptedException ex) {
							if (Modbus.debug)
								CCLog.error("InterruptedException: " + ex.getMessage());
						}
					}
					// write request message
					m_IO.writeMessage(m_Request);
					// read response message
					m_Response = m_IO.readResponse();
					finished = true;
				} catch (ModbusIOException e) {
					if (++tries >= m_Retries) {
						throw e;
					}
					if (Modbus.debug)
						CCLog.error("Execute try " + tries + " error: " + e.getMessage());
				}
			} while (!finished);
		}

		// 4. deal with exceptions
		if (m_Response instanceof CCExceptionResponse) {
			throw new CCModbusSlaveException(((CCExceptionResponse) m_Response).exceptionCode());
		}

		if (isCheckingValidity()) {
			checkValidity();
		}
		// toggle the id
		toggleTransactionID();
	}// execute

	/**
	 * Asserts if this <tt>ModbusTCPTransaction</tt> is executable.
	 *
	 * @throws CCModbusException if the transaction cannot be asserted.
	 */
	private void assertExecutable() throws CCModbusException {
		if (m_Request == null || m_IO == null) {
			throw new CCModbusException("Assertion failed, transaction not executable");
		}
	}// assertExecuteable

	/**
	 * Checks the validity of the transaction, by checking if the values of the
	 * response correspond to the values of the request.
	 *
	 * @throws CCModbusException if the transaction is not valid.
	 */
	private void checkValidity() {

	}// checkValidity

	/**
	 * Toggles the transaction identifier, to ensure that each transaction has a
	 * distinctive identifier.<br>
	 * When the maximum value of 65535 has been reached, the identifiers will
	 * start from zero again.
	 */
	private void toggleTransactionID() {
		if (isCheckingValidity()) {
			if (c_TransactionID == (Short.MAX_VALUE * 2)) {
				c_TransactionID = 0;
			} else {
				c_TransactionID++;
			}
		}
		m_Request.transactionID(getTransactionID());
	}// toggleTransactionID

}// class ModbusSerialTransaction
