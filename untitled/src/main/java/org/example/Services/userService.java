package org.example.Services;

import org.example.Interface.userInterface;
import org.example.Models.User;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class userService implements userInterface {

    private static final String TABLE = "User";

    @Override
    public User create(User user) {
        String sql = "INSERT INTO " + TABLE + " (nom, prenom, email, password, role, dateInscription, actif) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = MyDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getRole());
            ps.setDate(6, user.getDateInscription() != null ? Date.valueOf(user.getDateInscription()) : null);
            ps.setBoolean(7, user.isActif());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) user.setId(rs.getInt(1));
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating user", e);
        }
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT id, nom, prenom, email, password, role, dateInscription, actif FROM " + TABLE + " WHERE id = ?";
        try (Connection conn = MyDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT id, nom, prenom, email, password, role, dateInscription, actif FROM " + TABLE;
        List<User> list = new ArrayList<>();
        try (Connection conn = MyDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all users", e);
        }
        return list;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE " + TABLE + " SET nom=?, prenom=?, email=?, password=?, role=?, dateInscription=?, actif=? WHERE id=?";
        try (Connection conn = MyDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getRole());
            ps.setDate(6, user.getDateInscription() != null ? Date.valueOf(user.getDateInscription()) : null);
            ps.setBoolean(7, user.isActif());
            ps.setInt(8, user.getId());
            ps.executeUpdate();
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user", e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM " + TABLE + " WHERE id = ?";
        try (Connection conn = MyDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, nom, prenom, email, password, role, dateInscription, actif FROM " + TABLE + " WHERE email = ?";
        try (Connection conn = MyDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by email", e);
        }
        return Optional.empty();
    }

    private static User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setRole(rs.getString("role"));
        Date d = rs.getDate("dateInscription");
        u.setDateInscription(d != null ? d.toLocalDate() : null);
        u.setActif(rs.getBoolean("actif"));
        return u;
    }
}
