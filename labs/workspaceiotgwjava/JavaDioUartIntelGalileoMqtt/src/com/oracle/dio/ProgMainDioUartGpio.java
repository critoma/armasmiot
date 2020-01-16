package com.oracle.dio;

import com.oracle.dio.gpio.*;

import jdk.dio.*;

import mqtt_paho_eclipse_mosquitto.client.MqttPublishClient;

// @IoTApplication
public class ProgMainDioUartGpio
{	

	public static void main(String[] args) {

		if (args == null || args.length != 2) {
			System.out.println("In parameters command line include: uartDeviceNameString (e.g. ttyUSB0), gpioPin");
			System.exit(-1);
		}
		String deviceName = args[0];
		int pinValue = Integer.parseInt(args[1]);

		System.out.println("Test DIO UART and GPIO");
        
        //IoTUart2GalileoArduino iotuart = new IoTUart2GalileoArduino();
        try {
            //iotuart.openUartAndGpio(deviceName, pinValue);
	    IoTUart2GalileoArduino.init(deviceName);
        
            while (true) {
                //iotuart.poll();
                
                //Double tempCelsius = iotuart.getTemperature();
                //Double ec = iotuart.getEC();
		IoTUart2GalileoArduino.sendTextSerial("/getTemp");
		String tempCelsius = IoTUart2GalileoArduino.readTextLineSerial();
		IoTUart2GalileoArduino.readTextLineSerial(); //for Enter CR/LF

		Thread.sleep(2000);

                if (Double.parseDouble(tempCelsius) > 20) {
                    //iotuart.blinkLed();
		    IoTUart2GalileoArduino.blinkLed(pinValue);
                }

		IoTUart2GalileoArduino.sendTextSerial("/getEc");
		String ec = IoTUart2GalileoArduino.readTextLineSerial();
		IoTUart2GalileoArduino.readTextLineSerial(); //for enter - CR/LF

                System.out.println("Temperature = " + tempCelsius + ", ec = " + ec);
            
                // check-out: http://test.mosquitto.org/gauge/
                MqttPublishClient.publish("temp/random", "tcp://test.mosquitto.org:1883", "IoTSunRPi001-IoT-Node", 2, ""+tempCelsius);
            
                Thread.sleep(1000); // 1 sec
            }
            
        } catch (Exception e) {
			e.printStackTrace();
        } finally {
	            try {
			IoTUart2GalileoArduino.close();
/*
	                if (iotuart != null) {
	                    iotuart.closeUart();
	                }
*/
	            } catch (Exception e) {
	                // ignore, we're exiting
	            }

        }
		

	}
}
