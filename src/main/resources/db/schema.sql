-- ============================================================
-- Application Santé & Bien-être - Database Schema
-- ============================================================
-- This script creates the tables required for the Health & Wellness module.
-- Uses H2/MySQL compatible syntax.
-- ============================================================

-- Table: utilisateur (if not exists - referenced by sante_bien_etre)
-- Minimal user table for foreign key reference
CREATE TABLE IF NOT EXISTS utilisateur (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: sante_bien_etre
-- Stores daily health and wellness records
CREATE TABLE IF NOT EXISTS sante_bien_etre (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    humeur VARCHAR(50) NOT NULL,
    niveau_stress INT NOT NULL CHECK (niveau_stress >= 1 AND niveau_stress <= 10),
    qualite_sommeil INT NOT NULL CHECK (qualite_sommeil >= 1 AND qualite_sommeil <= 10),
    nutrition VARCHAR(255),
    activite_physique VARCHAR(255),
    developpement_personnel VARCHAR(500),
    recommandations TEXT,
    date_suivi DATE NOT NULL DEFAULT CURRENT_DATE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_sante_utilisateur FOREIGN KEY (user_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    CONSTRAINT chk_niveau_stress CHECK (niveau_stress BETWEEN 1 AND 10),
    CONSTRAINT chk_qualite_sommeil CHECK (qualite_sommeil BETWEEN 1 AND 10)
);

-- Index for efficient queries by user and date
CREATE INDEX IF NOT EXISTS idx_sante_user_id ON sante_bien_etre(user_id);
CREATE INDEX IF NOT EXISTS idx_sante_date_suivi ON sante_bien_etre(date_suivi);
CREATE INDEX IF NOT EXISTS idx_sante_user_date ON sante_bien_etre(user_id, date_suivi);

-- Insert default user for demo (if utilisateur table exists and is empty)
-- INSERT INTO utilisateur (nom, email) VALUES ('Utilisateur Demo', 'demo@santebienetre.com');
