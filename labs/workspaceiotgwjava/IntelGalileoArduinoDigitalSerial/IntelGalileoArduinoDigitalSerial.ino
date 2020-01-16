/*
 */
 
byte bufSize = 10;
char commandFromRPi[10];
char responseToRPi_temp[10];
char responseToRPi_ec[10];
char const * cmdFromRPi_temp = "/getTemp";
char const * cmdFromRPi_ec = "/getEc";

// the setup function runs once when you press reset or power the board
void setup() {
  // initialize digital pin LED_BUILTIN as an output.
  pinMode(LED_BUILTIN, OUTPUT);
  Serial.begin(9600);
}

// the loop function runs over and over again forever
void loop() {
  digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on (HIGH is the voltage level)
  delay(200);                       // wait 200 ms
  digitalWrite(LED_BUILTIN, LOW);    // turn the LED off by making the voltage LOW
  delay(200);                       // wait 200 ms

  //protocol:
  // /getTemp
  // /getEc
  int idxUart = 0, i = 0;
  
  if (Serial.available()) {
    delay(100);
    while(Serial.available() && idxUart < bufSize) {
      commandFromRPi[idxUart++] = Serial.read();
      if(commandFromRPi[idxUart-1] == '\n')
        break;
    }
    commandFromRPi[idxUart++] = '\0';
  }

  if (idxUart > 0) {
    
    if (strncmp(cmdFromRPi_temp, commandFromRPi, strlen(commandFromRPi)) == 0) {
      //sensorValueTemp = temperature;
      Serial.println(23.0, 3);     

    } else if (strncmp(cmdFromRPi_ec, commandFromRPi, strlen(commandFromRPi)) == 0) {
      //sensorValueEc = ECcurrent;
      Serial.println(53.0, 3);
      
    } else {
      // Unknown command
      //sensorValueTemp = 0;
      //sensorValueEc = 0;
      Serial.println(-2.0f);
    }
        
    // print out the value you read:
    //Serial.println(sensorValue);
    
  }

  delay(1000);        // delay in between reads for stability

}
