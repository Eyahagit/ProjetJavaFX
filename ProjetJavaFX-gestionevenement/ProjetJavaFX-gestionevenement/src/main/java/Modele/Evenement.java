package Modele;

import java.time.LocalDate;

public class Evenement
{
    private int idEvenement;
    private String titre;
    private String description;
    private LocalDate date;
    private String localisation;

    public Evenement()
    {
    }

    public Evenement(int idEvenement, String titre, String description, LocalDate date, String localisation)
    {
        this.idEvenement = idEvenement;
        this.titre = titre;
        this.description = description;
        this.date = date;
        this.localisation = localisation;
    }

    public int getIdEvenement()
    {
        return idEvenement;
    }

    public void setIdEvenement(int idEvenement)
    {
        this.idEvenement = idEvenement;
    }

    public String getTitre()
    {
        return titre;
    }

    public void setTitre(String titre)
    {
        this.titre = titre;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public LocalDate getDate()
    {
        return date;
    }

    public void setDate(LocalDate date)
    {
        this.date = date;
    }

    public String getLocalisation()
    {
        return localisation;
    }

    public void setLocalisation(String localisation)
    {
        this.localisation = localisation;
    }

    @Override
    public String toString()
    {
        return "Evenement{" +
                "idEvenement=" + idEvenement +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", localisation='" + localisation + '\'' +
                '}';
    }
}