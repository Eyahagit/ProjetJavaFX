package Services;

import entities.ForumPost;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDateTime;
import utils.Database;

public class ServiceForumPost {

    private Connection connection;

    public ServiceForumPost() {
        this.connection = Database.getInstance().getConnection();
    }

    public void ajouter(ForumPost post) {
        String query = "INSERT INTO forum_posts (nom, role, categorie, contenu, date_creation, archive, likes, dislikes, vues) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, post.getNom());
            pstmt.setString(2, post.getRole());
            pstmt.setString(3, post.getCategorie());
            pstmt.setString(4, post.getContenu());
            pstmt.setTimestamp(5, Timestamp.valueOf(post.getDateCreation()));
            pstmt.setBoolean(6, post.isArchive());
            pstmt.setInt(7, post.getLikes());
            pstmt.setInt(8, post.getDislikes());
            pstmt.setInt(9, post.getVues());

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                post.setIdPost(rs.getInt(1));
            }

            System.out.println("✅ Post ajouté: " + post.getIdPost());

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout du post");
            e.printStackTrace();
        }
    }

    public void supprimer(ForumPost post) {
        String query = "DELETE FROM forum_posts WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, post.getIdPost());
            pstmt.executeUpdate();
            System.out.println("🗑️ Post supprimé: " + post.getIdPost());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void modifier(ForumPost post) {
        String query = "UPDATE forum_posts SET nom = ?, role = ?, categorie = ?, contenu = ?, archive = ?, likes = ?, dislikes = ?, vues = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, post.getNom());
            pstmt.setString(2, post.getRole());
            pstmt.setString(3, post.getCategorie());
            pstmt.setString(4, post.getContenu());
            pstmt.setBoolean(5, post.isArchive());
            pstmt.setInt(6, post.getLikes());
            pstmt.setInt(7, post.getDislikes());
            pstmt.setInt(8, post.getVues());
            pstmt.setInt(9, post.getIdPost());

            pstmt.executeUpdate();
            System.out.println("✏️ Post modifié: " + post.getIdPost());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<ForumPost> recuperer() {
        ObservableList<ForumPost> posts = FXCollections.observableArrayList();
        String query = "SELECT * FROM forum_posts ORDER BY date_creation DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                ForumPost post = new ForumPost(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("role"),
                        rs.getString("categorie"),
                        rs.getString("contenu"),
                        rs.getTimestamp("date_creation").toLocalDateTime(),
                        rs.getBoolean("archive"),
                        rs.getInt("likes"),
                        rs.getInt("vues")
                );
                post.setDislikes(rs.getInt("dislikes"));
                posts.add(post);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    public ObservableList<ForumPost> recupererTrieParPopularite() {
        ObservableList<ForumPost> posts = FXCollections.observableArrayList();
        String query = "SELECT * FROM forum_posts ORDER BY (likes - dislikes) DESC, date_creation DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                ForumPost post = new ForumPost(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("role"),
                        rs.getString("categorie"),
                        rs.getString("contenu"),
                        rs.getTimestamp("date_creation").toLocalDateTime(),
                        rs.getBoolean("archive"),
                        rs.getInt("likes"),
                        rs.getInt("vues")
                );
                post.setDislikes(rs.getInt("dislikes"));
                posts.add(post);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    public void incrementerLike(int postId) {
        String query = "UPDATE forum_posts SET likes = likes + 1 WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, postId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void incrementerDislike(int postId) {
        String query = "UPDATE forum_posts SET dislikes = dislikes + 1 WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, postId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void incrementerVue(int postId) {
        String query = "UPDATE forum_posts SET vues = vues + 1 WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, postId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public int compter() {
        String query = "SELECT COUNT(*) FROM forum_posts";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}