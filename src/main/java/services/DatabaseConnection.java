package services;

import java.sql.*;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/pidev";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connexion à la base de données growmind établie");
            } catch (ClassNotFoundException e) {
                System.err.println("❌ Driver JDBC non trouvé");
                e.printStackTrace();
            } catch (SQLException e) {
                System.err.println("❌ Erreur de connexion à la base de données");
                e.printStackTrace();
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("🔌 Connexion à la base de données fermée");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Test de connexion réussi");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Test de connexion échoué");
            e.printStackTrace();
        }
        return false;
    }
}