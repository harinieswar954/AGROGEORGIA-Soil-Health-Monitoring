/*
 ESP32 + analog soil moisture sensor
 Java server endpoint:
 http://YOUR_COMPUTER_IP:8080/api/readings

 Only moisture is physically measured.
 Other values are demo/dataset values.
*/

#include <WiFi.h>
#include <HTTPClient.h>

#define SOIL_SENSOR_PIN 34

const char* WIFI_SSID = "username";
const char* WIFI_PASSWORD = "password";
const char* SERVER_URL = "http://YOUR_COMPUTER_IP:8080/api/readings";

void setup() {
  Serial.begin(115200);
  delay(1000);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting");
  while (WiFi.status() != WL_CONNECTED) { delay(500); Serial.print("."); }
  Serial.println();
  Serial.print("ESP32 IP: ");
  Serial.println(WiFi.localIP());
}

void loop() {
  int raw = analogRead(SOIL_SENSOR_PIN);
  int moisture = map(raw, 4095, 0, 0, 100);
  moisture = constrain(moisture, 0, 100);

  Serial.print("Raw: "); Serial.print(raw);
  Serial.print(" Moisture: "); Serial.print(moisture); Serial.println("%");

  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    String url = String(SERVER_URL)
      + "?moisture=" + String(moisture)
      + "&ph=6.7&nitrogen=120&phosphorus=45&potassium=80"
      + "&temperature=28&humidity=65&rainfall=120&source=esp32";
    http.begin(url);
    int code = http.GET();
    Serial.print("Server response: ");
    Serial.println(code);
    http.end();
  }
  delay(5000);
}
