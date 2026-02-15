package Service;

import Models.Psychologue;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePsychologue implements Iservice<Psychologue> {

    private Connection connection;

    public ServicePsychologue() {
        connection = MyDatabase.getInstance().getConnection(); // singleton
    }

    @Override
    public void ajouter(Psychologue p) throws SQLDataException{
        String sql = "INSERT INTO psychologue (nom, prenom, specialite, diplome, experience, tarif, email, telephone, idCabinet) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getPrenom());
            ps.setString(3, p.getSpecialite());
            ps.setString(4, p.getDiplome());
            ps.setInt(5, p.getExperience());
            ps.setDouble(6, p.getTarif());
            ps.setString(7, p.getEmail());
            ps.setString(8, p.getTelephone());
            ps.setInt(9, p.getIdCabinet());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(Psychologue p) throws SQLDataException{
        String sql = "DELETE FROM psychologue WHERE idPsychologue = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, p.getIdPsychologue());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void modifier(Psychologue p) throws SQLDataException{
        String sql = "UPDATE psychologue SET nom=?, prenom=?, specialite=?, diplome=?, experience=?, tarif=?, email=?, telephone=?, idCabinet=? WHERE idPsychologue=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getPrenom());
            ps.setString(3, p.getSpecialite());
            ps.setString(4, p.getDiplome());
            ps.setInt(5, p.getExperience());
            ps.setDouble(6, p.getTarif());
            ps.setString(7, p.getEmail());
            ps.setString(8, p.getTelephone());
            ps.setInt(9, p.getIdCabinet());
            ps.setInt(10, p.getIdPsychologue());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Psychologue> recuperer() throws SQLDataException{
        List<Psychologue> list = new ArrayList<>();
        String sql = "SELECT * FROM psychologue";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Psychologue p = new Psychologue();
                p.setIdPsychologue(rs.getInt("idPsychologue"));
                p.setNom(rs.getString("nom"));
                p.setPrenom(rs.getString("prenom"));
                p.setSpecialite(rs.getString("specialite"));
                p.setDiplome(rs.getString("diplome"));
                p.setExperience(rs.getInt("experience"));
                p.setTarif(rs.getDouble("tarif"));
                p.setEmail(rs.getString("email"));
                p.setTelephone(rs.getString("telephone"));
                p.setIdCabinet(rs.getInt("idCabinet"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // NOUVELLE MÉTHODE AVEC JOINTURE
    public List<Psychologue> recupererAvecCabinet() {
        List<Psychologue> list = new ArrayList<>();
        String sql = "SELECT p.*, c.nomCabinet, c.ville, c.adresse " +
                "FROM psychologue p " +
                "INNER JOIN cabinet c ON p.idCabinet = c.idCabinet " +
                "ORDER BY p.nom, p.prenom";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Psychologue p = new Psychologue();
                p.setIdPsychologue(rs.getInt("idPsychologue"));
                p.setNom(rs.getString("nom"));
                p.setPrenom(rs.getString("prenom"));
                p.setSpecialite(rs.getString("specialite"));
                p.setDiplome(rs.getString("diplome"));
                p.setExperience(rs.getInt("experience"));
                p.setTarif(rs.getDouble("tarif"));
                p.setEmail(rs.getString("email"));
                p.setTelephone(rs.getString("telephone"));
                p.setIdCabinet(rs.getInt("idCabinet"));

                // NOUVEAU : Infos du cabinet
                p.setNomCabinet(rs.getString("nomCabinet"));
                p.setVilleCabinet(rs.getString("ville"));
                p.setAdresseCabinet(rs.getString("adresse"));

                list.add(p);
            }
            System.out.println("✅ " + list.size() + " psychologues chargés avec infos cabinet");

        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement avec jointure: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }


}
