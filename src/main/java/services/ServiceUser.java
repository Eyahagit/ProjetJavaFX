package services;

import entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class ServiceUser {

    private Connection connection;

    public ServiceUser() {
        this.connection = DatabaseConnection.getConnection();
    }

    public User getById(int id) {
        String query = "SELECT * FROM users WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getTimestamp("date_inscription").toLocalDateTime(),
                        rs.getInt("points"),
                        rs.getString("badge")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void ajouterPoints(int userId, int points) {
        String query = "UPDATE users SET points = points + ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, points);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}