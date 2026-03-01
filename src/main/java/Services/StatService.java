package Services;

import utils.Database;

import java.sql.*;
import java.util.*;

/**
 * Stats with JOINs and aggregates across User, Ressource, Evaluation, Favori.
 */
public class StatService {

    /** Count per entity: "Users" -> count, "Ressources" -> count, etc. */
    public Map<String, Number> getEntityCounts() {
        Map<String, Number> map = new LinkedHashMap<>();
        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM `User`) AS users, " +
                "(SELECT COUNT(*) FROM Ressource) AS ressources, " +
                "(SELECT COUNT(*) FROM Evaluation) AS evaluations, " +
                "(SELECT COUNT(*) FROM Favori) AS favoris";
        try (Connection conn = Database.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                map.put("Utilisateurs", rs.getInt("users"));
                map.put("Ressources", rs.getInt("ressources"));
                map.put("Évaluations", rs.getInt("evaluations"));
                map.put("Favoris", rs.getInt("favoris"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("StatService getEntityCounts", e);
        }
        return map;
    }

    /** Ressources grouped by type. */
    public Map<String, Number> getRessourcesByType() {
        return groupByCount("Ressource", "type");
    }

    /** Ressources grouped by category. */
    public Map<String, Number> getRessourcesByCategory() {
        return groupByCount("Ressource", "category");
    }

    /** Evaluations grouped by note (1-5). */
    public Map<String, Number> getEvaluationsByNote() {
        Map<String, Number> map = new LinkedHashMap<>();
        String sql = "SELECT note, COUNT(*) AS cnt FROM Evaluation GROUP BY note ORDER BY note";
        try (Connection conn = Database.getInstance().getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            Set<Integer> seen = new HashSet<>();
            while (rs.next()) {
                int note = rs.getInt("note");
                seen.add(note);
                map.put("Note " + note, rs.getInt("cnt"));
            }
            for (int n = 1; n <= 5; n++)
                if (!seen.contains(n))
                    map.put("Note " + n, 0);
        } catch (SQLException e) {
            throw new RuntimeException("StatService getEvaluationsByNote", e);
        }
        return map;
    }

    /** Users grouped by role. */
    public Map<String, Number> getUsersByRole() {
        return groupByCount("`User`", "role");
    }

    /** Top N ressources by number of evaluations (JOIN Evaluation + Ressource). */
    public List<Map.Entry<String, Number>> getTopRessourcesByEvaluations(int top) {
        String sql = "SELECT r.id, r.title, COUNT(e.id) AS nb " +
                "FROM Ressource r LEFT JOIN Evaluation e ON r.id = e.ressourceId " +
                "GROUP BY r.id, r.title ORDER BY nb DESC LIMIT ?";
        List<Map.Entry<String, Number>> list = new ArrayList<>();
        try (Connection conn = Database.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, top);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String title = rs.getString("title");
                    if (title == null || title.length() > 30)
                        title = (title != null ? title.substring(0, Math.min(30, title.length())) + "…" : "Ressource");
                    list.add(new AbstractMap.SimpleEntry<>(title, rs.getInt("nb")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("StatService getTopRessourcesByEvaluations", e);
        }
        return list;
    }

    /** Evaluations per month (for line chart). */
    public Map<String, Number> getEvaluationsByMonth() {
        Map<String, Number> map = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(dateEvaluation, '%Y-%m') AS mois, COUNT(*) AS cnt " +
                "FROM Evaluation GROUP BY mois ORDER BY mois";
        try (Connection conn = Database.getInstance().getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                map.put(rs.getString("mois"), rs.getInt("cnt"));
        } catch (SQLException e) {
            throw new RuntimeException("StatService getEvaluationsByMonth", e);
        }
        return map;
    }

    /** Favoris per ressource (JOIN Favori + Ressource) - top N. */
    public List<Map.Entry<String, Number>> getTopRessourcesByFavoris(int top) {
        String sql = "SELECT r.id, r.title, COUNT(f.id) AS nb " +
                "FROM Ressource r LEFT JOIN Favori f ON r.id = f.ressourceId " +
                "GROUP BY r.id, r.title ORDER BY nb DESC LIMIT ?";
        List<Map.Entry<String, Number>> list = new ArrayList<>();
        try (Connection conn = Database.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, top);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String title = rs.getString("title");
                    if (title != null && title.length() > 28)
                        title = title.substring(0, 28) + "…";
                    if (title == null)
                        title = "Ressource";
                    list.add(new AbstractMap.SimpleEntry<>(title, rs.getInt("nb")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("StatService getTopRessourcesByFavoris", e);
        }
        return list;
    }

    /** Average evaluation per ressource (JOIN Evaluation + Ressource) - top N. */
    public List<Map.Entry<String, Number>> getAverageEvaluationPerRessource(int top) {
        String sql = "SELECT r.id, r.title, COALESCE(AVG(e.note), 0) AS moyenne " +
                "FROM Ressource r LEFT JOIN Evaluation e ON r.id = e.ressourceId " +
                "GROUP BY r.id, r.title ORDER BY moyenne DESC LIMIT ?";
        List<Map.Entry<String, Number>> list = new ArrayList<>();
        try (Connection conn = Database.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, top);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String title = rs.getString("title");
                    if (title != null && title.length() > 28)
                        title = title.substring(0, 28) + "…";
                    if (title == null)
                        title = "Ressource";
                    list.add(new AbstractMap.SimpleEntry<>(title, rs.getDouble("moyenne")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("StatService getAverageEvaluationPerRessource", e);
        }
        return list;
    }

    private Map<String, Number> groupByCount(String table, String column) {
        Map<String, Number> map = new LinkedHashMap<>();
        String sql = "SELECT " + column + ", COUNT(*) AS cnt FROM " + table + " GROUP BY " + column
                + " ORDER BY cnt DESC";
        try (Connection conn = Database.getInstance().getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String key = rs.getString(column);
                if (key == null)
                    key = "(vide)";
                map.put(key, rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("StatService groupByCount " + table + "." + column, e);
        }
        return map;
    }
}
