package Service;

import Models.RendezVous;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;  // Pour java.util.Date

public class ServiceRendezVous implements Iservice<RendezVous> {

    private Connection connection;

    public ServiceRendezVous() {
        connection = MyDatabase.getInstance().getConnection();
    }

    // ========== CRUD DE BASE MODIFIÉ ==========

    @Override
    public void ajouter(RendezVous r) {
        // REQUÊTE MODIFIÉE : Ajout des colonnes patient et rappel
        String sql = "INSERT INTO rendezvous (dateRdv, heure, statut, typeCons, idPsychologue, " +
                "telephone_patient, nom_patient, prenom_patient, rappel_envoye) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            // Champs existants
            ps.setDate(1, new java.sql.Date(r.getDateRdv().getTime()));
            ps.setString(2, r.getHeure());
            ps.setString(3, r.getStatut());
            ps.setString(4, r.getTypeCons());
            ps.setInt(5, r.getIdPsychologue());

            // NOUVEAUX CHAMPS patient
            ps.setString(6, r.getTelephonePatient());
            ps.setString(7, r.getNomPatient());
            ps.setString(8, r.getPrenomPatient());

            // NOUVEAU CHAMP rappel
            ps.setBoolean(9, r.isRappelEnvoye());

            ps.executeUpdate();
            System.out.println("✅ Rendez-vous ajouté avec informations patient !");

        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout: " + e.getMessage());
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
            System.err.println("❌ Erreur suppression: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void modifier(RendezVous r) {
        // REQUÊTE MODIFIÉE : Inclure les nouveaux champs
        String sql = "UPDATE rendezvous SET dateRdv=?, heure=?, statut=?, typeCons=?, idPsychologue=?, " +
                "telephone_patient=?, nom_patient=?, prenom_patient=?, rappel_envoye=? " +
                "WHERE idRdv=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            // Champs existants
            ps.setDate(1, new java.sql.Date(r.getDateRdv().getTime()));
            ps.setString(2, r.getHeure());
            ps.setString(3, r.getStatut());
            ps.setString(4, r.getTypeCons());
            ps.setInt(5, r.getIdPsychologue());

            // NOUVEAUX CHAMPS patient
            ps.setString(6, r.getTelephonePatient());
            ps.setString(7, r.getNomPatient());
            ps.setString(8, r.getPrenomPatient());

            // NOUVEAU CHAMP rappel
            ps.setBoolean(9, r.isRappelEnvoye());

            // WHERE clause
            ps.setInt(10, r.getIdRdv());

            ps.executeUpdate();
            System.out.println("✅ Rendez-vous modifié !");

        } catch (SQLException e) {
            System.err.println("❌ Erreur modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<RendezVous> recuperer() {
        List<RendezVous> list = new ArrayList<>();
        // REQUÊTE MODIFIÉE : Sélectionner tous les champs
        String sql = "SELECT * FROM rendezvous ORDER BY dateRdv DESC, heure";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                RendezVous r = new RendezVous();

                // Champs existants
                r.setIdRdv(rs.getInt("idRdv"));
                r.setDateRdv(rs.getDate("dateRdv"));
                r.setHeure(rs.getString("heure"));
                r.setStatut(rs.getString("statut"));
                r.setTypeCons(rs.getString("typeCons"));
                r.setIdPsychologue(rs.getInt("idPsychologue"));

                // NOUVEAUX CHAMPS patient
                r.setTelephonePatient(rs.getString("telephone_patient"));
                r.setNomPatient(rs.getString("nom_patient"));
                r.setPrenomPatient(rs.getString("prenom_patient"));

                // NOUVEAU CHAMP rappel
                r.setRappelEnvoye(rs.getBoolean("rappel_envoye"));
                r.setDateRappel(rs.getTimestamp("date_rappel"));

                list.add(r);
            }
            System.out.println("✅ " + list.size() + " rendez-vous chargés");
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    // ========== MÉTHODES AVEC JOINTURES MODIFIÉES ==========

    public List<RendezVous> recupererAvecDetails() {
        List<RendezVous> list = new ArrayList<>();
        // REQUÊTE MODIFIÉE : Inclure tous les nouveaux champs
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

                // Champs existants
                r.setIdRdv(rs.getInt("idRdv"));
                r.setDateRdv(rs.getDate("dateRdv"));
                r.setHeure(rs.getString("heure"));
                r.setStatut(rs.getString("statut"));
                r.setTypeCons(rs.getString("typeCons"));
                r.setIdPsychologue(rs.getInt("idPsychologue"));

                // NOUVEAUX CHAMPS patient
                r.setTelephonePatient(rs.getString("telephone_patient"));
                r.setNomPatient(rs.getString("nom_patient"));
                r.setPrenomPatient(rs.getString("prenom_patient"));

                // NOUVEAU CHAMP rappel
                r.setRappelEnvoye(rs.getBoolean("rappel_envoye"));
                r.setDateRappel(rs.getTimestamp("date_rappel"));

                // Infos psychologue (existants)
                r.setNomPsychologue(rs.getString("nomPsycho"));
                r.setPrenomPsychologue(rs.getString("prenomPsycho"));
                r.setSpecialitePsychologue(rs.getString("specialite"));

                // Infos cabinet (existants)
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

    // ========== NOUVELLES MÉTHODES POUR LES RAPPELS ==========

    /**
     * Récupère les rendez-vous par téléphone (pour l'historique patient)
     */
    public List<RendezVous> getByTelephone(String telephone) {
        List<RendezVous> list = new ArrayList<>();
        String sql = "SELECT r.*, " +
                "p.nom as nomPsycho, p.prenom as prenomPsycho " +
                "FROM rendezvous r " +
                "LEFT JOIN psychologue p ON r.idPsychologue = p.idPsychologue " +
                "WHERE r.telephone_patient = ? " +
                "ORDER BY r.dateRdv DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, telephone);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                RendezVous r = new RendezVous();

                // Champs existants
                r.setIdRdv(rs.getInt("idRdv"));
                r.setDateRdv(rs.getDate("dateRdv"));
                r.setHeure(rs.getString("heure"));
                r.setStatut(rs.getString("statut"));
                r.setTypeCons(rs.getString("typeCons"));
                r.setIdPsychologue(rs.getInt("idPsychologue"));

                // Informations patient
                r.setTelephonePatient(rs.getString("telephone_patient"));
                r.setNomPatient(rs.getString("nom_patient"));
                r.setPrenomPatient(rs.getString("prenom_patient"));

                // Infos psychologue pour affichage
                r.setNomPsychologue(rs.getString("nomPsycho"));
                r.setPrenomPsychologue(rs.getString("prenomPsycho"));

                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement par téléphone: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Marquer un rappel comme envoyé
     */
    public void marquerRappelEnvoye(int idRdv, Date dateRappel) {
        String sql = "UPDATE rendezvous SET rappel_envoye = ?, date_rappel = ? WHERE idRdv = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, true);
            ps.setTimestamp(2, new java.sql.Timestamp(dateRappel.getTime()));
            ps.setInt(3, idRdv);
            ps.executeUpdate();
            System.out.println("✅ Rappel marqué comme envoyé pour rendez-vous #" + idRdv);
        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour rappel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Récupère les rendez-vous à venir (non passés)
     */
    public List<RendezVous> getRendezVousAVenir() {
        List<RendezVous> list = new ArrayList<>();
        String sql = "SELECT * FROM rendezvous WHERE dateRdv >= CURDATE() ORDER BY dateRdv, heure";

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
                r.setTelephonePatient(rs.getString("telephone_patient"));
                r.setNomPatient(rs.getString("nom_patient"));
                r.setPrenomPatient(rs.getString("prenom_patient"));
                r.setRappelEnvoye(rs.getBoolean("rappel_envoye"));
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Récupère les rendez-vous sans rappel envoyé
     */
    public List<RendezVous> getRendezVousSansRappel() {
        List<RendezVous> list = new ArrayList<>();
        String sql = "SELECT r.*, " +
                "p.nom as nomPsycho, p.prenom as prenomPsycho, " +
                "c.nomCabinet " +
                "FROM rendezvous r " +
                "INNER JOIN psychologue p ON r.idPsychologue = p.idPsychologue " +
                "INNER JOIN cabinet c ON p.idCabinet = c.idCabinet " +
                "WHERE r.rappel_envoye = false AND r.dateRdv > CURDATE() " +
                "ORDER BY r.dateRdv";

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
                r.setTelephonePatient(rs.getString("telephone_patient"));
                r.setNomPatient(rs.getString("nom_patient"));
                r.setPrenomPatient(rs.getString("prenom_patient"));
                r.setRappelEnvoye(rs.getBoolean("rappel_envoye"));
                r.setNomPsychologue(rs.getString("nomPsycho"));
                r.setPrenomPsychologue(rs.getString("prenomPsycho"));
                r.setNomCabinet(rs.getString("nomCabinet"));
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ========== MÉTHODES EXISTANTES CONSERVÉES ==========

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

                // Ajout des infos patient si elles existent
                r.setTelephonePatient(rs.getString("telephone_patient"));
                r.setNomPatient(rs.getString("nom_patient"));
                r.setPrenomPatient(rs.getString("prenom_patient"));

                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement par psychologue: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

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

                // Ajout des infos patient
                r.setTelephonePatient(rs.getString("telephone_patient"));
                r.setNomPatient(rs.getString("nom_patient"));
                r.setPrenomPatient(rs.getString("prenom_patient"));

                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement par date: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public List<RendezVous> getRendezVousDuJour() {
        return getRendezVousByDate(new Date());
    }

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

                // Infos patient
                r.setTelephonePatient(rs.getString("telephone_patient"));
                r.setNomPatient(rs.getString("nom_patient"));
                r.setPrenomPatient(rs.getString("prenom_patient"));
                r.setRappelEnvoye(rs.getBoolean("rappel_envoye"));
                r.setDateRappel(rs.getTimestamp("date_rappel"));

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