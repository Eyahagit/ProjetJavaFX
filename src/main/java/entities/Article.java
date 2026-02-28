package entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Article {
    private String titre;
    private String description;
    private String contenu;
    private String url;
    private String imageUrl;
    private String source;
    private String auteur;
    private LocalDateTime datePublication;

    public Article() {}

    public Article(String titre, String description, String contenu, String url,
                   String imageUrl, String source, String auteur, LocalDateTime datePublication) {
        this.titre = titre;
        this.description = description;
        this.contenu = contenu;
        this.url = url;
        this.imageUrl = imageUrl;
        this.source = source;
        this.auteur = auteur;
        this.datePublication = datePublication;
    }

    // Getters et Setters
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }

    public LocalDateTime getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDateTime datePublication) { this.datePublication = datePublication; }

    public String getDateFormatted() {
        if (datePublication == null) return "";
        return datePublication.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}