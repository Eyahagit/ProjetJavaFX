package org.example.Models;

import java.time.LocalDate;

public class Favori {

    private int id;
    private User user;          // utilisateur qui ajoute en favori
    private Ressource ressource; // ressource mise en favori
    private LocalDate dateAjout;

    // Constructeurs
    public Favori() { }

    public Favori(User user, Ressource ressource, LocalDate dateAjout) {
        this.user = user;
        this.ressource = ressource;
        this.dateAjout = dateAjout;
    }

    public Favori(int id, User user, Ressource ressource, LocalDate dateAjout) {
        this(user, ressource, dateAjout);
        this.id = id;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Ressource getRessource() { return ressource; }
    public void setRessource(Ressource ressource) { this.ressource = ressource; }
    public LocalDate getDateAjout() { return dateAjout; }
    public void setDateAjout(LocalDate dateAjout) { this.dateAjout = dateAjout; }

    @Override
    public String toString() {
        return "Favori{" +
                "id=" + id +
                ", user=" + user +
                ", ressource=" + ressource +
                ", dateAjout=" + dateAjout +
                "}\n";
    }
}
