package services;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class ServiceGemini {

    private Map<String, Reponse[]> connaissances;
    private Map<String, String> contexteUtilisateur;
    private Random random;

    public ServiceGemini() {
        this.random = new Random();
        this.connaissances = new HashMap<>();
        this.contexteUtilisateur = new HashMap<>();
        initialiserConnaissances();
        System.out.println("✅ Assistant IA initialisé avec base de connaissances enrichie");
    }

    private void initialiserConnaissances() {

        // ===== ANXIÉTÉ =====
        ajouterConnaissances("anxiété", new Reponse[]{
                new Reponse("anxiété", "général",
                        "🌿 **Gérer l'anxiété au quotidien**\n\n" +
                                "L'anxiété est une réaction normale, mais elle peut devenir envahissante. Voici des techniques efficaces :\n\n" +
                                "1️⃣ **Respiration 4-7-8** : Inspirez par le nez (4s), retenez (7s), expirez par la bouche (8s). Répétez 3 fois.\n" +
                                "2️⃣ **Ancrage 5-4-3-2-1** : Nommez 5 choses que vous voyez, 4 que vous touchez, 3 que vous entendez, 2 que vous sentez, 1 que vous goûtez.\n" +
                                "3️⃣ **Acceptation** : Dites-vous \"C'est désagréable mais temporaire. Ça va passer.\"\n" +
                                "4️⃣ **Bouger** : Marchez 5 minutes, ça libère des endorphines.\n\n" +
                                "📞 Si l'anxiété persiste, consultez un professionnel ou appelez le 3114 (gratuit, 24h/24)."),

                new Reponse("anxiété", "crise",
                        "🆘 **En cas de crise d'angoisse**\n\n" +
                                "1. **Asseyez-vous** et posez vos pieds au sol.\n" +
                                "2. **Respirez lentement** : inspirez 4s, expirez 6s.\n" +
                                "3. **Rappelez-vous** : c'est une décharge d'adrénaline, pas une urgence médicale.\n" +
                                "4. **Tenez un objet** froid pour vous ancrer.\n" +
                                "5. **Mastiquez** un chewing-gum (trompe le cerveau).\n\n" +
                                "La crise passe en 5-20 minutes. Vous n'êtes pas en danger."),

                new Reponse("anxiété", "prévention",
                        "🌱 **Prévenir l'anxiété**\n\n" +
                                "• Méditation quotidienne (10 min)\n" +
                                "• Réduire caféine et alcool\n" +
                                "• Exercice physique régulier\n" +
                                "• Routine de sommeil stable\n" +
                                "• Tenir un journal d'anxiété pour identifier les déclencheurs\n" +
                                "• Limiter les actualités stressantes")
        });

        // ===== STRESS =====
        ajouterConnaissances("stress", new Reponse[]{
                new Reponse("stress", "général",
                        "⚡ **Gérer le stress professionnel**\n\n" +
                                "1️⃣ **Pomodoro** : 25 min travail, 5 min pause.\n" +
                                "2️⃣ **Micro-pause** : 2 minutes de respiration toutes les heures.\n" +
                                "3️⃣ **Déconnexion** : Pas d'emails après 20h.\n" +
                                "4️⃣ **Priorisation** : Matrice d'Eisenhower (urgent/important).\n" +
                                "5️⃣ **Déléguer** : Vous n'êtes pas obligé de tout faire seul.\n\n" +
                                "🧠 Le stress chronique fatigue le cerveau. Accordez-vous des vrais repos."),

                new Reponse("stress", "technique",
                        "💆 **Technique anti-stress express**\n\n" +
                                "• **Respiration cohérente** : 5 secondes inspiration, 5 secondes expiration, pendant 5 minutes.\n" +
                                "• **Étirements** : Levez les bras, penchez-vous, tournez la tête.\n" +
                                "• **Hydratation** : Un verre d'eau fraîche.\n" +
                                "• **Changement de décor** : Sortez 2 minutes.\n" +
                                "• **Auto-massage** : Massez vos tempes et votre nuque."),

                new Reponse("stress", "burnout",
                        "⚠️ **Prévenir l'épuisement professionnel**\n\n" +
                                "Signes avant-coureurs : fatigue constante, cynisme, baisse de performance.\n\n" +
                                "**Actions** :\n" +
                                "• Définissez des horaires et respectez-les\n" +
                                "• Apprenez à dire non\n" +
                                "• Prenez vos pauses (vraiment)\n" +
                                "• Parlez à votre manager\n" +
                                "• Consultez un médecin si nécessaire\n\n" +
                                "🎯 La santé passe avant le travail.")
        });

        // ===== SOMMEIL =====
        ajouterConnaissances("sommeil", new Reponse[]{
                new Reponse("sommeil", "hygiène",
                        "😴 **Améliorer son sommeil**\n\n" +
                                "**Routine du soir** :\n" +
                                "• 21h : Dernière collation légère\n" +
                                "• 22h : Pas d'écrans (lumière bleue)\n" +
                                "• 22h30 : Douche tiède + tisane (camomille, verveine)\n" +
                                "• 23h : Lecture calme (papier, pas d'écran)\n\n" +
                                "**La chambre** : 18°C, noir complet, calme.\n" +
                                "**À éviter** : Café après 16h, sport intense le soir, dîner copieux."),

                new Reponse("sommeil", "insomnie",
                        "🌙 **En cas d'insomnie**\n\n" +
                                "Si vous ne dormez pas après 20 minutes :\n" +
                                "• Levez-vous, allez dans une autre pièce\n" +
                                "• Lisez un livre (pas d'écran)\n" +
                                "• Écoutez de la musique douce ou des bruits blancs\n" +
                                "• Respirez profondément (4-7-8)\n" +
                                "• Écrivez vos pensées sur papier\n\n" +
                                "Ne restez pas au lit à ruminer, ça aggrave l'insomnie."),

                new Reponse("sommeil", "reve",
                        "💭 **Comprendre ses rêves**\n\n" +
                                "Les rêves sont le moyen pour le cerveau de traiter les émotions.\n" +
                                "• Tenez un journal de rêves au réveil\n" +
                                "• Les cauchemars fréquents peuvent indiquer un stress\n" +
                                "• Parlez de vos rêves, ça aide à les comprendre\n" +
                                "• Si les cauchemars persistent, consultez un professionnel")
        });

        // ===== DÉPRESSION =====
        ajouterConnaissances("dépression", new Reponse[]{
                new Reponse("dépression", "général",
                        "💙 **Comprendre la dépression**\n\n" +
                                "La dépression est une maladie, pas une faiblesse. Symptômes :\n" +
                                "• Tristesse persistante\n" +
                                "• Perte d'intérêt\n" +
                                "• Fatigue intense\n" +
                                "• Troubles du sommeil\n" +
                                "• Pensées négatives\n\n" +
                                "**Que faire ?**\n" +
                                "1. Consultez un médecin généraliste\n" +
                                "2. Parlez à vos proches\n" +
                                "3. Gardez une routine simple\n" +
                                "4. Appelez le 3114 si besoin\n\n" +
                                "🌱 La guérison est possible avec un accompagnement adapté."),

                new Reponse("dépression", "soutien",
                        "🤝 **Soutenir un proche déprimé**\n\n" +
                                "• Écoutez sans juger\n" +
                                "• Proposez votre aide concrète (courses, ménage)\n" +
                                "• Encouragez doucement sans forcer\n" +
                                "• Rappelez-lui que ce n'est pas sa faute\n" +
                                "• Prenez aussi soin de vous\n\n" +
                                "📞 Informez-vous sur les ressources d'aide disponibles.")
        });

        // ===== RELATIONS =====
        ajouterConnaissances("relation", new Reponse[]{
                new Reponse("relation", "couple",
                        "💑 **Améliorer sa relation de couple**\n\n" +
                                "1️⃣ **Communication non-violente** : Exprimez vos sentiments sans accuser (\"Je me sens... quand...\" plutôt que \"Tu es...\").\n" +
                                "2️⃣ **Temps de qualité** : 30 minutes par jour sans téléphone, juste pour discuter.\n" +
                                "3️⃣ **Rituels** : Un dîner aux chandelles par semaine, un café le matin ensemble.\n" +
                                "4️⃣ **Écoute active** : Reformulez ce que l'autre dit pour montrer que vous comprenez.\n" +
                                "5️⃣ **Compromis** : Trouvez des solutions qui conviennent aux deux.\n\n" +
                                "💡 Les conflits sont normaux, l'important est de les résoudre ensemble."),

                new Reponse("relation", "familial",
                        "👨‍👩‍👧 **Gérer les relations familiales**\n\n" +
                                "• Fixez des limites claires et respectueuses\n" +
                                "• Acceptez que vous ne pouvez pas changer les autres\n" +
                                "• Pratiquez le pardon (pour vous, pas pour eux)\n" +
                                "• Créez de nouveaux rituels positifs\n" +
                                "• Évitez les sujets qui fâchent si nécessaire\n\n" +
                                "🤝 Si c'est toxique, prendre ses distances peut être nécessaire."),

                new Reponse("relation", "amitie",
                        "👫 **Cultiver ses amitiés**\n\n" +
                                "• Prenez des nouvelles régulièrement\n" +
                                "• Proposez des activités simples (café, balade)\n" +
                                "• Soyez présent dans les moments difficiles\n" +
                                "• Acceptez que les amitiés évoluent\n" +
                                "• Ne vous forcez pas à garder des relations toxiques")
        });

        // ===== TRAVAIL =====
        ajouterConnaissances("travail", new Reponse[]{
                new Reponse("travail", "motivation",
                        "🔥 **Retrouver la motivation au travail**\n\n" +
                                "1. **Pourquoi ?** Rappelez-vous le sens de votre travail.\n" +
                                "2. **Petits pas** : Divisez les gros projets en tâches de 25 min.\n" +
                                "3. **Célébrez** : Chaque tâche finie mérite une reconnaissance.\n" +
                                "4. **Variez** : Alternez les types de tâches.\n" +
                                "5. **Récompenses** : Offrez-vous une pause après un effort.\n\n" +
                                "🌱 La motivation vient souvent après avoir commencé, pas avant."),

                new Reponse("travail", "conflit",
                        "🤔 **Gérer un conflit au travail**\n\n" +
                                "• Restez professionnel, pas personnel\n" +
                                "• Écoutez le point de vue de l'autre\n" +
                                "• Cherchez un terrain d'entente\n" +
                                "• Impliquez un tiers si nécessaire (RH, manager)\n" +
                                "• Documentez les faits objectifs\n\n" +
                                "⚖️ L'objectif est de trouver une solution, pas d'avoir raison.")
        });

        // ===== DEUIL =====
        ajouterConnaissances("deuil", new Reponse[]{
                new Reponse("deuil", "processus",
                        "🕊️ **Comprendre le deuil**\n\n" +
                                "Le deuil n'est pas linéaire. Les étapes (déni, colère, marchandage, tristesse, acceptation) peuvent revenir.\n\n" +
                                "**Conseils** :\n" +
                                "• Accordez-vous le temps nécessaire\n" +
                                "• Exprimez vos émotions (pleurer, écrire, parler)\n" +
                                "• Créez un rituel pour honorer la mémoire\n" +
                                "• Acceptez l'aide des proches\n" +
                                "• Rejoignez un groupe de parole\n\n" +
                                "📞 Si le deuil est trop lourd, parlez à un professionnel."),

                new Reponse("deuil", "enfant",
                        "👶 **Aider un enfant face au deuil**\n\n" +
                                "• Parlez-lui avec des mots simples et honnêtes\n" +
                                "• Rassurez-le sur sa propre sécurité\n" +
                                "• Laissez-le exprimer ses émotions\n" +
                                "• Maintenez les routines rassurantes\n" +
                                "• Lisez des livres adaptés sur le sujet\n\n" +
                                "💚 Les enfants comprennent et ressentent, ne les excluez pas.")
        });

        // ===== ALIMENTATION =====
        ajouterConnaissances("alimentation", new Reponse[]{
                new Reponse("alimentation", "equilibre",
                        "🍎 **Bien manger pour le moral**\n\n" +
                                "**Aliments qui boostent le moral** :\n" +
                                "• Oméga-3 : poissons gras, noix, graines de lin\n" +
                                "• Magnésium : chocolat noir (70%), légumes verts\n" +
                                "• Vitamine D : soleil, poissons gras, œufs\n" +
                                "• Probiotiques : yaourt, kéfir, choucroute\n" +
                                "• Tryptophane : banane, dinde, fromage\n\n" +
                                "🥗 Évitez les excès de sucre qui donnent un coup de pompe après."),

                new Reponse("alimentation", "compulsif",
                        "🍽️ **Gérer les compulsions alimentaires**\n\n" +
                                "• Ne sautez pas de repas\n" +
                                "• Mangez en pleine conscience (sans écran)\n" +
                                "• Identifiez vos déclencheurs (émotions, stress)\n" +
                                "• Trouvez des alternatives (boire de l'eau, marcher)\n" +
                                "• Consultez un nutritionniste ou psy si nécessaire\n\n" +
                                "💚 Vous méritez une relation saine avec la nourriture.")
        });

        // ===== EXERCICE =====
        ajouterConnaissances("exercice", new Reponse[]{
                new Reponse("exercice", "commencer",
                        "🏃 **Commencer le sport quand on n'aime pas ça**\n\n" +
                                "1. **Trouvez une activité agréable** : danse, marche, vélo, natation, yoga.\n" +
                                "2. **Commencez doucement** : 10 minutes par jour.\n" +
                                "3. **Fixez des objectifs réalistes** : 3 fois par semaine.\n" +
                                "4. **Faites-vous accompagner** : un ami motive.\n" +
                                "5. **Variez** pour ne pas vous lasser.\n\n" +
                                "💪 Le sport libère des endorphines, les hormones du bonheur."),

                new Reponse("exercice", "mental",
                        "🧠 **Sport et santé mentale**\n\n" +
                                "L'exercice physique :\n" +
                                "• Réduit l'anxiété et le stress (-30% après 30 min)\n" +
                                "• Améliore le sommeil\n" +
                                "• Augmente l'estime de soi\n" +
                                "• Combat la dépression légère\n" +
                                "• Améliore la mémoire et la concentration\n\n" +
                                "🎯 30 minutes de marche par jour suffisent pour ressentir les bienfaits.")
        });

        // ===== MÉDITATION =====
        ajouterConnaissances("méditation", new Reponse[]{
                new Reponse("méditation", "debuter",
                        "🧘 **Débuter la méditation**\n\n" +
                                "1. **Position** : Assis confortablement, dos droit.\n" +
                                "2. **Durée** : Commencez par 5 minutes.\n" +
                                "3. **Respiration** : Concentrez-vous sur votre souffle.\n" +
                                "4. **Pensées** : Quand l'esprit vagabonde, ramenez-le doucement.\n" +
                                "5. **Régularité** : Tous les jours plutôt qu'une heure par semaine.\n\n" +
                                "📱 Applications recommandées : Petit BamBou, Calm, Headspace."),

                new Reponse("méditation", "bienfaits",
                        "🌟 **Les bienfaits de la méditation**\n\n" +
                                "• Réduction du stress (-30% après 8 semaines)\n" +
                                "• Meilleure concentration\n" +
                                "• Moins d'anxiété\n" +
                                "• Meilleur sommeil\n" +
                                "• Plus de bienveillance envers soi-même\n" +
                                "• Régulation des émotions\n\n" +
                                "🔄 Les effets sont cumulatifs, continuez !")
        });
    }

    private void ajouterConnaissances(String theme, Reponse[] reponses) {
        connaissances.put(theme, reponses);
    }

    public String sendPrompt(String prompt) {
        String lowerPrompt = prompt.toLowerCase().trim();

        // Récupérer le contexte précédent
        String contexte = contexteUtilisateur.getOrDefault("dernier", "");

        // Améliorer la compréhension avec le contexte
        String promptAvecContexte = prompt;
        if (!contexte.isEmpty() && lowerPrompt.length() < 15) {
            promptAvecContexte = contexte + " " + prompt;
            lowerPrompt = promptAvecContexte.toLowerCase();
        }

        // Salutations
        if (lowerPrompt.matches(".*(bonjour|bonsoir|salut|hello|coucou|hey|slt).*")) {
            return getSalutation();
        }

        if (lowerPrompt.matches(".*(merci|thanks|merci beaucoup|thx).*")) {
            return getReponseMerci();
        }

        if (lowerPrompt.matches(".*(au revoir|bye|à bientôt|adieu|ciao).*")) {
            return getReponseAurevoir();
        }

        // Recherche avancée par mots-clés multiples
        for (Map.Entry<String, Reponse[]> entry : connaissances.entrySet()) {
            String theme = entry.getKey();

            // Vérifier si le thème est mentionné
            if (lowerPrompt.contains(theme)) {
                String reponse = selectionnerReponse(entry.getValue(), lowerPrompt);
                contexteUtilisateur.put("dernier", theme);
                return reponse;
            }

            // Synonymes et expressions courantes
            if ((theme.equals("anxiété") && (lowerPrompt.contains("angoisse") || lowerPrompt.contains("peur") || lowerPrompt.contains("panique") || lowerPrompt.contains("nerveux"))) ||
                    (theme.equals("stress") && (lowerPrompt.contains("pression") || lowerPrompt.contains("fatigue") || lowerPrompt.contains("épuisé") || lowerPrompt.contains("burnout") || lowerPrompt.contains("overbooké"))) ||
                    (theme.equals("sommeil") && (lowerPrompt.contains("dormir") || lowerPrompt.contains("insomnie") || lowerPrompt.contains("fatigué") || lowerPrompt.contains("nuit blanche") || lowerPrompt.contains("réveil"))) ||
                    (theme.equals("dépression") && (lowerPrompt.contains("triste") || lowerPrompt.contains("moral") || lowerPrompt.contains("vide") || lowerPrompt.contains("déprime") || lowerPrompt.contains("envie de rien"))) ||
                    (theme.equals("relation") && (lowerPrompt.contains("couple") || lowerPrompt.contains("ami") || lowerPrompt.contains("famille") || lowerPrompt.contains("conjoint") || lowerPrompt.contains("mariage") || lowerPrompt.contains("rupture"))) ||
                    (theme.equals("travail") && (lowerPrompt.contains("boulot") || lowerPrompt.contains("professionnel") || lowerPrompt.contains("carrière") || lowerPrompt.contains("emploi") || lowerPrompt.contains("collègue"))) ||
                    (theme.equals("deuil") && (lowerPrompt.contains("perte") || lowerPrompt.contains("mort") || lowerPrompt.contains("décès") || lowerPrompt.contains("disparition"))) ||
                    (theme.equals("alimentation") && (lowerPrompt.contains("manger") || lowerPrompt.contains("repas") || lowerPrompt.contains("bouffe") || lowerPrompt.contains("nutrition") || lowerPrompt.contains("régime"))) ||
                    (theme.equals("exercice") && (lowerPrompt.contains("sport") || lowerPrompt.contains("activité physique") || lowerPrompt.contains("bouger") || lowerPrompt.contains("gym") || lowerPrompt.contains("entraînement"))) ||
                    (theme.equals("méditation") && (lowerPrompt.contains("mediter") || lowerPrompt.contains("pleine conscience") || lowerPrompt.contains("yoga") || lowerPrompt.contains("zen") || lowerPrompt.contains("calme intérieur")))) {

                String reponse = selectionnerReponse(entry.getValue(), lowerPrompt);
                contexteUtilisateur.put("dernier", theme);
                return reponse;
            }
        }

        // Si aucun thème trouvé, réponse par défaut personnalisée
        contexteUtilisateur.put("dernier", "defaut");
        return getReponseDefaut();
    }

    private String selectionnerReponse(Reponse[] reponses, String prompt) {
        // Chercher un sous-thème spécifique
        for (Reponse r : reponses) {
            if (prompt.contains(r.sousTheme)) {
                return r.contenu;
            }
        }
        // Réponse aléatoire sinon
        return reponses[random.nextInt(reponses.length)].contenu;
    }

    private String getSalutation() {
        String[] salutations = {
                "👋 Bonjour ! Je suis votre assistant bien-être. Comment puis-je vous aider aujourd'hui ?",
                "🌱 Bonjour ! Parlez-moi de ce qui vous préoccupe (anxiété, stress, sommeil...).",
                "💚 Bonjour ! Je suis là pour vous écouter et vous conseiller en toute confidentialité.",
                "✨ Bonjour ! Ensemble, nous pouvons explorer vos défis et trouver des solutions."
        };
        return salutations[random.nextInt(salutations.length)];
    }

    private String getReponseMerci() {
        String[] reponses = {
                "🌿 Avec plaisir ! N'hésitez pas si vous avez d'autres questions.",
                "💙 Je suis content d'avoir pu vous aider ! Revenez quand vous voulez.",
                "😊 De rien ! C'est un plaisir de vous accompagner.",
                "✨ Tout le plaisir est pour moi ! Prenez soin de vous."
        };
        return reponses[random.nextInt(reponses.length)];
    }

    private String getReponseAurevoir() {
        String[] reponses = {
                "👋 Au revoir ! Prenez soin de vous et de votre santé mentale.",
                "🌱 À bientôt ! N'oubliez pas que je suis là pour vous.",
                "💚 Prenez soin de vous ! Revenez quand vous avez besoin de soutien.",
                "🌟 Bonne continuation ! N'hésitez pas à revenir."
        };
        return reponses[random.nextInt(reponses.length)];
    }

    private String getReponseDefaut() {
        String[] defaut = {
                "🌿 **Je peux vous aider sur ces sujets** :\n\n" +
                        "• 😟 **Anxiété** : crises, gestion quotidienne, prévention\n" +
                        "• ⚡ **Stress** : au travail, techniques rapides, burn-out\n" +
                        "• 😴 **Sommeil** : hygiène, insomnie, rêves\n" +
                        "• 💙 **Dépression** : comprendre, soutien, ressources\n" +
                        "• 💑 **Relations** : couple, famille, amitié\n" +
                        "• 🧘 **Méditation** : débuter, bienfaits, exercices\n" +
                        "• 🏃 **Exercice** : commencer, bienfaits pour le mental\n" +
                        "• 🕊️ **Deuil** : processus, aide\n" +
                        "• 🍎 **Alimentation** : équilibre, compulsions\n\n" +
                        "Posez-moi une question précise sur l'un de ces thèmes !",

                "💚 Je suis là pour vous écouter. De quoi aimeriez-vous parler ?\n" +
                        "(anxiété, stress, sommeil, dépression, relations, etc.)"
        };
        return defaut[random.nextInt(defaut.length)];
    }

    public CompletableFuture<String> sendPromptAsync(String prompt) {
        return CompletableFuture.completedFuture(sendPrompt(prompt));
    }

    // Classe interne pour stocker les réponses
    private class Reponse {
        String theme;
        String sousTheme;
        String contenu;

        Reponse(String theme, String sousTheme, String contenu) {
            this.theme = theme;
            this.sousTheme = sousTheme;
            this.contenu = contenu;
        }
    }
}