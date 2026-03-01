-- Création des tables pour les fonctionnalités IA et métiers avancés
-- Exécuter ces requêtes dans votre base de données MySQL

-- Table pour les sessions anonymes
CREATE TABLE IF NOT EXISTS anonymous_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    ip_address VARCHAR(45),
    user_agent TEXT,
    INDEX idx_session_id (session_id),
    INDEX idx_expires_at (expires_at),
    INDEX idx_is_active (is_active)
);

-- Table pour les évaluations de bien-être anonymes
CREATE TABLE IF NOT EXISTS anonymous_wellness_assessments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL,
    overall_wellness_score DECIMAL(5,2),
    stress_level DECIMAL(5,2),
    sleep_quality DECIMAL(5,2),
    mental_state DECIMAL(5,2),
    stress_category VARCHAR(50),
    sleep_category VARCHAR(50),
    mental_category VARCHAR(50),
    assessment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session_id (session_id),
    INDEX idx_assessment_date (assessment_date),
    INDEX idx_overall_score (overall_wellness_score)
);

-- Table pour les recommandations (liée aux évaluations)
CREATE TABLE IF NOT EXISTS assessment_recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assessment_id BIGINT NOT NULL,
    recommendation TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assessment_id) REFERENCES anonymous_wellness_assessments(id) ON DELETE CASCADE,
    INDEX idx_assessment_id (assessment_id)
);

-- Table pour les contenus de santé
CREATE TABLE IF NOT EXISTS health_contents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(100) NOT NULL,
    tags VARCHAR(500),
    author VARCHAR(255),
    publication_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_verified BOOLEAN DEFAULT FALSE,
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    INDEX idx_category (category),
    INDEX idx_is_verified (is_verified),
    INDEX idx_publication_date (publication_date)
);

-- Table pour les quiz de bien-être
CREATE TABLE IF NOT EXISTS wellness_quizzes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_is_active (is_active)
);

-- Table pour les questions des quiz
CREATE TABLE IF NOT EXISTS quiz_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    question_type VARCHAR(50) DEFAULT 'slider', -- slider, multiple_choice, text
    min_value INT DEFAULT 1,
    max_value INT DEFAULT 5,
    order_index INT DEFAULT 0,
    FOREIGN KEY (quiz_id) REFERENCES wellness_quizzes(id) ON DELETE CASCADE,
    INDEX idx_quiz_id (quiz_id),
    INDEX idx_order_index (order_index)
);

-- Table pour les interactions anonymes (pour analytics)
CREATE TABLE IF NOT EXISTS anonymous_interactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL,
    interaction_type VARCHAR(100) NOT NULL, -- view, click, assessment, share
    content_type VARCHAR(100), -- article, quiz, recommendation
    content_id VARCHAR(255),
    interaction_data JSON,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session_id (session_id),
    INDEX idx_interaction_type (interaction_type),
    INDEX idx_timestamp (timestamp)
);

-- Table pour les ressources de bien-être
CREATE TABLE IF NOT EXISTS wellness_resources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    resource_type VARCHAR(100) NOT NULL, -- article, video, exercise, meditation
    resource_url VARCHAR(500),
    duration_minutes INT,
    difficulty_level VARCHAR(50) DEFAULT 'beginner',
    tags VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_resource_type (resource_type),
    INDEX idx_difficulty_level (difficulty_level),
    INDEX idx_is_active (is_active)
);

-- Table pour les programmes de bien-être
CREATE TABLE IF NOT EXISTS wellness_programs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    duration_days INT NOT NULL,
    difficulty_level VARCHAR(50) DEFAULT 'beginner',
    category VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_difficulty_level (difficulty_level),
    INDEX idx_is_active (is_active)
);

-- Table pour les étapes des programmes
CREATE TABLE IF NOT EXISTS program_steps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    program_id BIGINT NOT NULL,
    step_title VARCHAR(255) NOT NULL,
    step_description TEXT,
    day_number INT NOT NULL,
    step_type VARCHAR(100), -- reading, exercise, meditation, assessment
    resource_id BIGINT,
    FOREIGN KEY (program_id) REFERENCES wellness_programs(id) ON DELETE CASCADE,
    FOREIGN KEY (resource_id) REFERENCES wellness_resources(id) ON DELETE SET NULL,
    INDEX idx_program_id (program_id),
    INDEX idx_day_number (day_number)
);

