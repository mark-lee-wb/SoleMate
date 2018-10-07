/*******************************************************************
 *  An example of bot that receives commands and turns on and off  *
 *  an LED.                                                        *
 *                                                                 *
 *  written by Giacarlo Bacchio (Gianbacchio on Github)            *
 *  adapted by Brian Lough                                         *
 *******************************************************************/
#include <ESP8266WiFi.h>
#include <WiFiClientSecure.h>
//#include <UniversalTelegramBot.h>
#include <SoftwareSerial.h>

// Initialize Wifi connection to the router
char ssid[] = "IOTLabTest";     // your network SSID (name) ****************************************
char password[] = "12345678"; // your network key **********************************************
String defaulte = "243681621"; // ******************************************************************************
String cardone = "One is at Home"; // ******************** Stores message to be sent
int onealert = 0; // ************************
// Initialize Telegram BOT
#define BOTtoken "686493040:AAHoMD0vRCS1dUIAUe7oUfN6-ol6BVN_s80"  // your Bot Token (Get from Botfather)******************
String apiKey = "WBMU3NM1B9OQJSYR";
const char* server = "api.thingspeak.com";
int i = -1;
SoftwareSerial Serial2(D5,D6);

WiFiClient client;
//UniversalTelegramBot bot(BOTtoken, client);

int Bot_mtbs = 1000; //mean time between scan messages
long Bot_lasttime;   //last time messages' scan has been done
bool Start = false;

const int ledPin = 13; 
int ledStatus = 0;

void handleNewMessages(int numNewMessages) { //*********************************
  digitalWrite(D1,HIGH);
  Serial.println("handleNewMessages");
  Serial.println(String(numNewMessages));

//  for (int i=0; i<numNewMessages; i++) {
//    String chat_id = String(bot.messages[i].chat_id);
//    String text = bot.messages[i].text;
//
//    String from_name = bot.messages[i].from_name;
//    if (from_name == "") from_name = "Guest";
//
//    if (text == "/check1") { /////////////////////////////////////////
//      bot.sendMessage(chat_id, cardone);
//    }
//
//    if (text == "/ledoff") {
//      ledStatus = 0;
//      digitalWrite(ledPin, LOW);    // turn the LED off (LOW is the voltage level)
//      bot.sendMessage(chat_id, "Led is OFF", "");
//    }
//
//    if (text == "/reset") { //////////////////////////////////////////
//      cardone = "One is at Home";
//      onealert = 0;
//      bot.sendMessage(chat_id, "reset complete.");
//    }
//
//    if (text == "/start") {
//      String welcome = "Welcome to Universal Arduino Telegram Bot library, " + from_name + ".\n";
//      welcome += "This is Flash Led Bot example.\n\n";
//      welcome += "/ledon : to switch the Led ON\n";
//      welcome += "/ledoff : to switch the Led OFF\n";
//      welcome += "/status : Returns current status of LED\n";
//      bot.sendMessage(chat_id, welcome, "Markdown");
//    }
//  }
  digitalWrite(D1,LOW);
}


void setup() {
  pinMode(D2, INPUT); //Blue Scanner for one
  pinMode(D3, INPUT); //Blue Scanner for two
  pinMode(D4, INPUT); //Red Scanner for one
  pinMode(D5, INPUT); //Red Scanner for two
  pinMode(D1,OUTPUT);
  pinMode(D8,OUTPUT);
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(D8, HIGH);
  Serial.begin(115200);
  Serial2.begin(115200);

  // Set WiFi to station mode and disconnect from an AP if it was Previously
  // connected
  WiFi.mode(WIFI_STA);
  WiFi.disconnect();
  delay(100);

  // attempt to connect to Wifi network:
  Serial.print("Connecting Wifi: ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }

  //If successfully connected to WIFI
  digitalWrite(LED_BUILTIN,HIGH);
  delay(5000);
  digitalWrite(LED_BUILTIN, LOW);

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());

  pinMode(ledPin, OUTPUT); // initialize digital ledPin as an output.
  delay(10);
  digitalWrite(ledPin, LOW); // initialize pin as off
  i = 0;
}

void loop() {
//  Serial.println("ima``live");
// This is for Telegram Bot. 
//  if( digitalRead(D3) == HIGH && onealert == 0){
//  digitalWrite( D1, HIGH);
//  bot.sendMessage(defaulte, "Suspicious Behaviour Detected- One has approached bus stop @ Jurong Point Interchange. Please Alert Authorities if neccesary.");
//  onealert = 1;
//  cardone = "One was last seen at the bus stop. Please Alert Authorities if neccesary.";
//  digitalWrite(D1, LOW);
//  }
//  
//  if(digitalRead(D2) == HIGH && onealert == 0 ){
//    cardone = "One was last at the Market";
//  }



  // below here dont need car anymore, it is just function to handle telegram messages
//  if (millis() > Bot_lasttime + Bot_mtbs)  {
//    int numNewMessages = bot.getUpdates(bot.last_message_received + 1);
//
//    while(numNewMessages) {
//      Serial.println("got response");
//      handleNewMessages(numNewMessages);
//      numNewMessages = bot.getUpdates(bot.last_message_received + 1);
//    }
//
//    Bot_lasttime = millis();
//  }


// Prototype Iteration for ThingSpeak 

String content = "";
char character;

  while(Serial2.available()) {
      content += Serial2.readString();
  }
  if (content != "") {
    Serial.println(content);
    uploadCloud(content);
    delay(16000);
  }


  
}

void uploadCloud (String location) {
  if (client.connect(server,80))   //   "184.106.153.149" or api.thingspeak.com
     {  
         Serial.println("connected");
         String postStr = apiKey;
         postStr +="&field1=";
         postStr += location;
         postStr +="&field2=";
         postStr += "test2";
         postStr += "\r\n\r\n";

         client.print("POST /update HTTP/1.1\n");
         client.print("Host: api.thingspeak.com\n");
         client.print("Connection: close\n");
         client.print("X-THINGSPEAKAPIKEY: "+apiKey+"\n");
         client.print("Content-Type: application/x-www-form-urlencoded\n");
         client.print("Content-Length: ");
         client.print(postStr.length());
         client.print("\n\n");
         client.print(postStr);

         Serial.print("Successfully uploaded to thingspeak");
     }
     else {
      Serial.println("not connected");
     }

     client.stop();
}

