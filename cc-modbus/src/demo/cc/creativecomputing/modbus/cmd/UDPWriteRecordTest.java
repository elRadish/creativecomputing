/**
 * Java Modbus Library (j2mod)
 * Copyright 2012, Julianne Frances Haugh
 * d/b/a greenHouse Gas and Electric
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
 */
package cc.creativecomputing.modbus.cmd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import cc.creativecomputing.modbus.Modbus;
import cc.creativecomputing.modbus.CCModbusException;
import cc.creativecomputing.modbus.ModbusIOException;
import cc.creativecomputing.modbus.CCModbusSlaveException;
import cc.creativecomputing.modbus.io.ModbusTransaction;
import cc.creativecomputing.modbus.io.ModbusUDPTransaction;
import cc.creativecomputing.modbus.msg.CCExceptionResponse;
import cc.creativecomputing.modbus.msg.CCAbstractModbusResponse;
import cc.creativecomputing.modbus.msg.ReadFileRecordRequest;
import cc.creativecomputing.modbus.msg.ReadFileRecordResponse;
import cc.creativecomputing.modbus.msg.WriteFileRecordRequest;
import cc.creativecomputing.modbus.msg.WriteFileRecordResponse;
import cc.creativecomputing.modbus.msg.ReadFileRecordRequest.RecordRequest;
import cc.creativecomputing.modbus.msg.ReadFileRecordResponse.RecordResponse;
import cc.creativecomputing.modbus.net.UDPMasterConnection;

/**
 * WriteRecordText -- Exercise the "WRITE FILE RECORD" Modbus
 * message.
 * 
 * @author Julie
 * @version 0.96
 */
public class UDPWriteRecordTest {

	/**
	 * usage -- Print command line arguments and exit.
	 */
	private static void usage() {
		System.out.println(
				"Usage: UDPWriteRecordTest address[:port[:unit]] file record registers [count]");
		
		System.exit(1);
	}

