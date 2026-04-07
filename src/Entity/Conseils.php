<?php

namespace App\Entity;

use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;

/**
 * Conseils
 *
 * @ORM\Table(name="conseils")
 * @ORM\Entity
 */
class Conseils
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
     * @ORM\Column(name="type_etat", type="string", length=50, nullable=false)
     */
    private $typeEtat;

    /**
     * @var int|null
     *
     * @ORM\Column(name="niveau", type="integer", nullable=true, options={"comment"="Niveau de gravité ou catégorie"})
     */
    private $niveau;

    /**
     * @var string
     *
     * @ORM\Column(name="conseil", type="text", length=65535, nullable=false)
     */
    private $conseil;

    /**
     * @var string|null
     *
     * @ORM\Column(name="categorie", type="string", length=50, nullable=true, options={"comment"="santé mentale, physique, nutrition, etc."})
     */
    private $categorie;

    /**
     * @var Collection<int, Utilisateur>
     *
     * @ORM\ManyToMany(targetEntity="Utilisateur", inversedBy="conseils")
     * @ORM\JoinTable(name="conseils_utilisateurs",
     *     joinColumns={@ORM\JoinColumn(name="conseil_id", referencedColumnName="id")},
     *     inverseJoinColumns={@ORM\JoinColumn(name="utilisateur_id", referencedColumnName="id")}
     * )
     */
    private Collection $utilisateurs;

    public function __construct()
    {
        $this->utilisateurs = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getTypeEtat(): ?string
    {
        return $this->typeEtat;
    }

    public function setTypeEtat(string $typeEtat): static
    {
        $this->typeEtat = $typeEtat;

        return $this;
    }

    public function getNiveau(): ?int
    {
        return $this->niveau;
    }

    public function setNiveau(?int $niveau): static
    {
        $this->niveau = $niveau;

        return $this;
    }

    public function getConseil(): ?string
    {
        return $this->conseil;
    }

    public function setConseil(string $conseil): static
    {
        $this->conseil = $conseil;

        return $this;
    }

    public function getCategorie(): ?string
    {
        return $this->categorie;
    }

    public function setCategorie(?string $categorie): static
    {
        $this->categorie = $categorie;

        return $this;
    }

    /**
     * @return Collection<int, Utilisateur>
     */
    public function getUtilisateurs(): Collection
    {
        return $this->utilisateurs;
    }

    public function addUtilisateur(Utilisateur $utilisateur): static
    {
        if (! $this->utilisateurs->contains($utilisateur)) {
            $this->utilisateurs->add($utilisateur);
            $utilisateur->addConseil($this);
        }

        return $this;
    }

    public function removeUtilisateur(Utilisateur $utilisateur): static
    {
        if ($this->utilisateurs->removeElement($utilisateur)) {
            $utilisateur->removeConseil($this);
        }

        return $this;
    }

}
