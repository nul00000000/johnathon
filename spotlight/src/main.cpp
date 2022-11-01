#include <Arduino.h>
#include <AccelStepper.h>
#include <ESPmDNS.h>
#include <WiFi.h>
#include <ESPAsyncWebServer.h>

//I definitely could have had this in a separate file that was in .gitignore but I fear my own forgetfulness
#define SSID "not on github :)"
#define PASS "also not on github :)"

#define LIGHT_PIN 22
#define IR_PIN 23

AccelStepper rotX(4, 26, 33, 25, 32);
AccelStepper rotY(4, 13, 14, 12, 27);

float x = 0;
float y = 0;

AsyncWebServer server(80);

void writeServos() {
	rotY.moveTo((int) (x * 200));
	rotX.moveTo((int) (y * 200));
}

void setup() {
	pinMode(LIGHT_PIN, OUTPUT);
	Serial.begin(115200);
	rotY.setMaxSpeed(1000);
	rotY.setAcceleration(400);
	rotY.setCurrentPosition(0);
	rotX.setMaxSpeed(1000);
	rotX.setAcceleration(400);
	rotX.setCurrentPosition(0);
	delay(2000);

	//wifi setup
	Serial.print("Connecting to WiFi");
	WiFi.begin(SSID, PASS);
	while(WiFi.status() != WL_CONNECTED) {
		delay(500);
		Serial.print(".");
	}
	Serial.println();
	Serial.println((String)"Connected with IP: " + WiFi.localIP().toString());

	//mdns setup
	if(!MDNS.begin("spotlight")) {
		Serial.println("Error starting mDNS");
	} else {
		Serial.println("Accessable at spotlight.local");
	}

	//set motor and light parameters from program or web interface
	server.on("/set", HTTP_GET, [](AsyncWebServerRequest *request) {
		auto paramX = request->getParam("x");
		auto paramY = request->getParam("y");
		auto paramS = request->getParam("s");
		auto paramI = request->getParam("i");
		if(paramX != nullptr) {
			x = min(max(paramX->value().toFloat(), -1.0f), 1.0f);
		}
		if(paramY != nullptr) {
			y = min(max(paramY->value().toFloat(), -1.0f), 1.0f);
		}
		if(paramS != nullptr) {
			digitalWrite(LIGHT_PIN, paramS->value().toInt() > 0);
		}
		if(paramI != nullptr) {
			digitalWrite(LIGHT_PIN, paramI->value().toInt() > 0);
		}
		writeServos();
		request->send(200, "text/plain", (String)"set " + x + " " + y);
	});

	server.begin();
}

void loop() {
	rotY.run();
	rotX.run();
}