export JAVA_HOME=/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt
export PATH=$PATH:$JAVA_HOME/bin

cd /home/pi/workspacejava/CoapServer/java_samples/ws4d-jcoap-applications
cd src
export CLASSPATH_COAP=.:../lib/ws4d-jcoap.jar:../lib/commons-cli-1.2.jar:../lib/log4j-1.2.16.jar:../lib/commons-codec-1.4.jar:../lib/commons-logging-1.1.1.jar:../lib/ehcache-core-2.4.3.jar:../lib/httpasyncclient-4.0-alpha2.jar:../lib/httpclient-4.2.jar:../lib/httpcore-4.2.jar:../lib/httpcore-nio-4.2.jar:../lib/slf4j-api-1.6.1.jar:../lib/slf4j-jdk14-1.6.1.jar
javac -classpath $CLASSPATH_COAP -d ../bin org/ws4d/coap/client/BasicCoapClient.java

cd ../bin
java -classpath $CLASSPATH_COAP org/ws4d/coap/client/BasicCoapClient 192.168.0.105 5683

java -classpath $CLASSPATH_COAP org/ws4d/coap/client/BasicCoapClient 10.171.70.71 5683

java -classpath $CLASSPATH_COAP org/ws4d/coap/server/BasicCoapServer
