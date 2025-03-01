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
package cc.creativecomputing.modbus.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import cc.creativecomputing.modbus.Modbus;
import cc.creativecomputing.modbus.io.ModbusTCPTransport;
import cc.creativecomputing.modbus.io.ModbusTransport;

/**
 * Class that implements a TCPSlaveConnection.
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 */
public class TCPSlaveConnection {

	// instance attributes
	private Socket m_Socket;
	private int m_Unit = 0;
	private int m_Timeout = Modbus.DEFAULT_TIMEOUT;
	private boolean m_Connected;
	private ModbusTCPTransport m_ModbusTransport;

	/**
	 * Constructs a <tt>TCPSlaveConnection</tt> instance using a given socket
	 * instance.
	 * 
	 * @param socket
	 *            the socket instance to be used for communication.
	 */
	public TCPSlaveConnection(Socket socket) {
		try {
			setSocket(socket);
		} catch (IOException ex) {
			if (Modbus.debug)
				System.out.println("TCPSlaveConnection::Socket invalid.");

			throw new IllegalStateException("Socket invalid.");
		}
	}

	/**
	 * Constructs a <tt>TCPSlaveConnection</tt> instance using a given socket
	 * instance.
	 * 
	 * @param socket
	 *            the socket instance to be used for communication.
	 * @param unit
	 *            the unit number for this slave connection.
	 */
	public TCPSlaveConnection(Socket socket, int unit) {
		m_Unit = unit;

		try {
			setSocket(socket);
		} catch (IOException ex) {
			if (Modbus.debug)
				System.out.println("TCPSlaveConnection::Socket invalid.");

			throw new IllegalStateException("Socket invalid.");
		}
	}

	/**
	 * Closes this <tt>TCPSlaveConnection</tt>.
	 */
	public void close() {
		if (m_Connected) {
			try {
				m_ModbusTransport.close();
				m_Socket.close();
			} catch (IOException ex) {
				if (Modbus.debug)
					ex.printStackTrace();
			}
			m_Connected = false;
		}
	}

	/**
	 * Returns the <tt>ModbusTransport</tt> associated with this
	 * <tt>TCPMasterConnection</tt>.
	 * 
	 * @return the connection's <tt>ModbusTransport</tt>.
	 */
	public ModbusTransport getModbusTransport() {
		return m_ModbusTransport;
	}

	/**
	 * Prepares the associated <tt>ModbusTransport</tt> of this
	 * <tt>TCPMasterConnection</tt> for use.
	 * 
	 * @param socket
	 *            the socket to be used for communication.
	 * @throws IOException
	 *             if an I/O related error occurs.
	 */
	private void setSocket(Socket socket) throws IOException {
		m_Socket = socket;
		
		if (m_ModbusTransport == null)
			m_ModbusTransport = new ModbusTCPTransport(m_Socket);
		else
			m_ModbusTransport.setSocket(m_Socket);

		m_Connected = true;
	}

	/**
	 * Returns the timeout for this <tt>TCPSlaveConnection</tt>.
	 * 
	 * @return the timeout as <tt>int</tt>.
	 */
	public int getTimeout() {
		return m_Timeout;
	}

	/**
	 * Sets the timeout for this <tt>TCPSlaveConnection</tt>.
	 * 
	 * @param timeout
	 *            the timeout in milliseconds as <tt>int</tt>.
	 */
	public void setTimeout(int timeout) {
		m_Timeout = timeout;

		try {
			m_Socket.setSoTimeout(m_Timeout);
		} catch (IOException ex) {
			// handle?
		}
	}

	/**
	 * Returns the destination port of this <tt>TCPSlaveConnection</tt>.
	 * 
	 * @return the port number as <tt>int</tt>.
	 */
	public int getPort() {
		return m_Socket.getLocalPort();
	}

	/**
	 * Returns the destination <tt>InetAddress</tt> of this
	 * <tt>TCPSlaveConnection</tt>.
	 * 
	 * @return the destination address as <tt>InetAddress</tt>.
	 */
	public InetAddress getAddress() {
		return m_Socket.getLocalAddress();
	}

	/**
	 * Returns the slave unit number for this connection. A unit number of 0
	 * means to accept all unit numbers, while a non-zero unit number means only
	 * to accept requests for that specific unit.
	 * 
	 * @returns unit number
	 */
	public int getUnitNumber() {
		return m_Unit;
	}

	/**
	 * Tests if this <tt>TCPSlaveConnection</tt> is connected.
	 * 
	 * @return <tt>true</tt> if connected, <tt>false</tt> otherwise.
	 */
	public boolean isConnected() {
		return m_Connected;
	}
}