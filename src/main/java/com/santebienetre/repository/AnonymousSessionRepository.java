package com.santebienetre.repository;

import com.santebienetre.model.AnonymousSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour les sessions anonymes
 */
public interface AnonymousSessionRepository {
    
    /**
     * Trouve une session par son ID
     */
    Optional<AnonymousSession> findBySessionId(String sessionId);
    
    /**
     * Trouve une session active par son ID
     */
    AnonymousSession findBySessionIdAndIsActiveTrue(String sessionId);
    
    /**
     * Trouve les sessions expirées
     */
    List<AnonymousSession> findByExpiresAtBeforeAndIsActiveTrue(LocalDateTime dateTime);
    
    /**
     * Compte les sessions actives
     */
    Long countActiveSessions(LocalDateTime now);
    
    /**
     * Nettoie les anciennes sessions
     */
    void deleteOldInactiveSessions(LocalDateTime cutoffDate);
    
    /**
     * Sauvegarde une session
     */
    AnonymousSession save(AnonymousSession session);
}
