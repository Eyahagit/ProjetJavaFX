package com.santebienetre.service;

import com.santebienetre.dao.SleepTrackingDAO;
import com.santebienetre.model.SleepTracking;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class SleepTrackingService {

    private final SleepTrackingDAO dao = new SleepTrackingDAO();

    public int add(SleepTracking s) {
        if (!validate(s)) return -1;
        s.setDureeMinutes(computeDurationMinutes(s.getDateSommeil(), s.getHeureCoucher(), s.getHeureReveil()));
        return dao.addSleepTracking(s);
    }

    public boolean update(SleepTracking s) {
        if (!validate(s)) return false;
        s.setDureeMinutes(computeDurationMinutes(s.getDateSommeil(), s.getHeureCoucher(), s.getHeureReveil()));
        return dao.updateSleepTracking(s);
    }

    public boolean delete(int id) {
        return dao.deleteSleepTracking(id);
    }

    public List<SleepTracking> getAll() {
        return dao.getAll();
    }

    public List<SleepTracking> getByUser(int userId) {
        return dao.getByUser(userId);
    }

    public List<SleepTracking> getByDateRange(int userId, LocalDate start, LocalDate end) {
        return dao.getByDateRange(userId, start, end);
    }

    public int computeDurationMinutes(LocalDate dateSommeil, LocalTime coucher, LocalTime reveil) {
        LocalDate baseDate = dateSommeil != null ? dateSommeil : LocalDate.now();
        LocalDateTime dtCoucher = LocalDateTime.of(baseDate, coucher);
        LocalDateTime dtReveil = LocalDateTime.of(baseDate, reveil);
        if (!dtReveil.isAfter(dtCoucher)) {
            dtReveil = dtReveil.plusDays(1);
        }
        return (int) Duration.between(dtCoucher, dtReveil).toMinutes();
    }

    private boolean validate(SleepTracking s) {
        if (s == null) return false;
        if (s.getUserId() <= 0) return false;
        if (s.getDateSommeil() == null) s.setDateSommeil(LocalDate.now());
        if (s.getHeureCoucher() == null || s.getHeureReveil() == null) return false;
        if (s.getQualiteSommeil() < 1 || s.getQualiteSommeil() > 5) return false;
        return true;
    }
}
