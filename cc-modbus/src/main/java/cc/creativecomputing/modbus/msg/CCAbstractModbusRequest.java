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

/***
 * Java Modbus Library (jamod)
 * Copyright 2010, greenHouse Computers, LLC
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

import cc.creativecomputing.modbus.CCModbusExceptionCode;
import cc.creativecomputing.modbus.CCModbusFunctionCode;

/**
 * Abstract class implementing a <tt>ModbusRequest</tt>. This class provides
 * specialised implementations with the functionality they have in common.
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 * 
 * @author jfhaugh (jfh@ghgande.com)
 * @version 1.2rc1-ghpc (09/27/2010) Added READ_MEI stuff.
 * 
 * @version 1.2rc1-ghpc (02/14/2011) Added REPORT_SLAVE_ID stuff.
 * 
 * @version 1.0-jamod (7/7/2012) Added new messages.
 */
public abstract class CCAbstractModbusRequest extends CCAbstractModbusMessage {

	/**
	 * Returns the <tt>ModbusResponse</tt> that correlates with this
	 * <tt>ModbusRequest</tt>.
	 * 
	 * <p>
	 * The response must include the unit number, function code, as well as any
	 * transport-specific header information.
	 * 
	 * <p>
	 * This method is used to create an empty response which must be populated
	 * by the caller. It is commonly used to un-marshal responses from Modbus
	 * slaves.
	 * 
	 * @return the corresponding <tt>ModbusResponse</tt>.
	 */
	public abstract CCAbstractModbusResponse response();

	/**
	 * Returns the <tt>ModbusResponse</tt> that represents the answer to this
	 * <tt>ModbusRequest</tt>.
	 * 
	 * <p>
	 * The implementation should take care about assembling the reply to this
	 * <tt>ModbusRequest</tt>.
	 * 
	 * <p>
	 * This method is used to create responses from the process image associated
	 * with the ModbusCoupler. It is commonly used to implement Modbus slave
	 * instances.
	 * 
	 * @return the corresponding <tt>ModbusResponse</tt>.
	 */
	public abstract CCAbstractModbusResponse createResponse();

	/**
	 * Factory method for creating exception responses with the given exception
	 * code.
	 * 
	 * @param theExceptionCode the code of the exception.
	 * @return a ModbusResponse instance representing the exception response.
	 */
	public CCAbstractModbusResponse createExceptionResponse(CCModbusExceptionCode theExceptionCode) {
		CCExceptionResponse response = new CCExceptionResponse(functionCode(), theExceptionCode);
		if (!isHeadless()) {
			response.transactionID(transactionID());
			response.protocolID(protocolID());
		} else {
			response.setHeadless();
		}
		response.unitID(unitID());
		return response;
	}

	/**
	 * Factory method creating the required specialized <tt>ModbusRequest</tt>
	 * instance.
	 * 
	 * @param theFunctionCode the function code of the request as <tt>int</tt>.
	 * @return a ModbusRequest instance specific for the given function type.
	 */
	public static CCAbstractModbusRequest createModbusRequest(CCModbusFunctionCode theFunctionCode) {
		CCAbstractModbusRequest request = null;

		switch (theFunctionCode) {
		case READ_COILS:
			request = new ReadCoilsRequest();
			break;
		case READ_INPUT_DISCRETES:
			request = new ReadInputDiscretesRequest();
			break;
		case READ_MULTIPLE_REGISTERS:
			request = new ReadMultipleRegistersRequest();
			break;
		case READ_INPUT_REGISTERS:
			request = new ReadInputRegistersRequest();
			break;
		case WRITE_COIL:
			request = new WriteCoilRequest();
			break;
		case WRITE_SINGLE_REGISTER:
			request = new WriteSingleRegisterRequest();
			break;
		case WRITE_MULTIPLE_COILS:
			request = new WriteMultipleCoilsRequest();
			break;
		case WRITE_MULTIPLE_REGISTERS:
			request = new WriteMultipleRegistersRequest();
			break;
		case READ_EXCEPTION_STATUS:
			request = new ReadExceptionStatusRequest();
			break;
		case READ_SERIAL_DIAGNOSTICS:
			request = new ReadSerialDiagnosticsRequest();
			break;
		case READ_COMM_EVENT_COUNTER:
			request = new ReadCommEventCounterRequest();
			break;
		case READ_COMM_EVENT_LOG:
			request = new ReadCommEventLogRequest();
			break;
		case REPORT_SLAVE_ID:
			request = new ReportSlaveIDRequest();
			break;
		case READ_FILE_RECORD:
			request = new ReadFileRecordRequest();
			break;
		case WRITE_FILE_RECORD:
			request = new WriteFileRecordRequest();
			break;
		case MASK_WRITE_REGISTER:
			request = new CCMaskWriteRegisterRequest();
			break;
		case READ_WRITE_MULTIPLE:
			request = new ReadWriteMultipleRequest();
			break;
		case READ_FIFO_QUEUE:
			request = new ReadFIFOQueueRequest();
			break;
		case READ_MEI:
			request = new ReadMEIRequest();
			break;
		default:
			request = new CCIllegalFunctionRequest(theFunctionCode);
			break;
		}
		return request;
	}
}
