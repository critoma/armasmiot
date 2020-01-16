package com.oracle.dio;

import com.oracle.dio.spi.*;
import com.oracle.dio.gpio.*;

import jdk.dio.*;

// @IoTApplication
public class DioSpiGpio
{
	private int adcSpiMcp3008_Device; //300
	private int analogChannelTemperature; //7
	private int analogChannelHumidity; //5
	private int gpioPin; // 21

	private MCP3008 adc = null;

	public DioSpiGpio() {
	}

	public void open(int adcSpiMcp3008_Device, int analogChannelTemperature, int analogChannelHumidity, int gpioPin) {
		this.adcSpiMcp3008_Device = adcSpiMcp3008_Device;
        	this.analogChannelTemperature = analogChannelTemperature;
		this.analogChannelHumidity = analogChannelHumidity;
		this.gpioPin = gpioPin;

		try {
	            adc = (MCP3008)DeviceManager.open(new MCP3008Config(this.adcSpiMcp3008_Device));
		} catch (Exception e) {
			e.printStackTrace();
	        }
	}

	public void close() {
		try {
	        	if (adc != null) {
	                    adc.close();
	                }
	        } catch (Exception e) {
	                // ignore, we're exiting	        
        	}
	}

	public double readTemperature() {
		// 2. Read temperature from A7
		int value = adc.readChannel(this.analogChannelTemperature);

		// apply ADC calculus for voltage value on 10 bits (2^10 = 1024)
		double voltage = value * 3.3;
		voltage /= 1024.0;
		
		// apply specific calculus for the temperature sensor
		double tempCelsius = (voltage - 0.5) * 100;

		return tempCelsius;
	}

	public double readHumidity() {
		// 1. Read humidity from A5
		int valueH = adc.readChannel(this.analogChannelHumidity);

		// apply ADC calculus for voltage value on 10 bits (2^10 = 1024)
		double voltageH = valueH * 3.3;
		voltageH /= 1024.0;

		return voltageH;
	}

	public double convertCelsius2F(double tempCelsius) {
		double tempF = tempCelsius * 2 + 22;
		return tempF;
	}

	public void blinkLed() {
		GPIOLedImpl.blinkLEDByGPIONumber(this.gpioPin);
	}

/*
	public static void printAllSensorsValues(int adcSpiMcp3008_Device, int analogChannelTemperature, int analogChannelHumidity, int gpioPin) {
		//int device = 300;
        	//int channel = 7;
		//int channelH = 5;
		//int pinValue = 21;

		int device = adcSpiMcp3008_Device;
        	int channel = analogChannelTemperature;
		int channelH = analogChannelHumidity;
		int pinValue = gpioPin;

		System.out.println("Test DIO SPI and GPIO");
	        MCP3008 adc = null;
	        try {
	            adc = (MCP3008)DeviceManager.open(new MCP3008Config(device));

		    //while (true) {
			
			// 1. Read humidity from A5
			int valueH = adc.readChannel(channelH);
			//System.out.println("sensor value voltage = " + value);
			// apply ADC calculus for voltage value on 10 bits (2^10 = 1024)
			double voltageH = valueH * 3.3;
			voltageH /= 1024.0;		


			// 2. Read temperature from A7
			int value = adc.readChannel(channel);
			//System.out.println("sensor value voltage = " + value);
			// apply ADC calculus for voltage value on 10 bits (2^10 = 1024)
			double voltage = value * 3.3;
			voltage /= 1024.0;
		
			// apply specific calculus for the temperature sensor
			double tempCelsius = (voltage - 0.5) * 100;
			double tempF = tempCelsius * 2 + 22;
		        
			System.out.println("MCP3008: Channel " + channel + ": temperature = " + tempCelsius +" C, " + tempF + " F; channel " + channelH + ": humidity = " + voltageH);

			if (tempCelsius > 28)
				GPIOLedImpl.blinkLEDByGPIONumber(pinValue);		
			
			Thread.sleep(2000); // 2 sec
		    //} //end for
		    
	        } catch (Exception e) {
			e.printStackTrace();
	        } finally {
	            try {
	                if (adc != null) {
	                    adc.close();
	                }
	            } catch (Exception e) {
	                // ignore, we're exiting
	            }
        	}
		

	}
*/
}