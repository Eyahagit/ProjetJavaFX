<?php

namespace App\Form;

use App\Entity\SanteBienEtre;
use App\Entity\Utilisateur;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\OptionsResolver\OptionsResolver;

class SanteBienEtreType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('humeur', TextType::class, [
                'required' => true,
                'attr' => [
                    'required' => true,
                    'maxlength' => 50,
                ],
            ])
            ->add('niveauStress', IntegerType::class, [
                'required' => true,
                'attr' => [
                    'required' => true,
                    'min' => 1,
                    'max' => 10,
                ],
            ])
            ->add('qualiteSommeil', IntegerType::class, [
                'required' => true,
                'attr' => [
                    'required' => true,
                    'min' => 1,
                    'max' => 10,
                ],
            ])
            ->add('nutrition')
            ->add('activitePhysique')
            ->add('developpementPersonnel')
            ->add('recommandations')
            ->add('dateSuivi', DateType::class, [
                'widget' => 'single_text',
                'required' => true,
                'html5' => true,
                'attr' => [
                    'required' => true,
                ],
            ])
            ->add('dateCreation')
            ->add('user', EntityType::class, [
                'class' => Utilisateur::class,
                'choice_label' => 'id',
            ])
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => SanteBienEtre::class,
        ]);
    }
}
