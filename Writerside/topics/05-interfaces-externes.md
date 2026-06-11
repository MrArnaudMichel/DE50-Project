# 5. Spécifications des interfaces externes

## Interface matériel / logiciel

### Configuration minimale

| Composant | Configuration minimale |
|---|---|
| Système d'exploitation | Windows (7, 10 ou 11) — **Windows uniquement** |
| Processeur | Compatible avec l'exécution d'IBM Rhapsody 9.0 |
| Mémoire RAM | 4 Go minimum (8 Go recommandés, requis par IBM Rhapsody) |
| Espace disque | 500 Mo (installation Rhapsody non comprise) |
| JDK | JDK 1.8 (Java 8) installé et accessible dans le PATH |

### Périphériques requis

| Périphérique | Usage |
|---|---|
| Écran | Affichage des diagrammes Rhapsody et des boîtes de dialogue Swing |
| Souris | Sélection des éléments graphiques dans les diagrammes (UC2, UC3, UC6) |
| Clavier | Saisie de noms d'éléments dans Rhapsody |

---

## Interface logiciel / logiciel

### IBM Rhapsody 9.0

| Propriété | Valeur |
|---|---|
| **Nom** | IBM Rational Rhapsody |
| **Version** | 9.0 (obligatoire — l'API COM utilisée est spécifique à cette version) |
| **Provenance** | Fourni par IBM. Disponible via les licences académiques UTBM. |
| **But d'utilisation** | Application hôte du plugin. Fournit l'API Java via `rhapsody.jar`. |
| **Interface** | `IRPApplication`, `IRPProject`, `IRPProfile`, `IRPFlow`, `IRPActor`, `IRPCollection`, `RPUserPlugin`, `RhapsodyAppServer` |
| **Mode d'accès** | Appel de `RhapsodyAppServer.getActiveRhapsodyApplication()` pour obtenir l'application active |

Le JAR `rhapsody.jar` est inclus comme dépendance locale dans `lib/rhapsody.jar` et déclaré dans `pom.xml` avec `<scope>system</scope>`.

### Maven 3.x

| Propriété | Valeur |
|---|---|
| **Nom** | Apache Maven |
| **Version** | 3.x |
| **But** | Outil de build et de gestion des dépendances |
| **Usage** | Compilation du projet, packaging en JAR (`mvn package`) |

### Java Swing (javax.swing)

| Propriété | Valeur |
|---|---|
| **Nom** | Java Swing |
| **Version** | Incluse dans JDK 1.8 |
| **But** | Affichage des boîtes de dialogue interactives du plugin |
| **Classes utilisées** | `JOptionPane` (sélection des pondérations UC3), `JColorChooser` (sélection de couleur UC6) |

---

## Interface Homme / logiciel

### Menu contextuel Rhapsody

Le plugin s'intègre dans le menu de Rhapsody via la classe `SVNPlugin`. Les commandes disponibles depuis le menu sont :

| Entrée de menu | Cas d'utilisation |
|---|---|
| `SVN Configure` | UC1 — Configurer le profil |
| `SVN Create Arc` | UC2 — Créer un arc de valeur |
| (via sélection + menu) | UC3 — Éditer les pondérations |
| `SVN Calculate` | UC4 — Calculer l'importance |
| `SVN Colorize Stakeholders` | UC5 — Coloriser les stakeholders |
| `SVN Set Arc Color` | UC6 — Coloriser un arc |
| `SVN Update Arc Labels` | UC7 — Mettre à jour les labels |
| `SVN Clean` | UC8 — Nettoyer le modèle |

### Boîtes de dialogue Swing

Deux commandes ouvrent des fenêtres interactives :

**UC3 — SVN Edit Arc** : deux boîtes `JOptionPane.showInputDialog` successives permettent de sélectionner :
1. La valeur de `benefitRanking` parmi `{MIGHT_BE, SHOULD_BE, MUST_BE}`
2. La valeur de `supplyImportance` parmi `{LOW, MEDIUM, HIGH}`

**UC6 — SVN Set Arc Color** : une fenêtre `JColorChooser` permet à l'utilisateur de choisir librement une couleur pour l'arc sélectionné.

### Messages console

Le plugin communique son état interne via des messages sur la sortie standard (console Rhapsody ou terminal) :

| Préfixe | Signification |
|---|---|
| `[SVN]` | Message d'information (opération normale) |
| `[SVN]` (sur `System.err`) | Message d'erreur ou d'avertissement |

Exemples de messages produits :
- `[SVN] Plugin initialisé.` — à l'ouverture du plugin
- `[SVN] Importance Stakeholder1 = 0.3750` — après UC4
- `[SVN] Nettoyage terminé : 7 élément(s) supprimé(s).` — après UC8
- `[SVN] Commande inconnue : <nom>` — si une entrée de menu non reconnue est déclenchée

### Spécification des messages d'erreur

| Situation | Message affiché |
|---|---|
| Élément sélectionné non valide pour UC3 | Boîte `JOptionPane` WARNING : *"Sélectionnez un arc «valuearc» dans le diagramme."* |
| Élément sélectionné non valuearc pour UC3 | Boîte `JOptionPane` WARNING : *"L'élément sélectionné n'est pas un «valuearc»."* |
| Moins de 2 éléments sélectionnés pour UC2 | Message console : *"Sélectionnez 2 éléments (stakeholder ou system) dans le diagramme."* |
| Aucun projet actif | Message console : *"Aucun projet actif."* |
| Aucun stakeholder trouvé pour UC4 | Message console : *"Aucun stakeholder trouvé."* |
