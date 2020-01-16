package com.oracle.dio;

import com.oracle.dio.spi.*;
import com.oracle.dio.gpio.*;

import jdk.dio.*;


import jcoap.client.BasicCoapClient;
import mqtt_paho_eclipse_mosquitto.client.MqttPublishClient;

import org.json.JSONException;
import org.json.JSONObject;

// @IoTApplication
public class ProgMainDioSpiGpio
{	

	public static void main(String[] args) {
		//int device = 300;
        	//int channel = 7;
		//int channelH = 5;
		//int pinValue = 21;

		if (args == null || args.length != 4) {
			System.out.println("In parameters command line include: deviceId, channelTemperature, channelHumidity, gpioPin");
			System.exit(-1);
		}
		int device = Integer.parseInt(args[0]);
        	int channel = Integer.parseInt(args[1]);
		int channelH = Integer.parseInt(args[2]);
		int pinValue = Integer.parseInt(args[3]);

		System.out.println("Test DIO SPI and GPIO");
	        MCP3008 adc = null;
	        try {
	            adc = (MCP3008)DeviceManager.open(new MCP3008Config(device));

		    BasicCoapClient basicCoapClient = BasicCoapClient.getInstance(new String[] {"127.0.0.1" /*"vs0.inf.ethz.ch" "coap.me"*/});
		    while (true) {
			
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
		        double humidity = voltageH;
			System.out.println("MCP3008: Channel " + channel + ": temperature = " + tempCelsius +" C, " + tempF + " F; channel " + channelH + ": humidity = " + voltageH);

			if (tempCelsius > 28)
				GPIOLedImpl.blinkLEDByGPIONumber(pinValue);	

			System.out.println("Read Sensors and Create JSON objects + Send via CoAP, MQTT and REST API ---------------------------");
			    
			    // Step 1: Create JSON Object:
			    String jsonObjString = new String("\n{" +
			    		"\t\n\"BIoTandJCSecureLab-HVAC\": {" +
			    		"\t\n\"temperature\":\""+tempCelsius+"\"," +
			    		"\t\n\"humidity\":\""+humidity+"\"," + 
			    		"\t\n\"description\":\"IoTSunRPi001 is considered IoT Node and it sends as CoAP, MQTT and REST client: the temperature and humidity \"" +
			    		"\t\n}" +
			    		"\n}");

			    JSONObject jsonObject = new JSONObject(jsonObjString);            
			    System.out.println("JSON Object = " + jsonObject);             
			    
			    // Step2: Now pass JSON Data to CoAP over UDP - Client POST Request
			    basicCoapClient.runTestClient(jsonObject.toString());
			    //basicCoapClient.runTestClient("" + tempCelsius);
			    
			    // Step3: Now pass JSON Data to MQTT over TCP - Client Publisher
			    //MqttPublishClient.publish("MQTT Examples", "tcp://iot.eclipse.org:1883", "IoTSunRPi001-IoT-Node", 2, jsonObject.toString());       
// check-out: http://test.mosquitto.org/gauge/
MqttPublishClient.publish("temp/random", "tcp://test.mosquitto.org:1883", "IoTSunRPi001-IoT-Node", 2, ""+tempCelsius);

			    	
			
			Thread.sleep(2000); // 2 sec
		    } //end for
		    
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
}