package entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class User {

    private int id;
    private String nom;
    private String email;
    private String role;
    private LocalDateTime dateInscription;
    private int points;
    private String badge;

    public User(String nom, String email, String role) {
        this.nom = nom;
        this.email = email;
        this.role = role;
        this.dateInscription = LocalDateTime.now();
        this.points = 0;
        this.badge = "🌱 Nouveau";
    }

    public User(int id, String nom, String email, String role,
                LocalDateTime dateInscription, int points, String badge) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.role = role;
        this.dateInscription = dateInscription;
        this.points = points;
        this.badge = badge;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }

    public void ajouterPoints(int points) {
        this.points += points;
        updateBadge();
    }

    private void updateBadge() {
        if (role.contains("Médecin")) {
            if (points > 100) badge = "🏆 Expert";
            else if (points > 50) badge = "⭐ Confirmé";
            else badge = "🩺 Praticien";
        } else {
            if (points > 80) badge = "🌟 Contributeur";
            else if (points > 30) badge = "💫 Actif";
            else badge = "🌱 Nouveau";
        }
    }

    public String getDateInscriptionFormatted() {
        return dateInscription.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}