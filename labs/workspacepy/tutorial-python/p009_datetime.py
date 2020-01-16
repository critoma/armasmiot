#!/usr/bin/python
import time;

localtime = time.asctime( time.localtime(time.time()) )
print "Local current time :", localtime
print "time.altzone %d " % time.altzone

import calendar

cal = calendar.month(2017, 11)
print "Here is the calendar:"
print cal
