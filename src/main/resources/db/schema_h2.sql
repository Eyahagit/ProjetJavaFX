-- ============================================================
-- Application Santé & Bien-être - H2 Database Schema
-- ============================================================
-- H2-specific schema (slightly different syntax from MySQL)
-- ============================================================

CREATE TABLE IF NOT EXISTS utilisateur (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS sante_bien_etre (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    humeur VARCHAR(50) NOT NULL,
    niveau_stress INT NOT NULL,
    qualite_sommeil INT NOT NULL,
    nutrition VARCHAR(255),
    activite_physique VARCHAR(255),
    developpement_personnel VARCHAR(500),
    recommandations CLOB,
    date_suivi DATE NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sante_utilisateur FOREIGN KEY (user_id) REFERENCES utilisateur(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_sante_user_id ON sante_bien_etre(user_id);
CREATE INDEX IF NOT EXISTS idx_sante_date_suivi ON sante_bien_etre(date_suivi);
