package com.santebienetre.dao;

import com.santebienetre.model.SleepTracking;
import com.santebienetre.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SleepTrackingDAO {

    private static final String INSERT_SQL =
            "INSERT INTO sleep_tracking (user_id, date_sommeil, heure_coucher, heure_reveil, duree_minutes, qualite_sommeil, commentaire) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE sleep_tracking SET user_id=?, date_sommeil=?, heure_coucher=?, heure_reveil=?, duree_minutes=?, qualite_sommeil=?, commentaire=? " +
            "WHERE id=?";

    private static final String DELETE_SQL = "DELETE FROM sleep_tracking WHERE id=?";

    private static final String SELECT_ALL_SQL =
            "SELECT id, user_id, date_sommeil, heure_coucher, heure_reveil, duree_minutes, qualite_sommeil, commentaire " +
            "FROM sleep_tracking ORDER BY date_sommeil DESC, id DESC";

    private static final String SELECT_BY_USER_SQL =
            "SELECT id, user_id, date_sommeil, heure_coucher, heure_reveil, duree_minutes, qualite_sommeil, commentaire " +
            "FROM sleep_tracking WHERE user_id=? ORDER BY date_sommeil DESC, id DESC";

    private static final String SELECT_BY_DATE_RANGE_SQL =
            "SELECT id, user_id, date_sommeil, heure_coucher, heure_reveil, duree_minutes, qualite_sommeil, commentaire " +
            "FROM sleep_tracking WHERE user_id=? AND date_sommeil BETWEEN ? AND ? ORDER BY date_sommeil ASC";

    public int addSleepTracking(SleepTracking s) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement insertUser = conn.prepareStatement(
                    "INSERT IGNORE INTO utilisateur (id, nom, email) VALUES (?, ?, ?)")) {
                insertUser.setInt(1, s.getUserId());
                insertUser.setString(2, "Utilisateur " + s.getUserId());
                insertUser.setString(3, "user" + s.getUserId() + "@local");
                insertUser.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
                setParams(ps, s);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean updateSleepTracking(SleepTracking s) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            setParams(ps, s);
            ps.setInt(8, s.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteSleepTracking(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<SleepTracking> getAll() {
        List<SleepTracking> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<SleepTracking> getByUser(int userId) {
        List<SleepTracking> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_USER_SQL)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<SleepTracking> getByDateRange(int userId, LocalDate start, LocalDate end) {
        List<SleepTracking> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_DATE_RANGE_SQL)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void setParams(PreparedStatement ps, SleepTracking s) throws SQLException {
        ps.setInt(1, s.getUserId());
        ps.setDate(2, Date.valueOf(s.getDateSommeil()));
        ps.setTime(3, Time.valueOf(s.getHeureCoucher()));
        ps.setTime(4, Time.valueOf(s.getHeureReveil()));
        ps.setInt(5, s.getDureeMinutes());
        ps.setInt(6, s.getQualiteSommeil());
        ps.setString(7, s.getCommentaire());
    }

    private SleepTracking map(ResultSet rs) throws SQLException {
        LocalDate date = rs.getDate("date_sommeil").toLocalDate();
        Time coucher = rs.getTime("heure_coucher");
        Time reveil = rs.getTime("heure_reveil");
        LocalTime hCoucher = coucher != null ? coucher.toLocalTime() : null;
        LocalTime hReveil = reveil != null ? reveil.toLocalTime() : null;

        return new SleepTracking(
                rs.getInt("id"),
                rs.getInt("user_id"),
                date,
                hCoucher,
                hReveil,
                rs.getInt("duree_minutes"),
                rs.getInt("qualite_sommeil"),
                rs.getString("commentaire")
        );
    }
}
