#!/usr/bin/env python
import time
import os
import RPi.GPIO as GPIO

GPIO.setmode(GPIO.BCM)

# GPIO.setup(4, GPIO.OUT)
# GPIO.setup(17, GPIO.OUT)
GPIO.setup(21, GPIO.OUT)


while True:
#        GPIO.output(4, True)
#        time.sleep(0.5)
#        GPIO.output(4, False)
#        time.sleep(0.5)

#        GPIO.output(17, True)
#        time.sleep(0.5)
#        GPIO.output(17, False)
#        time.sleep(0.5)

        GPIO.output(21, True)
        time.sleep(0.5)
        GPIO.output(21, False)
        time.sleep(0.5)
