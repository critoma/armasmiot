package com.oracle.dio.gpio;


import java.util.*;

import jdk.dio.*;
import jdk.dio.gpio.*;
import java.io.IOException;


/* GPIOLedImpl
 * This class demonstrates how to use the use an output GPIO pin to control an LED.
 * For wiring instructions see Device I/O wiki page at https://wiki.openjdk.java.net/display/dio/Getting+Started.
 */
public class GPIOLedImpl {
	public static void blinkLEDByGPIONumber(int pinNumber) {
	        System.out.println("Blinking LED on GPIO pin number " + pinNumber);
	        GPIOPin pin = null;
	        try {
	            GPIOPinConfig pinConfig = new GPIOPinConfig(DeviceConfig.DEFAULT,
	                                                        pinNumber,
	                                                        GPIOPinConfig.DIR_OUTPUT_ONLY,
	                                                        GPIOPinConfig.MODE_OUTPUT_PUSH_PULL,
	                                                        GPIOPinConfig.TRIGGER_NONE,
	                                                        false);
	            pin = (GPIOPin)DeviceManager.open(GPIOPin.class, pinConfig);
	            boolean pinOn = false;
	            //for (int i = 0; i < 20; i++) 
		    {	                
	                pinOn = !pinOn;
	                pin.setValue(pinOn);
			Thread.sleep(100);
	            }
	        } catch (IOException ioe) {
	            throw new LEDExampleException("IOException while opening device. Make sure you have the appropriate operating system permission to access GPIO devices.", ioe);
	        } catch(InterruptedException ie) {
	            // ignore
	        } finally {
	            try {
	                System.out.println("Closing GPIO pin number " + pinNumber);
	                if (pin != null) {
	                    pin.close();
	                }
	            } catch (Exception e) {
	                throw new LEDExampleException("Received exception while trying to close device.", e);
            	    }
        	}
	}
}


class LEDExampleException extends RuntimeException {
    public LEDExampleException(String msg, Throwable t) {
        super(msg,t);
    }
}