package dao;

import Models.SanteBienEtre;
import utils.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for SanteBienEtre entity.
 * Provides full CRUD operations using JDBC PreparedStatements.
 *
 * @author Application Santé & Bien-être
 * @version 2.0
 */
public class SanteBienEtreDAO {

    // Constantes SQL
    private static final String TABLE_NAME = "sante_bien_etre";

    private static final String INSERT_SQL =
            "INSERT INTO " + TABLE_NAME + " (user_id, humeur, niveau_stress, qualite_sommeil, " +
                    "nutrition, activite_physique, developpement_personnel, recommandations, date_suivi) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE " + TABLE_NAME + " SET user_id=?, humeur=?, niveau_stress=?, qualite_sommeil=?, " +
                    "nutrition=?, activite_physique=?, developpement_personnel=?, recommandations=?, date_suivi=? " +
                    "WHERE id=?";

    private static final String DELETE_SQL = "DELETE FROM " + TABLE_NAME + " WHERE id=?";

    private static final String SELECT_ALL_SQL =
            "SELECT id, user_id, humeur, niveau_stress, qualite_sommeil, nutrition, " +
                    "activite_physique, developpement_personnel, recommandations, date_suivi " +
                    "FROM " + TABLE_NAME + " ORDER BY date_suivi DESC, id DESC";

    private static final String SELECT_BY_USER_SQL =
            "SELECT id, user_id, humeur, niveau_stress, qualite_sommeil, nutrition, " +
                    "activite_physique, developpement_personnel, recommandations, date_suivi " +
                    "FROM " + TABLE_NAME + " WHERE user_id=? ORDER BY date_suivi DESC, id DESC";

    private static final String SELECT_BY_ID_SQL =
            "SELECT id, user_id, humeur, niveau_stress, qualite_sommeil, nutrition, " +
                    "activite_physique, developpement_personnel, recommandations, date_suivi " +
                    "FROM " + TABLE_NAME + " WHERE id=?";

    private static final String SELECT_BY_DATE_RANGE_SQL =
            "SELECT id, user_id, humeur, niveau_stress, qualite_sommeil, nutrition, " +
                    "activite_physique, developpement_personnel, recommandations, date_suivi " +
                    "FROM " + TABLE_NAME + " WHERE user_id=? AND date_suivi BETWEEN ? AND ? ORDER BY date_suivi ASC";

    private static final String SELECT_DISTINCT_USERS_SQL =
            "SELECT DISTINCT user_id FROM " + TABLE_NAME + " ORDER BY user_id";

    private static final String CHECK_USER_SQL = "SELECT id FROM utilisateur WHERE id=?";

    private static final String INSERT_USER_SQL =
            "INSERT IGNORE INTO utilisateur (id, nom, email) VALUES (?, ?, ?)";

