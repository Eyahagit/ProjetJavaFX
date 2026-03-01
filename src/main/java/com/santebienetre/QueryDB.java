package com.santebienetre;

import com.santebienetre.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Simple utility to query the configured MySQL/MariaDB database directly.
 *
 * Usage (optional overrides):
 *   PowerShell:
 *     java "-Ddb.host=127.0.0.1" "-Ddb.port=3307" "-Ddb.name=grownmind" "-Ddb.user=root" "-Ddb.password=" ... com.santebienetre.QueryDB
 */
public class QueryDB {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("[OK] Connected to database.");

            // Print current database name (helps verify you are on grownmind)
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT DATABASE() AS db")) {
                if (rs.next()) {
                    System.out.println("[INFO] DATABASE() = " + rs.getString("db"));
                }
            }

            System.out.println("\nUTILISATEUR:");
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id, nom, email FROM utilisateur ORDER BY id")) {
                while (rs.next()) {
                    System.out.println("  ID: " + rs.getInt("id")
                            + " | Nom: " + rs.getString("nom")
                            + " | Email: " + rs.getString("email"));
                }
            }

            System.out.println("\nSANTE_BIEN_ETRE (latest 20):");
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT id, user_id, humeur, niveau_stress, qualite_sommeil, nutrition, date_suivi " +
                                 "FROM sante_bien_etre ORDER BY date_suivi DESC, id DESC"
                 )) {
                int rows = 0;
                while (rs.next() && rows < 20) {
                    rows++;
                    System.out.println("  #" + rs.getInt("id")
                            + " user=" + rs.getInt("user_id")
                            + " humeur=" + rs.getString("humeur")
                            + " stress=" + rs.getInt("niveau_stress")
                            + " sommeil=" + rs.getInt("qualite_sommeil")
                            + " nutrition=" + rs.getString("nutrition")
                            + " date=" + rs.getDate("date_suivi"));
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
            e.printStackTrace();
        }
    }
}
