package org.example.Models;

import java.time.LocalDate;
import java.util.List;

public class User {

    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String role; // ADMIN, DOCTEUR, CLIENT
    private LocalDate dateInscription;
    private boolean actif;

    // Relations
    private List<Evaluation> evaluations; // toutes les évaluations faites par l'utilisateur
    private List<Favori> favoris;         // toutes les ressources favorites de l'utilisateur

    // Constructeurs
    public User() { }

    public User(String nom, String prenom, String email, String password,
                String role, LocalDate dateInscription, boolean actif) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.role = role;
        this.dateInscription = dateInscription;
        this.actif = actif;
    }

    public User(int id, String nom, String prenom, String email, String password,
                String role, LocalDate dateInscription, boolean actif) {
        this(nom, prenom, email, password, role, dateInscription, actif);
        this.id = id;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDate getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDate dateInscription) { this.dateInscription = dateInscription; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public List<Evaluation> getEvaluations() { return evaluations; }
    public void setEvaluations(List<Evaluation> evaluations) { this.evaluations = evaluations; }
    public List<Favori> getFavoris() { return favoris; }
    public void setFavoris(List<Favori> favoris) { this.favoris = favoris; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", dateInscription=" + dateInscription +
                ", actif=" + actif +
                "}\n";
    }
}
