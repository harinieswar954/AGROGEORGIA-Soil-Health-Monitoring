
package com.agrogeorgia.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = System.getenv("DB_URL");
    private static final String USER = System.getenv("DB_USER");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    public static Connection getConnection() throws SQLException {

        if (URL == null || URL.isBlank()
                || USER == null || USER.isBlank()
                || PASSWORD == null || PASSWORD.isBlank()) {

            throw new SQLException(
                "Database environment variables are not configured."
            );
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean testConnection() {

        try (Connection connection = getConnection()) {

            return connection != null && !connection.isClosed();

        } catch (SQLException e) {

            System.out.println(
                "Database connection failed: " + e.getMessage()
            );

            return false;
        }
    }
}
