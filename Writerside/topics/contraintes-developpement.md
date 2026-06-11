# 7. Les contraintes de développement

## Fiabilité et tolérance aux fautes

Le plugin doit fonctionner de manière robuste même en cas de modèle Rhapsody incomplet ou mal configuré. Les principales mesures de fiabilité implémentées sont :

| Situation anormale | Comportement du plugin |
|---|---|
| Aucun projet Rhapsody actif | Log d'erreur console, arrêt gracieux de la commande sans exception non gérée |
| Stéréotype ou tag absent du modèle | Vérification préalable via `RhapsodyWrapper.hasStereotype()` avant toute manipulation |
| Élément sélectionné non conforme | Message d'avertissement à l'utilisateur (boîte `JOptionPane` ou log console) |
| Absence de nœud `«system»` | Fallback automatique sur le calcul simplifié par somme des arcs |
| Échec d'une opération API Rhapsody | Les exceptions sont catchées localement avec un log d'erreur ; l'opération est ignorée sans arrêt du plugin |
| Suppression d'un élément déjà supprimé | Tentative de `deleteFromProject()` en fallback si la méthode spécifique échoue |

Le plugin **ne lève jamais d'exception non gérée** susceptible de planter IBM Rhapsody. Toutes les opérations à risque sont encadrées par des blocs `try/catch`.

## Comportement dans les situations anormales

Les exceptions critiques sont traitées selon la politique suivante :

- **Exceptions lors de la création du profil** : log d'erreur, abandon de l'opération en cours, le profil peut être dans un état partiel
- **Exceptions lors du calcul** : log d'erreur, le score concerné est ignoré ou remis à 0
- **Exceptions lors des opérations graphiques** : log d'erreur, tentative d'une méthode alternative (ex. : `setGraphicalPropertyOfText` si `setDisplayName` échoue)

## Sécurité

Le plugin ne gère pas de données sensibles (mots de passe, informations personnelles, données réseau). Les contraintes de sécurité se limitent à :

- **Restriction d'accès** : le plugin n'expose pas d'API externe. Il ne peut être déclenché que depuis IBM Rhapsody (menu ou ligne de commande locale).
- **Contrôle des données** : les valeurs de tags (`benefitRanking`, `supplyImportance`) sont validées contre des listes de valeurs prédéfinies. Tout tag absent ou invalide est traité comme une valeur par défaut (`MIGHT_BE` / score 0.2).
- **Pas de persistance externe** : aucune donnée n'est écrite en dehors du modèle Rhapsody. Pas de fichier temporaire, pas de connexion réseau.

## Utilisation de standards

| Domaine | Standard utilisé |
|---|---|
| Méthode de calcul SVN | Équations de Cameron (2007) — voir Annexe A |
| Matrice de score des arcs | Figure 3, INCOSE 2018 — voir Annexe B |
| Langage de modélisation | SysML (via profil Rhapsody) |
| Langage de programmation | Java 8 (conformité JDK 1.8) |
| Outil de build | Maven 3.x (convention de structure `src/main/java`) |
| Encodage | UTF-8 (déclaré dans `pom.xml`) |

## Maintenabilité

Le code est structuré pour faciliter son évolution :

- **Séparation des responsabilités** : les commandes ne contiennent pas de logique métier ; celle-ci est déléguée aux services.
- **Centralisation des constantes** : tous les noms de stéréotypes, tags et types sont définis dans `SVNConstants`, évitant les magic strings dispersées dans le code.
- **Wrapper d'API** : `RhapsodyWrapper` isole les appels à l'API Rhapsody, facilitant les adaptations à d'autres versions.

## Contraintes de déploiement

Le plugin est distribué sous forme d'un **JAR autonome** (`RhapsodySVN-1.0-SNAPSHOT.jar`) à placer dans le répertoire d'installation de Rhapsody. La configuration du menu Rhapsody doit être effectuée manuellement dans le fichier de configuration de Rhapsody pour déclarer le plugin et ses entrées de menu.
