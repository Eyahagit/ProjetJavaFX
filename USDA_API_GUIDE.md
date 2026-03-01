# 🇺🇸 USDA FoodData Central - API 100% GRATUITE

## 🎯 **Pourquoi choisir USDA ?**

- **Base de données officielle** du gouvernement américain
- **100% gratuite** - aucune limite de requêtes
- **Plus de 400,000 aliments** référencés
- **Données nutritionnelles** complètes et fiables
- **Aucune carte de crédit** requise

---

## 🔑 **Obtenir votre clé API (2 minutes)**

### Étape 1 : Créer un compte
1. **Allez sur** : https://fdc.nal.usda.gov/api-key-signup
2. **Remplissez le formulaire** :
   - **First Name** : votre prénom
   - **Last Name** : votre nom
   - **Email** : votre email
   - **Organization** : "Personnel" (ou votre école/entreprise)
   - **Purpose** : "Application de suivi santé personnel"
3. **Cochez** "I'm not a robot"
4. **Cliquez sur "Submit"**

### Étape 2 : Recevoir votre clé
- **Vérifiez votre email** immédiatement
- **Votre clé API** sera dans l'email reçu
- **Format** : chaine de caractères (ex: `ABCD1234EFGH5678`)

---

## ⚙️ **Configurer dans l'application**

### 1. Ouvrez le fichier de configuration
**Chemin** : `src/main/resources/api-config.properties`

### 2. Remplacez la clé USDA
```properties
# Avant
USDA_API_KEY=VOTRE_USDA_API_KEY_ICI

# Après (avec votre vraie clé)
USDA_API_KEY=ABCD1234EFGH5678
```

### 3. Sauvegardez le fichier

---

## 🚀 **Tester l'API**

1. **Redémarrez l'application** avec `mvn javafx:run`
2. **Cliquez sur "🤖 Dashboard IA (API)"** (bouton violet)
3. **Testez avec** :
   - "apple" (pomme)
   - "banana" (banane)
   - "chicken breast" (poulet)
4. **Vérifiez la source** : "USDA FoodData Central"

---

## 📊 **Avantages de USDA**

| Caractéristique | USDA | Autres API |
|-----------------|------|------------|
| **Coût** | 🆓 100% GRATUIT | 💰 Limité/Payant |
| **Base de données** | 400,000+ aliments | Variable |
| **Fiabilité** | ⭐⭐⭐⭐⭐ Officiel | ⭐⭐⭐ Variable |
| **Limite requêtes** | 🚀 Aucune | 📊 Limitée |
| **Carte de crédit** | ❌ Non requise | ✅ Souvent requise |

---

## 🎯 **Exemples de recherche**

### ✅ **Aliments qui fonctionnent bien :**
- "apple"
- "banana" 
- "rice"
- "chicken"
- "bread"
- "milk"
- "cheese"

### 🌍 **Note importante :**
USDA utilise principalement des **noms en anglais**. Pour les aliments français, essayez d'abord avec Open Food Facts (déjà intégré).

---

## 🔧 **Dépannage**

### **"API Key not found"**
→ Vérifiez que vous avez bien remplacé `VOTRE_USDA_API_KEY_ICI` dans le fichier de configuration

### **"No results found"**
→ Essayez avec des noms simples en anglais
→ Open Food Facts fonctionnera pour les aliments français

### **"Rate limit exceeded"**
→ USDA n'a PAS de limite - vous ne devriez jamais voir cette erreur

---

## 🎉 **Résultat final**

Une fois configuré, vous aurez :
- ✅ **Accès illimité** à la base de données USDA
- ✅ **Informations nutritionnelles** complètes
- ✅ **Fiabilité officielle** gouvernementale
- ✅ **Aucun coût** maintenant et jamais

**USDA est la meilleure option gratuite pour des données nutritionnelles fiables !** 🇺🇸🚀
