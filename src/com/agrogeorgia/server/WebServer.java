package com.agrogeorgia.server;

import com.agrogeorgia.ai.CropPrediction;
import com.agrogeorgia.database.DatabaseConnection;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;

public class WebServer {

    private final CropPrediction ai;
    private HttpServer server;

    public WebServer(CropPrediction ai) {
        this.ai = ai;
    }

    public void start() throws IOException {

        server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/api/predict", this::predict);
        server.createContext("/api/readings", this::readings);
        server.createContext("/api/history", this::history);
        server.createContext("/api/research", this::research);
        server.createContext("/", this::staticFiles);

        server.setExecutor(null);
        server.start();

        System.out.println("Web server started at:");
        System.out.println("http://localhost:8080");
    }

    private void predict(HttpExchange e) throws IOException {

        try {

            Map<String, String> p = query(e.getRequestURI());

            double moisture = number(p, "moisture", 50);
            double ph = number(p, "ph", 6.5);
            double nitrogen = number(p, "nitrogen", 50);
            double phosphorus = number(p, "phosphorus", 50);
            double potassium = number(p, "potassium", 50);
            double temperature = number(p, "temperature", 25);
            double humidity = number(p, "humidity", 60);
            double rainfall = number(p, "rainfall", 100);

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

        } catch (Exception ex) {

            json(
                    e,
                    500,
                    "{\"status\":\"error\",\"message\":\""
                            + esc(ex.getMessage())
                            + "\"}"
            );
        }
    }

    private void readings(HttpExchange e) throws IOException {

        if (!"GET".equalsIgnoreCase(e.getRequestMethod())) {
            json(e, 405, "{\"error\":\"GET method required\"}");
            return;
        }

        try {

            Map<String, String> p = query(e.getRequestURI());

            double moisture = number(p, "moisture", 50);
            double ph = number(p, "ph", 6.5);
            double nitrogen = number(p, "nitrogen", 50);
            double phosphorus = number(p, "phosphorus", 50);
            double potassium = number(p, "potassium", 50);
            double temperature = number(p, "temperature", 25);
            double humidity = number(p, "humidity", 60);
            double rainfall = number(p, "rainfall", 100);

            String source = p.getOrDefault("source", "web");

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
                    "{\"status\":\"saved\","
                            + "\"message\":\"Reading saved successfully\"}"
            );

        } catch (Exception ex) {

            json(
                    e,
                    500,
                    "{\"status\":\"error\",\"message\":\""
                            + esc(ex.getMessage())
                            + "\"}"
            );
        }
    }

    private void history(HttpExchange e) throws IOException {

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

        } catch (SQLException ex) {

            json(
                    e,
                    500,
                    "{\"error\":\""
                            + esc(ex.getMessage())
                            + "\"}"
            );

            return;
        }

        s.append("]");

        json(e, 200, s.toString());
    }

    private void research(HttpExchange e) throws IOException {

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

        } catch (SQLException ex) {

            json(
                    e,
                    500,
                    "{\"error\":\""
                            + esc(ex.getMessage())
                            + "\"}"
            );
        }
    }

    private void staticFiles(HttpExchange e) throws IOException {

        String path = e.getRequestURI().getPath();

        if (path.equals("/")) {
            path = "/index.html";
        }

        if (path.contains("..")) {
            text(e, 403, "Forbidden");
            return;
        }

        Path file = Paths.get("web" + path);

        if (!Files.exists(file) || Files.isDirectory(file)) {
            text(e, 404, "Page not found");
            return;
        }

        byte[] data = Files.readAllBytes(file);

        String type = contentType(path);

        e.getResponseHeaders().set(
                "Content-Type",
                type
        );

        e.sendResponseHeaders(
                200,
                data.length
        );

        try (OutputStream os = e.getResponseBody()) {
            os.write(data);
        }
    }

    private String contentType(String path) {

        if (path.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        }

        if (path.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }

        if (path.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        }

        return "text/plain; charset=UTF-8";
    }

    private Map<String, String> query(URI uri) {

        java.util.HashMap<String, String> map =
                new java.util.HashMap<>();

        String q = uri.getRawQuery();

        if (q == null || q.isEmpty()) {
            return map;
        }

        for (String part : q.split("&")) {

            String[] kv = part.split("=", 2);

            String key = decode(kv[0]);

            String value =
                    kv.length > 1
                            ? decode(kv[1])
                            : "";

            map.put(key, value);
        }

        return map;
    }

    private String decode(String value) {

        try {
            return java.net.URLDecoder.decode(
                    value,
                    StandardCharsets.UTF_8
            );
        } catch (Exception ex) {
            return value;
        }
    }

    private double number(
            Map<String, String> map,
            String key,
            double defaultValue
    ) {

        try {

            String value = map.get(key);

            if (value == null || value.isEmpty()) {
                return defaultValue;
            }

            return Double.parseDouble(value);

        } catch (Exception ex) {

            return defaultValue;
        }
    }

    private String soilType(
            double ph,
            double moisture
    ) {

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

        return crop.substring(0, 1).toUpperCase()
                + crop.substring(1).toLowerCase();
    }

    private String esc(String x) {

        if (x == null) {
            return "";
        }

        return x
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ");
    }

    private void text(
            HttpExchange e,
            int code,
            String x
    ) throws IOException {

        byte[] b =
                x.getBytes(StandardCharsets.UTF_8);

        e.sendResponseHeaders(
                code,
                b.length
        );

        try (OutputStream os = e.getResponseBody()) {
            os.write(b);
        }
    }

    private void json(
            HttpExchange e,
            int code,
            String x
    ) throws IOException {

        e.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        text(e, code, x);
    }
}