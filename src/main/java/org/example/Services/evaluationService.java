package org.example.Services;

import org.example.Interface.evaluationInterface;
import org.example.Models.Evaluation;
import org.example.Models.Ressource;
import org.example.Models.User;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class evaluationService implements evaluationInterface {

    private static final String TABLE = "Evaluation";

    @Override
    public Evaluation create(Evaluation evaluation) {
        User u = evaluation.getUser();
        if (u == null && org.example.utils.StaticUser.isSet()) {
            evaluation.setUser(org.example.utils.StaticUser.get());
        }
        if (evaluation.getUser() == null) {
            throw new IllegalStateException(
                    "Evaluation requires a user. Set StaticUser or pass user on the evaluation.");
        }
        String sql = "INSERT INTO " + TABLE
                + " (userId, ressourceId, note, commentaire, dateEvaluation) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = MyDatabase.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, evaluation.getUser().getId());
            ps.setInt(2, evaluation.getRessource().getId());
            ps.setInt(3, evaluation.getNote());
            ps.setString(4, evaluation.getCommentaire());
            ps.setDate(5, evaluation.getDateEvaluation() != null ? Date.valueOf(evaluation.getDateEvaluation()) : null);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    evaluation.setId(rs.getInt(1));
            }
            return evaluation;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating evaluation", e);
        }
    }

    @Override
    public Optional<Evaluation> findById(int id) {
        String sql = "SELECT id, userId, ressourceId, note, commentaire, dateEvaluation FROM " + TABLE
                + " WHERE id = ?";
        try (Connection conn = MyDatabase.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding evaluation by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Evaluation> findAll() {   //create
        String sql = "SELECT id, userId, ressourceId, note, commentaire, dateEvaluation FROM " + TABLE;
        List<Evaluation> list = new ArrayList<>();
        try (Connection conn = MyDatabase.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all evaluations", e);
        }
        return list;
    }

    @Override
    public Evaluation update(Evaluation evaluation) {
        String sql = "UPDATE " + TABLE
                + " SET userId=?, ressourceId=?, note=?, commentaire=?, dateEvaluation=? WHERE id=?";
        try (Connection conn = MyDatabase.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, evaluation.getUser().getId());
            ps.setInt(2, evaluation.getRessource().getId());
            ps.setInt(3, evaluation.getNote());
            ps.setString(4, evaluation.getCommentaire());
            ps.setDate(5, evaluation.getDateEvaluation() != null ? Date.valueOf(evaluation.getDateEvaluation()) : null);
            ps.setInt(6, evaluation.getId());
            ps.executeUpdate();
            return evaluation;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating evaluation", e);
        }
    }

    @Override
    public boolean delete(int id) {   //            Quand je clique sur supprimer, le Controller récupère l’id, puis appelle service.delete(i
        String sql = "DELETE FROM " + TABLE + " WHERE id = ?";
        try (Connection conn = MyDatabase.getInstance().getConnection();   //MyDatabase.getInstance().getConnection() pour obtenir une connexion JDBC, ensuite j’exécute une requête SQL DELETE avec PreparedStatement.
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;    //renvoie le nombre de lignes supprimées, donc je sais si la suppression a réussi.
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting evaluation", e);
        }
    }

    @Override
    public List<Evaluation> findByUserId(int userId) {
        return findByForeignKey("userId", userId);
    }

    @Override
    public List<Evaluation> findByRessourceId(int ressourceId) {
        return findByForeignKey("ressourceId", ressourceId);
    }

    private List<Evaluation> findByForeignKey(String column, int value) {
        String sql = "SELECT id, userId, ressourceId, note, commentaire, dateEvaluation FROM " + TABLE + " WHERE "
                + column + " = ?";
        List<Evaluation> list = new ArrayList<>();
        try (Connection conn = MyDatabase.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding evaluations by " + column, e);
        }
        return list;
    }

    private static Evaluation mapRow(ResultSet rs) throws SQLException {
        Evaluation e = new Evaluation();
        e.setId(rs.getInt("id"));

        // Load full User object
        int userId = rs.getInt("userId");
        userService userSvc = new userService();
        User u = userSvc.findById(userId).orElse(new User());
        if (u.getId() == 0) {
            u.setId(userId);
            u.setNom("Utilisateur");
            u.setPrenom("Inconnu");
        }
        e.setUser(u);

        // Load full Ressource object
        int ressourceId = rs.getInt("ressourceId");
        ressourceService ressourceSvc = new ressourceService();
        Ressource r = ressourceSvc.findById(ressourceId).orElse(new Ressource());
        if (r.getId() == 0) {
            r.setId(ressourceId);
        }
        e.setRessource(r);

        e.setNote(rs.getInt("note"));
        e.setCommentaire(rs.getString("commentaire"));
        Date d = rs.getDate("dateEvaluation");
        e.setDateEvaluation(d != null ? d.toLocalDate() : null);
        return e;
    }
}
