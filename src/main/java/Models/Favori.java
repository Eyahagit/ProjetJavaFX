package Models;

import java.time.LocalDate;

public class Favori {

    private int id;
    private int userId; // ID de l'utilisateur qui ajoute en favori
    private Ressource ressource; // ressource mise en favori
    private LocalDate dateAjout;

    // Constructeurs
    public Favori() {
    }

    public Favori(int userId, Ressource ressource, LocalDate dateAjout) {
        this.userId = userId;
        this.ressource = ressource;
        this.dateAjout = dateAjout;
    }

    public Favori(int id, int userId, Ressource ressource, LocalDate dateAjout) {
        this(userId, ressource, dateAjout);
        this.id = id;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Ressource getRessource() {
        return ressource;
    }

    public void setRessource(Ressource ressource) {
        this.ressource = ressource;
    }

    public LocalDate getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(LocalDate dateAjout) {
        this.dateAjout = dateAjout;
    }

    @Override
    public String toString() {
        return "Favori{" +
                "id=" + id +
                ", userId=" + userId +
                ", ressource=" + ressource +
                ", dateAjout=" + dateAjout +
                "}\n";
    }
}
