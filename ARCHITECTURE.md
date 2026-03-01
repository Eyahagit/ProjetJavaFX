# Architecture MVC - Gestion Santé & Bien-être

## Vue d'ensemble

L'application respecte l'architecture MVC (Model-View-Controller) avec une couche DAO pour la persistance.

## Couches et interactions

### 1. Model (Modèle)
**Classe :** `SanteBienEtre.java`

- Représente l'entité métier
- Contient les attributs : id, userId, humeur, niveauStress, qualiteSommeil, nutrition, activitePhysique, developpementPersonnel, recommandations, dateSuivi
- Aucune dépendance vers la vue ou la base de données

### 2. View (Vue)
**Fichier :** `GestionSanteBienEtre.fxml`

- Définit l'interface utilisateur déclarativement
- Formulaire de saisie (TextField, ComboBox, Spinner, DatePicker)
- TableView pour afficher les enregistrements
- LineChart pour l'évolution du stress
- Boutons : Ajouter, Modifier, Supprimer, Effacer

### 3. Controller (Contrôleur)
**Classe :** `GestionSanteBienEtreController.java`

- Écoute les événements utilisateur (clics, sélections)
- Appelle le Service pour les opérations métier
- Met à jour l'ObservableList liée à la TableView
- Affiche les Alertes (succès/erreur)
- Remplit le formulaire lorsqu'une ligne est sélectionnée

**Flux :** View → Controller → Service → DAO → Model

### 4. Service (Logique métier)
**Classe :** `SanteBienEtreService.java`

- Valide les données avant persistance
- Génère les recommandations personnalisées selon :
  - Niveau de stress (>7 → yoga, respiration)
  - Qualité du sommeil (<5 → hygiène du sommeil)
  - Humeur (Stressé → relaxation)
  - Nutrition et activité physique
- Délègue la persistance au DAO

### 5. DAO (Accès aux données)
**Classe :** `SanteBienEtreDAO.java`

- Méthodes CRUD : add, update, delete, getAll, getByUser, getByDateRange
- Utilise JDBC PreparedStatement (protection contre les injections SQL)
- Conversion ResultSet → SanteBienEtre

## Schéma de flux des données

```
[Utilisateur] → [FXML View]
       ↓
[Controller] ← événements
       ↓
[Service] ← validation + recommandations
       ↓
[DAO] ← PreparedStatement
       ↓
[MySQL/MariaDB Database (grownmind:3307)]
       ↓
[ObservableList] → [TableView] (affichage)
```

## Fichiers SQL

- `schema.sql` : Version MySQL/générique
- `schema_h2.sql` : Version H2
- `DatabaseInit.java` : Création automatique des tables au démarrage
