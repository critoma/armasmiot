package com.oracle.dio;

//import com.oracle.dio.spi.*;
import com.oracle.dio.gpio.*;

//import jdk.dio.*;

import java.io.*;
import java.nio.*;
import jdk.dio.*;
import jdk.dio.Device.*;
import jdk.dio.spi.*;
import jdk.dio.spibus.*;
import jdk.dio.i2cbus.*;
import jdk.dio.gpio.*;
import jdk.dio.DeviceConfig;
import jdk.dio.spibus.SPIDeviceConfig;

import java.util.*;

// @IoTGWCLApplication
public class DioSpiI2c
{

	private SPIDevice spiDevice = null; // Accelerometer Sensor Sparkfun ADXL345
	private I2CDevice i2cDevice = null; // Temperature and Humidity Sparkfun Sensor 

	private static byte POWER_CONTROL_VALUE = (byte)0x08;
	private static byte WRITE = (byte)0x00;
	private static byte READ  = (byte)0x80;
	private static byte DATA_REG = (byte)0x32;
	private static byte DATA_FORMAT_REGISTER = (byte)0x31;
	private static byte MULTIBYTE_TRANSFER = (byte)0x40; 
	private static byte DATA_FORMAT_VALUE = (byte)0x0B; 
	private static byte POWER_CONTROL_REGISTER = (byte)0x2D;

       private DeviceDescriptor deviceDecriptor;
	private SPIDeviceConfig spiDeviceConfiguration;

	private short x_accel, y_accel, z_accel;
	private double vibrations;
	private double i2cTemperature, i2cHumidity;
	private int gpioPin = 21;

	public DioSpiI2c() {
	}

	public void openSpiAndI2C(int idSpi, int idI2C) {
		initByIdSPIAccel(idSpi); // 300
		initByIdI2CTempHumidity(idI2C); // 41	
	}	

	public void setPin(int pin) {
		this.gpioPin = pin;
	}

	public void blinkLed() {
		GPIOLedImpl.blinkLEDByGPIONumber(this.gpioPin);
	}

	public void initByIdSPIAccel(int id)
	{

		this.spiDeviceConfiguration = new SPIDeviceConfig(0, 0, 500000, 3, 8, Device.BIG_ENDIAN);

		try {
			//spiDevice = (SPIDevice)DeviceManager.open(SPIDevice.class, this.spiDeviceConfiguration); // 301 - SPI peripheral from dio.properties-raspberrypi
			spiDevice = (SPIDevice)DeviceManager.open(id); // 301 - SPI peripheral from dio.properties-raspberrypi

			//deviceDecriptor = (DeviceDescriptor) spiDevice.getDescriptor();
			//spiDeviceConfiguration = (SPIDeviceConfig) deviceDecriptor.getConfiguration();

			ByteBuffer out1 = ByteBuffer.allocate(2);
			ByteBuffer in1 = ByteBuffer.allocate(2);

			out1.put((byte)((WRITE | DATA_FORMAT_REGISTER) & 0xff));
			out1.put((byte)(DATA_FORMAT_VALUE & 0xFF));
			out1.flip();

			try {
				spiDevice.writeAndRead(out1, in1);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			ByteBuffer out2 = ByteBuffer.allocate(2);
			ByteBuffer in2 = ByteBuffer.allocate(2);

			out2.put((byte)((WRITE | POWER_CONTROL_REGISTER) & 0xFF));
			out2.put((byte)(POWER_CONTROL_VALUE & 0xFF));
			out2.flip();

			try {
				spiDevice.writeAndRead(out2, in2);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} catch (Exception e) { 
			throw new RuntimeException(e);
		}
	}

	public void initByIdI2CTempHumidity(int id)
	{
		try {
			this.i2cDevice = (I2CDevice) DeviceManager.open(id);//41 - I2C peripheral from dio.properties-raspberrypi
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public void close() {
		closeSpi();
		closeI2C();
	}

	public void closeSpi() {
		try {
			if (spiDevice != null) {
				spiDevice.close();
			}
		} catch (Exception e) {
		}
	}

	public void closeI2C() {
		try {
			if (i2cDevice != null) {
				i2cDevice.close();
			}
		} catch (Exception e) {
		}
	}

	private void readSpiAccelValues()
	{
		x_accel = 0;
		y_accel = 0;
		z_accel = 0;

		ByteBuffer out = ByteBuffer.allocate(7);
		ByteBuffer in = ByteBuffer.allocate(7);

		
		out.put((byte)(READ | MULTIBYTE_TRANSFER | DATA_REG));
		out.put((byte)0);
		out.put((byte)0);
		out.put((byte)0);
		out.put((byte)0);
		out.put((byte)0);
		out.put((byte)0);

		out.flip();

		try {

			spiDevice.writeAndRead(out, in);

		} catch (Exception e) {
			//throw new RuntimeException(e);
			e.printStackTrace();
		}

		x_accel=(short)( (((byte)in.get(2) & 0xFF)<<8) | ((byte)in.get(1) & 0xFF) );		
		y_accel=(short)( (((byte)in.get(4) & 0xFF)<<8) | ((byte)in.get(3) & 0xFF) );		
		z_accel=(short)( (((byte)in.get(6) & 0xFF)<<8) | ((byte)in.get(5) & 0xFF) );	

		this.vibrations = Math.sqrt(Math.pow(x_accel, 2) + Math.pow(y_accel, 2) + Math.pow(z_accel, 2));	
		System.out.println("\n ADXL345 accelerometer values : X = " + x_accel +", Y = " + y_accel + ", Z = " + z_accel + ", vibrations = " + this.vibrations);
	}

	private void readI2CSensor()
	{
		try
		{
			
			ByteBuffer data;
			int count = 0;
			short H_dat = 0, T_dat = 0;			
			try {			
				data = ByteBuffer.allocateDirect(4);
				i2cDevice.write(0x00); 
				Thread.sleep(10); 
				count = i2cDevice.read(data);

				if (count == 4) {
					//System.out.println("I2C read : " + (short)(data.get(0) & 0xFF) +" " + (int)(data.get(1)  & 0xFF) + " " + (short)(data.get(2) & 0xFF) + " " + (short)(data.get(3) & 0xFF));

					int _status = ((byte)(data.get(0) & 0xFF) >> 6) & 0x03;
					H_dat = (short)(((short)((byte)(data.get(0) & 0x3f)) << 8) | (short)(data.get(1) & 0xFF));

					T_dat = (short)(((short)(data.get(2) & 0xFF) << 8) | (short)(data.get(3) & 0xFF));
					T_dat = (short)(T_dat / 4);
						  
					//System.out.println("Status = " + _status + " Temp = " + (float) (T_dat * 1.007e-2 - 40.0) + " Humi = " + (float)(H_dat * 6.10e-3)); 
					i2cTemperature = (double) (T_dat * 1.007e-2 - 40.0);
					i2cHumidity = (double) (H_dat * 6.10e-3);

				} else System.out.println("i2c read error");
			 } catch (Exception e) {
				throw new Exception();
			 }			 
			 		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void readTemperatureAndHumidity() {
		// 2. Read temperature and humidity
		this.readI2CSensor();
	}

	public void readVibrations() {
		this.readSpiAccelValues();
	}

	public double getTemperature() {
		return this.i2cTemperature;
	}

	public double getHumidity() {
		return this.i2cHumidity;
	}

	public double getVibrations() {
		return this.vibrations;
	}


	public double convertCelsius2F(double tempCelsius) {
		double tempF = tempCelsius * 2 + 22;
		return tempF;
	}
}