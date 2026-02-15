package org.example.Models;

import java.time.LocalDate;
import java.util.List;

public class Ressource {

    private int id;
    private String title;
    private String description;
    private String type;      // FORMATION, ARTICLE, VIDEO, EVENT
    private String category;  // Santé, Bien-être, Développement personnel
    private String content;   // texte, lien vidéo,
    private String author;
    private LocalDate dateCreation;
    private String status;    // PUBLISHED, DRAFT

    // Relations
    private List<Evaluation> evaluations; // toutes les évaluations de la ressource
    private List<Favori> favoris;         // tous les favoris liés à cette ressource

    // Constructeurs
    public Ressource() { }

    public Ressource(String title, String description, String type, String category,
                     String content, String author, LocalDate dateCreation, String status) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.category = category;
        this.content = content;
        this.author = author;
        this.dateCreation = dateCreation;
        this.status = status;
    }

    public Ressource(int id, String title, String description, String type, String category,
                     String content, String author, LocalDate dateCreation, String status) {
        this(title, description, type, category, content, author, dateCreation, status);
        this.id = id;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<Evaluation> getEvaluations() { return evaluations; }
    public void setEvaluations(List<Evaluation> evaluations) { this.evaluations = evaluations; }
    public List<Favori> getFavoris() { return favoris; }
    public void setFavoris(List<Favori> favoris) { this.favoris = favoris; }

    @Override
    public String toString() {
        return "Ressource{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", category='" + category + '\'' +
                ", content='" + content + '\'' +
                ", author='" + author + '\'' +
                ", dateCreation=" + dateCreation +
                ", status='" + status + '\'' +
                "}\n";
    }
}

