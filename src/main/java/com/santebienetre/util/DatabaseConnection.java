package com.santebienetre.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for managing database connections.
 */
public final class DatabaseConnection {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final String DEFAULT_PORT = "3307";
    private static final String DEFAULT_DB = "grownmind";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private DatabaseConnection() {
        // Prevent instantiation
    }

    /**
     * Gets a new database connection.
     * @return Connection instance
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        String host = System.getProperty("db.host", DEFAULT_HOST);
        String port = System.getProperty("db.port", DEFAULT_PORT);
        String db = System.getProperty("db.name", DEFAULT_DB);
        String user = System.getProperty("db.user", DEFAULT_USER);
        String password = System.getProperty("db.password", DEFAULT_PASSWORD);

        String url = "jdbc:mysql://" + host + ":" + port + "/" + db +
                "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        System.err.println("[DEBUG] Attempting DB connection to: " + url + " as " + user);
        Connection conn = DriverManager.getConnection(url, user, password);
        System.err.println("[DEBUG] DB connection established.");
        return conn;
    }
}
