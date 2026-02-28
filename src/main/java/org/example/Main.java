package org.example;

import org.example.Controllers.EvaluationController;
import org.example.Controllers.FavoriController;
import org.example.Controllers.RessourceController;
import org.example.Models.Evaluation;
import org.example.Models.Favori;
import org.example.Models.Ressource;
import org.example.Services.MailService;
import org.example.utils.MyDatabase;
import org.example.utils.StaticUser;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class Main {
        /**
         * Call this from tests or other entry points to ensure DB tables and static
         * user are ready.
         */
        public static void initDatabaseAndStaticUser() {
                MyDatabase db = MyDatabase.getInstance();
                try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {

                        // ---- Table User ----
                        String sqlUser = "CREATE TABLE IF NOT EXISTS User (" +
                                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                                        "nom VARCHAR(50) NOT NULL," +
                                        "prenom VARCHAR(50) NOT NULL," +
                                        "email VARCHAR(100) NOT NULL UNIQUE," +
                                        "password VARCHAR(100) NOT NULL," +
                                        "role VARCHAR(20) NOT NULL," +
                                        "dateInscription DATE," +
                                        "actif BOOLEAN" +
                                        ");";
                        stmt.executeUpdate(sqlUser);

                        // ---- Table Ressource ----
                        String sqlRessource = "CREATE TABLE IF NOT EXISTS Ressource (" +
                                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                                        "title VARCHAR(100) NOT NULL," +
                                        "description TEXT," +
                                        "type VARCHAR(20)," +
                                        "category VARCHAR(50)," +
                                        "content TEXT," +
                                        "author VARCHAR(100)," +
                                        "dateCreation DATE," +
                                        "status VARCHAR(20)" +
                                        ");";
                        stmt.executeUpdate(sqlRessource);

                        // ---- Table Evaluation ----
                        String sqlEvaluation = "CREATE TABLE IF NOT EXISTS Evaluation (" +
                                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                                        "userId INT NOT NULL," +
                                        "ressourceId INT NOT NULL," +
                                        "note INT," +
                                        "commentaire TEXT," +
                                        "dateEvaluation DATE," +
                                        "FOREIGN KEY (userId) REFERENCES User(id) ON DELETE CASCADE," +
                                        "FOREIGN KEY (ressourceId) REFERENCES Ressource(id) ON DELETE CASCADE" +
                                        ");";
                        stmt.executeUpdate(sqlEvaluation);

                        // ---- Table Favori ----
                        String sqlFavori = "CREATE TABLE IF NOT EXISTS Favori (" +
                                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                                        "userId INT NOT NULL," +
                                        "ressourceId INT NOT NULL," +
                                        "dateAjout DATE," +
                                        "FOREIGN KEY (userId) REFERENCES User(id) ON DELETE CASCADE," +
                                        "FOREIGN KEY (ressourceId) REFERENCES Ressource(id) ON DELETE CASCADE" +
                                        ");";
                        stmt.executeUpdate(sqlFavori);

                        System.out.println("Tables created successfully!");

                } catch (SQLException e) {
                        System.out.println("Erreur lors de la création des tables : " + e.getMessage());
                        return;
                }

                StaticUser.setId(1);
                System.out.println("Static user set to ID 1 for testing.");
        }

        public static void main(String[] args) {
                initDatabaseAndStaticUser();

                RessourceController ressourceCtrl = new RessourceController();
                EvaluationController evaluationCtrl = new EvaluationController();
                FavoriController favoriCtrl = new FavoriController();

                int currentUserId = StaticUser.getId();

                LocalDate today = LocalDate.now();

                Ressource res = new Ressource(
                                "Ma première ressource",
                                "kkkkkkkkkkkkkkkkkkkkkk",
                                "ARTICLE",
                                "Santé",
                                "Contenu de l'article...",
                                "Auteur Eya",
                                today,
                                "PUBLISHED");
                res = ressourceCtrl.create(res);
                System.out.println("Ressource ajoutée: id=" + res.getId() + ", title=" + res.getTitle());

                Evaluation eval = new Evaluation(currentUserId, res, 5, "Très utile !", today);
                eval = evaluationCtrl.create(eval);
                System.out.println("Evaluation ajoutée: id=" + eval.getId() + ", note=" + eval.getNote());

                Favori fav = new Favori(currentUserId, res, today);
                fav = favoriCtrl.create(fav);
                System.out.println("Favori ajouté: id=" + fav.getId());

                MailService.sendEmail(
                                MailService.DEFAULT_TO_EMAIL,
                                "Test GrowMind",
                                "Email de test depuis l'application JavaFX GrowMind.");
        }
}