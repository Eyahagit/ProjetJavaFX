package org.example.Services;

import org.example.Interface.ressourseInterface;
import org.example.Models.Ressource;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ressourceService implements ressourseInterface {

    private static final String TABLE = "Ressource";

    @Override
    public Ressource create(Ressource ressource) {
        String sql = "INSERT INTO " + TABLE + " (title, description, type, category, content, author, dateCreation, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = MyDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setRessourceParams(ps, ressource);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) ressource.setId(rs.getInt(1));
            }
            return ressource;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating ressource", e);
        }
    }

    @Override
    public Optional<Ressource> findById(int id) {
        String sql = "SELECT id, title, description, type, category, content, author, dateCreation, status FROM " + TABLE + " WHERE id = ?";
        try (Connection conn = MyDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding ressource by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Ressource> findAll() {
        String sql = "SELECT id, title, description, type, category, content, author, dateCreation, status FROM " + TABLE;
        List<Ressource> list = new ArrayList<>();
        try (Connection conn = MyDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all ressources", e);
        }
        return list;
    }

    @Override
    public Ressource update(Ressource ressource) {
        String sql = "UPDATE " + TABLE + " SET title=?, description=?, type=?, category=?, content=?, author=?, dateCreation=?, status=? WHERE id=?";
        try (Connection conn = MyDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setRessourceParams(ps, ressource);
            ps.setInt(9, ressource.getId());
            ps.executeUpdate();
            return ressource;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating ressource", e);
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
            throw new RuntimeException("Error deleting ressource", e);
        }
    }

    @Override
    public List<Ressource> findByType(String type) {
        return findByColumn("type", type);
    }

    @Override
    public List<Ressource> findByCategory(String category) {
        return findByColumn("category", category);
    }

    @Override
    public List<Ressource> findByStatus(String status) {
        return findByColumn("status", status);
    }

    private List<Ressource> findByColumn(String column, String value) {
        String sql = "SELECT id, title, description, type, category, content, author, dateCreation, status FROM " + TABLE + " WHERE " + column + " = ?";
        List<Ressource> list = new ArrayList<>();
        try (Connection conn = MyDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding ressources by " + column, e);
        }
        return list;
    }

    private static void setRessourceParams(PreparedStatement ps, Ressource r) throws SQLException {
        ps.setString(1, r.getTitle());
        ps.setString(2, r.getDescription());
        ps.setString(3, r.getType());
        ps.setString(4, r.getCategory());
        ps.setString(5, r.getContent());
        ps.setString(6, r.getAuthor());
        ps.setDate(7, r.getDateCreation() != null ? Date.valueOf(r.getDateCreation()) : null);
        ps.setString(8, r.getStatus());
    }

    private static Ressource mapRow(ResultSet rs) throws SQLException {
        Ressource r = new Ressource();
        r.setId(rs.getInt("id"));
        r.setTitle(rs.getString("title"));
        r.setDescription(rs.getString("description"));
        r.setType(rs.getString("type"));
        r.setCategory(rs.getString("category"));
        r.setContent(rs.getString("content"));
        r.setAuthor(rs.getString("author"));
        Date d = rs.getDate("dateCreation");
        r.setDateCreation(d != null ? d.toLocalDate() : null);
        r.setStatus(rs.getString("status"));
        return r;
    }
}
