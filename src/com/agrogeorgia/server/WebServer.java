
package com.agrogeorgia.server;

import com.agrogeorgia.ai.CropPrediction;
import com.agrogeorgia.database.DatabaseConnection;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WebServer {

    private final CropPrediction ai;
    private HttpServer server;

    public WebServer(CropPrediction ai) {
        this.ai = ai;
    }

    public void start() throws IOException {

        String portValue = System.getenv("PORT");
        int port = (portValue != null && !portValue.isBlank())
                ? Integer.parseInt(portValue)
                : 8080;

        if (port < 1 || port > 65535) {
            throw new IOException("Invalid server port configuration.");
        }

        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/predict", this::predict);
        server.createContext("/api/readings", this::readings);
        server.createContext("/api/history", this::history);
        server.createContext("/api/research", this::research);
        server.createContext("/", this::staticFiles);

        server.setExecutor(null);
        server.start();

        System.out.println("Web server started on port: " + port);
    }

    private boolean allowGet(HttpExchange e) throws IOException {

        if (!"GET".equalsIgnoreCase(e.getRequestMethod())) {
            e.getResponseHeaders().set("Allow", "GET");
            json(e, 405, "{\"error\":\"Method not allowed\"}");
            return false;
        }

        return true;
    }

    private void checkPath(HttpExchange e, String expected) {

        if (!expected.equals(e.getRequestURI().getPath())) {
            throw new IllegalArgumentException("Invalid API path.");
        }
    }

    private void predict(HttpExchange e) throws IOException {

        if (!allowGet(e)) {
            return;
        }

        try {

            checkPath(e, "/api/predict");

            Map<String, String> p = query(e.getRequestURI());

            double moisture = number(p, "moisture", 50, 0, 100);
            double ph = number(p, "ph", 6.5, 0, 14);
            double nitrogen = number(p, "nitrogen", 50, 0, Double.MAX_VALUE);
            double phosphorus = number(p, "phosphorus", 50, 0, Double.MAX_VALUE);
            double potassium = number(p, "potassium", 50, 0, Double.MAX_VALUE);
            double temperature = number(p, "temperature", 25, -50, 60);
            double humidity = number(p, "humidity", 60, 0, 100);
            double rainfall = number(p, "rainfall", 100, 0, Double.MAX_VALUE);

            String crop = ai.predict(
                    nitrogen,
                    phosphorus,
                    potassium,
                    temperature,
                    humidity,
                    ph,
                    rainfall
            );

            String soil = soilType(ph, moisture);

            String health = soilHealth(
                    moisture,
                    nitrogen,
                    phosphorus,
                    potassium
            );

            String response =
                    "{"
                    + "\"soilType\":\"" + esc(soil) + "\","
                    + "\"suitablePlant\":\"" + esc(title(crop)) + "\","
                    + "\"growthDuration\":\"" + esc(ai.duration(crop)) + "\","
                    + "\"soilHealth\":\"" + esc(health) + "\","
                    + "\"modelStatus\":\"" + esc(ai.status()) + "\""
                    + "}";

            json(e, 200, response);

        } catch (IllegalArgumentException ex) {

            json(e, 400, "{\"status\":\"error\",\"message\":\"Invalid input. Please check the values.\"}");

        } catch (Exception ex) {

            System.err.println("Prediction error: " + ex.getMessage());

            json(e, 500, "{\"status\":\"error\",\"message\":\"Prediction could not be completed.\"}");
        }
    }

    private void readings(HttpExchange e) throws IOException {

        if (!allowGet(e)) {
            return;
        }

        try {

            checkPath(e, "/api/readings");

            Map<String, String> p = query(e.getRequestURI());

            double moisture = number(p, "moisture", 50, 0, 100);
            double ph = number(p, "ph", 6.5, 0, 14);
            double nitrogen = number(p, "nitrogen", 50, 0, Double.MAX_VALUE);
            double phosphorus = number(p, "phosphorus", 50, 0, Double.MAX_VALUE);
            double potassium = number(p, "potassium", 50, 0, Double.MAX_VALUE);
            double temperature = number(p, "temperature", 25, -50, 60);
            double humidity = number(p, "humidity", 60, 0, 100);
            double rainfall = number(p, "rainfall", 100, 0, Double.MAX_VALUE);

            String source = p.getOrDefault("source", "web");

            if (source.isBlank() || source.length() > 50) {
                throw new IllegalArgumentException("Invalid source.");
            }

            String crop = ai.predict(
                    nitrogen,
                    phosphorus,
                    potassium,
                    temperature,
                    humidity,
                    ph,
                    rainfall
            );

            String soil = soilType(ph, moisture);

            String health = soilHealth(
                    moisture,
                    nitrogen,
                    phosphorus,
                    potassium
            );

            String sql =
                    "INSERT INTO soil_readings "
                    + "(moisture, ph, nitrogen, phosphorus, potassium, "
                    + "temperature, humidity, rainfall, soil_type, "
                    + "suitable_plant, growth_duration, soil_health, source) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (
                    Connection con = DatabaseConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql)
            ) {

                ps.setDouble(1, moisture);
                ps.setDouble(2, ph);
                ps.setDouble(3, nitrogen);
                ps.setDouble(4, phosphorus);
                ps.setDouble(5, potassium);
                ps.setDouble(6, temperature);
                ps.setDouble(7, humidity);
                ps.setDouble(8, rainfall);
                ps.setString(9, soil);
                ps.setString(10, title(crop));
                ps.setString(11, ai.duration(crop));
                ps.setString(12, health);
                ps.setString(13, source);

                ps.executeUpdate();
            }

            json(
                    e,
                    200,
                    "{\"status\":\"saved\",\"message\":\"Reading saved successfully\"}"
            );

        } catch (IllegalArgumentException ex) {

            json(e, 400, "{\"status\":\"error\",\"message\":\"Invalid input. Please check the values.\"}");

        } catch (SQLException ex) {

            System.err.println("Database save error: " + ex.getMessage());

            json(e, 503, "{\"status\":\"error\",\"message\":\"Database is currently unavailable.\"}");

        } catch (Exception ex) {

            System.err.println("Reading save error: " + ex.getMessage());

            json(e, 500, "{\"status\":\"error\",\"message\":\"Reading could not be saved.\"}");
        }
    }

    private void history(HttpExchange e) throws IOException {

        if (!allowGet(e)) {
            return;
        }

        try {

            checkPath(e, "/api/history");

            StringBuilder s = new StringBuilder();
            s.append("[");

            String sql =
                    "SELECT id, moisture, ph, nitrogen, phosphorus, potassium, "
                    + "temperature, soil_type, suitable_plant, growth_duration, "
                    + "source, reading_time "
                    + "FROM soil_readings "
                    + "ORDER BY id DESC LIMIT 50";

            try (
                    Connection con = DatabaseConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql);
                    ResultSet r = ps.executeQuery()
            ) {

                boolean first = true;

                while (r.next()) {

                    if (!first) {
                        s.append(",");
                    }

                    first = false;

                    s.append("{")
                            .append("\"id\":")
                            .append(r.getInt("id"))

                            .append(",\"moisture\":")
                            .append(r.getDouble("moisture"))

                            .append(",\"ph\":")
                            .append(r.getDouble("ph"))

                            .append(",\"nitrogen\":")
                            .append(r.getDouble("nitrogen"))

                            .append(",\"phosphorus\":")
                            .append(r.getDouble("phosphorus"))

                            .append(",\"potassium\":")
                            .append(r.getDouble("potassium"))

                            .append(",\"temperature\":")
                            .append(r.getDouble("temperature"))

                            .append(",\"soilType\":\"")
                            .append(esc(r.getString("soil_type")))
                            .append("\"")

                            .append(",\"plant\":\"")
                            .append(esc(r.getString("suitable_plant")))
                            .append("\"")

                            .append(",\"duration\":\"")
                            .append(esc(r.getString("growth_duration")))
                            .append("\"")

                            .append(",\"source\":\"")
                            .append(esc(r.getString("source")))
                            .append("\"")

                            .append(",\"time\":\"")
                            .append(esc(r.getString("reading_time")))
                            .append("\"")

                            .append("}");
                }
            }

            s.append("]");
            json(e, 200, s.toString());

        } catch (IllegalArgumentException ex) {

            json(e, 400, "{\"error\":\"Invalid request.\"}");

        } catch (SQLException ex) {

            System.err.println("History database error: " + ex.getMessage());

            json(e, 503, "{\"error\":\"Reading history is currently unavailable.\"}");
        }
    }

    private void research(HttpExchange e) throws IOException {

        if (!allowGet(e)) {
            return;
        }

        try {

            checkPath(e, "/api/research");

            String sql =
                    "SELECT COUNT(*) AS total, "
                    + "COALESCE(AVG(moisture),0) AS avg_moisture "
                    + "FROM soil_readings";

            try (
                    Connection con = DatabaseConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql);
                    ResultSet r = ps.executeQuery()
            ) {

                if (r.next()) {

                    int total = r.getInt("total");
                    double avg = r.getDouble("avg_moisture");

                    String response =
                            "{"
                            + "\"total\":" + total + ","
                            + "\"averageMoisture\":"
                            + String.format(Locale.US, "%.1f", avg)
                            + "}";

                    json(e, 200, response);
                }
            }

        } catch (IllegalArgumentException ex) {

            json(e, 400, "{\"error\":\"Invalid request.\"}");

        } catch (SQLException ex) {

            System.err.println("Research database error: " + ex.getMessage());

            json(e, 503, "{\"error\":\"Research statistics are currently unavailable.\"}");
        }
    }

    private void staticFiles(HttpExchange e) throws IOException {

        if (!"GET".equalsIgnoreCase(e.getRequestMethod())) {
            e.getResponseHeaders().set("Allow", "GET");
            text(e, 405, "Method not allowed");
            return;
        }

        try {

            String path = e.getRequestURI().getPath();

            if (path.equals("/")) {
                path = "/index.html";
            }

            Path root = Paths.get("web").toAbsolutePath().normalize();

            Path file = root.resolve(path.substring(1)).normalize();

            if (!file.startsWith(root)) {
                text(e, 403, "Forbidden");
                return;
            }

            if (!Files.isRegularFile(file)) {
                text(e, 404, "Page not found");
                return;
            }

            byte[] data = Files.readAllBytes(file);

            e.getResponseHeaders().set(
                    "Content-Type",
                    contentType(file.toString())
            );

            e.sendResponseHeaders(200, data.length);

            try (OutputStream os = e.getResponseBody()) {
                os.write(data);
            }

        } catch (Exception ex) {

            System.err.println("Static file error: " + ex.getMessage());

            text(e, 500, "Unable to load page.");
        }
    }

    private String contentType(String path) {

        String lower = path.toLowerCase(Locale.ROOT);

        if (lower.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        }

        if (lower.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }

        if (lower.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        }

        return "text/plain; charset=UTF-8";
    }

    private Map<String, String> query(URI uri) {

        Map<String, String> map = new HashMap<>();

        String q = uri.getRawQuery();

        if (q == null || q.isEmpty()) {
            return map;
        }

        if (q.length() > 4096) {
            throw new IllegalArgumentException("Query is too long.");
        }

        String[] parts = q.split("&", -1);

        if (parts.length > 20) {
            throw new IllegalArgumentException("Too many parameters.");
        }

        for (String part : parts) {

            if (part.isEmpty()) {
                throw new IllegalArgumentException("Invalid query.");
            }

            String[] kv = part.split("=", 2);

            String key = decode(kv[0]);

            String value = kv.length > 1
                    ? decode(kv[1])
                    : "";

            if (key.isBlank() || map.containsKey(key)) {
                throw new IllegalArgumentException("Invalid query parameter.");
            }

            map.put(key, value);
        }

        return map;
    }

    private String decode(String value) {

        try {

            return URLDecoder.decode(
                    value,
                    StandardCharsets.UTF_8
            );

        } catch (IllegalArgumentException ex) {

            throw new IllegalArgumentException("Invalid URL encoding.");
        }
    }

    private double number(
            Map<String, String> map,
            String key,
            double defaultValue,
            double min,
            double max
    ) {

        String value = map.get(key);

        if (value == null) {
            return defaultValue;
        }

        if (value.isBlank()) {
            throw new IllegalArgumentException("Missing value: " + key);
        }

        try {

            double result = Double.parseDouble(value);

            if (!Double.isFinite(result) || result < min || result > max) {
                throw new IllegalArgumentException("Value out of range: " + key);
            }

            return result;

        } catch (NumberFormatException ex) {

            throw new IllegalArgumentException("Invalid number: " + key);
        }
    }

    private String soilType(double ph, double moisture) {

        if (ph < 5.5) {
            return "Acidic Soil";
        }

        if (ph > 7.5) {
            return "Alkaline Soil";
        }

        if (moisture < 30) {
            return "Dry Loamy Soil";
        }

        if (moisture > 75) {
            return "Moist Loamy Soil";
        }

        return "Loamy Soil";
    }

    private String soilHealth(
            double moisture,
            double nitrogen,
            double phosphorus,
            double potassium
    ) {

        int score = 0;

        if (moisture >= 30 && moisture <= 70) {
            score++;
        }

        if (nitrogen >= 40) {
            score++;
        }

        if (phosphorus >= 30) {
            score++;
        }

        if (potassium >= 30) {
            score++;
        }

        if (score >= 4) {
            return "Healthy";
        }

        if (score >= 2) {
            return "Moderate";
        }

        return "Needs Attention";
    }

    private String title(String crop) {

        if (crop == null || crop.isEmpty()) {
            return "Unknown";
        }

        return crop.substring(0, 1).toUpperCase(Locale.ROOT)
                + crop.substring(1).toLowerCase(Locale.ROOT);
    }

    private String esc(String value) {

        if (value == null) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {

            char c = value.charAt(i);

            switch (c) {

                case '"':
                    result.append("\\\"");
                    break;

                case '\\':
                    result.append("\\\\");
                    break;

                case '\b':
                    result.append("\\b");
                    break;

                case '\f':
                    result.append("\\f");
                    break;

                case '\n':
                    result.append("\\n");
                    break;

                case '\r':
                    result.append("\\r");
                    break;

                case '\t':
                    result.append("\\t");
                    break;

                default:
                    if (c < 0x20) {
                        result.append(String.format("\\u%04x", (int) c));
                    } else {
                        result.append(c);
                    }
            }
        }

        return result.toString();
    }

    private void text(
            HttpExchange e,
            int code,
            String value
    ) throws IOException {

        e.getResponseHeaders().set(
                "X-Content-Type-Options",
                "nosniff"
        );

        e.getResponseHeaders().set(
                "X-Frame-Options",
                "DENY"
        );

        e.getResponseHeaders().set(
                "Referrer-Policy",
                "strict-origin-when-cross-origin"
        );

        byte[] data = value.getBytes(StandardCharsets.UTF_8);

        if (e.getResponseHeaders().getFirst("Content-Type") == null) {
            e.getResponseHeaders().set(
                    "Content-Type",
                    "text/plain; charset=UTF-8"
            );
        }

        e.sendResponseHeaders(code, data.length);

        try (OutputStream os = e.getResponseBody()) {
            os.write(data);
        }
    }

    private void json(
            HttpExchange e,
            int code,
            String value
    ) throws IOException {

        e.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        text(e, code, value);
    }
}
