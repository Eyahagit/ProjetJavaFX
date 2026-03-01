# Guide d'Intégration API - GrowMind Santé & Bien-être

## 🎉 Nouvelles Fonctionnalités API Intégrées

Votre application inclut maintenant deux puissantes intégrations API :

### 🍽️ 1. API Nutritionnelle
- **Source**: Open Food Facts API (gratuite) + Edamam Nutrition API (optionnelle)
- **Fonctionnalités**: 
  - Analyse nutritionnelle des aliments
  - Calories, protéines, glucides, lipides
  - Conseils santé personnalisés
  - Interface de recherche intuitive

### ⌚ 2. API Fitbit
- **Source**: Fitbit Web API (OAuth 2.0)
- **Fonctionnalités**:
  - Synchronisation des données d'activité
  - Nombre de pas en temps réel
  - Calories brûlées
  - Données de sommeil
  - Connexion sécurisée via OAuth

---

## 🚀 Configuration des API

### API Nutritionnelle (Optionnel)

Pour des résultats plus précis avec Edamam :

1. **Créer un compte Edamam** :
   - Allez sur [https://developer.edamam.com/](https://developer.edamam.com/)
   - Inscrivez-vous et créez une application

2. **Obtenir vos clés API** :
   - `Application ID`
   - `Application Key`

3. **Configurer dans le code** :
   ```java
   // Dans AIDashboardController.java ou NutritionApiService.java
   nutritionService.setEdamamCredentials("VOTRE_APP_ID", "VOTRE_APP_KEY");
   ```

> **Note**: L'application fonctionne déjà avec Open Food Facts sans configuration requise !

### API Fitbit (Optionnel)

Pour connecter votre compte Fitbit :

1. **Créer une application Fitbit** :
   - Allez sur [https://dev.fitbit.com/](https://dev.fitbit.com/)
   - Créez un compte développeur
   - Créez une nouvelle application

2. **Configurer l'application** :
   - `Client ID`
   - `Client Secret`
   - `Callback URL`: `http://localhost:8080/callback`
   - **Scopes requis**: `activity nutrition profile settings sleep social weight`

3. **Configurer dans le code** :
   ```java
   // Dans AIDashboardController.java ou FitbitApiService.java
   fitbitService.setCredentials("VOTRE_CLIENT_ID", "VOTRE_CLIENT_SECRET", "http://localhost:8080/callback");
   ```

---

## 📖 Utilisation

### 🍽️ Recherche Nutritionnelle

1. Dans le Dashboard IA Avancé, trouvez la section "🍽️ Analyse Nutritionnelle"
2. Entrez un aliment (ex: "sandwich poulet", "pomme", "pizza")
3. Cliquez sur "🔍 Rechercher"
4. Les résultats s'affichent avec :
   - Calories et valeurs nutritionnelles
   - Source des données
   - Conseils santé personnalisés

### ⌚ Connexion Fitbit

1. Dans le Dashboard IA Avancé, trouvez la section "⌚ Intégration Fitbit"
2. Cliquez sur "🔌 Connexion Fitbit"
3. Une page web s'ouvre pour autoriser l'application
4. Connectez-vous à votre compte Fitbit
5. Copiez le code d'autorisation depuis l'URL
6. Collez le code dans la boîte de dialogue
7. Vos données Fitbit s'affichent automatiquement !

---

## 🔧 Fonctionnalités Techniques

### Architecture des Services

```
┌─────────────────────────────────┐
│        AIDashboardController    │
├─────────────────────────────────┤
│  NutritionApiService            │
│  ├── Open Food Facts (gratuit)  │
│  └── Edamam (optionnel)         │
├─────────────────────────────────┤
│  FitbitApiService               │
│  ├── OAuth 2.0 Flow             │
│  ├── Token Management           │
│  └── Activity Data Sync         │
└─────────────────────────────────┘
```

### Gestion des Erreurs

- **Requêtes API**: Timeout de 10s connexion, 30s lecture
- **Réseaux**: Messages d'erreur clairs pour l'utilisateur
- **Tokens Fitbit**: Rafraîchissement automatique des tokens expirés
- **Fallback**: Utilisation de données par défaut si API indisponible

### Sécurité

- **Fitbit**: OAuth 2.0 avec tokens sécurisés
- **Clés API**: Configuration requise (pas de clés en dur)
- **HTTPS**: Toutes les communications API utilisent SSL/TLS

---

## 🎯 Exemples d'Utilisation

### Analyse Nutritionnelle

```
Input: "sandwich poulet"
Output:
🍽️ sandwich poulet
Source: Open Food Facts
🔥 Calories: 285 kcal
🥩 Protéines: 18.5g
🍞 Glucides: 32.2g
🥑 Lipides: 9.8g

💡 Conseils santé:
✅ Équilibre nutritionnel correct !
💪 Riche en protéines, excellent pour les muscles.
```

### Données Fitbit

```
Status: ✅ Connecté
Pas: 8,432
Calories brûlées: 2,150
Distance: 6.2 km
Sommeil: 7h 15min
```

---

## 🚨 Dépannage

### Problèmes Communs

**API Nutrition ne fonctionne pas** :
- Vérifiez votre connexion internet
- Open Food Facts fonctionne sans configuration
- Pour Edamam, vérifiez vos clés API

**Connexion Fitbit échoue** :
- Vérifiez vos identifiants développeur Fitbit
- Assurez-vous que le Callback URL est correct
- Vérifiez que les scopes sont bien configurés

**Tokens Fitbit expirés** :
- L'application gère automatiquement le rafraîchissement
- Si problème persistant, déconnectez/reconnectez

### Logs et Debug

Les messages de debug s'affichent dans la console :
```
[DEBUG] Loading dashboard data for user: 1
[DEBUG] Fetching nutrition data for: sandwich poulet
[DEBUG] Fitbit authentication successful
[DEBUG] Today's steps: 8432
```

---

## 📚 Ressources Utiles

### Documentation API
- [Open Food Facts API](https://world.openfoodfacts.org/api/v2)
- [Edamam Nutrition API](https://developer.edamam.com/edamam-docs-nutrition-api)
- [Fitbit Web API](https://dev.fitbit.com/build/reference/web-api/)

### Support
- Problèmes avec les clés API : Contactez les fournisseurs respectifs
- Bugs dans l'application : Vérifiez les logs console
- Questions techniques : Consultez la documentation du code

---

## 🎉 Conclusion

Votre application GrowMind est maintenant équipée d'intégrations API puissantes qui enrichissent l'expérience utilisateur :

- **Analyse nutritionnelle instantanée** pour des choix alimentaires éclairés
- **Synchronisation Fitbit** pour un suivi d'activité complet
- **Interface intuitive** intégrée au dashboard existant
- **Architecture robuste** avec gestion d'erreurs et sécurité

N'hésitez pas à explorer ces nouvelles fonctionnalités et à les configurer selon vos besoins !
