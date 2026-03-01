package Services;

import Interface.favoriInterface;
import Models.Favori;
import Models.Ressource;
import utils.StaticUser;
import utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class favoriService implements favoriInterface {

    private static final String TABLE = "Favori";

    @Override
    public Favori create(Favori favori) {
        int userId = favori.getUserId();
        if (userId <= 0 && StaticUser.isSet()) {
            favori.setUserId(StaticUser.getId());
        }
        if (favori.getUserId() <= 0) {
            throw new IllegalStateException("Favori requires a user ID. Set StaticUser or pass user ID on the favori.");
        }
        String sql = "INSERT INTO " + TABLE + " (userId, ressourceId, dateAjout) VALUES (?, ?, ?)";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, favori.getUserId());
            ps.setInt(2, favori.getRessource().getId());
            ps.setDate(3, favori.getDateAjout() != null ? Date.valueOf(favori.getDateAjout()) : null);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    favori.setId(rs.getInt(1));
            }
            return favori;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating favori", e);
        }
    }

    @Override
    public Optional<Favori> findById(int id) {
        String sql = "SELECT id, userId, ressourceId, dateAjout FROM " + TABLE + " WHERE id = ?";
        try (Connection conn = Database.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding favori by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Favori> findAll() {
        String sql = "SELECT id, userId, ressourceId, dateAjout FROM " + TABLE;
        List<Favori> list = new ArrayList<>();
        try (Connection conn = Database.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all favoris", e);
        }
        return list;
    }

    @Override
    public Favori update(Favori favori) {
        String sql = "UPDATE " + TABLE + " SET userId=?, ressourceId=?, dateAjout=? WHERE id=?";
        try (Connection conn = Database.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, favori.getUserId());
            ps.setInt(2, favori.getRessource().getId());
            ps.setDate(3, favori.getDateAjout() != null ? Date.valueOf(favori.getDateAjout()) : null);
            ps.setInt(4, favori.getId());
            ps.executeUpdate();
            return favori;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating favori", e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM " + TABLE + " WHERE id = ?";
        try (Connection conn = Database.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting favori", e);
        }
    }

    @Override
    public List<Favori> findByUserId(int userId) {
        return findByForeignKey("userId", userId);
    }

    @Override
    public List<Favori> findByRessourceId(int ressourceId) {
        return findByForeignKey("ressourceId", ressourceId);
    }

    @Override
    public Optional<Favori> findByUserIdAndRessourceId(int userId, int ressourceId) {
        String sql = "SELECT id, userId, ressourceId, dateAjout FROM " + TABLE
                + " WHERE userId = ? AND ressourceId = ?";
        try (Connection conn = Database.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, ressourceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding favori by user and ressource", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean deleteByUserIdAndRessourceId(int userId, int ressourceId) {
        String sql = "DELETE FROM " + TABLE + " WHERE userId = ? AND ressourceId = ?";
        try (Connection conn = Database.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, ressourceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting favori by user and ressource", e);
        }
    }

    private List<Favori> findByForeignKey(String column, int value) {
        String sql = "SELECT id, userId, ressourceId, dateAjout FROM " + TABLE + " WHERE " + column + " = ?";
        List<Favori> list = new ArrayList<>();
        try (Connection conn = Database.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding favoris by " + column, e);
        }
        return list;
    }

    private static Favori mapRow(ResultSet rs) throws SQLException {
        Favori f = new Favori();
        f.setId(rs.getInt("id"));
        f.setUserId(rs.getInt("userId"));
        Ressource r = new Ressource();
        r.setId(rs.getInt("ressourceId"));
        f.setRessource(r);
        Date d = rs.getDate("dateAjout");
        f.setDateAjout(d != null ? d.toLocalDate() : null);
        return f;
    }
}