-- Insertion de données de démonstration
INSERT INTO health_contents (title, content, category, author, is_verified) VALUES
('Comprendre le stress : causes et solutions', 'Le stress est une réaction naturelle de l''organisme...', 'Dr. Martin Psychologue', TRUE),
('Les bienfaits d''une bonne nuit de sommeil', 'Un sommeil de qualité est essentiel pour la santé...', 'Dr. Dubois Médecin', TRUE),
('Méditation pour débutants', 'La méditation peut aider à réduire le stress...', 'Sophie Coach Bien-être', TRUE),
('L''exercice physique et la santé mentale', 'L''activité physique régulière a des effets positifs...', 'Dr. Laurent Sport', TRUE);

INSERT INTO wellness_resources (title, description, resource_type, duration_minutes, difficulty_level, tags) VALUES
('Respiration profonde 5 minutes', 'Exercice de respiration pour calmer le stress', 'exercise', 5, 'beginner', 'stress, respiration, calme'),
('Méditation guidée 10 minutes', 'Méditation de pleine conscience pour débutants', 'meditation', 10, 'beginner', 'meditation, pleine conscience, debutant'),
('Étirements doux', 'Étirements pour relâcher les tensions', 'exercise', 15, 'beginner', 'etirements, tension, relaxation'),
('Journal de gratitude', 'Exercice d''écriture pour cultiver la gratitude', 'exercise', 10, 'beginner', 'gratitude, ecriture, positif');

INSERT INTO wellness_programs (title, description, duration_days, difficulty_level, category) VALUES
('Défi 7 jours anti-stress', 'Programme pour réduire le stress en une semaine', 7, 'beginner', 'stress'),
('Améliorer son sommeil en 14 jours', 'Programme pour mieux dormir', 14, 'beginner', 'sommeil'),
('Développement personnel 21 jours', 'Programme de croissance personnelle', 21, 'intermediate', 'developpement');

-- Insertion des étapes pour le programme anti-stress
INSERT INTO program_steps (program_id, step_title, step_description, day_number, step_type) VALUES
(1, 'Jour 1: Prendre conscience', 'Identifiez vos sources de stress', 1, 'reading'),
(1, 'Jour 2: Respiration', 'Pratiquez la respiration profonde', 2, 'exercise'),
(1, 'Jour 3: Méditation', '10 minutes de méditation guidée', 3, 'meditation'),
(1, 'Jour 4: Activité physique', '30 minutes de marche', 4, 'exercise'),
(1, 'Jour 5: Journal', 'Écrivez vos pensées', 5, 'exercise'),
(1, 'Jour 6: Relaxation', 'Étirements et relaxation', 6, 'exercise'),
(1, 'Jour 7: Bilan', 'Évaluez vos progrès', 7, 'assessment');

-- Créer un quiz de bien-être par défaut
INSERT INTO wellness_quizzes (title, description, category, is_active) VALUES
('Évaluation de bien-être complète', 'Évaluez votre niveau de stress, sommeil et santé mentale', 'general', TRUE);

-- Insertion des questions du quiz
INSERT INTO quiz_questions (quiz_id, question_text, min_value, max_value, order_index) VALUES
(1, 'À quelle fréquence vous sentez-vous stressé ?', 1, 5, 1),
(1, 'Quelle est l''intensité de votre stress ?', 1, 5, 2),
(1, 'Comment évaluez-vous votre stress au travail ?', 1, 5, 3),
(1, 'Comment évaluez-vous votre stress personnel ?', 1, 5, 4),
(1, 'Quel est votre niveau d''anxiété général ?', 1, 5, 5),
(1, 'Combien d''heures dormez-vous par nuit ?', 1, 5, 6),
(1, 'Comment évaluez-vous la qualité de votre sommeil ?', 1, 5, 7),
(1, 'Votre horaire de sommeil est-il régulier ?', 1, 5, 8),
(1, 'Combien de temps pour vous endormir ?', 1, 5, 9),
(1, 'Combien de réveils nocturnes ?', 1, 5, 10),
(1, 'Comment évaluez-vous votre humeur générale ?', 1, 5, 11),
(1, 'Quel est votre niveau d''énergie ?', 1, 5, 12),
(1, 'Comment évaluez-vous votre capacité de concentration ?', 1, 5, 13),
(1, 'Comment évaluez-vous vos interactions sociales ?', 1, 5, 14),
(1, 'Quel est votre niveau de satisfaction de vie ?', 1, 5, 15);

COMMIT;
