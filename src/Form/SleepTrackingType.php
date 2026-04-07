<?php

namespace App\Form;

use App\Entity\SleepTracking;
use App\Entity\Utilisateur;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class SleepTrackingType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('dateSommeil')
            ->add('heureCoucher')
            ->add('heureReveil')
            ->add('dureeMinutes')
            ->add('qualiteSommeil')
            ->add('commentaire')
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
            'data_class' => SleepTracking::class,
        ]);
    }
}
