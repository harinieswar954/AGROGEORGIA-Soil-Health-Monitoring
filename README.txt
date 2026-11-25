AI-BASED SOIL HEALTH MONITORING AND SURVEY SYSTEM - JAVA

Java 17 + Weka + JDBC/MySQL + built-in Java HTTP server + HTML/CSS/JS + ESP32.

IMPORTANT:
Only soil moisture is physically measured in the current hardware prototype.
pH, N, P, K, temperature, humidity and rainfall are dataset/demo inputs.
Twilio SMS is intentionally disabled for this submission.

1. Put Crop_recommendation.csv in data\
2. Put mysql-connector-j-9.6.0.jar and weka.jar in lib\
3. Run database\soil_project.sql in MySQL.
4. Edit MySQL password in DatabaseConnection.java if needed.
5. Compile:
javac -encoding UTF-8 -cp "lib\*;src" -d out src\com\agrogeorgia\database\DatabaseConnection.java src\com\agrogeorgia\model\SoilReading.java src\com\agrogeorgia\ai\CropPrediction.java src\com\agrogeorgia\server\WebServer.java src\com\agrogeorgia\Main.java
6. Run:
java -cp "out;lib\*" com.agrogeorgia.Main
7. Open:
http://127.0.0.1:8080

Hardware-independent demo:
AI Analysis -> Analyze Soil -> Save Reading
Then History and Research Portal.

ESP32 is optional. See esp32\soil_monitor.ino.

Recommended presentation statement:
"The current prototype uses an ESP32 and soil-moisture sensor for real-time moisture
sensing. A machine-learning model trained on a crop recommendation dataset predicts
suitable crops from soil and environmental parameters. The Java application stores
observations in MySQL and provides dashboards for students and researchers.
Additional pH, NPK and temperature sensors and SMS notification are future extensions."
