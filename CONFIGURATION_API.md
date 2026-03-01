# 🚀 Configuration des API - Guide Rapide

## 📋 Étape 1 : Obtenir vos clés API

### 🍽️ API Nutrition (Edamam - OPTIONNEL)
**Lien direct** : https://developer.edamam.com/

1. Allez sur https://developer.edamam.com/
2. Cliquez sur **"Get Started"**
3. Créez un compte gratuit
4. Cliquez sur **"Create New Application"**
5. Choisissez **"Nutrition Analysis"**
6. Remplissez le formulaire et validez
7. **Notez vos clés** :
   - `Application ID` : _________________________
   - `Application Key` : _________________________

### ⌚ API Fitbit (OBLIGATOIRE pour synchronisation)
**Lien direct** : https://dev.fitbit.com/

1. Allez sur https://dev.fitbit.com/
2. Cliquez sur **"Sign Up"** (créez un compte)
3. Cliquez sur **"Register an App"**
4. Remplissez :
   - **Application Name**: `GrowMind Santé`
   - **Description**: `Application de suivi santé et bien-être`
   - **Application Website**: `http://localhost:8080`
   - **Organization**: `Personnel`
   - **Technical Contact**: votre email
5. **OAuth 2.0 Application Type**: `Personal`
6. **Callback URL**: `http://localhost:8080/callback`
7. **Default Access Type**: `Read-Only`
8. **Scopes à cocher** : `activity nutrition profile settings sleep social weight`
9. **Notez vos clés** :
   - `Client ID` : _________________________
   - `Client Secret` : _________________________

---

## ⚙️ Étape 2 : Configurer les clés dans l'application

### Ouvrez le fichier de configuration :
**Chemin** : `src/main/resources/api-config.properties`

### Remplacez les valeurs par vos clés :

```properties
# API Edamam Nutrition (si vous en avez)
EDAMAM_APP_ID=VOTRE_VRAI_APP_ID_ICI
EDAMAM_APP_KEY=VOTRE_VRAI_APP_KEY_ICI

# API Fitbit (obligatoire)
FITBIT_CLIENT_ID=VOTRE_VRAI_CLIENT_ID_ICI
FITBIT_CLIENT_SECRET=VOTRE_VRAI_CLIENT_SECRET_ICI
FITBIT_REDIRECT_URI=http://localhost:8080/callback
```

**Exemple avec de vraies clés :**
```properties
EDAMAM_APP_ID=abcd1234
EDAMAM_APP_KEY=efgh5678
FITBIT_CLIENT_ID=ijkl9012
FITBIT_CLIENT_SECRET=mnop3456
```

---

## 🔄 Étape 3 : Redémarrer l'application

1. Arrêtez l'application si elle est en cours
2. Relancez avec : `mvn javafx:run`
3. Vérifiez la console - vous devriez voir :
   ```
   [CONFIG] API configuration loaded successfully
   [NUTRITION] Service initialized with Edamam: Configured
   [FITBIT] Service initialized with Client ID: Configured
   ```

---

## 🎯 Étape 4 : Tester les API

### 🍽️ Test Nutrition :
1. Cliquez sur **"🤖 Dashboard IA (API)"** (bouton violet)
2. Dans **"🍽️ Analyse Nutritionnelle"** :
   - Tapez : `sandwich poulet`
   - Cliquez sur **"🔍 Rechercher"**
3. Vous devriez voir les informations nutritionnelles apparaître

### ⌚ Test Fitbit :
1. Dans la même fenêtre, trouvez **"⌚ Intégration Fitbit"**
2. Cliquez sur **"🔌 Connexion Fitbit"**
3. Suivez les instructions pour autoriser l'application
4. Une fois connecté, vos pas devraient s'afficher

---

## 🚨 Dépannage

### **"Service not configured" dans la console**
→ Vérifiez que vous avez bien rempli le fichier `api-config.properties`

### **Erreur de connexion Fitbit**
→ Vérifiez que le Callback URL dans Fitbit est exactement : `http://localhost:8080/callback`

### **API Nutrition ne fonctionne pas**
→ Open Food Facts fonctionne sans clé (gratuit)
→ Edamam nécessite une clé pour de meilleurs résultats

### **Le bouton violet n'apparaît pas**
→ Relancez l'application avec `mvn javafx:run`

---

## 🎉 Résultat final

Une fois configuré, vous aurez :
- ✅ **Analyse nutritionnelle** instantanée
- ✅ **Synchronisation Fitbit** automatique
- ✅ **Dashboard IA** avec vraies données
- ✅ **Conseils santé** personnalisés

**Les API sont prêtes !** 🚀
