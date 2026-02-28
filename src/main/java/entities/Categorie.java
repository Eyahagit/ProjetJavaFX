package entities;

public class Categorie {

    private int id;
    private String nom;
    private String icone;
    private String couleur;

    public Categorie(String nom, String icone, String couleur) {
        this.nom = nom;
        this.icone = icone;
        this.couleur = couleur;
    }

    public Categorie(int id, String nom, String icone, String couleur) {
        this.id = id;
        this.nom = nom;
        this.icone = icone;
        this.couleur = couleur;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getIcone() { return icone; }
    public void setIcone(String icone) { this.icone = icone; }
    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { this.couleur = couleur; }

    @Override
    public String toString() {
        return icone + " " + nom;
    }
}