	public static void main(String[] args) {
		InetAddress	ipAddress = null;
		int			port = Modbus.DEFAULT_PORT;
		int			unit = 0;
		UDPMasterConnection connection = null;
		ReadFileRecordRequest rdRequest = null;
		ReadFileRecordResponse rdResponse = null;
		WriteFileRecordRequest wrRequest = null;
		WriteFileRecordResponse wrResponse = null;
		ModbusTransaction	trans = null;
		int			file = 0;
		int			record = 0;
		int			registers = 0;
		int			requestCount = 1;

		/*
		 * Get the command line parameters.
		 */
		if (args.length < 4 || args.length > 5)
			usage();
		
		String serverAddress = args[0];
		String parts[] = serverAddress.split(":");
		String hostName = parts[0];
		
		try {
			/*
			 * Address is of the form
			 * 
			 * hostName:port:unitNumber
			 * 
			 * where
			 * 
			 * hostName -- Standard text host name
			 * port		-- Modbus port, 502 is the default
			 * unit		-- Modbus unit number, 0 is the default
			 */
			if (parts.length > 1) {
				port = Integer.parseInt(parts[1]);
				
				if (parts.length > 2)
					unit = Integer.parseInt(parts[2]);
			}
			ipAddress = InetAddress.getByName(hostName);
			
			file = Integer.parseInt(args[1]);
			record = Integer.parseInt(args[2]);
			registers = Integer.parseInt(args[3]);
			
			if (args.length > 4)
				requestCount = Integer.parseInt(args[4]);
		} catch (NumberFormatException x) {
			System.err.println("Invalid parameter");
			usage();
		} catch (UnknownHostException x) {
			System.err.println("Unknown host: " + hostName);
			System.exit(1);
		} catch (Exception ex) {
			ex.printStackTrace();
			usage();
			System.exit(1);
		}

		try {
			
			/*
			 * Setup the UDP connection to the Modbus/UDP Master
			 */
			connection = new UDPMasterConnection(ipAddress);
			connection.setPort(port);
			connection.connect();
			connection.setTimeout(500);

			if (Modbus.debug)
				System.out.println("Connected to " + ipAddress.toString() + ":"
						+ connection.getPort());

			for (int i = 0; i < requestCount; i++) {
				/*
				 * Setup the READ FILE RECORD request.  The record number
				 * will be incremented for each loop.
				 */
				rdRequest = new ReadFileRecordRequest();
				rdRequest.unitID(unit);
				
				RecordRequest recordRequest =
						rdRequest.new RecordRequest(file, record + i, registers);
				rdRequest.addRequest(recordRequest);
				
				if (Modbus.debug)
					System.out.println("Request: " + rdRequest.hexMessage());

				/*
				 * Setup the transaction.
				 */
				trans = new ModbusUDPTransaction(connection);
				trans.setRequest(rdRequest);

				/*
				 * Execute the transaction.
				 */
				try {
					trans.execute();
				} catch (CCModbusSlaveException x) {
					System.err.println("Slave Exception: " +
							x.getLocalizedMessage());
					continue;
				} catch (ModbusIOException x) {
					System.err.println("I/O Exception: " +
							x.getLocalizedMessage());
					continue;					
				} catch (CCModbusException x) {
					System.err.println("Modbus Exception: " +
							x.getLocalizedMessage());
					continue;					
				}
				
				short values[];
				
				wrRequest = new WriteFileRecordRequest();
				wrRequest.unitID(unit);

				CCAbstractModbusResponse dummy = trans.getResponse();
				if (dummy == null) {
					System.err.println("No response for transaction " + i);
					continue;
				}
				if (dummy instanceof CCExceptionResponse) {
					CCExceptionResponse exception = (CCExceptionResponse) dummy;

					System.err.println(exception);

					continue;
				} else if (dummy instanceof ReadFileRecordResponse) {
					rdResponse = (ReadFileRecordResponse) dummy;

					if (Modbus.debug)
						System.out.println("Response: "
								+ rdResponse.hexMessage());

					int count = rdResponse.getRecordCount();
					for (int j = 0;j < count;j++) {
						RecordResponse data = rdResponse.getRecord(j);
						values = new short[data.getWordCount()];
						for (int k = 0;k < data.getWordCount();k++)
							values[k] = data.getRegister(k).toShort();
						
						System.out.println("read data[" + j + "] = " +
								Arrays.toString(values));
						
						WriteFileRecordRequest.RecordRequest wrData =
								wrRequest.new RecordRequest(file, record + i, values);
						wrRequest.addRequest(wrData);
					}
				} else {
					/*
					 * Unknown message.
					 */
					System.out.println(
							"Unknown Response: " + dummy.hexMessage());
				}
				
				/*
				 * Setup the transaction.
				 */
				trans = new ModbusUDPTransaction(connection);
				trans.setRequest(wrRequest);

				/*
				 * Execute the transaction.
				 */
				try {
					trans.execute();
				} catch (CCModbusSlaveException x) {
					System.err.println("Slave Exception: " +
							x.getLocalizedMessage());
					continue;
				} catch (ModbusIOException x) {
					System.err.println("I/O Exception: " +
							x.getLocalizedMessage());
					continue;					
				} catch (CCModbusException x) {
					System.err.println("Modbus Exception: " +
							x.getLocalizedMessage());
					continue;					
				}
				
				dummy = trans.getResponse();
				if (dummy == null) {
					System.err.println("No response for transaction " + i);
					continue;
				}
				if (dummy instanceof CCExceptionResponse) {
					CCExceptionResponse exception = (CCExceptionResponse) dummy;

					System.err.println(exception);

					continue;
				} else if (dummy instanceof WriteFileRecordResponse) {
					wrResponse = (WriteFileRecordResponse) dummy;

					if (Modbus.debug)
						System.out.println("Response: "
								+ wrResponse.hexMessage());

					int count = wrResponse.getRequestCount();
					for (int j = 0;j < count;j++) {
						WriteFileRecordResponse.RecordResponse data =
								wrResponse.getRecord(j);
						values = new short[data.getWordCount()];
						for (int k = 0;k < data.getWordCount();k++)
							values[k] = data.getRegister(k).toShort();
						
						System.out.println("write response data[" + j + "] = " +
								Arrays.toString(values));
					}
				} else {
					/*
					 * Unknown message.
					 */
					System.out.println(
							"Unknown Response: " + dummy.hexMessage());
				}
			}
			
			/*
			 * Teardown the connection.
			 */
			connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
