# Application Santé & Bien-être

Application de bureau JavaFX pour le suivi de la santé et du bien-être personnel.

## Module : Gestion Santé & Bien-être

### Fonctionnalités
- **Suivi quotidien de l'humeur** : Très bien, Bien, Moyen, Stressé, Fatigué
- **Test de stress/anxiété** : Niveau 1–10
- **Recommandations personnalisées** : Sport, yoga, nutrition, sommeil, développement personnel
- **CRUD complet** : Ajout, modification, suppression, consultation
- **Filtrage par date** : Afficher les enregistrements sur une plage de dates
- **Graphique d'évolution du stress** : Visualisation de l'évolution du niveau de stress

### Architecture MVC

```
┌─────────────────────────────────────────────────────────────────┐
│  View (FXML)                                                    │
│  GestionSanteBienEtre.fxml                                      │
│  - Formulaires, TableView, LineChart                            │
└───────────────────────┬─────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────────┐
│  Controller                                                     │
│  GestionSanteBienEtreController.java                            │
│  - Gère les événements utilisateur                              │
│  - Met à jour la vue (ObservableList)                           │
└───────────────────────┬─────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────────┐
│  Service                                                        │
│  SanteBienEtreService.java                                      │
│  - Validation des données                                       │
│  - Génération des recommandations personnalisées                │
└───────────────────────┬─────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────────┐
│  DAO                                                            │
│  SanteBienEtreDAO.java                                          │
│  - Accès base de données (JDBC PreparedStatement)               │
└───────────────────────┬─────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────────┐
│  Model                                                          │
│  SanteBienEtre.java                                             │
│  - Entité métier                                                │
└─────────────────────────────────────────────────────────────────┘
```

### Prérequis
- Java 17+
- Maven 3.6+
- JavaFX 21

### Compilation et exécution

```bash
cd ApplicationSanteBienEtre
mvn clean compile
mvn javafx:run
```

Ou avec exec plugin :
```bash
mvn exec:java
```

### Base de données
- MariaDB/MySQL : base `grownmind` sur `127.0.0.1:3307`
- Config par défaut dans `src/main/java/com/santebienetre/util/DatabaseConnection.java` (surcharge possible via `-Ddb.host`, `-Ddb.port`, `-Ddb.name`, `-Ddb.user`, `-Ddb.password`)
- Tables utilisées : `utilisateur`, `sante_bien_etre`, `sleep_tracking` (créées au démarrage si absentes)
- Utilisateur démo : ID 1 ("Utilisateur Demo") si absent
- Test rapide : lancer `com.santebienetre.QueryDB` et vérifier `DATABASE() = grownmind`

### Structure du projet

```
src/main/java/com/santebienetre/
├── App.java                    # Point d'entrée
├── model/
│   └── SanteBienEtre.java      # Entité
├── dao/
│   └── SanteBienEtreDAO.java   # Accès données
├── service/
│   └── SanteBienEtreService.java # Logique métier
├── controller/
│   └── GestionSanteBienEtreController.java
└── util/
    ├── DatabaseConnection.java
    └── DatabaseInit.java

src/main/resources/
├── fxml/
│   └── GestionSanteBienEtre.fxml
└── db/
    ├── schema.sql
    └── schema_h2.sql
```

### Règles de recommandations
- **Stress > 7** : Yoga, respiration, méditation
- **Sommeil < 5** : Hygiène du sommeil
- **Humeur "Stressé"** : Activités relaxantes
- **Nutrition vide/irrégulière** : Conseils nutritionnels
- **Peu d'activité** : Marche, Pilates
