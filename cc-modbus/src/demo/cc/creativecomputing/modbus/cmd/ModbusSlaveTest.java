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
package cc.creativecomputing.modbus.cmd;

import cc.creativecomputing.modbus.Modbus;
import cc.creativecomputing.modbus.ModbusCoupler;
import cc.creativecomputing.modbus.net.ModbusListener;
import cc.creativecomputing.modbus.net.ModbusListenerFactory;
import cc.creativecomputing.modbus.procimg.File;
import cc.creativecomputing.modbus.procimg.Record;
import cc.creativecomputing.modbus.procimg.SimpleDigitalIn;
import cc.creativecomputing.modbus.procimg.SimpleDigitalOut;
import cc.creativecomputing.modbus.procimg.SimpleInputRegister;
import cc.creativecomputing.modbus.procimg.SimpleProcessImage;
import cc.creativecomputing.modbus.procimg.SimpleRegister;

/**
 * Class implementing a simple Modbus/TCP slave. A simple process image is
 * available to test functionality and behaviour of the implementation.
 * 
 * @author Julie Haugh
 * @version 0.97 (8/12/12)
 */
public class ModbusSlaveTest {
	public static void main(String[] args) {
		ModbusListener listener = null;
		SimpleProcessImage spi = null;

		try {
			System.out.println("j2mod Modbus Slave (Server) v0.97");

			/*
			 * Create the process image for this test.
			 */
			spi = new SimpleProcessImage();

			spi.addDigitalOut(new SimpleDigitalOut(true));
			spi.addDigitalOut(new SimpleDigitalOut(true));

			spi.addDigitalIn(new SimpleDigitalIn(false));
			spi.addDigitalIn(new SimpleDigitalIn(true));
			spi.addDigitalIn(new SimpleDigitalIn(false));
			spi.addDigitalIn(new SimpleDigitalIn(true));

			spi.addFile(new File(0, 10).setRecord(0, new Record(0, 10))
					.setRecord(1, new Record(1, 10))
					.setRecord(2, new Record(2, 10))
					.setRecord(3, new Record(3, 10))
					.setRecord(4, new Record(4, 10))
					.setRecord(5, new Record(5, 10))
					.setRecord(6, new Record(6, 10))
					.setRecord(7, new Record(7, 10))
					.setRecord(8, new Record(8, 10))
					.setRecord(9, new Record(9, 10)));
			
			spi.addFile(new File(1, 20)
					.setRecord(0, new Record(0, 10))
					.setRecord(1, new Record(1, 20))
					.setRecord(2, new Record(2, 20))
					.setRecord(3, new Record(3, 20))
					.setRecord(4, new Record(4, 20))
					.setRecord(5, new Record(5, 20))
					.setRecord(6, new Record(6, 20))
					.setRecord(7, new Record(7, 20))
					.setRecord(8, new Record(8, 20))
					.setRecord(9, new Record(9, 20))
					.setRecord(10, new Record(10, 10))
					.setRecord(11, new Record(11, 20))
					.setRecord(12, new Record(12, 20))
					.setRecord(13, new Record(13, 20))
					.setRecord(14, new Record(14, 20))
					.setRecord(15, new Record(15, 20))
					.setRecord(16, new Record(16, 20))
					.setRecord(17, new Record(17, 20))
					.setRecord(18, new Record(18, 20))
					.setRecord(19, new Record(19, 20))
					);

			// allow checking LSB/MSB order
			spi.addDigitalIn(new SimpleDigitalIn(true));
			spi.addDigitalIn(new SimpleDigitalIn(true));
			spi.addDigitalIn(new SimpleDigitalIn(true));
			spi.addDigitalIn(new SimpleDigitalIn(true));

			spi.addRegister(new SimpleRegister(251));
			spi.addInputRegister(new SimpleInputRegister(45));

			// 2. create the coupler holding the image
			ModbusCoupler.getReference().setProcessImage(spi);
			ModbusCoupler.getReference().setMaster(false);
			ModbusCoupler.getReference().setUnitID(15);

			// 3. create a listener with 3 threads in pool
			System.out.println("Creating.");

			listener = ModbusListenerFactory.createModbusListener(args[0]);
			
			System.out.println("Listening.");
			
			while (listener.isListening()) {
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException x) {
					listener.stop();
					break;
				}
			}
			
			System.out.println("Done.");
		} catch (Exception x) {
			if (Modbus.debug)
				x.printStackTrace();
			
			if (listener != null)
				listener.stop();
		}
	}
}
