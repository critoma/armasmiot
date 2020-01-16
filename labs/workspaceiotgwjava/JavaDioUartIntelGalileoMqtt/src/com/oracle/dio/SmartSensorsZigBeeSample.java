/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.oracle.iot.xbee_smart_sensors;

import com.oracle.dio.DioSpiI2c;
import com.oracle.dio.xbee_uart.XBeeUartImpl;
import com.oracle.dio.gpio.GPIOLedImpl;
import java.io.*;

public class SmartSensorsZigBeeSample {

    // Temperature
    private double temperature;

    // Threshold for temperature before an alert is issued
    private double threshold;

    // The rate, in milliseconds, at which temperature readings are sent
    // to the server. Setting sampleRate to zero effectively turns sampling
    // off. The sample rate may be set via the /sample_rate resource.
    private int sampleRate;

    private double humidity;
    private double vibrations;
    private boolean led;


    // The minimum acceptable value for sample_rate, in milliseconds.
    private static final int MIN_SAMPLE_RATE = 10;

    // The maximum acceptable value for sample_rate, in millisecons.
    private static final int MAX_SAMPLE_RATE = 30 * 1000;

    private boolean running = false;


    private static XBeeUartImpl dioXBeeDevice = null;

    private SmartSensorsZigBeeSample() {
	this.temperature = 20.0;
	this.threshold = this.temperature + 10;
	this.sampleRate = 1 * 1000;

	this.humidity = 0;
	this.vibrations = 0;
	this.led = false;
	this.running = true;	
	dioXBeeDevice = new XBeeUartImpl();	
    }

    /**
     * Get the current temperature value. This method is synchronized because
     * the value might be modified by a request from the server and the handler
     * is called on its own thread.
     * @return the temperature value
     */
    public synchronized double getTemperature() {
        return temperature;
    }

    /**
     * Set the current temperature value. This method is synchronized because
     * the value might be modified by a request from the server and the handler
     * is called on its own thread. This method is private since only the
     * device should modify the temperature value.
     * @param t the new value
     */
    private synchronized void setTemperature(double t) {
        temperature = t;
    }

    /**
     * Get the current temperature threshold value. This method is synchronized
     * because the value might be modified by a request from the server and
     * the handler is called on its own thread.
     * @return the temperature value
     */
    public synchronized double getTemperatureThreshold() {
        return threshold;
    }

    /**
     * Set the current temperature threshold value. This method is synchronized
     * because the value might be modified by a request from the server and
     * the handler is called on its own thread.
     * @param t the new value
     */
    public synchronized void setTemperatureThreshold(double t) {
        threshold = t;
    }

    /**
     * Get the current sampling rate. This method is synchronized because
     * the value might be modified by a request from the server and the handler
     * is called on its own thread.
     * @return the sampling rate
     */
    public synchronized int getSampleRate() {
        return sampleRate;
    }

    /**
     * Get the current sampling rate. This method is synchronized because
     * the value might be modified by a request from the server and the handler
     * is called on its own thread. This method is private since only the
     * device should modify the sampling rate value.
     * @param r the new value
     */
    private synchronized void setSampleRate(int r) {
        sampleRate = r;
    }

    public synchronized double getHumidity() {
        return humidity;
    }

    private synchronized void setHumidity(double h) {
        humidity = h;
    }

    public synchronized double getVibrations() {
        return vibrations;
    }

    private synchronized void setVibrations(double v) {
        vibrations = v;
    }

    public synchronized boolean getLed() {
        return led;
    }

    private synchronized void setLed(boolean ledValue) {
        led = ledValue;
    }

    /**
     * @return true if the temperature sending loop is running
     */
    public boolean isRunning() { return running; }

    private void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Log a message.
     * @param s The message to log
     */
    public static void log(String s) {
        System.out.println(s);
    }


