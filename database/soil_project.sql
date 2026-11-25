CREATE DATABASE IF NOT EXISTS soil_project;
USE soil_project;

CREATE TABLE IF NOT EXISTS soil_readings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    moisture DOUBLE NOT NULL,
    ph DOUBLE NOT NULL,
    nitrogen DOUBLE NOT NULL,
    phosphorus DOUBLE NOT NULL,
    potassium DOUBLE NOT NULL,
    temperature DOUBLE NOT NULL,
    humidity DOUBLE NOT NULL,
    rainfall DOUBLE NOT NULL,
    soil_type VARCHAR(100) NOT NULL,
    suitable_plant VARCHAR(100) NOT NULL,
    growth_duration VARCHAR(100) NOT NULL,
    soil_health VARCHAR(100) NOT NULL,
    source VARCHAR(30) NOT NULL,
    reading_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
