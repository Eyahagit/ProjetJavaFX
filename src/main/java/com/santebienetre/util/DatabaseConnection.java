package com.santebienetre.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for managing database connections.
 * Uses H2 embedded database for standalone application.
 */
public final class DatabaseConnection {

    private static final String URL = "jdbc:h2:./data/santebienetre;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private DatabaseConnection() {
        // Prevent instantiation
    }

    /**
     * Gets a new database connection.
     * @return Connection instance
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
