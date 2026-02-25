package Services;

import Models.users;
import utils.Database;
import utils.PasswordUtils; // ← NOUVEL IMPORT

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUser implements Iservices<users> {
    private Connection connection;

    public ServiceUser() {
        connection = Database.getInstance().getConnection();
    }

    @Override
    public void ajouter(users users) throws SQLException {
        String sql = "INSERT INTO `users` (name, second_name, age, gender, phone_number, birth_date, email, password, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, users.getName());
            stmt.setString(2, users.getSecond_name());
            stmt.setInt(3, users.getAge());
            stmt.setString(4, users.getGender());
            stmt.setInt(5, users.getPhone_number());
            stmt.setString(6, users.getBirth_date());
            stmt.setString(7, users.getEmail());
            stmt.setString(8, users.getPassword()); // ← Le mot de passe est déjà haché par le contrôleur
            stmt.setString(9, users.getRole());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                users.setId(generatedId);
            }
        }
    }

    @Override
    public void supprimer(users users) throws SQLException {
        String sql = "DELETE FROM `users` WHERE id=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, users.getId());
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void modifier(users users) throws SQLException {
        System.out.println("\n========== ServiceUser.modifier ==========");
        System.out.println("ID: " + users.getId());
        System.out.println("Nom: " + users.getName());
        System.out.println("Email: " + users.getEmail());
        System.out.println("Genre: " + users.getGender());

        // ✅ Vérification que gender n'est pas null
        if (users.getGender() == null || users.getGender().trim().isEmpty()) {
            throw new SQLException("Le genre ne peut pas être null");
        }

        // ✅ Ne mettre à jour que les champs modifiables
        String sql = "UPDATE users SET name = ?, second_name = ?, age = ?, gender = ?, phone_number = ?, email = ? WHERE id = ?";
        // On ne met pas à jour birth_date, password, role

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, users.getName());
            preparedStatement.setString(2, users.getSecond_name());
            preparedStatement.setInt(3, users.getAge());
            preparedStatement.setString(4, users.getGender());
            preparedStatement.setInt(5, users.getPhone_number());
            preparedStatement.setString(6, users.getEmail());
            preparedStatement.setInt(7, users.getId());

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("✅ Users modifiés: " + rowsAffected + " ligne(s)");

            if (rowsAffected == 0) {
                System.out.println("⚠️ Aucune ligne modifiée - ID " + users.getId() + " peut-être inexistant");
            }
        }
    }

    @Override
    public List<users> recuperer() throws SQLException {
        List<users> userslist = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                users user = new users();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setSecond_name(rs.getString("second_name"));
                user.setAge(rs.getInt("age"));
                user.setGender(rs.getString("gender"));
                user.setPhone_number(rs.getInt("phone_number"));
                user.setBirth_date(rs.getString("birth_date"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password")); // ← C'EST LE HASH
                user.setRole(rs.getString("role"));
                userslist.add(user);
            }
            System.out.println("✅ Users found: " + userslist.size());
        }
        return userslist;
    }

    public int countUsers() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM users";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            if (rs.next()) {
                int count = rs.getInt("total");
                System.out.println("✅ Total users: " + count);
                return count;
            }
        }
        return 0;
    }

    // ========== MÉTHODE D'AUTHENTIFICATION AVEC HASHAGE ==========


    public users authenticate(String email, String plainPassword) throws SQLException {
        users user = getByEmail(email);

        if (user != null) {
            // Vérifier d'abord si l'utilisateur est bloqué
            if (user.isBlocked()) {
                throw new SQLException("Votre compte a été bloqué . ");
            }

            // Ensuite vérifier le mot de passe
            if (PasswordUtils.checkPassword(plainPassword, user.getPassword())) {
                return user; // Authentification réussie
            }
        }

        return null; // Échec (utilisateur non trouvé ou mot de passe incorrect)
    }

    // ========== MÉTHODE POUR CHANGER LE MOT DE PASSE ==========

    /**
     * Change le mot de passe d'un utilisateur (avec hashage)
     * @param userId ID de l'utilisateur
     * @param newPlainPassword Nouveau mot de passe en clair
     * @return true si réussi, false sinon
     */
    public boolean changePassword(int userId, String newPlainPassword) throws SQLException {
        String hashedPassword = PasswordUtils.hashPassword(newPlainPassword);
        String sql = "UPDATE users SET password = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, hashedPassword);
            pstmt.setInt(2, userId);

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("✅ Mot de passe changé pour l'utilisateur ID: " + userId);
            return rowsAffected > 0;
        }
    }

    public users getByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                users user = new users();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setSecond_name(rs.getString("second_name"));
                user.setAge(rs.getInt("age"));
                user.setGender(rs.getString("gender"));
                user.setPhone_number(rs.getInt("phone_number"));
                user.setBirth_date(rs.getString("birth_date"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password")); // ← C'EST LE HASH
                user.setRole(rs.getString("role"));
                user.setBlocked(rs.getBoolean("is_blocked"));
                return user;
            }
        }
        return null;
    }

    // Méthodes supplémentaires utiles
    public users getById(int id) throws SQLException {
        String sql = "SELECT * FROM `users` WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                users user = new users();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setSecond_name(rs.getString("second_name"));
                user.setAge(rs.getInt("age"));
                user.setGender(rs.getString("gender"));
                user.setPhone_number(rs.getInt("phone_number"));
                user.setBirth_date(rs.getString("birth_date"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password")); // ← C'EST LE HASH
                user.setRole(rs.getString("role"));
                user.setBlocked(rs.getBoolean("is_blocked"));
                return user;
            }
        }
        return null;
    }

    // ========== MÉTHODES POUR TWILIO ==========

    /**
     * Vérifie si un téléphone existe dans la base de données
     */
    public boolean userExistsByPhone(String telephone) {
        String sql = "SELECT id FROM users WHERE phone = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, telephone);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("❌ Erreur userExistsByPhone: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Met à jour le mot de passe par téléphone (AVEC HASHAGE)
     */
    public boolean updatePasswordByPhone(String telephone, String newPlainPassword) {
        String hashedPassword = PasswordUtils.hashPassword(newPlainPassword); // ← HASHAGE AJOUTÉ
        String sql = "UPDATE users SET password = ? WHERE phone = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, hashedPassword); // ← STOCKAGE DU HASH
            stmt.setString(2, telephone);

            int rowsAffected = stmt.executeUpdate();
            System.out.println("✅ Lignes modifiées par téléphone: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur updatePasswordByPhone: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupère un utilisateur par son téléphone
     */
    public users getByPhone(String telephone) throws SQLException {
        String sql = "SELECT * FROM users WHERE phone = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, telephone);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                users user = new users();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setSecond_name(rs.getString("second_name"));
                user.setAge(rs.getInt("age"));
                user.setGender(rs.getString("gender"));
                user.setPhone_number(rs.getInt("phone_number"));
                user.setBirth_date(rs.getString("birth_date"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password")); // ← C'EST LE HASH
                user.setRole(rs.getString("role"));
                return user;
            }
        }
        return null;
    }


    /**
     * Bloque un utilisateur
     * @param userId ID de l'utilisateur à bloquer
     * @return true si réussi, false sinon
     */
    public boolean blockUser(int userId) throws SQLException {
        String sql = "UPDATE users SET is_blocked = TRUE WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("✅ Utilisateur " + userId + " bloqué");
            return rowsAffected > 0;
        }
    }

    /**
     * Débloque un utilisateur
     * @param userId ID de l'utilisateur à débloquer
     * @return true si réussi, false sinon
     */
    public boolean unblockUser(int userId) throws SQLException {
        String sql = "UPDATE users SET is_blocked = FALSE WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("✅ Utilisateur " + userId + " débloqué");
            return rowsAffected > 0;
        }
    }

    /**
     * Vérifie si un utilisateur est bloqué
     */
    public boolean isUserBlocked(int userId) throws SQLException {
        String sql = "SELECT is_blocked FROM users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("is_blocked");
            }
        }
        return false;
    }
}