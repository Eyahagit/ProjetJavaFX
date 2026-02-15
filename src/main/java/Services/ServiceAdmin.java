package Services;

import Models.*;
import utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class ServiceAdmin implements Iservices<admin>{
    private Connection connection;
    public ServiceAdmin() {
        connection = Database.getInstance().getConnection();
    }
    @Override
    public void ajouter(admin admin) throws SQLDataException {
        String sqladmin = "INSERT INTO `admins` (id_user,actif) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sqladmin)) {
            stmt.setInt(1, admin.getId());
            stmt.setBoolean(2, admin.isActif());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(admin admin) throws SQLException {
        // 1. Supprimer de admins d'abord
        String sqlAdmin = "DELETE FROM admins WHERE id_user = ?";
        try (PreparedStatement stmtAdmin = connection.prepareStatement(sqlAdmin)) {
            stmtAdmin.setInt(1, admin.getId());
            stmtAdmin.executeUpdate();
            System.out.println("✅ Admin record deleted");
        }

        // 2. Supprimer de users ensuite
        String sqlUser = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement stmtUser = connection.prepareStatement(sqlUser)) {
            stmtUser.setInt(1, admin.getId());
            stmtUser.executeUpdate();
            System.out.println("✅ User record deleted");
        }
    }

    @Override
    public void modifier(admin admin) throws SQLException {
        System.out.println("\n========== MODIFICATION ADMIN ==========");
        System.out.println("ID: " + admin.getId());
        System.out.println("Nom: " + admin.getName());

        int rowsUser = 0;
        int rowsAdmin = 0;

        // Démarrer une transaction
        connection.setAutoCommit(false);

        try {
            // 1. Mettre à jour users
            String sqlUser = "UPDATE users SET name = ?, second_name = ?, email = ?, age = ?, gender = ?, phone_number = ? WHERE id = ?";
            try (PreparedStatement stmtUser = connection.prepareStatement(sqlUser)) {
                stmtUser.setString(1, admin.getName());
                stmtUser.setString(2, admin.getSecond_name());
                stmtUser.setString(3, admin.getEmail());
                stmtUser.setInt(4, admin.getAge());
                stmtUser.setString(5, admin.getGender());
                stmtUser.setInt(6, admin.getPhone_number());
                stmtUser.setInt(7, admin.getId());

                rowsUser = stmtUser.executeUpdate();
                System.out.println("✅ Users modifiés: " + rowsUser + " ligne(s)");
            }

            // 2. Mettre à jour admins
            String sqlAdmin = "UPDATE admins SET actif = ? WHERE id_user = ?";
            try (PreparedStatement stmtAdmin = connection.prepareStatement(sqlAdmin)) {
                stmtAdmin.setBoolean(1, admin.isActif());
                stmtAdmin.setInt(2, admin.getId());

                rowsAdmin = stmtAdmin.executeUpdate();
                System.out.println("✅ Admins modifiés: " + rowsAdmin + " ligne(s)");
            }

            // Valider la transaction
            connection.commit();
            System.out.println("✅ Transaction validée");

            if (rowsUser > 0 || rowsAdmin > 0) {
                System.out.println("✅ Admin mis à jour avec succès");
            } else {
                System.out.println("⚠️ Aucune modification détectée pour l'ID: " + admin.getId());
            }

        } catch (SQLException e) {
            // Annuler la transaction en cas d'erreur
            connection.rollback();
            System.err.println("❌ Erreur, transaction annulée: " + e.getMessage());
            throw e;
        } finally {
            // Restaurer l'autocommit
            connection.setAutoCommit(true);
        }
    }

    @Override
    public List<admin> recuperer() throws SQLException {
        List<admin> list = new ArrayList<>();
        String sql = "SELECT a.*, u.name, u.second_name, u.age, u.gender, u.phone_number, " +
                "u.birth_date, u.email, u.password, u.role " +
                "FROM admins a " +
                "JOIN users u ON a.id_user = u.id";

        System.out.println("🔍 ServiceAdmin.recuperer() executing...");

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                admin a = new admin();

                // ✅ Informations de users
                a.setId(rs.getInt("id_user"));
                a.setName(rs.getString("name"));
                a.setSecond_name(rs.getString("second_name"));
                a.setAge(rs.getInt("age"));
                a.setGender(rs.getString("gender"));
                a.setPhone_number(rs.getInt("phone_number"));
                a.setBirth_date(rs.getString("birth_date"));
                a.setEmail(rs.getString("email"));
                a.setPassword(rs.getString("password"));
                a.setRole(rs.getString("role"));

                // ✅ Informations spécifiques d'admin
                a.setActif(rs.getBoolean("actif"));

                list.add(a);
            }
            System.out.println("✅ Admins found: " + list.size());

        } catch (SQLException e) {
            System.err.println("❌ Error in ServiceAdmin.recuperer: " + e.getMessage());
            throw e;
        }
        return list;
    }
    public admin getById(int id) throws SQLException {
        String sql = "SELECT * FROM admins WHERE id_user = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                admin a = new admin();
                a.setId(rs.getInt("id_user"));
                a.setActif(rs.getBoolean("actif"));

                // Récupérer les infos users
                users user = new ServiceUser().getById(id);
                if (user != null) {
                    a.setName(user.getName());
                    a.setSecond_name(user.getSecond_name());
                    a.setEmail(user.getEmail());
                    a.setRole(user.getRole());
                }
                return a;
            }
        }
        return null;
    }
}
