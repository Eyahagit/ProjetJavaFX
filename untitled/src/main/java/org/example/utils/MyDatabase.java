package org.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class MyDatabase {
    private static final String URL = "jdbc:mysql://localhost:3306/eyaprojet?useSSL=false&serverTimezone=UTC";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";
    private static MyDatabase instance;

    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    /**
     * Returns a new connection each time. Caller must close it (e.g. in try-with-resources).
     * This avoids "connection closed" errors when services close their connection after use.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    private MyDatabase() {
        try (Connection c = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            if (c.isValid(1)) System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            System.out.println("Database connection check: " + e.getMessage());
        }
    }
}
