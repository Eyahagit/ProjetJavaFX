package entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Reponse {

    private int idReponse;
    private int idPost;
    private int userId;
    private String auteur;
    private String contenu;
    private LocalDateTime dateReponse;
    private int likes;
    private int dislikes;

    public Reponse(int idPost, String auteur, String contenu, LocalDateTime dateReponse) {
        this.idPost = idPost;
        this.auteur = auteur;
        this.contenu = contenu;
        this.dateReponse = dateReponse;
        this.likes = 0;
        this.dislikes = 0;
    }

    public Reponse(int idReponse, int idPost, String auteur, String contenu,
                   LocalDateTime dateReponse, int likes) {
        this.idReponse = idReponse;
        this.idPost = idPost;
        this.auteur = auteur;
        this.contenu = contenu;
        this.dateReponse = dateReponse;
        this.likes = likes;
        this.dislikes = 0;
    }

    public int getIdReponse() { return idReponse; }
    public void setIdReponse(int idReponse) { this.idReponse = idReponse; }

    public int getIdPost() { return idPost; }
    public void setIdPost(int idPost) { this.idPost = idPost; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public LocalDateTime getDateReponse() { return dateReponse; }
    public void setDateReponse(LocalDateTime dateReponse) { this.dateReponse = dateReponse; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public int getDislikes() { return dislikes; }
    public void setDislikes(int dislikes) { this.dislikes = dislikes; }

    public void incrementerLike() { this.likes++; }
    public void incrementerDislike() { this.dislikes++; }

    public String getDateReponseFormatted() {
        return dateReponse.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}