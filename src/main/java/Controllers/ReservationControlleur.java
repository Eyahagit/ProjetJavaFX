package Controllers;

import Models.Reservation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationControlleur {

    private Connection cnx;

    public ReservationControlleur(Connection cnx) {
        this.cnx = cnx;
    }

    // Ajouter une réservation - CORRIGÉ pour inclure toutes les colonnes REQUISES
    public void ajouter(Reservation r) throws SQLException {
        // Ajout de la colonne remarques (obligatoire car NOT NULL dans votre table)
        String sql = "INSERT INTO reservation (idEvenement, utilisateur_id, nom, email, telephone, nombre_personnes, remarques, date_reservation) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, r.getIdEvenement());
            ps.setInt(2, r.getUtilisateurId());
            ps.setString(3, r.getNom() != null ? r.getNom() : "");
            ps.setString(4, r.getEmail());
            ps.setString(5, r.getTelephone() != null ? r.getTelephone() : "");
            ps.setInt(6, r.getNombrePersonnes());
            ps.setTimestamp(8, Timestamp.valueOf(r.getDateReservation())); // Ajout de date_reservation

            ps.executeUpdate();
        }
    }

    // Récupérer les réservations d'un utilisateur
    public List<Reservation> getByUtilisateur(int utilisateurId) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservation WHERE utilisateur_id = ? ORDER BY date_reservation DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, utilisateurId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Reservation r = new Reservation();
                r.setIdReservation(rs.getInt("idReservation"));
                r.setIdEvenement(rs.getInt("idEvenement"));
                r.setUtilisateurId(rs.getInt("utilisateur_id"));
                r.setNom(rs.getString("nom"));
                r.setEmail(rs.getString("email"));
                r.setTelephone(rs.getString("telephone"));
                r.setNombrePersonnes(rs.getInt("nombre_personnes"));

                Timestamp timestamp = rs.getTimestamp("date_reservation");
                if (timestamp != null) {
                    r.setDateReservation(timestamp.toLocalDateTime());
                }

                reservations.add(r);
            }
        }
        return reservations;
    }

    // Récupérer les réservations d'un événement
    public List<Reservation> getByEvenement(int idEvenement) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservation WHERE idEvenement = ? ORDER BY date_reservation DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idEvenement);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Reservation r = new Reservation();
                r.setIdReservation(rs.getInt("idReservation"));
                r.setIdEvenement(rs.getInt("idEvenement"));
                r.setUtilisateurId(rs.getInt("utilisateur_id"));
                r.setNom(rs.getString("nom"));
                r.setEmail(rs.getString("email"));
                r.setTelephone(rs.getString("telephone"));
                r.setNombrePersonnes(rs.getInt("nombre_personnes"));

                Timestamp timestamp = rs.getTimestamp("date_reservation");
                if (timestamp != null) {
                    r.setDateReservation(timestamp.toLocalDateTime());
                }

                reservations.add(r);
            }
        }
        return reservations;
    }

    // Compter les réservations pour un événement
    public int countByEvenement(int idEvenement) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM reservation WHERE idEvenement = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idEvenement);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }
}