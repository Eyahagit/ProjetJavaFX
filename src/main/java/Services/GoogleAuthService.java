package Services;

import Models.users;
import utils.Database;
import utils.GoogleAuthUtil;
import utils.PasswordUtils;

import java.sql.*;
import java.security.SecureRandom;
import java.util.Base64;

public class GoogleAuthService {

    private static GoogleAuthService instance;
    private Database database;
    private GoogleAuthUtil googleAuth;

    private GoogleAuthService() {
        database = Database.getInstance();
        googleAuth = GoogleAuthUtil.getInstance();
    }

    public static GoogleAuthService getInstance() {
        if (instance == null) {
            instance = new GoogleAuthService();
        }
        return instance;
    }

    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[12];
        random.nextBytes(bytes);
        String plainPassword = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        // 🔐 Hacher le mot de passe aléatoire
        return PasswordUtils.hashPassword(plainPassword);
    }

    private users mapGoogleToUser() {
        users user = new users();

        // Email
        user.setEmail(googleAuth.getEmail());

        // Nom complet -> séparer en name et second_name
        String fullName = googleAuth.getFullName();
        if (fullName != null && !fullName.isEmpty()) {
            String[] nameParts = fullName.split(" ", 2);
            user.setName(nameParts[0]); // Prénom
            if (nameParts.length > 1) {
                user.setSecond_name(nameParts[1]); // Nom
            } else {
                user.setSecond_name("");
            }
        } else {
            // Si pas de nom, utiliser la partie locale de l'email
            String emailName = user.getEmail().split("@")[0];
            user.setName(emailName);
            user.setSecond_name("");
        }

        // Valeurs par défaut CORRECTES
        user.setAge(0);                          // 0 = non renseigné
        user.setGender("");                      // Chaîne vide au lieu de "0"
        user.setPhone_number(0);                 // 0 = non renseigné
        user.setBirth_date("");                   // Chaîne vide
        user.setPassword(generateRandomPassword());
        user.setRole("patient");

        System.out.println("✅ Utilisateur Google mappé: " + user.getEmail());
        System.out.println("   Nom: " + user.getName());
        System.out.println("   Rôle: " + user.getRole());

        return user;
    }

    public users authenticateWithGoogle() {
        Connection conn = null;

        try {
            // 1. Authentifier avec Google
            if (!googleAuth.authenticate()) {
                System.err.println("❌ Échec authentification Google");
                return null;
            }

            // 2. Récupérer les infos Google
            if (!googleAuth.getUserInfo()) {
                System.err.println("❌ Impossible de récupérer les infos Google");
                return null;
            }

            String email = googleAuth.getEmail();
            if (email == null) {
                System.err.println("❌ Email Google non trouvé");
                return null;
            }

            // 3. OBTENIR UNE CONNEXION UNIQUE pour toute l'opération
            conn = database.getConnection();
            if (conn == null) {
                System.err.println("❌ Impossible de se connecter à la BD");
                return null;
            }

            // 4. Vérifier si l'utilisateur existe déjà
            users existingUser = getUserByEmail(conn, email);

            if (existingUser != null) {
                System.out.println("✅ Utilisateur existant: " + email);
                return existingUser;
            } else {
                // 5. Créer nouvel utilisateur avec la MÊME connexion
                System.out.println("🆕 Nouvel utilisateur: " + email);
                users newUser = mapGoogleToUser();

                int id = createUser(conn, newUser);
                if (id > 0) {
                    newUser.setId(id);
                    System.out.println("✅ Utilisateur Google créé avec ID: " + id);
                    return newUser;
                } else {
                    System.err.println("❌ Échec création utilisateur dans BD");
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Récupère un utilisateur par email (avec connexion existante)
     */
    private users getUserByEmail(Connection conn, String email) {
        String query = "SELECT * FROM users WHERE email = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

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
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getUserByEmail: " + e.getMessage());
        }
        return null;
    }

    /**
     * Crée un utilisateur (avec connexion existante)
     */
    private int createUser(Connection conn, users user) {
        try {
            // Définir dtype à partir du role
            if (user.getRole() != null && (user.getDtype() == null || user.getDtype().isEmpty())) {
                user.setDtype(user.getRole().toLowerCase()); // "patient" ou "doctor"
            }
            // 1. Insérer d'abord dans users
            String userQuery = "INSERT INTO users (name, second_name, age, gender, phone_number, birth_date, email, password, role, dtype) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement userStmt = conn.prepareStatement(userQuery, Statement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, user.getName());
            userStmt.setString(2, user.getSecond_name());
            userStmt.setInt(3, user.getAge());
            userStmt.setString(4, user.getGender());
            userStmt.setInt(5, user.getPhone_number());
            userStmt.setString(6, user.getBirth_date());
            userStmt.setString(7, user.getEmail());
            userStmt.setString(8, user.getPassword());
            userStmt.setString(9, user.getRole());
            userStmt.setString(10, user.getDtype());

            int affected = userStmt.executeUpdate();
            System.out.println("✅ Utilisateur inséré dans users: " + affected);

            if (affected > 0) {
                ResultSet rs = userStmt.getGeneratedKeys();
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    System.out.println("✅ ID utilisateur généré: " + userId);

                    // 2. Insérer dans patient avec l'id_user
                    String patientQuery = "INSERT INTO patients ( blood_type, weight, height,id_user) " +
                            "VALUES (?, ?, ?, ?)";

                    PreparedStatement patientStmt = conn.prepareStatement(patientQuery);
                          // id_user = l'ID de l'utilisateur
                    patientStmt.setString(1, "");       // blood_type vide par défaut
                    patientStmt.setInt(2, 0);            // weight 0 par défaut
                    patientStmt.setInt(3, 0);
                    patientStmt.setInt(4, userId);// height 0 par défaut

                    int patientAffected = patientStmt.executeUpdate();
                    System.out.println("✅ Patient inséré dans table patient: " + patientAffected);

                    return userId;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur createUser: " + e.getMessage());
            e.printStackTrace();
        }

        return -1;
    }
}