# 🏃‍♂️ API Strava - Alternative GRATUITE à Fitbit

## 🎯 **Pourquoi choisir Strava ?**

- **🆓 100% GRATUIT** pour les développeurs
- **📱 Application populaire** de suivi sportif
- **🚀 30,000 requêtes/jour** (plus que suffisant)
- **🔧 OAuth simple** - beaucoup plus facile que Fitbit
- **⚡ Configuration rapide** - 5 minutes maximum

---

## 🔑 **Obtenir vos clés API Strava (3 minutes)**

### Étape 1 : Créer un compte Strava
1. **Allez sur** : https://www.strava.com/register
2. **Créez un compte** (gratuit)
3. **Validez votre email**

### Étape 2 : Créer une application API
1. **Allez sur** : https://developers.strava.com/
2. **Cliquez sur "Get Started"**
3. **Connectez-vous** avec votre compte Strava
4. **Cliquez sur "Create API App"**
5. **Remplissez le formulaire** :
   - **Application Name**: `GrowMind Santé`
   - **Category**: `Health & Fitness`
   - **Website**: `http://localhost:8080`
   - **Authorization Callback Domain**: `localhost`
   - **Description**: `Application de suivi santé et bien-être`
6. **Acceptez les termes** et cliquez sur "Create"

### Étape 3 : Notez vos clés
Votre application sera créée avec :
- **Client ID** : (ex: `123456`)
- **Client Secret** : (ex: `abcd1234efgh5678`)

---

## ⚙️ **Configurer dans l'application**

### 1. Ouvrez le fichier de configuration
**Chemin** : `src/main/resources/api-config.properties`

### 2. Remplacez les clés Strava
```properties
# Avant
STRAVA_CLIENT_ID=VOTRE_STRAVA_CLIENT_ID_ICI
STRAVA_CLIENT_SECRET=VOTRE_STRAVA_CLIENT_SECRET_ICI

# Après (avec vos vraies clés)
STRAVA_CLIENT_ID=123456
STRAVA_CLIENT_SECRET=abcd1234efgh5678
```

### 3. Sauvegardez le fichier

---

## 🚀 **Utiliser Strava dans l'application**

### 1. Redémarrez l'application
```bash
mvn javafx:run
```

### 2. Accédez au Dashboard IA
- Cliquez sur **"🤖 Dashboard IA (API)"** (bouton violet)

### 3. Connectez Strava
- Dans la section **"⌚ Intégration Strava"**
- Cliquez sur **"🔌 Connexion Strava"**
- Autorisez l'application sur Strava
- Vos activités apparaîtront automatiquement !

---

## 📊 **Ce que Strava peut faire**

### ✅ **Fonctionnalités disponibles :**
- **🏃‍♂️ Dernière activité** (course, vélo, natation...)
- **📏 Distance** parcourue
- **⏱️ Durée** de l'activité
- **📝 Type d'activité** (running, cycling, swimming...)
- **🔄 Synchronisation** automatique

### 📈 **Informations affichées :**
```
🏃‍♂️ Dernière activité Strava:
📝 Course du matin
🏃‍♂️ Type: Running
📏 Distance: 5.2 km
⏱️ Durée: 32 min
✅ Connecté à Strava
```

---

## 🆚 **Strava vs Fitbit**

| Caractéristique | Strava | Fitbit |
|-----------------|--------|--------|
| **Coût** | 🆓 100% GRATUIT | 💰 Limité |
| **Configuration** | ⚡ 5 minutes | 🐌 30+ minutes |
| **OAuth** | ✅ Simple | ❌ Complexe |
| **Limite requêtes** | 🚀 30,000/jour | 📊 Limité |
| **Popularité** | ⭐⭐⭐⭐⭐ Sportifs | ⭐⭐⭐ Général |

---

## 🔧 **Dépannage**

### **"Client ID not found"**
→ Vérifiez que vous avez bien remplacé les clés dans `api-config.properties`

### **"Callback URL mismatch"**
→ Assurez-vous d'avoir mis `localhost` comme domaine de callback

### **"No activities found"**
→ Vous devez avoir au moins une activité enregistrée sur Strava
→ Faites une petite course ou enregistrez une activité manuellement

---

## 🎉 **Avantages de Strava**

- ✅ **Configuration ultra-rapide**
- ✅ **API gratuite et généreuse**
- ✅ **Communauté sportive active**
- ✅ **Application mobile excellente**
- ✅ **Compatible avec tous les sports**

**Strava est la meilleure alternative gratuite à Fitbit - plus simple et plus rapide à configurer !** 🏃‍♂️🚀

---

## 🎯 **Prochaines étapes**

1. **Obtenez vos clés** sur https://developers.strava.com/
2. **Configurez-les** dans `api-config.properties`
3. **Redémarrez** l'application
4. **Connectez Strava** en 2 clics
5. **Profitez** du suivi sportif automatique !

**Strava transformera votre application en véritable coach sportif !** 💪
