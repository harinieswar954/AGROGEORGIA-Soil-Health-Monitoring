
package com.agrogeorgia;

import com.agrogeorgia.ai.CropPrediction;
import com.agrogeorgia.database.DatabaseConnection;
import com.agrogeorgia.server.WebServer;

public class Main {

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println(" AI Based Soil Health Monitoring System");
        System.out.println("======================================");

        try {

            System.out.println("Checking MySQL connection...");

            boolean databaseAvailable = DatabaseConnection.testConnection();

            if (databaseAvailable) {
                System.out.println("MySQL connection successful.");
            } else {
                System.out.println("WARNING: MySQL is unavailable.");
                System.out.println("Website will start without database features.");
            }

            System.out.println("Loading AI model...");

            CropPrediction ai = new CropPrediction();

            System.out.println("AI model status: " + ai.status());

            WebServer server = new WebServer(ai);

            server.start();

            System.out.println();
            System.out.println("Website server started successfully.");

        } catch (Exception e) {

            System.out.println();
            System.out.println("SYSTEM STARTUP ERROR");
            System.out.println("--------------------");
            e.printStackTrace();
        }
    }
}
