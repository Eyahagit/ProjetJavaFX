package Services;

import Models.doctor;
import utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDoctor implements Iservices<doctor> {
    private Connection connection;
    public ServiceDoctor() {
        connection = Database.getInstance().getConnection();
    }
    @Override
    public void ajouter(doctor d) throws SQLDataException {
        String sqlDoctor = "INSERT INTO `doctors` (id_user,specialty,experience,diplome,disponible,tarifConsultation,actif) VALUES (?, ?, ?, ? , ? , ? , ? )";

        try (PreparedStatement stmt = connection.prepareStatement(sqlDoctor)) {
            stmt.setInt(1, d.getId());
            stmt.setString(2, d.getSpecialty());
            stmt.setInt(3, d.getExperience());
            stmt.setString(4,d.getDiplome());
            stmt.setBoolean(5, d.isDisponible());
            stmt.setDouble(6, d.getTarifConsultation());
            stmt.setBoolean(7, d.isActif());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(doctor doctor) throws SQLException {
        // 1. Supprimer de doctors
        String sqlDoctor = "DELETE FROM doctors WHERE id_user = ?";
        try (PreparedStatement stmtDoctor = connection.prepareStatement(sqlDoctor)) {
            stmtDoctor.setInt(1, doctor.getId());
            int doctorRows = stmtDoctor.executeUpdate();
            System.out.println("✅ Doctor deleted from doctors table: " + doctorRows);
        }

        // 2. Supprimer de users
        String sqlUser = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement stmtUser = connection.prepareStatement(sqlUser)) {
            stmtUser.setInt(1, doctor.getId());
            int userRows = stmtUser.executeUpdate();
            System.out.println("✅ Doctor deleted from users table: " + userRows);
        }
    }

    @Override
    public void modifier(doctor doctor) throws SQLException {
        System.out.println("\n========== MODIFICATION DOCTOR ==========");
        System.out.println("ID: " + doctor.getId());
        System.out.println("Nom: " + doctor.getName());           // ← Devrait être "Hayder"
        System.out.println("Prénom: " + doctor.getSecond_name());
        System.out.println("Spécialité: " + doctor.getSpecialty());

        int rowsUser = 0;
        int rowsDoctor = 0;

        // 1. Mettre à jour users
        String sqlUser = "UPDATE users SET name = ?, second_name = ?, email = ?, age = ?, gender = ?, phone_number = ? WHERE id = ?";
        try (PreparedStatement stmtUser = connection.prepareStatement(sqlUser)) {
            stmtUser.setString(1, doctor.getName());
            stmtUser.setString(2, doctor.getSecond_name());
            stmtUser.setString(3, doctor.getEmail());
            stmtUser.setInt(4, doctor.getAge());
            stmtUser.setString(5, doctor.getGender());
            stmtUser.setInt(6, doctor.getPhone_number());
            stmtUser.setInt(7, doctor.getId());

            rowsUser = stmtUser.executeUpdate();
            System.out.println("✅ Users modifiés: " + rowsUser + " ligne(s)");
        }

        // 2. Mettre à jour doctors
        String sqlDoctor = "UPDATE doctors SET specialty = ?, experience = ?, diplome = ?, disponible = ?, tarifConsultation = ?, actif = ? WHERE id_user = ?";
        try (PreparedStatement stmtDoctor = connection.prepareStatement(sqlDoctor)) {
            stmtDoctor.setString(1, doctor.getSpecialty());
            stmtDoctor.setInt(2, doctor.getExperience());
            stmtDoctor.setString(3, doctor.getDiplome());
            stmtDoctor.setBoolean(4, doctor.isDisponible());
            stmtDoctor.setDouble(5, doctor.getTarifConsultation());
            stmtDoctor.setBoolean(6, doctor.isActif());
            stmtDoctor.setInt(7, doctor.getId());

            rowsDoctor = stmtDoctor.executeUpdate();
            System.out.println("✅ Doctors modifiés: " + rowsDoctor + " ligne(s)");
        }

        if (rowsUser > 0 || rowsDoctor > 0) {
            System.out.println("✅ Doctor mis à jour avec succès");
        } else {
            System.out.println("❌ Aucune mise à jour");
        }
    }

    @Override
    public List<doctor> recuperer() throws SQLException {
        List<doctor> list = new ArrayList<>();
        String sql = "SELECT d.*, u.name, u.second_name, u.age, u.gender, u.phone_number, " +
                "u.birth_date, u.email, u.password, u.role " +
                "FROM doctors d " +
                "JOIN users u ON d.id_user = u.id";

        System.out.println("🔍 ServiceDoctor.recuperer() executing...");

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                doctor d = new doctor();

                // ✅ Informations de users
                d.setId(rs.getInt("id_user"));
                d.setName(rs.getString("name"));
                d.setSecond_name(rs.getString("second_name"));
                d.setAge(rs.getInt("age"));
                d.setGender(rs.getString("gender"));
                d.setPhone_number(rs.getInt("phone_number"));
                d.setBirth_date(rs.getString("birth_date"));
                d.setEmail(rs.getString("email"));
                d.setPassword(rs.getString("password"));
                d.setRole(rs.getString("role"));

                // ✅ Informations spécifiques de doctors
                d.setSpecialty(rs.getString("specialty"));
                d.setExperience(rs.getInt("experience"));
                d.setDiplome(rs.getString("diplome"));
                d.setDisponible(rs.getBoolean("disponible"));
                d.setTarifConsultation(rs.getDouble("tarifConsultation"));
                d.setActif(rs.getBoolean("actif"));

                list.add(d);
            }
            System.out.println("✅ Doctors found: " + list.size());

        } catch (SQLException e) {
            System.err.println("❌ Error in ServiceDoctor.recuperer: " + e.getMessage());
            throw e;
        }
        return list;
    }
    public doctor getById(int id) throws SQLException {
        System.out.println("\n🔍 ServiceDoctor.getById(" + id + ")");

        String sql = "SELECT d.*, u.name, u.second_name, u.email, u.age, u.gender, u.phone_number, u.birth_date " +
                "FROM doctors d " +
                "JOIN users u ON d.id_user = u.id " +
                "WHERE d.id_user = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                doctor d = new doctor();

                // ✅ TOUS les champs de users
                d.setId(rs.getInt("id_user"));
                d.setName(rs.getString("name"));
                d.setSecond_name(rs.getString("second_name"));
                d.setEmail(rs.getString("email"));
                d.setAge(rs.getInt("age"));
                d.setGender(rs.getString("gender"));
                d.setPhone_number(rs.getInt("phone_number"));
                d.setBirth_date(rs.getString("birth_date"));

                // ✅ TOUS les champs de doctors
                d.setSpecialty(rs.getString("specialty"));
                d.setExperience(rs.getInt("experience"));
                d.setDiplome(rs.getString("diplome"));
                d.setDisponible(rs.getBoolean("disponible"));
                d.setTarifConsultation(rs.getDouble("tarifConsultation"));
                d.setActif(rs.getBoolean("actif"));

                System.out.println("✅ Doctor chargé: " + d.getName() + " - " + d.getSpecialty());
                return d;
            } else {
                System.out.println("❌ Aucun doctor avec ID: " + id);
            }
        }
        return null;
    }
}
