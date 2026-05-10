package Services;

import Models.patient;
import utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePatient implements Iservices <patient> {
    private Connection connection;
    public ServicePatient() {
        connection = Database.getInstance().getConnection();
    }
    @Override
    public void ajouter(patient p) {

        String sqlPatient = "INSERT INTO `patients` (id_user, blood_type, weight, height) VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlPatient)) {
            preparedStatement.setInt(1, p.getId());
            preparedStatement.setString(2, p.getBlood_type());
            preparedStatement.setDouble(3, p.getWeight());
            preparedStatement.setDouble(4, p.getHeight());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(patient patient) throws SQLException {
        // 1. Supprimer de patients
        String sqlPatient = "DELETE FROM patients WHERE id_user = ?";
        try (PreparedStatement stmtPatient = connection.prepareStatement(sqlPatient)) {
            stmtPatient.setInt(1, patient.getId());
            int patientRows = stmtPatient.executeUpdate();
            System.out.println("✅ Patient deleted from patients table: " + patientRows);
        }

        // 2. Supprimer de users
        String sqlUser = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement stmtUser = connection.prepareStatement(sqlUser)) {
            stmtUser.setInt(1, patient.getId());
            int userRows = stmtUser.executeUpdate();
            System.out.println("✅ Patient deleted from users table: " + userRows);
        }
    }

    /*@Override
    public void modifier(patient patient) throws SQLException {
        // 1. Mettre à jour users
        String sqlUser = "UPDATE users SET name = ?, second_name = ?, email = ?, age = ?, gender = ?, phone_number = ? WHERE id = ?";
        try (PreparedStatement stmtUser = connection.prepareStatement(sqlUser)) {
            stmtUser.setString(1, patient.getName());
            stmtUser.setString(2, patient.getSecond_name());
            stmtUser.setString(3, patient.getEmail());
            stmtUser.setInt(4, patient.getAge());
            stmtUser.setString(5, patient.getGender());
            stmtUser.setInt(6, patient.getPhone_number());
            stmtUser.setInt(7, patient.getId());
            stmtUser.executeUpdate();
        }

        // 2. Mettre à jour patients
        String sqlPatient = "UPDATE patients SET blood_type = ?, weight = ?, height = ? WHERE id_user = ?";
        try (PreparedStatement stmtPatient = connection.prepareStatement(sqlPatient)) {
            stmtPatient.setString(1, patient.getBlood_type());
            stmtPatient.setDouble(2, patient.getWeight());
            stmtPatient.setDouble(3, patient.getHeight());
            stmtPatient.setInt(4, patient.getId());
            stmtPatient.executeUpdate();
        }

        System.out.println("✅ Patient updated: " + patient.getName());
    }*/
    @Override
    public void modifier(patient patient) throws SQLException {
        System.out.println("\n========== MODIFICATION PATIENT ==========");
        System.out.println("ID: " + patient.getId());
        System.out.println("Nom: " + patient.getName());           // ← Doit être "Hayder"
        System.out.println("Email: " + patient.getEmail());        // ← Doit être correct
        System.out.println("Groupe sanguin: " + patient.getBlood_type());

        int rowsUser = 0;
        int rowsPatient = 0;

        // 1. Mettre à jour users
        String sqlUser = "UPDATE users SET name = ?, second_name = ?, email = ?, age = ?, gender = ?, phone_number = ? WHERE id = ?";
        try (PreparedStatement stmtUser = connection.prepareStatement(sqlUser)) {
            stmtUser.setString(1, patient.getName());
            stmtUser.setString(2, patient.getSecond_name());
            stmtUser.setString(3, patient.getEmail());
            stmtUser.setInt(4, patient.getAge());
            stmtUser.setString(5, patient.getGender());
            stmtUser.setInt(6, patient.getPhone_number());
            stmtUser.setInt(7, patient.getId());

            rowsUser = stmtUser.executeUpdate();
            System.out.println("✅ Users modifiés: " + rowsUser + " ligne(s)");
        }

        // 2. Mettre à jour patients
        String sqlPatient = "UPDATE patients SET blood_type = ?, weight = ?, height = ? WHERE id_user = ?";
        try (PreparedStatement stmtPatient = connection.prepareStatement(sqlPatient)) {
            stmtPatient.setString(1, patient.getBlood_type());
            stmtPatient.setDouble(2, patient.getWeight());
            stmtPatient.setDouble(3, patient.getHeight());
            stmtPatient.setInt(4, patient.getId());

            rowsPatient = stmtPatient.executeUpdate();
            System.out.println("✅ Patients modifiés: " + rowsPatient + " ligne(s)");
        }

        if (rowsUser > 0 || rowsPatient > 0) {
            System.out.println("✅ Patient mis à jour avec succès");
        } else {
            System.out.println("❌ Aucune mise à jour");
        }
    }

    @Override
    public List<patient> recuperer() throws SQLException {
        List<patient> list = new ArrayList<>();
        String sql = "SELECT p.*, u.name, u.second_name, u.age, u.gender, u.phone_number, " +
                "u.birth_date, u.email, u.password, u.role " +
                "FROM patients p " +
                "JOIN users u ON p.id_user = u.id";

        System.out.println("🔍 ServicePatient.recuperer() executing...");

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                patient p = new patient();

                // ✅ Informations de users
                p.setId(rs.getInt("id_user"));
                p.setName(rs.getString("name"));
                p.setSecond_name(rs.getString("second_name"));
                p.setAge(rs.getInt("age"));
                p.setGender(rs.getString("gender"));
                p.setPhone_number(rs.getInt("phone_number"));
                p.setBirth_date(rs.getString("birth_date"));
                p.setEmail(rs.getString("email"));
                p.setPassword(rs.getString("password"));
                p.setRole(rs.getString("role"));

                // ✅ Informations spécifiques de patients
                p.setBlood_type(rs.getString("blood_type"));
                p.setWeight(rs.getDouble("weight"));
                p.setHeight(rs.getDouble("height"));

                list.add(p);

                // Debug: afficher le premier patient
                if (list.size() == 1) {
                    System.out.println("   Exemple: " + p.getName() + " " + p.getSecond_name() +
                            ", Age: " + p.getAge() + ", Blood: " + p.getBlood_type());
                }
            }
            System.out.println("✅ Patients found: " + list.size());

        } catch (SQLException e) {
            System.err.println("❌ Error in ServicePatient.recuperer: " + e.getMessage());
            throw e;
        }
        return list;
    }
    public patient getById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ? AND role = 'patient'";

        System.out.println("🔍 Getting patient with ID: " + id);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                patient p = new patient();

                // ✅ Récupérer TOUS les champs
                p.setId(rs.getInt("id_user"));
                p.setName(rs.getString("name"));
                p.setSecond_name(rs.getString("second_name"));
                p.setEmail(rs.getString("email"));
                p.setAge(rs.getInt("age"));                 // ← AJOUTÉ
                p.setGender(rs.getString("gender"));        // ← AJOUTÉ
                p.setPhone_number(rs.getInt("phone_number")); // ← AJOUTÉ
                p.setBirth_date(rs.getString("birth_date")); // ← AJOUTÉ

                p.setBlood_type(rs.getString("blood_type"));
                p.setWeight(rs.getDouble("weight"));
                p.setHeight(rs.getDouble("height"));

                System.out.println("✅ Patient loaded: " + p.getName() +
                        ", Gender: " + p.getGender() +
                        ", Birth: " + p.getBirth_date());
                return p;
            } else {
                System.out.println("❌ No patient found with ID: " + id);
            }
        }
        return null;
    }

}
