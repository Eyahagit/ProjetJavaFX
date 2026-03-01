package com.santebienetre.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes the database and creates tables if they don't exist.
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
                "  recommandations TEXT," +
                "  date_suivi DATE NOT NULL," +
                "  date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  CONSTRAINT fk_sante_utilisateur FOREIGN KEY (user_id) REFERENCES utilisateur(id) ON DELETE CASCADE" +
                ")"
            );

            // Create sleep_tracking table
            st.execute(
                "CREATE TABLE IF NOT EXISTS sleep_tracking (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id INT NOT NULL," +
                "  date_sommeil DATE NOT NULL," +
                "  heure_coucher TIME NOT NULL," +
                "  heure_reveil TIME NOT NULL," +
                "  duree_minutes INT NOT NULL," +
                "  qualite_sommeil TINYINT NOT NULL," +
                "  commentaire VARCHAR(1000)," +
                "  date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  CONSTRAINT fk_sleep_utilisateur FOREIGN KEY (user_id) REFERENCES utilisateur(id) ON DELETE CASCADE" +
                ")"
            );

            // Insert default user if empty (ignore if already exists)
            st.execute("INSERT IGNORE INTO utilisateur (id, nom, email) VALUES (1, 'Utilisateur Demo', 'demo@santebienetre.com')");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