    /**
     * Adds a new SanteBienEtre record.
     * @param s The entity to add
     * @return The generated id, or -1 on failure
     */
    public int addSanteBienEtre(SanteBienEtre s) {
        if (s == null) {
            System.err.println("[ERROR] Cannot add null entity");
            return -1;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            // Obtenir la connexion
            conn = Database.getInstance().getConnection();

            // ⭐ Vérifier si la connexion est valide
            if (conn == null || conn.isClosed()) {
                System.err.println("[ERROR] Connection is null or closed");
                return -1;
            }

            // Créer l'utilisateur si nécessaire
            ensureUserExists(conn, s.getUserId());

            // Préparer et exécuter l'insertion
            ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            setStatementParams(ps, s);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    System.out.println("[INFO] Enregistrement ajouté avec succès, ID: " + generatedId);
                    return generatedId;
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] addSanteBienEtre failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // ⭐ Fermer uniquement les ressources, PAS la connexion !
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            // ⭐ NE PAS fermer conn - elle sera réutilisée
        }
        return -1;
    }

    /**
     * Ensures a user exists in the database, creates a minimal one if not.
     */
    private void ensureUserExists(Connection conn, int userId) throws SQLException {
        PreparedStatement checkUser = null;
        ResultSet rs = null;
        PreparedStatement insertUser = null;

        try {
            checkUser = conn.prepareStatement(CHECK_USER_SQL);
            checkUser.setInt(1, userId);
            rs = checkUser.executeQuery();

            if (!rs.next()) {
                insertUser = conn.prepareStatement(INSERT_USER_SQL);
                insertUser.setInt(1, userId);
                insertUser.setString(2, "Utilisateur " + userId);
                insertUser.setString(3, "user" + userId + "@local");
                insertUser.executeUpdate();
                System.out.println("[INFO] Utilisateur " + userId + " créé automatiquement");
            }
        } finally {
            // Fermer uniquement les statements, PAS la connexion
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (checkUser != null) checkUser.close(); } catch (SQLException e) {}
            try { if (insertUser != null) insertUser.close(); } catch (SQLException e) {}
        }
    }

    /**
     * Updates an existing SanteBienEtre record.
     * @param s The entity to update (must have valid id)
     * @return true if update succeeded, false otherwise
     */
    public boolean updateSanteBienEtre(SanteBienEtre s) {
        if (s == null || s.getId() <= 0) {
            System.err.println("[ERROR] Invalid entity for update: " + s);
            return false;
        }

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = Database.getInstance().getConnection();
            if (conn == null || conn.isClosed()) return false;

            ps = conn.prepareStatement(UPDATE_SQL);
            setStatementParams(ps, s);
            ps.setInt(10, s.getId());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("[INFO] Enregistrement " + s.getId() + " mis à jour");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] updateSanteBienEtre failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
        }
        return false;
    }

    /**
     * Deletes a SanteBienEtre record by id.
     * @param id The record id
     * @return true if deletion succeeded, false otherwise
     */
    public boolean deleteSanteBienEtre(int id) {
        if (id <= 0) {
            System.err.println("[ERROR] Invalid id for deletion: " + id);
            return false;
        }

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = Database.getInstance().getConnection();
            if (conn == null || conn.isClosed()) return false;

            ps = conn.prepareStatement(DELETE_SQL);
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("[INFO] Enregistrement " + id + " supprimé");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] deleteSanteBienEtre failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
        }
        return false;
    }

    /**
     * Retrieves all distinct user IDs.
     * @return List of user IDs
     */
    public List<Integer> getAllUserIds() {
        List<Integer> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = Database.getInstance().getConnection();
            if (conn == null || conn.isClosed()) return list;

            ps = conn.prepareStatement(SELECT_DISTINCT_USERS_SQL);
            rs = ps.executeQuery();

            while (rs.next()) {
                list.add(rs.getInt("user_id"));
            }
            System.out.println("[DEBUG] " + list.size() + " user IDs récupérés");
        } catch (SQLException e) {
            System.err.println("[ERROR] getAllUserIds failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
        }
        return list;
    }

    /**
     * Retrieves all SanteBienEtre records.
     * @return List of all records
     */
    public List<SanteBienEtre> getAllSanteBienEtre() {
        List<SanteBienEtre> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = Database.getInstance().getConnection();
            if (conn == null || conn.isClosed()) return list;

            ps = conn.prepareStatement(SELECT_ALL_SQL);
            rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
            System.out.println("[DEBUG] " + list.size() + " enregistrements récupérés");
        } catch (SQLException e) {
            System.err.println("[ERROR] getAllSanteBienEtre failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
        }
        return list;
    }

    /**
     * Retrieves SanteBienEtre records for a specific user.
     * @param userId The user id
     * @return List of records for the user
     */
    public List<SanteBienEtre> getSanteBienEtreByUser(int userId) {
        List<SanteBienEtre> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = Database.getInstance().getConnection();
            if (conn == null || conn.isClosed()) return list;

            ps = conn.prepareStatement(SELECT_BY_USER_SQL);
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
            System.out.println("[DEBUG] " + list.size() + " enregistrements pour user " + userId);
        } catch (SQLException e) {
            System.err.println("[ERROR] getSanteBienEtreByUser failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
        }
        return list;
    }

    /**
     * Retrieves a single record by id.
     * @param id Record id
     * @return The record or null if not found
     */
    public SanteBienEtre getSanteBienEtreById(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = Database.getInstance().getConnection();
            if (conn == null || conn.isClosed()) return null;

            ps = conn.prepareStatement(SELECT_BY_ID_SQL);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] getSanteBienEtreById failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
        }
        return null;
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

        if (startDate == null || endDate == null) {
            System.err.println("[ERROR] startDate or endDate is null");
            return list;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = Database.getInstance().getConnection();
            if (conn == null || conn.isClosed()) return list;

            ps = conn.prepareStatement(SELECT_BY_DATE_RANGE_SQL);
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(startDate));
            ps.setDate(3, Date.valueOf(endDate));
            rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
            System.out.println("[DEBUG] " + list.size() + " enregistrements pour user " + userId +
                    " entre " + startDate + " et " + endDate);
        } catch (SQLException e) {
            System.err.println("[ERROR] getSanteBienEtreByDateRange failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
        }
        return list;
    }

    /**
     * Sets prepared statement parameters from a SanteBienEtre object.
     */
    private void setStatementParams(PreparedStatement ps, SanteBienEtre s) throws SQLException {
        ps.setInt(1, s.getUserId());
        ps.setString(2, s.getHumeur());
        ps.setInt(3, s.getNiveauStress());
        ps.setInt(4, s.getQualiteSommeil());
        ps.setString(5, s.getNutrition());
        ps.setString(6, s.getActivitePhysique());
        ps.setString(7, s.getDeveloppementPersonnel());
        ps.setString(8, s.getRecommandations());

        LocalDate date = s.getDateSuivi();
        ps.setDate(9, Date.valueOf(date != null ? date : LocalDate.now()));
    }

    /**
     * Maps a ResultSet row to a SanteBienEtre object.
     */
    private SanteBienEtre mapResultSet(ResultSet rs) throws SQLException {
        SanteBienEtre sante = new SanteBienEtre();

        sante.setId(rs.getInt("id"));
        sante.setUserId(rs.getInt("user_id"));
        sante.setHumeur(rs.getString("humeur"));
        sante.setNiveauStress(rs.getInt("niveau_stress"));
        sante.setQualiteSommeil(rs.getInt("qualite_sommeil"));
        sante.setNutrition(rs.getString("nutrition"));
        sante.setActivitePhysique(rs.getString("activite_physique"));
        sante.setDeveloppementPersonnel(rs.getString("developpement_personnel"));

        Date sqlDate = rs.getDate("date_suivi");
        sante.setDateSuivi(sqlDate != null ? sqlDate.toLocalDate() : LocalDate.now());

        sante.setRecommandations(rs.getString("recommandations"));

        return sante;
    }
}