package Modele;

public class Personne
{

    private int id;
    private String nom;
    private String prenom;
    private String classe;

    public Personne()
    {
    }

    public Personne(int id, String nom, String prenom, String classe)
    {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.classe = classe;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getNom()
    {
        return nom;
    }

    public void setNom(String nom)
    {
        this.nom = nom;
    }

    public String getPrenom()
    {
        return prenom;
    }

    public void setPrenom(String prenom)
    {
        this.prenom = prenom;
    }

    public String getClasse()
    {
        return classe;
    }

    public void setClasse(String classe)
    {
        this.classe = classe;
    }

    @Override
    public String toString()
    {
        return "Personne{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", classe='" + classe + '\'' +
                '}';
    }
}