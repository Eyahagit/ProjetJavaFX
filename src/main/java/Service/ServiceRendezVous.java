package Service;

import Models.RendezVous;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;  // ← Pour java.util.Date

public class ServiceRendezVous implements Iservice<RendezVous> {

    private Connection connection;

    public ServiceRendezVous() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(RendezVous r) {
        String sql = "INSERT INTO rendezvous (dateRdv, heure, statut, typeCons, idPsychologue) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            // IMPORTANT: Convertir java.util.Date en java.sql.Date
            java.sql.Date sqlDate = new java.sql.Date(r.getDateRdv().getTime());
            ps.setDate(1, sqlDate);
            ps.setString(2, r.getHeure());
            ps.setString(3, r.getStatut());
            ps.setString(4, r.getTypeCons());
            ps.setInt(5, r.getIdPsychologue());
            ps.executeUpdate();
            System.out.println("✅ Rendez-vous ajouté !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(RendezVous r) {
        String sql = "DELETE FROM rendezvous WHERE idRdv = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, r.getIdRdv());
            ps.executeUpdate();
            System.out.println("✅ Rendez-vous supprimé !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression rendez-vous: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void modifier(RendezVous r) {
        String sql = "UPDATE rendezvous SET dateRdv=?, heure=?, statut=?, typeCons=?, idPsychologue=? WHERE idRdv=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(r.getDateRdv().getTime()));
            ps.setString(2, r.getHeure());
            ps.setString(3, r.getStatut());
            ps.setString(4, r.getTypeCons());
            ps.setInt(5, r.getIdPsychologue());
            ps.setInt(6, r.getIdRdv());
            ps.executeUpdate();
            System.out.println("✅ Rendez-vous modifié !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur modification rendez-vous: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<RendezVous> recuperer() {
        List<RendezVous> list = new ArrayList<>();
        String sql = "SELECT * FROM rendezvous ORDER BY dateRdv DESC, heure";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                RendezVous r = new RendezVous();
                r.setIdRdv(rs.getInt("idRdv"));
                r.setDateRdv(rs.getDate("dateRdv"));
                r.setHeure(rs.getString("heure"));
                r.setStatut(rs.getString("statut"));
                r.setTypeCons(rs.getString("typeCons"));
                r.setIdPsychologue(rs.getInt("idPsychologue"));
                list.add(r);
            }
            System.out.println("✅ " + list.size() + " rendez-vous chargés");
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement rendez-vous: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    // ========== MÉTHODES AVEC JOINTURES ==========

    /**
     * Récupère tous les rendez-vous avec les détails du psychologue et du cabinet
     */
    public List<RendezVous> recupererAvecDetails() {
        List<RendezVous> list = new ArrayList<>();
        String sql = "SELECT r.*, " +
                "p.nom as nomPsycho, p.prenom as prenomPsycho, p.specialite, " +
                "c.nomCabinet, c.ville " +
                "FROM rendezvous r " +
                "INNER JOIN psychologue p ON r.idPsychologue = p.idPsychologue " +
                "INNER JOIN cabinet c ON p.idCabinet = c.idCabinet " +
                "ORDER BY r.dateRdv DESC, r.heure";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                RendezVous r = new RendezVous();
                r.setIdRdv(rs.getInt("idRdv"));
                r.setDateRdv(rs.getDate("dateRdv"));
                r.setHeure(rs.getString("heure"));
                r.setStatut(rs.getString("statut"));
                r.setTypeCons(rs.getString("typeCons"));
                r.setIdPsychologue(rs.getInt("idPsychologue"));

                // Infos psychologue
                r.setNomPsychologue(rs.getString("nomPsycho"));
                r.setPrenomPsychologue(rs.getString("prenomPsycho"));
                r.setSpecialitePsychologue(rs.getString("specialite"));

                // Infos cabinet
                r.setNomCabinet(rs.getString("nomCabinet"));
                r.setVilleCabinet(rs.getString("ville"));

                list.add(r);
            }
            System.out.println("✅ " + list.size() + " rendez-vous chargés avec détails");
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement détails: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Récupère les rendez-vous d'un psychologue spécifique
     */
    public List<RendezVous> getRendezVousByPsychologue(int idPsychologue) {
        List<RendezVous> list = new ArrayList<>();
        String sql = "SELECT r.*, p.nom as nomPsycho, p.prenom as prenomPsycho " +
                "FROM rendezvous r " +
                "INNER JOIN psychologue p ON r.idPsychologue = p.idPsychologue " +
                "WHERE r.idPsychologue = ? " +
                "ORDER BY r.dateRdv DESC, r.heure";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idPsychologue);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                RendezVous r = new RendezVous();
                r.setIdRdv(rs.getInt("idRdv"));
                r.setDateRdv(rs.getDate("dateRdv"));
                r.setHeure(rs.getString("heure"));
                r.setStatut(rs.getString("statut"));
                r.setTypeCons(rs.getString("typeCons"));
                r.setIdPsychologue(rs.getInt("idPsychologue"));
                r.setNomPsychologue(rs.getString("nomPsycho"));
                r.setPrenomPsychologue(rs.getString("prenomPsycho"));

                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement par psychologue: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Récupère les rendez-vous d'une date spécifique
     */
    public List<RendezVous> getRendezVousByDate(Date date) {
        List<RendezVous> list = new ArrayList<>();
        String sql = "SELECT r.*, p.nom as nomPsycho, p.prenom as prenomPsycho, c.nomCabinet " +
                "FROM rendezvous r " +
                "INNER JOIN psychologue p ON r.idPsychologue = p.idPsychologue " +
                "INNER JOIN cabinet c ON p.idCabinet = c.idCabinet " +
                "WHERE r.dateRdv = ? " +
                "ORDER BY r.heure";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(date.getTime()));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                RendezVous r = new RendezVous();
                r.setIdRdv(rs.getInt("idRdv"));
                r.setDateRdv(rs.getDate("dateRdv"));
                r.setHeure(rs.getString("heure"));
                r.setStatut(rs.getString("statut"));
                r.setTypeCons(rs.getString("typeCons"));
                r.setIdPsychologue(rs.getInt("idPsychologue"));
                r.setNomPsychologue(rs.getString("nomPsycho"));
                r.setPrenomPsychologue(rs.getString("prenomPsycho"));
                r.setNomCabinet(rs.getString("nomCabinet"));

                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement par date: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Récupère les rendez-vous du jour
     */
    public List<RendezVous> getRendezVousDuJour() {
        return getRendezVousByDate(new Date());
    }

    /**
     * Compte les rendez-vous par statut
     */
    public int countByStatut(String statut) {
        String sql = "SELECT COUNT(*) as total FROM rendezvous WHERE statut = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Récupère un rendez-vous par son ID avec tous les détails
     */
    public RendezVous getById(int id) {
        String sql = "SELECT r.*, p.nom as nomPsycho, p.prenom as prenomPsycho, p.specialite, " +
                "c.nomCabinet, c.ville " +
                "FROM rendezvous r " +
                "INNER JOIN psychologue p ON r.idPsychologue = p.idPsychologue " +
                "INNER JOIN cabinet c ON p.idCabinet = c.idCabinet " +
                "WHERE r.idRdv = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                RendezVous r = new RendezVous();
                r.setIdRdv(rs.getInt("idRdv"));
                r.setDateRdv(rs.getDate("dateRdv"));
                r.setHeure(rs.getString("heure"));
                r.setStatut(rs.getString("statut"));
                r.setTypeCons(rs.getString("typeCons"));
                r.setIdPsychologue(rs.getInt("idPsychologue"));

                // Infos psychologue
                r.setNomPsychologue(rs.getString("nomPsycho"));
                r.setPrenomPsychologue(rs.getString("prenomPsycho"));
                r.setSpecialitePsychologue(rs.getString("specialite"));

                // Infos cabinet
                r.setNomCabinet(rs.getString("nomCabinet"));
                r.setVilleCabinet(rs.getString("ville"));

                return r;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}