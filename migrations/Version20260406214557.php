<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260406214557 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Normalizes conseils_utilisateurs as a pure many-to-many join table and aligns sleep_tracking qualite_sommeil with Doctrine mapping.';
    }

    public function up(Schema $schema): void
    {
        $this->abortIf(
            'mysql' !== $this->connection->getDatabasePlatform()->getName(),
            'This migration can only be executed safely on MySQL/MariaDB.'
        );

        $this->addSql('ALTER TABLE conseils_utilisateurs MODIFY id INT NOT NULL');
        $this->addSql('DROP INDEX `primary` ON conseils_utilisateurs');
        $this->addSql('ALTER TABLE conseils_utilisateurs DROP FOREIGN KEY conseils_utilisateurs_ibfk_1');
        $this->addSql('ALTER TABLE conseils_utilisateurs DROP FOREIGN KEY conseils_utilisateurs_ibfk_2');
        $this->addSql('ALTER TABLE conseils_utilisateurs DROP id, DROP date_attribution, DROP est_vu, CHANGE utilisateur_id utilisateur_id INT NOT NULL, CHANGE conseil_id conseil_id INT NOT NULL');
        $this->addSql('ALTER TABLE conseils_utilisateurs ADD PRIMARY KEY (conseil_id, utilisateur_id)');
        $this->addSql('DROP INDEX conseil_id ON conseils_utilisateurs');
        $this->addSql('CREATE INDEX IDX_38573DCA668A3E03 ON conseils_utilisateurs (conseil_id)');
        $this->addSql('DROP INDEX utilisateur_id ON conseils_utilisateurs');
        $this->addSql('CREATE INDEX IDX_38573DCAFB88E14F ON conseils_utilisateurs (utilisateur_id)');
        $this->addSql('ALTER TABLE conseils_utilisateurs ADD CONSTRAINT conseils_utilisateurs_ibfk_1 FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id)');
        $this->addSql('ALTER TABLE conseils_utilisateurs ADD CONSTRAINT conseils_utilisateurs_ibfk_2 FOREIGN KEY (conseil_id) REFERENCES conseils (id)');
        $this->addSql('ALTER TABLE sleep_tracking CHANGE qualite_sommeil qualite_sommeil INT NOT NULL');
    }

    public function down(Schema $schema): void
    {
        $this->abortIf(
            'mysql' !== $this->connection->getDatabasePlatform()->getName(),
            'This migration can only be executed safely on MySQL/MariaDB.'
        );

        $this->addSql('ALTER TABLE conseils_utilisateurs DROP FOREIGN KEY FK_38573DCA668A3E03');
        $this->addSql('ALTER TABLE conseils_utilisateurs DROP FOREIGN KEY FK_38573DCAFB88E14F');
        $this->addSql('ALTER TABLE conseils_utilisateurs ADD id INT AUTO_INCREMENT NOT NULL, ADD date_attribution DATETIME DEFAULT CURRENT_TIMESTAMP, ADD est_vu TINYINT(1) DEFAULT 0, CHANGE conseil_id conseil_id INT DEFAULT NULL, CHANGE utilisateur_id utilisateur_id INT DEFAULT NULL, DROP PRIMARY KEY, ADD PRIMARY KEY (id)');
        $this->addSql('DROP INDEX idx_38573dca668a3e03 ON conseils_utilisateurs');
        $this->addSql('CREATE INDEX conseil_id ON conseils_utilisateurs (conseil_id)');
        $this->addSql('DROP INDEX idx_38573dcafb88e14f ON conseils_utilisateurs');
        $this->addSql('CREATE INDEX utilisateur_id ON conseils_utilisateurs (utilisateur_id)');
        $this->addSql('ALTER TABLE conseils_utilisateurs ADD CONSTRAINT FK_38573DCA668A3E03 FOREIGN KEY (conseil_id) REFERENCES conseils (id)');
        $this->addSql('ALTER TABLE conseils_utilisateurs ADD CONSTRAINT FK_38573DCAFB88E14F FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id)');
        $this->addSql('ALTER TABLE sleep_tracking CHANGE qualite_sommeil qualite_sommeil TINYINT(1) NOT NULL');
    }
}
