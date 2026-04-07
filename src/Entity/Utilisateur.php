<?php

namespace App\Entity;

use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;

/**
 * Utilisateur
 *
 * @ORM\Table(name="utilisateur", uniqueConstraints={@ORM\UniqueConstraint(name="email", columns={"email"})})
 * @ORM\Entity
 */
class Utilisateur
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
     * @ORM\Column(name="nom", type="string", length=100, nullable=false)
     */
    private $nom;

    /**
     * @var string
     *
     * @ORM\Column(name="email", type="string", length=255, nullable=false)
     */
    private $email;

    /**
     * @var Collection<int, SanteBienEtre>
     *
     * @ORM\OneToMany(targetEntity="SanteBienEtre", mappedBy="user")
     */
    private Collection $santeBienEtres;

    /**
     * @var Collection<int, SleepTracking>
     *
     * @ORM\OneToMany(targetEntity="SleepTracking", mappedBy="user")
     */
    private Collection $sleepTrackings;

    /**
     * @var Collection<int, Humeurs>
     *
     * @ORM\OneToMany(targetEntity="Humeurs", mappedBy="utilisateur")
     */
    private Collection $humeurs;

    /**
     * @var Collection<int, Tests>
     *
     * @ORM\OneToMany(targetEntity="Tests", mappedBy="utilisateur")
     */
    private Collection $tests;

    /**
     * @var Collection<int, Conseils>
     *
     * @ORM\ManyToMany(targetEntity="Conseils", mappedBy="utilisateurs")
     */
    private Collection $conseils;

    public function __construct()
    {
        $this->santeBienEtres = new ArrayCollection();
        $this->sleepTrackings = new ArrayCollection();
        $this->humeurs = new ArrayCollection();
        $this->tests = new ArrayCollection();
        $this->conseils = new ArrayCollection();
    }

    public function __toString(): string
    {
        return $this->nom ?: ($this->email ?: (string) $this->id);
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getNom(): ?string
    {
        return $this->nom;
    }

    public function setNom(string $nom): static
    {
        $this->nom = $nom;

        return $this;
    }

    public function getEmail(): ?string
    {
        return $this->email;
    }

    public function setEmail(string $email): static
    {
        $this->email = $email;

        return $this;
    }

    /**
     * @return Collection<int, SanteBienEtre>
     */
    public function getSanteBienEtres(): Collection
    {
        return $this->santeBienEtres;
    }

    /**
     * @return Collection<int, SleepTracking>
     */
    public function getSleepTrackings(): Collection
    {
        return $this->sleepTrackings;
    }

    /**
     * @return Collection<int, Humeurs>
     */
    public function getHumeurs(): Collection
    {
        return $this->humeurs;
    }

    /**
     * @return Collection<int, Tests>
     */
    public function getTests(): Collection
    {
        return $this->tests;
    }

    /**
     * @return Collection<int, Conseils>
     */
    public function getConseils(): Collection
    {
        return $this->conseils;
    }

    public function addConseil(Conseils $conseil): static
    {
        if (! $this->conseils->contains($conseil)) {
            $this->conseils->add($conseil);
            $conseil->addUtilisateur($this);
        }

        return $this;
    }

    public function removeConseil(Conseils $conseil): static
    {
        if ($this->conseils->removeElement($conseil)) {
            $conseil->removeUtilisateur($this);
        }

        return $this;
    }

}
