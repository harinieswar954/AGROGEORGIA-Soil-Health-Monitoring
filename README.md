# 🌱 AGROGEORGIA – AI-Powered Smart Soil Health Monitoring and Survey System

## Project Overview

AGROGEORGIA is an AI-powered smart agriculture project designed to support farmers in understanding soil conditions and making informed crop selection decisions.

The system combines IoT-based soil moisture monitoring, machine learning, and a web-based interface to provide soil analysis and crop recommendations.

## Problem Addressed

Selecting suitable crops without understanding soil conditions can affect agricultural productivity.

AGROGEORGIA addresses this challenge by analysing important soil parameters and providing machine-learning-based crop recommendations to support better agricultural decisions.

## Key Features

- **Soil Monitoring:** ESP32 integration with a soil moisture sensor.
- **AI Crop Recommendation:** Uses a trained Random Forest classification model to recommend suitable crops.
- **Soil Parameter Analysis:** Analyses Nitrogen, Phosphorus, Potassium, temperature, humidity, pH and rainfall.
- **Soil Health Information:** Displays soil type and soil health information.
- **Crop Growth Information:** Provides estimated crop growth duration.
- **Web Dashboard:** Provides a user-friendly interface for viewing analysis and project information.
- **Database Integration:** MySQL support for storing soil readings and retrieving historical records.

## Technologies Used

| Component | Technology |
|---|---|
| Programming Language | Java |
| Machine Learning | Weka – Random Forest |
| Database | MySQL |
| IoT Hardware | ESP32 |
| Sensor | Soil Moisture Sensor |
| Frontend | HTML, CSS, JavaScript |
| Web Server | Java HTTP Server |
| Deployment | Docker and Render |

## AI Model

The project uses a Random Forest classification algorithm with the Crop Recommendation dataset.

The model analyses soil and environmental parameters to predict a suitable crop from the available crop classes.

## System Architecture

1. Soil moisture data is collected through the ESP32 sensor.
2. Additional soil and environmental parameters are provided through the application's dataset and analysis inputs.
3. The Java application processes the input values.
4. The trained machine learning model generates a crop recommendation.
5. The web interface displays the prediction and soil information.
6. MySQL provides database storage for supported features.

## Live Deployment

https://agrogeorgia-soil-health-monitoring.onrender.com/

## GitHub Repository

https://github.com/harinieswar954/AGROGEORGIA-Soil-Health-Monitoring

## Project Structure

- `src/` – Java application source code
- `src/com/agrogeorgia/ai/` – Machine learning implementation
- `src/com/agrogeorgia/database/` – Database connection
- `src/com/agrogeorgia/server/` – Web server and API
- `web/` – Website pages and frontend resources
- `data/` – Crop recommendation dataset
- `database/` – SQL database scripts
- `esp32/` – ESP32 sensor program
- `lib/` – External Java libraries

## Project Impact

AGROGEORGIA demonstrates how Artificial Intelligence, Machine Learning and Internet of Things technologies can be combined to support smart agriculture.

The system aims to make soil analysis and crop recommendation more accessible through an integrated web-based platform.

## Future Enhancements

- Integration of additional real-time soil sensors.
- Cloud database integration.
- Improved crop prediction using larger and diverse datasets.
- Mobile-friendly farmer dashboard.
- More advanced soil health analytics.

## Developed For

PROMPTWARS × AI VIBEATHON 2026
