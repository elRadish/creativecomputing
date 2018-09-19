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
package cc.creativecomputing.demo.protocol.serial;

import cc.creativecomputing.app.CCApplication;
import cc.creativecomputing.app.modules.CCBasicAppListener;
import cc.creativecomputing.core.logging.CCLog;
import cc.creativecomputing.protocol.serial.CCSerialInput;
import cc.creativecomputing.protocol.serial.CCSerialEvent;
import cc.creativecomputing.protocol.serial.CCSerialModule;

public class CCSerialTest implements CCSerialEvent, CCBasicAppListener{

	@Override
	public void start() {
		for(String myPort:CCSerialModule.list()){
			CCLog.info(myPort);
		}
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}
	
	private StringBuffer _myMessageBuffer = new StringBuffer();
	
	@Override
	public void event(CCSerialInput theSerialPort) {
		char myChar = theSerialPort.readChar();
		if(myChar == '\n'){
			CCLog.info(_myMessageBuffer.toString().trim());
			_myMessageBuffer = new StringBuffer();
			return;
		}
		_myMessageBuffer.append(myChar);
	}
	
	public static void main(String[] args) {
//		CCApplication myApplicationManager = new CCApplication(CCSerialTest.class);
//		
//		CCSerialModule mySerialModule = new CCSerialModule();
//		myApplicationManager.addModule(mySerialModule);
////		mySerialModule. = "COM1";
//		mySerialModule.rate = 57600;
		
		for(String myPort:CCSerialModule.list()){
			CCLog.info(myPort);
		}
		
		
//		myApplicationManager.start();
	}
}
