package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class mydb {

    final String URL = "jdbc:mysql://127.0.0.1:3306/doua?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8&serverTimezone=UTC&connectTimeout=5000&socketTimeout=5000";
    final String DB_NAME = "doua";
    final String USERNAME = "root";
    final String PASSWORD = "";

    private Connection connection;
    private static mydb instance;
    private volatile String lastError;

    public mydb() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("✅ Connexion réussie !");
            forceSchema();
        } catch (SQLException e) {
            lastError = e.getMessage();
            System.err.println("❌ Echec de Connexion: " + e.getMessage());
            e.printStackTrace();
            connection = null;
        } catch (ClassNotFoundException e) {
            lastError = e.getMessage();
            System.err.println("❌ MySQL Driver introuvable: " + e.getMessage());
            connection = null;
        }
    }

    private void forceSchema() {
        if (connection == null) {
            return;
        }

        try {
            if (!connection.isClosed()) {
                connection.setCatalog(DB_NAME);
                
                try (Statement st = connection.createStatement()) {
                    st.execute("USE " + DB_NAME);
                }
            }
        } catch (SQLException ignored) {
            System.err.println("⚠️ Schema force failed: " + ignored.getMessage());
        }
    }

    public static mydb getInstance() {
        if (instance == null) {
            instance = new mydb();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                System.out.println("🔄 Reconnecting to database...");
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                forceSchema();
                System.out.println("✅ Reconnected successfully!");
                lastError = null;
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
            System.err.println("❌ Connection error: " + e.getMessage());
            connection = null;
        } catch (ClassNotFoundException e) {
            lastError = e.getMessage();
            System.err.println("❌ MySQL Driver introuvable: " + e.getMessage());
            connection = null;
        }
        return connection;
    }

    public String getLastError() {
        return lastError;
    }
    
    public void testConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                try (Statement st = connection.createStatement()) {
                    st.executeQuery("SELECT 1");
                }
                System.out.println("✅ Connection test successful");
            } else {
                System.out.println("❌ Connection is null or closed");
            }
        } catch (SQLException e) {
            System.err.println("❌ Connection test failed: " + e.getMessage());
        }
    }
}
