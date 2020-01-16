package com.oracle.dio;

import com.oracle.dio.gpio.*;
import java.io.*;


// @IoTApplication

import jdk.dio.*;
//import jdk.dio.uart.UART;
//import jdk.dio.uart.UARTConfig;
import jdk.dio.*;
import jdk.dio.uart.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.*;


public class IoTUart2GalileoArduino {
    
    /**
     * Name of serial device to open
     */
/*
    private String UART_DEVICE_NAME = "ttyUSB0";
    
    private BufferedReader serialBufferedReader;
    private BufferedWriter serialBufferedWriter;
    private InputStream serialInputStream;
    private OutputStream serialOutputStream;
*/    
    /**
     * This variable is a temperature reading that was read during poll method
     */
/*
    private String temp = "";
    private String ec = "";
    
    private final static String commandTemp = "/getTemp";
    private final static int MAX_TECMD_SIZE = commandTemp.getBytes().length;
    
    private final static String commandEc = "/getEc";
    private final static int MAX_ECCMD_SIZE = commandEc.getBytes().length;
    
    private final static int MAX_RES_SIZE = 100;
    
    private static ByteBuffer cmdTempBuf = ByteBuffer.allocateDirect(MAX_TECMD_SIZE);
    private static ByteBuffer cmdEcBuf = ByteBuffer.allocateDirect(MAX_ECCMD_SIZE);
    
    private static ByteBuffer resTempBuf = ByteBuffer.allocateDirect(MAX_RES_SIZE);
    private static ByteBuffer resEcBuf = ByteBuffer.allocateDirect(MAX_RES_SIZE);
    
    private static Object myLock = new Object();
    
    int respSize = 0;
*/    
    /**
     * UART device
     */
    //private UART uart;
    //private int gpioPin;

    private static UARTConfig config = null;
    private static UART uart = null;

    private static OutputStream os = null;
    private static InputStream is = null;

    private static BufferedWriter writer = null;
    //private static BufferedReader reader = null;

/*
    public IoTUart2GalileoArduino() {
        
    }
*/
    /**
     * Opens the UART device, and if successful creates a uart thermometer endpoint
     */
/*
    public void openUartAndGpio(String uartDeviceName, int gpioPin) {
        if (uartDeviceName != null) {
            this.UART_DEVICE_NAME = uartDeviceName;
        }
        
        String uartDevice = uartDeviceName;
        System.out.println("Opening UART device: " + uartDevice);
        config = new UARTConfig(uartDevice,
                                           DeviceConfig.DEFAULT,
                                           9600,
                                           UARTConfig.DATABITS_8,
                                           UARTConfig.PARITY_NONE,
                                           UARTConfig.STOPBITS_1,
                                           UARTConfig.FLOWCONTROL_NONE);
        
        try {
            this.uart = DeviceManager.open(config);
            this.gpioPin = gpioPin;
          
        } catch (Exception e) {
            // There are 5 exception types that can be thrown by the open method.
            // For this adapter, simply display the exception and return
            e.printStackTrace();
        }
    }
    
    public UART getUart() {
        return this.uart;
    }

    
    public void closeUart() throws IOException {
        uart.close();
    }
*/
	public static void init() throws IOException {
		try {
			init("ttyACM0", DeviceConfig.DEFAULT, 9600, UARTConfig.DATABITS_8, UARTConfig.PARITY_NONE, UARTConfig.STOPBITS_1, UARTConfig.FLOWCONTROL_NONE);
		} catch (IOException ioe) {
	            throw new IOException("IOException while opening device. Make sure you have the appropriate operating system permission to access UART devices.", ioe);
	        } 
	}

	public static void init(String deviceName) throws IOException {
		try {
			init(deviceName, DeviceConfig.DEFAULT, 9600, UARTConfig.DATABITS_8, UARTConfig.PARITY_NONE, UARTConfig.STOPBITS_1, UARTConfig.FLOWCONTROL_NONE);
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
				System.out.println("Writer = " + text);
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
				System.out.println("Reader = " + sb.toString());
			}
	        } catch (IOException ioe) {
	            throw new IOException("IOException while reading data over the device. Make sure you have the appropriate operating system permission to access UART devices.", ioe);
	        } 
		return sb.toString();
	}

	public static void blinkLed(int gpioPin) {
		GPIOLedImpl.blinkLEDByGPIONumber(gpioPin);
    	}


}
