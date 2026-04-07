<?php

namespace App\Entity;

use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;

/**
 * Humeurs
 *
 * @ORM\Table(name="humeurs", indexes={@ORM\Index(name="utilisateur_id", columns={"utilisateur_id"})})
 * @ORM\Entity
 */
class Humeurs
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
     * @var int
     *
     * @ORM\Column(name="niveau", type="integer", nullable=false, options={"comment"="1-5 (très mauvais à excellent)"})
     */
    private $niveau;

    /**
     * @var string|null
     *
     * @ORM\Column(name="notes", type="text", length=65535, nullable=true)
     */
    private $notes;

    /**
     * @var \DateTime|null
     *
     * @ORM\Column(name="date_creation", type="datetime", nullable=true, options={"default"="CURRENT_TIMESTAMP"})
     */
    private $dateCreation;

    /**
     * @var \Utilisateur
     *
     * @ORM\ManyToOne(targetEntity="Utilisateur", inversedBy="humeurs")
     * @ORM\JoinColumns({
     *   @ORM\JoinColumn(name="utilisateur_id", referencedColumnName="id")
     * })
     */
    private $utilisateur;

    public function __construct()
    {
        $this->dateCreation = new \DateTime();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getNiveau(): ?int
    {
        return $this->niveau;
    }

    public function setNiveau(int $niveau): static
    {
        $this->niveau = $niveau;

        return $this;
    }

    public function getNotes(): ?string
    {
        return $this->notes;
    }

    public function setNotes(?string $notes): static
    {
        $this->notes = $notes;

        return $this;
    }

    public function getDateCreation(): ?\DateTime
    {
        return $this->dateCreation;
    }

    public function setDateCreation(?\DateTime $dateCreation): static
    {
        $this->dateCreation = $dateCreation;

        return $this;
    }

    public function getUtilisateur(): ?Utilisateur
    {
        return $this->utilisateur;
    }

    public function setUtilisateur(?Utilisateur $utilisateur): static
    {
        $this->utilisateur = $utilisateur;

        return $this;
    }


}
