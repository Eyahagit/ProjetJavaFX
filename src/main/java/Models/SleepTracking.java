package Models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class SleepTracking {

    private int id;
    private int userId;
    private LocalDate dateSommeil;
    private LocalTime heureCoucher;
    private LocalTime heureReveil;
    private int dureeMinutes;
    private int qualiteSommeil;
    private String commentaire;

    public SleepTracking() {
        this.dateSommeil = LocalDate.now();
    }

    public SleepTracking(int userId, LocalDate dateSommeil, LocalTime heureCoucher, LocalTime heureReveil,
                         int dureeMinutes, int qualiteSommeil, String commentaire) {
        this.userId = userId;
        this.dateSommeil = dateSommeil != null ? dateSommeil : LocalDate.now();
        this.heureCoucher = heureCoucher;
        this.heureReveil = heureReveil;
        this.dureeMinutes = dureeMinutes;
        this.qualiteSommeil = qualiteSommeil;
        this.commentaire = commentaire;
    }

    public SleepTracking(int id, int userId, LocalDate dateSommeil, LocalTime heureCoucher, LocalTime heureReveil,
                         int dureeMinutes, int qualiteSommeil, String commentaire) {
        this.id = id;
        this.userId = userId;
        this.dateSommeil = dateSommeil != null ? dateSommeil : LocalDate.now();
        this.heureCoucher = heureCoucher;
        this.heureReveil = heureReveil;
        this.dureeMinutes = dureeMinutes;
        this.qualiteSommeil = qualiteSommeil;
        this.commentaire = commentaire;
    }

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

    public LocalDate getDateSommeil() {
        return dateSommeil;
    }

    public void setDateSommeil(LocalDate dateSommeil) {
        this.dateSommeil = dateSommeil;
    }

    public LocalTime getHeureCoucher() {
        return heureCoucher;
    }

    public void setHeureCoucher(LocalTime heureCoucher) {
        this.heureCoucher = heureCoucher;
    }

    public LocalTime getHeureReveil() {
        return heureReveil;
    }

    public void setHeureReveil(LocalTime heureReveil) {
        this.heureReveil = heureReveil;
    }

    public int getDureeMinutes() {
        return dureeMinutes;
    }

    public void setDureeMinutes(int dureeMinutes) {
        this.dureeMinutes = dureeMinutes;
    }

    public int getQualiteSommeil() {
        return qualiteSommeil;
    }

    public void setQualiteSommeil(int qualiteSommeil) {
        this.qualiteSommeil = qualiteSommeil;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SleepTracking that = (SleepTracking) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SleepTracking{" +
                "id=" + id +
                ", userId=" + userId +
                ", dateSommeil=" + dateSommeil +
                ", dureeMinutes=" + dureeMinutes +
                ", qualiteSommeil=" + qualiteSommeil +
                '}';
    }
}
