import spidev
import time
spi = spidev.SpiDev()
spi.open(0,0)

def readadc(adcnum):
    if ((adcnum > 7) or (adcnum < 0)):
        return -1
    r = spi.xfer2([1,(8+adcnum)<<4,0])
    adcout = ((r[1]&3) << 8) + r[2]
    return adcout

while True:
    for adcInput in range(0,8):
        value = readadc(adcInput)
        print "---------------------------"
        print "ADC(", adcInput,")= ", value
        print "---------------------------"
    time.sleep(1)
