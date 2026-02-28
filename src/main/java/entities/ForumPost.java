package entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ForumPost {

    private int idPost;
    private int userId;
    private String nom;
    private String role;
    private String categorie;
    private String contenu;
    private LocalDateTime dateCreation;
    private boolean archive;
    private int likes;
    private int dislikes;
    private int vues;

    public ForumPost() {}

    public ForumPost(String nom, String role, String categorie, String contenu,
                     LocalDateTime dateCreation, boolean archive) {
        this.nom = nom;
        this.role = role;
        this.categorie = categorie;
        this.contenu = contenu;
        this.dateCreation = dateCreation;
        this.archive = archive;
        this.likes = 0;
        this.dislikes = 0;
        this.vues = 0;
    }

    public ForumPost(int idPost, String nom, String role, String categorie, String contenu,
                     LocalDateTime dateCreation, boolean archive, int likes, int vues) {
        this.idPost = idPost;
        this.nom = nom;
        this.role = role;
        this.categorie = categorie;
        this.contenu = contenu;
        this.dateCreation = dateCreation;
        this.archive = archive;
        this.likes = likes;
        this.dislikes = 0;
        this.vues = vues;
    }

    public int getIdPost() { return idPost; }
    public void setIdPost(int idPost) { this.idPost = idPost; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public boolean isArchive() { return archive; }
    public void setArchive(boolean archive) { this.archive = archive; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public int getDislikes() { return dislikes; }
    public void setDislikes(int dislikes) { this.dislikes = dislikes; }

    public int getVues() { return vues; }
    public void setVues(int vues) { this.vues = vues; }

    public void incrementerLike() { this.likes++; }
    public void incrementerDislike() { this.dislikes++; }
    public void incrementerVue() { this.vues++; }

    public String getDateCreationFormatted() {
        return dateCreation.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getBadge() {
        if (role.contains("Médecin")) {
            if (likes > 50) return "🏆 Expert";
            else if (likes > 20) return "⭐ Confirmé";
            else return "🩺 Praticien";
        } else {
            if (likes > 30) return "🌟 Contributeur";
            else if (likes > 10) return "💫 Actif";
            else return "🌱 Nouveau";
        }
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", role, nom);
    }
}