

package com.oracle.iot.sample;


import com.oracle.dio.DioSpiI2c;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Properties;


public class DeviceClientSample {


    private double temperature;


    // The rate, in milliseconds, at which temperature readings are sent
    // to the server. Setting sampleRate to zero effectively turns sampling
    // off. The sample rate may be set via the /sample_rate resource.
    private int sampleRate;


    private double humidity;
    private double vibrations;
    private boolean led;


    // The minimum acceptable value for sample_rate, in milliseconds.
    private static final int SAMPLE_RATE = 1000;

    private boolean running = false;

    private DeviceClientSample() {
	this.temperature = 20.0;
	this.sampleRate = 1 * 1000;

	this.humidity = 0;
	this.vibrations = 0;
	this.led = false;

       this.running = true;
    }


    public static void main(String[] args) {

        DeviceClientSample dcs = new DeviceClientSample();
	DioSpiI2c dioSpiI2c = null;

	double TEMPERATURE_UPPER_LIMIT = 24;

        try {

            while (dcs.running) {
		try {	

		    	dioSpiI2c = new DioSpiI2c();

			dioSpiI2c.openSpiAndI2C(301, 41);
			dioSpiI2c.readTemperatureAndHumidity();		
			dcs.temperature = dioSpiI2c.getTemperature();//temperature = dioSpiI2c.convertCelsius2F(temperature);
			dcs.humidity = dioSpiI2c.getHumidity();
			
			dioSpiI2c.readVibrations();
	    		dcs.vibrations = dioSpiI2c.getVibrations();

			if (dcs.temperature > 23) dcs.led = true; else dcs.led = false;
			if (dcs.led == true)
				dioSpiI2c.blinkLed();
			
			System.out.println("Temperature = " + dcs.temperature + ", humidity = " + dcs.humidity + ", vibrations = " + dcs.vibrations);
			

		     	try {
			   if (dioSpiI2c != null) { 
				dioSpiI2c.close(); 
				dioSpiI2c = null; 
			   }
                      	
			} catch (Exception e) {
				e.printStackTrace();
			}

			Thread.sleep(DeviceClientSample.SAMPLE_RATE);
                    
		} catch(Exception e) {
			e.printStackTrace();
		}

            }

            // stop sending and receiving messages, and release resources
            System.out.println("Close deviceClient");

        } catch (IllegalStateException e) {
            System.exit(-1);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
