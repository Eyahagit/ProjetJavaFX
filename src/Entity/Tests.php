<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;

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
     * @ORM\Column(name="type_test", type="string", length=50, nullable=false, options={"comment"="stress, anxiété, etc."})
     */
    private $typeTest;

    /**
     * @var int
     *
     * @ORM\Column(name="score", type="integer", nullable=false)
     */
    private $score;

    /**
     * @var \DateTime|null
     *
     * @ORM\Column(name="date_test", type="datetime", nullable=true, options={"default"="CURRENT_TIMESTAMP"})
     */
    private $dateTest = 'CURRENT_TIMESTAMP';

    /**
     * @var \Utilisateur
     *
     * @ORM\ManyToOne(targetEntity="Utilisateur", inversedBy="tests")
     * @ORM\JoinColumns({
     *   @ORM\JoinColumn(name="utilisateur_id", referencedColumnName="id")
     * })
     */
    private $utilisateur;

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
