# 🚀 Transfert des fichiers manquants - GrowMind Application

## 📋 Fichiers à transférer

Tu dois transférer ces 2 fichiers depuis ton PC actuel vers l'autre PC :

### 1. Contrôleur Sommeil
**Chemin source sur ton PC :**
```
c:\Users\iyed\OneDrive - ESPRIT\Desktop\ApplicationSanteBienEtre\ApplicationSanteBienEtre\src\main\java\com\santebienetre\controller\GestionSommeilController.java
```

**Destination sur l'autre PC :**
```
[chemin_projet_autre_PC]\src\main\java\com\santebienetre\controller\GestionSommeilController.java
```

### 2. Interface FXML Sommeil
**Chemin source sur ton PC :**
```
c:\Users\iyed\OneDrive - ESPRIT\Desktop\ApplicationSanteBienEtre\ApplicationSanteBienEtre\src\main\resources\fxml\GestionSommeil.fxml
```

**Destination sur l'autre PC :**
```
[chemin_projet_autre_PC]\src\main\resources\fxml\GestionSommeil.fxml
```

## 🎯 Étapes de transfert

### Étape 1 : Créer le ZIP
1. Sélectionne les 2 fichiers ci-dessus
2. Clic droit → Envoyer vers → Dossier compressé
3. Nomme le fichier : `growmind-sommeil-files.zip`

### Étape 2 : WeTransfer
1. Va sur https://wetransfer.com
2. Upload le fichier `growmind-sommeil-files.zip`
3. Envoie-le à l'autre PC

### Étape 3 : Installation sur l'autre PC
1. Télécharge et décompresse le ZIP
2. Place les fichiers dans les bons dossiers (voir chemins ci-dessus)
3. Ouvre le projet dans IntelliJ IDEA
4. Recharge le projet Maven (clic sur l'icône Maven Reload)

## ✅ Vérification après transfert

Dans IntelliJ sur l'autre PC, vérifie que tu as :
- ✅ `GestionSommeilController.java` dans le package `com.santebienetre.controller`
- ✅ `GestionSommeil.fxml` dans le dossier `resources/fxml`
- ✅ Pas d'erreurs dans les imports

## 🎮 Test final

1. Exécute le projet sur l'autre PC
2. Clique sur le bouton **"🌙 Sommeil"**
3. L'écran de gestion du sommeil devrait s'ouvrir

## 📝 Notes importantes

- Ces fichiers ajoutent la fonctionnalité complète de suivi du sommeil
- Le bouton "🌙 Sommeil" ne fonctionnait pas avant car ces fichiers manquaient
- Après le transfert, les deux projets seront identiques

---
**Créé le :** 15/02/2026
**Projet :** GrowMind - Santé & Bien-être
**Fichiers :** 2 fichiers (contrôleur + FXML)
