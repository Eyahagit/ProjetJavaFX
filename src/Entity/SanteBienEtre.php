<?php

namespace App\Entity;

use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

/**
 * SanteBienEtre
 *
 * @ORM\Table(name="sante_bien_etre", indexes={@ORM\Index(name="idx_sante_user_date", columns={"user_id", "date_suivi"}), @ORM\Index(name="fk_sante_utilisateur", columns={"user_id"}), @ORM\Index(name="idx_sante_user_id", columns={"user_id"}), @ORM\Index(name="idx_sante_date_suivi", columns={"date_suivi"})})
 * @ORM\Entity
 */
class SanteBienEtre
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
     * @var string
     *
     * @ORM\Column(name="humeur", type="string", length=50, nullable=false)
     */
    #[Assert\NotBlank(message: 'Humeur is required.')]
    private $humeur;

    /**
     * @var int
     *
     * @ORM\Column(name="niveau_stress", type="integer", nullable=false)
     */
    #[Assert\Range(
        min: 1,
        max: 10,
        notInRangeMessage: 'Stress level must be between {{ min }} and {{ max }}.'
    )]
    private $niveauStress;

    /**
     * @var int
     *
     * @ORM\Column(name="qualite_sommeil", type="integer", nullable=false)
     */
    #[Assert\Range(
        min: 1,
        max: 10,
        notInRangeMessage: 'Sleep quality must be between {{ min }} and {{ max }}.'
    )]
    private $qualiteSommeil;

    /**
     * @var string|null
     *
     * @ORM\Column(name="nutrition", type="string", length=255, nullable=true)
     */
    private $nutrition;

    /**
     * @var string|null
     *
     * @ORM\Column(name="activite_physique", type="string", length=255, nullable=true)
     */
    private $activitePhysique;

    /**
     * @var string|null
     *
     * @ORM\Column(name="developpement_personnel", type="string", length=500, nullable=true)
     */
    private $developpementPersonnel;

    /**
     * @var string|null
     *
     * @ORM\Column(name="recommandations", type="text", length=65535, nullable=true)
     */
    private $recommandations;

    /**
     * @var \DateTime
     *
     * @ORM\Column(name="date_suivi", type="date", nullable=false)
     */
    #[Assert\NotBlank(message: 'Follow-up date is required.')]
    private $dateSuivi;

    /**
     * @var \DateTime
     *
     * @ORM\Column(name="date_creation", type="datetime", nullable=false, options={"default"="CURRENT_TIMESTAMP"})
     */
    private $dateCreation;

    /**
     * @var \Utilisateur
     *
     * @ORM\ManyToOne(targetEntity="Utilisateur", inversedBy="santeBienEtres")
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

    public function getHumeur(): ?string
    {
        return $this->humeur;
    }

    public function setHumeur(string $humeur): static
    {
        $this->humeur = $humeur;

        return $this;
    }

    public function getNiveauStress(): ?int
    {
        return $this->niveauStress;
    }

    public function setNiveauStress(int $niveauStress): static
    {
        $this->niveauStress = $niveauStress;

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

    public function getNutrition(): ?string
    {
        return $this->nutrition;
    }

    public function setNutrition(?string $nutrition): static
    {
        $this->nutrition = $nutrition;

        return $this;
    }

    public function getActivitePhysique(): ?string
    {
        return $this->activitePhysique;
    }

    public function setActivitePhysique(?string $activitePhysique): static
    {
        $this->activitePhysique = $activitePhysique;

        return $this;
    }

    public function getDeveloppementPersonnel(): ?string
    {
        return $this->developpementPersonnel;
    }

    public function setDeveloppementPersonnel(?string $developpementPersonnel): static
    {
        $this->developpementPersonnel = $developpementPersonnel;

        return $this;
    }

    public function getRecommandations(): ?string
    {
        return $this->recommandations;
    }

    public function setRecommandations(?string $recommandations): static
    {
        $this->recommandations = $recommandations;

        return $this;
    }

    public function getDateSuivi(): ?\DateTime
    {
        return $this->dateSuivi;
    }

    public function setDateSuivi(\DateTime $dateSuivi): static
    {
        $this->dateSuivi = $dateSuivi;

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
