package com.oracle.dio.xbee_uart;

import java.util.*;

import jdk.dio.*;
import jdk.dio.uart.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;


/* XBeeUartImpl
 * This class demonstrates how to use XBee Pro S1 device over UART for sending data over ZigBee
 */
public class XBeeUartImpl {

	private static UARTConfig config = null;
	private static UART uart = null;

	private static OutputStream os = null;
	private static InputStream is = null;

	private static BufferedWriter writer = null;
	//private static BufferedReader reader = null;

	public static void init() throws IOException {
		try {
			init("ttyUSB0", DeviceConfig.DEFAULT, 9600, UARTConfig.DATABITS_8, UARTConfig.PARITY_NONE, UARTConfig.STOPBITS_1, UARTConfig.FLOWCONTROL_NONE);
		} catch (IOException ioe) {
	            throw new IOException("IOException while opening device. Make sure you have the appropriate operating system permission to access UART devices.", ioe);
	        } 
	}

	public static void init(String deviceName, int channel, int baudRate, int dataBits, int parity, int stopBits, int flowControl) throws IOException {
		try {
			config = new UARTConfig(deviceName, channel, baudRate, dataBits, parity, stopBits, flowControl);
			uart = DeviceManager.open(config);

			os = Channels.newOutputStream(uart);
			is = Channels.newInputStream(uart);

			writer = new BufferedWriter(new OutputStreamWriter(os));
			//reader = new BufferedReader(new InputStreamReader(is));
		} catch (IOException ioe) {
	            throw new IOException("IOException while opening device. Make sure you have the appropriate operating system permission to access UART devices.", ioe);
	        } 
	}

	public static void close() throws IOException {
		if (writer != null) {
			writer.close();
			writer = null;
		}

		//if (reader != null) {
		//	reader.close();
		//	reader = null;
		//}

		if (os != null) os.close();
		if (is != null) is.close();

		if (uart != null) uart.close();
	}

	public static void sendTextSerial(String text) throws IOException {	        
	        try {
			if (writer != null) {
				writer.write(text);
				writer.flush();
				//System.out.println("Writer = " + text);
			}
	        } catch (IOException ioe) {
	            throw new IOException("IOException while sending data over the device. Make sure you have the appropriate operating system permission to access UART devices.", ioe);
	        } 
	}

	public static String readTextLineSerial() throws IOException {	        
		//String aux = ""; 
		int iaux;
		StringBuilder sb = new StringBuilder();
	        try {
			if (is != null) {
				iaux = is.read();
				while ( (iaux != 10) && (iaux != 13) ) {
					sb.append(Character.toString((char)iaux));
					iaux = is.read();
					
				}
				//System.out.println("Reader = " + sb.toString());
			}
	        } catch (IOException ioe) {
	            throw new IOException("IOException while reading data over the device. Make sure you have the appropriate operating system permission to access UART devices.", ioe);
	        } 
		return sb.toString();
	}
/*
	public static String readAllTextSerial() throws IOException {	        
		String aux = "";
		StringBuilder sb = new StringBuilder();
	        try {
			if (reader != null) {
				while ((aux = reader.readLine()) != null) {
					sb.append(aux).append("\r\n");
				}
				System.out.println("Reader = " + sb.toString());
			}
	        } catch (IOException ioe) {
	            throw new IOException("IOException while reading data over the device. Make sure you have the appropriate operating system permission to access UART devices.", ioe);
	        } 
		return sb.toString();
	}
*/

}

