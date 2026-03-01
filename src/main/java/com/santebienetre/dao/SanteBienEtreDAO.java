package com.santebienetre.dao;

import com.santebienetre.model.SanteBienEtre;
import com.santebienetre.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for SanteBienEtre entity.
 * Provides full CRUD operations using JDBC PreparedStatements.
 * 
 * @author Application Santé & Bien-être
 * @version 1.0
 */
public class SanteBienEtreDAO {

    private static final String INSERT_SQL = 
            "INSERT INTO sante_bien_etre (user_id, humeur, niveau_stress, qualite_sommeil, " +
            "nutrition, activite_physique, developpement_personnel, recommandations, date_suivi) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL = 
            "UPDATE sante_bien_etre SET user_id=?, humeur=?, niveau_stress=?, qualite_sommeil=?, " +
            "nutrition=?, activite_physique=?, developpement_personnel=?, recommandations=?, date_suivi=? " +
            "WHERE id=?";

    private static final String DELETE_SQL = "DELETE FROM sante_bien_etre WHERE id=?";

    private static final String SELECT_ALL_SQL = 
            "SELECT id, user_id, humeur, niveau_stress, qualite_sommeil, nutrition, " +
            "activite_physique, developpement_personnel, recommandations, date_suivi " +
            "FROM sante_bien_etre ORDER BY date_suivi DESC, id DESC";

    private static final String SELECT_BY_USER_SQL = 
            "SELECT id, user_id, humeur, niveau_stress, qualite_sommeil, nutrition, " +
            "activite_physique, developpement_personnel, recommandations, date_suivi " +
            "FROM sante_bien_etre WHERE user_id=? ORDER BY date_suivi DESC, id DESC";

    private static final String SELECT_BY_ID_SQL = 
            "SELECT id, user_id, humeur, niveau_stress, qualite_sommeil, nutrition, " +
            "activite_physique, developpement_personnel, recommandations, date_suivi " +
            "FROM sante_bien_etre WHERE id=?";

    private static final String SELECT_BY_USER_AND_DATE_SQL = 
            "SELECT id, user_id, humeur, niveau_stress, qualite_sommeil, nutrition, " +
            "activite_physique, developpement_personnel, recommandations, date_suivi " +
            "FROM sante_bien_etre WHERE user_id=? AND date_suivi=? ORDER BY id DESC";

    private static final String SELECT_BY_DATE_RANGE_SQL = 
            "SELECT id, user_id, humeur, niveau_stress, qualite_sommeil, nutrition, " +
            "activite_physique, developpement_personnel, recommandations, date_suivi " +
            "FROM sante_bien_etre WHERE user_id=? AND date_suivi BETWEEN ? AND ? ORDER BY date_suivi ASC";

    /**
     * Adds a new SanteBienEtre record.
     * @param s The entity to add
     * @return The generated id, or -1 on failure
     */
    public int addSanteBienEtre(SanteBienEtre s) {
        // Use a single connection so we can check/create user and insert record atomically
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Ensure the utilisateur exists for the provided user_id. If not, create a minimal user.
            try (PreparedStatement checkUser = conn.prepareStatement("SELECT id FROM utilisateur WHERE id=?")) {
                checkUser.setInt(1, s.getUserId());
                try (ResultSet rs = checkUser.executeQuery()) {
                    if (!rs.next()) {
                        try (PreparedStatement insertUser = conn.prepareStatement(
                                "INSERT IGNORE INTO utilisateur (id, nom, email) VALUES (?, ?, ?)")) {
                            insertUser.setInt(1, s.getUserId());
                            insertUser.setString(2, "Utilisateur " + s.getUserId());
                            insertUser.setString(3, "user" + s.getUserId() + "@local");
                            insertUser.executeUpdate();
                        }
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
                setStatementParams(ps, s, false);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            return rs.getInt(1);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Updates an existing SanteBienEtre record.
     * @param s The entity to update (must have valid id)
     * @return true if update succeeded, false otherwise
     */
    public boolean updateSanteBienEtre(SanteBienEtre s) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {

            setStatementParams(ps, s, false);
            ps.setInt(10, s.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a SanteBienEtre record by id.
     * @param id The record id
     * @return true if deletion succeeded, false otherwise
     */
    public boolean deleteSanteBienEtre(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves all distinct user IDs.
     * @return List of user IDs
     */
    public List<Integer> getAllUserIds() {
        List<Integer> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT DISTINCT user_id FROM sante_bien_etre ORDER BY user_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getInt("user_id"));
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] getAllUserIds failed: " + e.getMessage());
            e.printStackTrace();
        }
        System.err.println("[DEBUG] DAO getAllUserIds returning " + list.size() + " items.");
        return list;
    }

    /**
     * Retrieves all SanteBienEtre records.
     * @return List of all records
     */
    public List<SanteBienEtre> getAllSanteBienEtre() {
        List<SanteBienEtre> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] getAllSanteBienEtre failed: " + e.getMessage());
            e.printStackTrace();
        }
        System.err.println("[DEBUG] DAO getAllSanteBienEtre returning " + list.size() + " items.");
        return list;
    }

    /**
     * Retrieves SanteBienEtre records for a specific user.
     * @param userId The user id
     * @return List of records for the user
     */
    public List<SanteBienEtre> getSanteBienEtreByUser(int userId) {
        List<SanteBienEtre> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_USER_SQL)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Retrieves records by user and date range (for filtering).
     * @param userId User id
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of records in date range
     */
    public List<SanteBienEtre> getSanteBienEtreByDateRange(int userId, LocalDate startDate, LocalDate endDate) {
        List<SanteBienEtre> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_DATE_RANGE_SQL)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(startDate));
            ps.setDate(3, Date.valueOf(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Retrieves a single record by id.
     * @param id Record id
     * @return The record or null if not found
     */
    public SanteBienEtre getSanteBienEtreById(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setStatementParams(PreparedStatement ps, SanteBienEtre s, boolean includeId) throws SQLException {
        ps.setInt(1, s.getUserId());
        ps.setString(2, s.getHumeur());
        ps.setInt(3, s.getNiveauStress());
        ps.setInt(4, s.getQualiteSommeil());
        ps.setString(5, s.getNutrition());
        ps.setString(6, s.getActivitePhysique());
        ps.setString(7, s.getDeveloppementPersonnel());
        ps.setString(8, s.getRecommandations());
        ps.setDate(9, Date.valueOf(s.getDateSuivi() != null ? s.getDateSuivi() : LocalDate.now()));
    }

    private SanteBienEtre mapResultSet(ResultSet rs) throws SQLException {
        return new SanteBienEtre(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("humeur"),
                rs.getInt("niveau_stress"),
                rs.getInt("qualite_sommeil"),
                rs.getString("nutrition"),
                rs.getString("activite_physique"),
                rs.getString("developpement_personnel"),
                rs.getString("recommandations"),
                rs.getDate("date_suivi") != null ? rs.getDate("date_suivi").toLocalDate() : LocalDate.now()
        );
    }
}
