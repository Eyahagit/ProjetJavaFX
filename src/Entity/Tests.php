<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

/**
 * Tests
 *
 * @ORM\Table(name="tests", indexes={@ORM\Index(name="utilisateur_id", columns={"utilisateur_id"})})
 * @ORM\Entity
 */
class Tests
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
     * @ORM\Column(name="type_test", type="string", length=50, nullable=false)
     */
    #[Assert\NotBlank(message: "Le type de test est obligatoire")]
    #[Assert\Length(max: 50, maxMessage: "Maximum 50 caractères")]
    private $typeTest;

    /**
     * @var int
     *
     * @ORM\Column(name="score", type="integer", nullable=false)
     */
    #[Assert\NotBlank(message: "Le score est obligatoire")]
    #[Assert\Range(
        min: 0,
        max: 10,
        notInRangeMessage: "Le score doit être entre {{ min }} et {{ max }}"
    )]
    private $score;

    /**
     * @var \DateTime|null
     *
     * @ORM\Column(name="date_test", type="datetime", nullable=true)
     */
    #[Assert\NotNull(message: "La date est obligatoire")]
    #[Assert\LessThanOrEqual("now", message: "La date ne peut pas être dans le futur")]
    private $dateTest;

    /**
     * @var \Utilisateur
     *
     * @ORM\ManyToOne(targetEntity="Utilisateur", inversedBy="tests")
     * @ORM\JoinColumn(name="utilisateur_id", referencedColumnName="id", nullable=false)
     */
    #[Assert\NotNull(message: "L'utilisateur est obligatoire")]
    private $utilisateur;

    public function __construct()
    {
        $this->dateTest = new \DateTime();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getTypeTest(): ?string
    {
        return $this->typeTest;
    }

    public function setTypeTest(string $typeTest): static
    {
        $this->typeTest = $typeTest;
        return $this;
    }

    public function getScore(): ?int
    {
        return $this->score;
    }

    public function setScore(int $score): static
    {
        $this->score = $score;
        return $this;
    }

    public function getDateTest(): ?\DateTime
    {
        return $this->dateTest;
    }

    public function setDateTest(?\DateTime $dateTest): static
    {
        $this->dateTest = $dateTest;
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