package Services;

import Models.Evenement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementControlleur implements CrudEven<Evenement> {

    private Connection cnx;

    // 🔹 Connexion injectée depuis l'extérieur
    public EvenementControlleur(Connection cnx) {
        this.cnx = cnx;
    }

    // ================== AJOUTER ==================
    @Override
    public void ajouter(Evenement e) {
        String sql = "INSERT INTO evenement (titre, description, date, localisation) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, e.getTitre());
            ps.setString(2, e.getDescription());
            ps.setDate(3, Date.valueOf(e.getDate()));
            ps.setString(4, e.getLocalisation());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // ================== MODIFIER ==================
    @Override
    public void modifier(Evenement e) {
        String sql = "UPDATE evenement SET titre=?, description=?, date=?, localisation=? WHERE idEvenement=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, e.getTitre());
            ps.setString(2, e.getDescription());
            ps.setDate(3, Date.valueOf(e.getDate()));
            ps.setString(4, e.getLocalisation());
            ps.setInt(5, e.getIdEvenement());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // ================== SUPPRIMER ==================
    @Override
    public void supprimer(int id) {
        String sql = "DELETE FROM evenement WHERE idEvenement=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // ================== AFFICHER ==================
    @Override
    public List<Evenement> afficher() {
        List<Evenement> evenements = new ArrayList<>();
        String sql = "SELECT * FROM evenement";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Evenement e = new Evenement();
                e.setIdEvenement(rs.getInt("idEvenement"));
                e.setTitre(rs.getString("titre"));
                e.setDescription(rs.getString("description"));

                // Conversion Date SQL → LocalDate
                Date sqlDate = rs.getDate("date");
                if (sqlDate != null) {
                    e.setDate(sqlDate.toLocalDate());
                }

                e.setLocalisation(rs.getString("localisation"));
                evenements.add(e);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return evenements;
    }

    // ================== RECHERCHER ==================
    @Override
    public Evenement rechercher(int id) {
        String sql = "SELECT * FROM evenement WHERE idEvenement=?";
        Evenement e = null;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                e = new Evenement();
                e.setIdEvenement(rs.getInt("idEvenement"));
                e.setTitre(rs.getString("titre"));
                e.setDescription(rs.getString("description"));

                // Conversion Date SQL → LocalDate
                Date sqlDate = rs.getDate("date");
                if (sqlDate != null) {
                    e.setDate(sqlDate.toLocalDate());
                }

                e.setLocalisation(rs.getString("localisation"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return e;
    }
}