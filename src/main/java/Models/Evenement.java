package Models;

import java.time.LocalDate;

public class Evenement
{
    private int idEvenement;
    private String titre;
    private String description;
    private LocalDate date;
    private String localisation;
    private float popularity_score;
    private int predicted_attendance;
    private float dynamic_price;
    private float base_price;
    private int max_capacity;
    private String venue_layout;

    public Evenement()
    {
    }

    public Evenement(int idEvenement, String titre, String description, LocalDate date, String localisation, float popularity_score, int predicted_attendance, float dynamic_price, float base_price, int max_capacity, String venue_layout) {
        this.idEvenement = idEvenement;
        this.titre = titre;
        this.description = description;
        this.date = date;
        this.localisation = localisation;
        this.popularity_score = popularity_score;
        this.predicted_attendance = predicted_attendance;
        this.dynamic_price = dynamic_price;
        this.base_price = base_price;
        this.max_capacity = max_capacity;
        this.venue_layout = venue_layout;
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

    public float getPopularity_score() {
        return popularity_score;
    }

    public void setPopularity_score(float popularity_score) {
        this.popularity_score = popularity_score;
    }

    public int getPredicted_attendance() {
        return predicted_attendance;
    }

    public void setPredicted_attendance(int predicted_attendance) {
        this.predicted_attendance = predicted_attendance;
    }

    public float getDynamic_price() {
        return dynamic_price;
    }

    public void setDynamic_price(float dynamic_price) {
        this.dynamic_price = dynamic_price;
    }

    public float getBase_price() {
        return base_price;
    }

    public void setBase_price(float base_price) {
        this.base_price = base_price;
    }

    public int getMax_capacity() {
        return max_capacity;
    }

    public void setMax_capacity(int max_capacity) {
        this.max_capacity = max_capacity;
    }

    public String getVenue_layout() {
        return venue_layout;
    }

    public void setVenue_layout(String venue_layout) {
        this.venue_layout = venue_layout;
    }

    @Override
    public String toString() {
        return "Evenement{" +
                "idEvenement=" + idEvenement +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", localisation='" + localisation + '\'' +
                ", popularity_score=" + popularity_score +
                ", predicted_attendance=" + predicted_attendance +
                ", dynamic_price=" + dynamic_price +
                ", base_price=" + base_price +
                ", max_capacity=" + max_capacity +
                ", venue_layout='" + venue_layout + '\'' +
                '}';
    }
}