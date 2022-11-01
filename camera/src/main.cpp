#include "esp_camera.h"
#include <WiFi.h>
#include "esp_timer.h"
#include "img_converters.h"
#include "Arduino.h"
#include "fb_gfx.h"
#include <ESPAsyncWebServer.h>
#include <ESPmDNS.h>

//get pranked
const char* ssid = "i can just see my friends scouring my code for this";
const char* password = "prank on them they got fooled ghouled and fooled";

//this code is largely adapted from the code shown here:
//https://RandomNerdTutorials.com/esp32-cam-video-streaming-web-server-camera-home-assistant/

#define PWDN_GPIO_NUM     32
#define RESET_GPIO_NUM    -1
#define XCLK_GPIO_NUM      0
#define SIOD_GPIO_NUM     26
#define SIOC_GPIO_NUM     27

#define Y9_GPIO_NUM       35
#define Y8_GPIO_NUM       34
#define Y7_GPIO_NUM       39
#define Y6_GPIO_NUM       36
#define Y5_GPIO_NUM       21
#define Y4_GPIO_NUM       19
#define Y3_GPIO_NUM       18
#define Y2_GPIO_NUM        5
#define VSYNC_GPIO_NUM    25
#define HREF_GPIO_NUM     23
#define PCLK_GPIO_NUM     22

#define STATUS_PIN 33

AsyncWebServer server(80);

camera_fb_t* fb = nullptr;
uint8_t* img;
uint32_t img_len;

sensor_t* settings;

uint64_t frameTime = 0;
uint64_t getTime = 0;

bool worky = false;

String debug = "";

void debugLine(String line) {
	debug += line + "\n";
}

void updateFrame() {
	uint64_t a = micros();
	esp_camera_fb_return(fb);
	fb = esp_camera_fb_get();
	worky = fb;
	debugLine("Frame Success: " + worky ? "Worky" : "No Worky :(");
	if (!worky) {
		return;
	} else {
		if(fb->format != PIXFORMAT_JPEG){
			bool jpeg_converted = frame2jpg(fb, 80, &img, &img_len);
			if(!jpeg_converted){
				return;
			}
		} else {
			img_len = fb->len;
			img = fb->buf;
		}
	}
	frameTime = micros();
}

void setup() { 
	pinMode(STATUS_PIN, OUTPUT);
	pinMode(4, OUTPUT);

	camera_config_t config;
	config.ledc_channel = LEDC_CHANNEL_0;
	config.ledc_timer = LEDC_TIMER_0;
	config.pin_d0 = Y2_GPIO_NUM;
	config.pin_d1 = Y3_GPIO_NUM;
	config.pin_d2 = Y4_GPIO_NUM;
	config.pin_d3 = Y5_GPIO_NUM;
	config.pin_d4 = Y6_GPIO_NUM;
	config.pin_d5 = Y7_GPIO_NUM;
	config.pin_d6 = Y8_GPIO_NUM;
	config.pin_d7 = Y9_GPIO_NUM;
	config.pin_xclk = XCLK_GPIO_NUM;
	config.pin_pclk = PCLK_GPIO_NUM;
	config.pin_vsync = VSYNC_GPIO_NUM;
	config.pin_href = HREF_GPIO_NUM;
	config.pin_sscb_sda = SIOD_GPIO_NUM;
	config.pin_sscb_scl = SIOC_GPIO_NUM;
	config.pin_pwdn = PWDN_GPIO_NUM;
	config.pin_reset = RESET_GPIO_NUM;
	config.xclk_freq_hz = 20000000;
	config.pixel_format = PIXFORMAT_JPEG; 
	config.frame_size = FRAMESIZE_VGA;
	config.jpeg_quality = 5;
	config.fb_count = 2;

	// Camera init
	esp_err_t err = esp_camera_init(&config);
	if (err != ESP_OK) {
		digitalWrite(STATUS_PIN, LOW);
		return;
	}
	settings = esp_camera_sensor_get();

	// Wi-Fi connection
	WiFi.begin(ssid, password);
	while (WiFi.status() != WL_CONNECTED) {
		delay(250);
		digitalWrite(STATUS_PIN, LOW);
		delay(250);
		digitalWrite(STATUS_PIN, HIGH);
	}

	if(!MDNS.begin("camera")) {
		digitalWrite(STATUS_PIN, HIGH);
		return;
	}

	//it is done this way so that web browsers can grab the frame as well as programs
	server.on("/get", HTTP_GET, [](AsyncWebServerRequest *request) {
		updateFrame();
		uint64_t a = micros();
		request->send(200, "image/jpeg", String(img, img_len));
		getTime = a;
	});

	//some camera parameters can be controlled on the fly
	server.on("/set", HTTP_GET, [](AsyncWebServerRequest *request) {
		if(request->hasParam("con")) {
			settings->set_contrast(settings, request->getParam("con")->value().toInt());
		}
		if(request->hasParam("sat")) {
			settings->set_saturation(settings, request->getParam("sat")->value().toInt());
		}
		if(request->hasParam("exp")) {
			settings->set_aec_value(settings, request->getParam("exp")->value().toInt());
		}
		if(request->hasParam("exc")) {
			settings->set_exposure_ctrl(settings, request->getParam("exc")->value().toInt());
		}
		if(request->hasParam("fls")) {
			digitalWrite(4, request->getParam("fls")->value().toInt() > 0);
		}
		request->send(200, "image/jpeg", String(img, img_len));
	});

	//useful for debugging mostly (and figuring out why the images are coming in at SPF rather than FPS)
	server.on("/info", HTTP_GET, [](AsyncWebServerRequest *request) {
		request->send(200, "text/plain", (String) (worky ? "working" : "not working") + "\n" + 
				"Frame Time: " + frameTime + "\n" + 
				"Get Time: " + getTime + "\n" + 
				"Wifi SSID: " + WiFi.SSID() + "\n"
				"Wifi Strength: " + WiFi.RSSI() + "\n"
				"Local IP: " + WiFi.localIP().toString() + "\n\n" +
				"Frame Debug:" + "\n\n" + debug);
		debug = "";
	});

	server.begin();
}

void loop() {}