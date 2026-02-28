package Services;

import entities.Reponse;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDateTime;
import utils.Database;

public class ServiceReponse {

    private Connection connection;

    public ServiceReponse() {
        this.connection = Database.getInstance().getConnection();
    }

    public void ajouter(Reponse reponse) {
        String query = "INSERT INTO reponses (post_id, auteur, contenu, date_reponse, likes, dislikes) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, reponse.getIdPost());
            pstmt.setString(2, reponse.getAuteur());
            pstmt.setString(3, reponse.getContenu());
            pstmt.setTimestamp(4, Timestamp.valueOf(reponse.getDateReponse()));
            pstmt.setInt(5, reponse.getLikes());
            pstmt.setInt(6, reponse.getDislikes());

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                reponse.setIdReponse(rs.getInt(1));
            }

            System.out.println("💬 Réponse ajoutée: " + reponse.getIdReponse());

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout de la réponse");
            e.printStackTrace();
        }
    }

    public void supprimer(Reponse reponse) {
        String query = "DELETE FROM reponses WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, reponse.getIdReponse());
            pstmt.executeUpdate();
            System.out.println("🗑️ Réponse supprimée: " + reponse.getIdReponse());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<Reponse> getAllByPost(int idPost) {
        ObservableList<Reponse> reponses = FXCollections.observableArrayList();
        String query = "SELECT * FROM reponses WHERE post_id = ? ORDER BY date_reponse ASC";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idPost);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Reponse reponse = new Reponse(
                        rs.getInt("id"),
                        rs.getInt("post_id"),
                        rs.getString("auteur"),
                        rs.getString("contenu"),
                        rs.getTimestamp("date_reponse").toLocalDateTime(),
                        rs.getInt("likes")
                );
                reponse.setDislikes(rs.getInt("dislikes"));
                reponses.add(reponse);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reponses;
    }

    public void incrementerLike(int reponseId) {
        String query = "UPDATE reponses SET likes = likes + 1 WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, reponseId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void incrementerDislike(int reponseId) {
        String query = "UPDATE reponses SET dislikes = dislikes + 1 WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, reponseId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int compterParPost(int idPost) {
        String query = "SELECT COUNT(*) FROM reponses WHERE post_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idPost);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
}