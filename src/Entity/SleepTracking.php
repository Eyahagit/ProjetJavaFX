<?php

namespace App\Entity;

use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;

/**
 * SleepTracking
 *
 * @ORM\Table(name="sleep_tracking", indexes={@ORM\Index(name="fk_sleep_utilisateur", columns={"user_id"}), @ORM\Index(name="idx_sleep_user_id", columns={"user_id"}), @ORM\Index(name="idx_sleep_date_sommeil", columns={"date_sommeil"})})
 * @ORM\Entity
 */
class SleepTracking
{
    /**
     * @var int
     *
     * @ORM\Column(name="id", type="integer", nullable=false)
     * @ORM\Id
     * @ORM\GeneratedValue(strategy="IDENTITY")
     */
    private $id;

    /**
     * @var \DateTime
     *
     * @ORM\Column(name="date_sommeil", type="date", nullable=false)
     */
    private $dateSommeil;

    /**
     * @var \DateTime
     *
     * @ORM\Column(name="heure_coucher", type="time", nullable=false)
     */
    private $heureCoucher;

    /**
     * @var \DateTime
     *
     * @ORM\Column(name="heure_reveil", type="time", nullable=false)
     */
    private $heureReveil;

    /**
     * @var int
     *
     * @ORM\Column(name="duree_minutes", type="integer", nullable=false)
     */
    private $dureeMinutes;

    /**
     * @var int
     *
     * @ORM\Column(name="qualite_sommeil", type="integer", nullable=false)
     */
    private $qualiteSommeil;

    /**
     * @var string|null
     *
     * @ORM\Column(name="commentaire", type="string", length=1000, nullable=true)
     */
    private $commentaire;

    /**
     * @var \DateTime
     *
     * @ORM\Column(name="date_creation", type="datetime", nullable=false, options={"default"="CURRENT_TIMESTAMP"})
     */
    private $dateCreation;

    /**
     * @var \Utilisateur
     *
     * @ORM\ManyToOne(targetEntity="Utilisateur", inversedBy="sleepTrackings")
     * @ORM\JoinColumns({
     *   @ORM\JoinColumn(name="user_id", referencedColumnName="id", nullable=false, onDelete="CASCADE")
     * })
     */
    private $user;

    public function __construct()
    {
        $this->dateCreation = new \DateTime();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getDateSommeil(): ?\DateTime
    {
        return $this->dateSommeil;
    }

    public function setDateSommeil(\DateTime $dateSommeil): static
    {
        $this->dateSommeil = $dateSommeil;

        return $this;
    }

    public function getHeureCoucher(): ?\DateTime
    {
        return $this->heureCoucher;
    }

    public function setHeureCoucher(\DateTime $heureCoucher): static
    {
        $this->heureCoucher = $heureCoucher;

        return $this;
    }

    public function getHeureReveil(): ?\DateTime
    {
        return $this->heureReveil;
    }

    public function setHeureReveil(\DateTime $heureReveil): static
    {
        $this->heureReveil = $heureReveil;

        return $this;
    }

    public function getDureeMinutes(): ?int
    {
        return $this->dureeMinutes;
    }

    public function setDureeMinutes(int $dureeMinutes): static
    {
        $this->dureeMinutes = $dureeMinutes;

        return $this;
    }

    public function getQualiteSommeil(): ?int
    {
        return $this->qualiteSommeil;
    }

    public function setQualiteSommeil(int $qualiteSommeil): static
    {
        $this->qualiteSommeil = $qualiteSommeil;

        return $this;
    }

    public function getCommentaire(): ?string
    {
        return $this->commentaire;
    }

    public function setCommentaire(?string $commentaire): static
    {
        $this->commentaire = $commentaire;

        return $this;
    }

    public function getDateCreation(): ?\DateTime
    {
        return $this->dateCreation;
    }

    public function setDateCreation(\DateTime $dateCreation): static
    {
        $this->dateCreation = $dateCreation;

        return $this;
    }

    public function getUser(): ?Utilisateur
    {
        return $this->user;
    }

    public function setUser(?Utilisateur $user): static
    {
        $this->user = $user;

        return $this;
    }


}
