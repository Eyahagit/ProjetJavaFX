package utiles;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

public class mydb {

    final String URL = "jdbc:mysql://localhost:3306/douaa";
    final String USERNAME = "root";
    final String PASSWORD = "";

    private Connection connection;
    private static mydb instance;


    public mydb() {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connexion réussie !!");
        } catch (SQLException e) {
            System.out.println("Echec de Connexion !!");
            e.printStackTrace();
        }
    }

    public static mydb getInstance() {
        if (instance == null) {
            instance = new mydb();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
