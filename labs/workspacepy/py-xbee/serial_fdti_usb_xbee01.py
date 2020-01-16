import serial, sys
## from xbee import XBee

SERIALPORT = "/dev/ttyUSB0"
BAUDRATE = 9600

#TIMEOUT = 1

ser = serial.Serial(SERIALPORT, BAUDRATE)

## xbee = XBee(ser)

counter = 0

print 'Start FDTI USB for XBee'

while True:
   try:
     ## response = xbee.wait_read_frame()
     response = ser.readline()
     if response:
         print response

     counter += 1
     ser.write('ACK %d \n'%counter)

   except KeyboardInterrupt:
     break

ser.close()
