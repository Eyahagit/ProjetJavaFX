package com.santebienetre.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes the H2 database and creates tables if they don't exist.
 */
public final class DatabaseInit {

    private DatabaseInit() {}

    public static void initialize() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {

            // Create utilisateur table (minimal for FK)
            st.execute(
                "CREATE TABLE IF NOT EXISTS utilisateur (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  nom VARCHAR(100) NOT NULL," +
                "  email VARCHAR(255) UNIQUE NOT NULL" +
                ")"
            );

            // Create sante_bien_etre table
            st.execute(
                "CREATE TABLE IF NOT EXISTS sante_bien_etre (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id INT NOT NULL," +
                "  humeur VARCHAR(50) NOT NULL," +
                "  niveau_stress INT NOT NULL," +
                "  qualite_sommeil INT NOT NULL," +
                "  nutrition VARCHAR(255)," +
                "  activite_physique VARCHAR(255)," +
                "  developpement_personnel VARCHAR(500)," +
                "  recommandations CLOB," +
                "  date_suivi DATE NOT NULL," +
                "  date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  CONSTRAINT fk_sante_utilisateur FOREIGN KEY (user_id) REFERENCES utilisateur(id) ON DELETE CASCADE" +
                ")"
            );

            // Insert default user if empty (ignore if already exists)
            st.execute("INSERT INTO utilisateur (id, nom, email) SELECT 1, 'Utilisateur Demo', 'demo@santebienetre.com' " +
                    "WHERE NOT EXISTS (SELECT 1 FROM utilisateur WHERE id = 1)");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