    public static void main(String[] args) {

        SmartSensorsZigBeeSample smartSensors = new SmartSensorsZigBeeSample();

	double TEMPERATURE_UPPER_LIMIT = 30;

        try {
		double temperature = smartSensors.getTemperature();
		double humidity = smartSensors.getHumidity();
		double vibrations = smartSensors.getVibrations();
		boolean ledV = smartSensors.getLed();

		int itBad = 0;
		double[] tempBadDelta = new double[] {1,1,1,2,2,2,2,2,2,2,2,3,3,3,3,3,3,4,4,4,4,4,5,5,5,5,5,5,5,5,6,6,6,6,6,6,6,6,7,7,7,7,7,8,8,8,8,8,8,9,9,9,9,10,10,10,10,10,10,10};
		double[] humidityBadDelta = new double[] {1,1,2,2,3,3,4,4,5,5,6,7,8,9,10,1,1,2,2,3,3,4,4,5,5,6,7,8,9,10,1,1,2,2,3,3,4,4,5,5,6,7,8,9,10,1,1,2,2,3,3,4,4,5,5,6,7,8,9,10};
		double[] vibeBadDelta = new double[] {2,2,2,2,2,2,2,2,2,4,4,4,4,4,4,4,6,6,6,6,6,6,6,6,6,6,6,6,6,8,8,8,8,8,8,8,8,8,9,9,9,9,9,9,9,9,9,9,10,10,10,10,10,10,10,10,10,10,10,10};

		dioXBeeDevice.init();
		DioSpiI2c dioSpiI2c = null;

		while (smartSensors.isRunning()) {
			try {

				if (!smartSensors.isRunning()) break;	

				dioSpiI2c = new DioSpiI2c();
	
				dioSpiI2c.openSpiAndI2C(301, 41);
				dioSpiI2c.readTemperatureAndHumidity();		
				temperature = dioSpiI2c.getTemperature(); 
				double temperatureF = dioSpiI2c.convertCelsius2F(temperature);
				humidity = dioSpiI2c.getHumidity();
				
				dioSpiI2c.readVibrations();
		    		vibrations = dioSpiI2c.getVibrations();
	
				ledV = smartSensors.getLed();
				if (ledV == true)
					dioSpiI2c.blinkLed();
				
				smartSensors.setTemperature(temperature); 
				smartSensors.setHumidity(humidity); 
				smartSensors.setVibrations(vibrations);


				// proprietary protocol:
				// /getSmartSensors/temperature
				// /getSmartSensors/humidity
				// /getSmartSensors/vibrations
 				// /getSmartSensors/all
				String path = dioXBeeDevice.readTextLineSerial();
				String strEnd = dioXBeeDevice.readTextLineSerial();
				log("path command = " + path + strEnd);

				String msg; String command = "";
				try {
				java.util.regex.Pattern p = java.util.regex.Pattern.compile("^[/\\\\]?(?:.+[/\\\\]+?)?(.+?)[/\\\\]?$");
				java.util.regex.Matcher matcher = p.matcher(path);
				if ( matcher.find() ) command = matcher.group(1);
				
				if (command.compareTo("temperature") == 0) {
					//msg = new String("{temperature: " + temperature + ", temperatureF: " + temperatureF + "}\r\n");
					msg = new String("" + temperature + "\r\n");
				} else if(command.compareTo("humidity") == 0) {
					//msg = new String("{humidity: " + humidity + "}\r\n");
					msg = new String("" + humidity + "\r\n");
				} else if(command.compareTo("vibrations") == 0) {
					//msg = new String("{vibrations: " + vibrations + "}\r\n");
					msg = new String("" + vibrations + "\r\n");
				} else if(command.compareTo("all") == 0) {
					msg = new String("{temperature: " + temperature + ", temperatureF: " + temperatureF + ", humidity: " + humidity + ", vibrations: " + vibrations + "}\r\n");
				} else {
					msg = new String("{temperature: " + temperature + ", temperatureF: " + temperatureF + ", humidity: " + humidity + ", vibrations: " + vibrations + "}\r\n");
				}
				log("command = " + command + ", msg = " + msg);
				} catch(Exception regexpEx) {
				msg = new String("{tempC: " + temperature + ", tempF: " + temperatureF + ", humidity: " + humidity + ", vibrations: " + vibrations + "}\r\n");
				log("Ex command = " + command + ", msg = " + msg);
				}
				// work-around because of unsync
				//msg = new String("" + humidity + "\r\n");
				Thread.sleep(100);
				if (temperature < TEMPERATURE_UPPER_LIMIT) {
					// Create a DataMessage
					log("\nSending message...");

					dioXBeeDevice.sendTextSerial(msg);

	                   		log("\n" + msg + "\n");
					Thread.sleep(smartSensors.getSampleRate());
				} else {
					// insert fault data pattern in 60 seconds, with 60 dots
					itBad = 0;
					while (itBad < 60) {				
                    				log("\nSending message...");

						dioXBeeDevice.sendTextSerial(msg);
	
        	            			log("\n" + msg + "\n");
						itBad++;
						Thread.sleep(smartSensors.getSampleRate());
					}
				} //end if

			     	try {
					if (dioSpiI2c != null) { dioSpiI2c.close(); dioSpiI2c = null; }
				} catch (Exception e) {
					e.printStackTrace();
				}
                    
			} catch(Exception e) {
				log("stack trace = "); e.printStackTrace();
			}


		} // end while

            

        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        } finally {
		// stop sending messages via XBee/ZigBee over serial
		log("Close ZigBee smart sensor device");
		try { dioXBeeDevice.close(); } catch(IOException ioee) {ioee.printStackTrace();}
	}
    }
}
