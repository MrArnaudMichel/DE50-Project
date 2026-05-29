# DE50-Project — RhapsodySVN

Plugin Java pour **IBM Rhapsody 9.0** permettant de modéliser et calculer un **Stakeholder Value Network (SVN)** directement dans un modèle SysML/UML.

---

## Table des matières

- [Contexte](#contexte)
- [Prérequis](#prérequis)
- [Installation](#installation)
- [Lancement et test](#lancement-et-test)
- [Commandes disponibles](#commandes-disponibles)
- [Définition du profil SVN](#définition-du-profil-svn)
- [Algorithme de calcul](#algorithme-de-calcul)
- [Architecture du code](#architecture-du-code)

---

## Contexte

Le **Stakeholder Value Network (SVN)** est un outil d'analyse permettant d'évaluer l'importance des parties prenantes (stakeholders) d'un système. Ce plugin Rhapsody automatise :

1. La **création d'un profil SysML** dédié au SVN (stéréotypes, tags, types énumérés)
2. Le **calcul de l'importance** de chaque stakeholder via les équations de Cameron (2007) et les matrices de score de l'INCOSE 2018
3. La **visualisation** : colorisation automatique des stakeholders selon leur rang, étiquetage des arcs

Le plugin interagit avec une instance Rhapsody **déjà ouverte** via l'API Java COM (`RhapsodyAppServer`).

---

## Prérequis

| Composant        | Version requise                                |
| ---------------- | ---------------------------------------------- |
| **IBM Rhapsody** | 9.0 (avec un projet par défaut ouvert)         |
| **Java (JDK)**   | 1.8 (ex. Amazon Corretto 1.8)                  |
| **OS**           | Windows (requis pour les DLLs COM de Rhapsody) |
| **Maven**        | 3.x (pour la compilation)                      |

### Fichiers nécessaires (inclus dans `lib/`)

```
lib/
├── rhapsody.jar          # API Java Rhapsody
├── rhapsody.dll          # DLL native 32 bits
└── 64Bit/
    └── rhapsody.dll      # DLL native 64 bits
```

> **Note** : Le fichier `rhapsody.jar` est fourni par IBM Rhapsody et se trouve aussi dans `C:\Program Files\IBM\Rhapsody\9.0\Share\JavaAPI\`.

---

## Installation

### 1. Cloner le dépôt

```bash
git clone https://github.com/MrArnaudMichel/DE50-Project.git
cd DE50-Project
```

### 2. Compiler le projet

```bash
mvn compile
```

### 3. Packager le projet

```bash
mvn package
```

### 3.1. En cas de recompilation

```bash
mvn clean
mvn package
```

---

## Lancement et test

### Prérequis de lancement

1. **IBM Rhapsody doit être ouvert** avec un projet actif contenant au moins un package `Default`

### Via ligne de commande

Chaque commande est une classe Java indépendante avec un `main()`. Elles se lancent toutes de la même façon :

```bash
java "-Djava.library.path=lib\64Bit" -cp "target\RhapsodySVN-1.0-SNAPSHOT.jar;lib\rhapsody.jar" <ClassePrincipale>
```

Remplacez `<ClassePrincipale>` par la commande souhaitée parmi celles décrites ci-dessous.

### Via le plugin Rhapsody (intégration complète)

Lorsque le JAR est chargé comme plugin dans Rhapsody, les commandes sont accessibles via le menu contextuel. `SVNPlugin` est le point d'entrée — il délègue chaque action aux classes de commande correspondantes.

---

## Commandes disponibles

Voici l'ensemble des classes exécutables du projet. Chacune peut être lancée en ligne de commande ou invoquée via le plugin Rhapsody.

### `fr.utbm.RhapsodySVN.Main`

Point d'entrée principal pour les tests rapides. Lance en séquence `SVNConfigureCommand`, `SVNCalculateCommand` et `SVNEditArcCommand` sur le projet actif.

```bash
java "-Djava.library.path=lib\64Bit" -cp "target\RhapsodySVN-1.0-SNAPSHOT.jar;lib\rhapsody.jar" fr.utbm.RhapsodySVN.Main
```

---

### `fr.utbm.RhapsodySVN.SVNConfigureCommand`

**Crée ou met à jour le profil SVN** (`SVNProfile`) dans le projet Rhapsody ouvert. Recrée proprement les stéréotypes, types énumérés et tags si le profil existait déjà (mode nettoyage activé par défaut dans cette commande).

```bash
java "-Djava.library.path=lib\64Bit" -cp "target\RhapsodySVN-1.0-SNAPSHOT.jar;lib\rhapsody.jar" fr.utbm.RhapsodySVN.SVNConfigureCommand
```

> À lancer **en premier** avant tout autre commande, pour initialiser le profil.

---

### `fr.utbm.RhapsodySVN.SVNCalculateCommand`

**Calcule l'importance relative** de chaque stakeholder trouvé dans le projet, en parcourant les arcs `«valuearc»` et en appliquant la matrice de score INCOSE 2018. Met à jour le tag `importanceScore` de chaque stakeholder.

```bash
java "-Djava.library.path=lib\64Bit" -cp "target\RhapsodySVN-1.0-SNAPSHOT.jar;lib\rhapsody.jar" fr.utbm.RhapsodySVN.SVNCalculateCommand
```

> Requiert que des éléments `«stakeholder»`, `«system»` et `«valuearc»` existent dans le modèle.

---

### `fr.utbm.RhapsodySVN.SVNEditArcCommand`

**Édite les pondérations d'un arc** (`«valuearc»`) sélectionné dans Rhapsody. Ouvre deux boîtes de dialogue successives pour choisir le `benefitRanking` (`MIGHT_BE`, `SHOULD_BE`, `MUST_BE`) et la `supplyImportance` (`LOW`, `MEDIUM`, `HIGH`).

```bash
java "-Djava.library.path=lib\64Bit" -cp "target\RhapsodySVN-1.0-SNAPSHOT.jar;lib\rhapsody.jar" fr.utbm.RhapsodySVN.SVNEditArcCommand
```

> L'arc `«valuearc»` doit être **sélectionné dans Rhapsody** avant le lancement.

---

### `fr.utbm.RhapsodySVN.SVNArcColorCommand`

**Ouvre un sélecteur de couleur** et applique la couleur choisie à l'arc `«valuearc»` actuellement sélectionné dans le diagramme.

```bash
java "-Djava.library.path=lib\64Bit" -cp "target\RhapsodySVN-1.0-SNAPSHOT.jar;lib\rhapsody.jar" fr.utbm.RhapsodySVN.SVNArcColorCommand
```

> Un arc `«valuearc»` doit être **sélectionné graphiquement** dans Rhapsody.

---

### `fr.utbm.RhapsodySVN.SVNColorizeStakeholdersCommand`

**Colorise automatiquement les stakeholders** dans le diagramme selon leur `importanceScore` : du rouge (importance faible) au vert (importance élevée). Met aussi en gras les éléments les plus importants.

```bash
java "-Djava.library.path=lib\64Bit" -cp "target\RhapsodySVN-1.0-SNAPSHOT.jar;lib\rhapsody.jar" fr.utbm.RhapsodySVN.SVNColorizeStakeholdersCommand
```

> Lancer **après** `SVNCalculateCommand` pour que les scores soient disponibles. Un élément du diagramme doit être sélectionné.

---

### `fr.utbm.RhapsodySVN.SVNLabelArcCommand`

**Affiche les étiquettes** `B:/S:` (BenefitRanking / SupplyImportance) sur les arcs `«valuearc»` dans tous les diagrammes SVN du projet.

```bash
java "-Djava.library.path=lib\64Bit" -cp "target\RhapsodySVN-1.0-SNAPSHOT.jar;lib\rhapsody.jar" fr.utbm.RhapsodySVN.SVNLabelArcCommand
```

---

### `fr.utbm.RhapsodySVN.SVNCleanCommand`

**Supprime tous les éléments SVN** du package `Default` (stakeholders, systems, arcs, diagrammes) et supprime le profil `SVNProfile`. Utile pour repartir de zéro.

```bash
java "-Djava.library.path=lib\64Bit" -cp "target\RhapsodySVN-1.0-SNAPSHOT.jar;lib\rhapsody.jar" fr.utbm.RhapsodySVN.SVNCleanCommand
```

> ⚠️ **Irréversible** : sauvegardez votre projet Rhapsody avant d'exécuter cette commande.

---

### Séquence de test complète recommandée

Pour tester le plugin de bout en bout :

```bash
# 1. Initialiser le profil SVN
java "-Djava.library.path=lib\64Bit" -cp "target\RhapsodySVN-1.0-SNAPSHOT.jar;lib\rhapsody.jar" fr.utbm.RhapsodySVN.SVNConfigureCommand

# 2. (Dans Rhapsody) Créer manuellement des stakeholders, un system, et des arcs valuearc
#    ou utiliser SVNCreateArcCommand depuis le plugin pour créer des arcs entre éléments sélectionnés

# 3. Éditer les pondérations d'un arc sélectionné
java "-Djava.library.path=lib\64Bit" -cp "target\RhapsodySVN-1.0-SNAPSHOT.jar;lib\rhapsody.jar" fr.utbm.RhapsodySVN.SVNEditArcCommand

# 4. Calculer les importances
java "-Djava.library.path=lib\64Bit" -cp "target\RhapsodySVN-1.0-SNAPSHOT.jar;lib\rhapsody.jar" fr.utbm.RhapsodySVN.SVNCalculateCommand

# 5. Coloriser les stakeholders selon leur rang
java "-Djava.library.path=lib\64Bit" -cp "target\RhapsodySVN-1.0-SNAPSHOT.jar;lib\rhapsody.jar" fr.utbm.RhapsodySVN.SVNColorizeStakeholdersCommand

# 6. Afficher les étiquettes sur les arcs
java "-Djava.library.path=lib\64Bit" -cp "target\RhapsodySVN-1.0-SNAPSHOT.jar;lib\rhapsody.jar" fr.utbm.RhapsodySVN.SVNLabelArcCommand
```

---

## Définition du profil SVN

Le plugin crée un profil Rhapsody nommé **`SVNProfile`** contenant les éléments suivants :

### Stéréotypes

| Stéréotype      | Métaclasse cible     | New Term | Description                                         |
| --------------- | -------------------- | -------- | --------------------------------------------------- |
| `«stakeholder»` | `Actor`              | Oui      | Représente un acteur du réseau de valeur            |
| `«system»`      | `Class`              | Oui      | Représente le système évalué                        |
| `«valuearc»`    | `Flow`               | Oui      | Arc de valeur entre un stakeholder et le système    |
| `«SVNDiagram»`  | `ObjectModelDiagram` | Oui      | Diagramme dédié à la visualisation du SVN           |

### Tags (Tagged Values)

| Tag                | Porté par       | Type                      | Description                                |
| ------------------ | --------------- | ------------------------- | ------------------------------------------ |
| `importanceScore`  | `«stakeholder»` | Texte libre               | Score d'importance calculé (entre 0 et 1)  |
| `benefitRanking`   | `«valuearc»`    | `BenefitRanking` (enum)   | Niveau de bénéfice de l'arc                |
| `supplyImportance` | `«valuearc»`    | `SupplyImportance` (enum) | Niveau d'importance de l'approvisionnement |

### Types énumérés

| Type               | Littéraux                          | Matrice de score (High / Medium / Low) |
| ------------------ | ---------------------------------- | --------------------------------------- |
| `BenefitRanking`   | `MIGHT_BE`, `SHOULD_BE`, `MUST_BE` | voir tableau ci-dessous                 |
| `SupplyImportance` | `LOW`, `MEDIUM`, `HIGH`            | voir tableau ci-dessous                 |

---

## Algorithme de calcul

Le `CalculationService` implémente les équations des SVN (Cameron 2007, INCOSE 2018).

### Matrice de score des arcs (Figure 3, INCOSE 2018)

|                | MIGHT_BE | SHOULD_BE | MUST_BE |
| -------------- | -------- | --------- | ------- |
| **HIGH**       | 0.30     | 0.50      | 0.95    |
| **MEDIUM**     | 0.20     | 0.40      | 0.80    |
| **LOW**        | 0.10     | 0.20      | 0.40    |

### Formule d'importance relative (Équation 2, Cameron 2007)

```
I(stakeholder_i) = Σ score(arc) pour tous les arcs liés à stakeholder_i
                   ──────────────────────────────────────────────────────
                        Σ score(arc) pour tous les arcs du graphe
```

Le calcul tient compte des **value loops** (boucles de valeur passant par le système central) quand un nœud `«system»` est présent, ou bascule sur une somme simple des arcs dans le cas contraire.

**Résultat** : chaque stakeholder reçoit un `importanceScore` normalisé (la somme de tous les scores = 1.0), puis est colorisé du rouge au vert selon son rang.

---

## Architecture du code

```
src/main/java/fr/utbm/RhapsodySVN/
├── Main.java                              # Entrée CLI : enchaîne Configure → Calculate → EditArc
├── SVNPlugin.java                         # Point d'entrée plugin Rhapsody (RPUserPlugin)
│
├── SVNConfigureCommand.java               # Crée/met à jour le profil SVNProfile
├── SVNCalculateCommand.java               # Calcule l'importanceScore de chaque stakeholder
├── SVNEditArcCommand.java                 # Édite benefitRanking et supplyImportance d'un arc (UI Swing)
├── SVNArcColorCommand.java                # Sélecteur de couleur pour un arc (UI Swing)
├── SVNColorizeStakeholdersCommand.java    # Colorise les stakeholders selon leur rang
├── SVNCreateArcCommand.java               # Crée un valuearc entre deux éléments sélectionnés
├── SVNLabelArcCommand.java                # Affiche les étiquettes B:/S: sur les arcs
├── SVNCleanCommand.java                   # Supprime tous les éléments SVN du projet
│
├── constants/
│   └── SVNConstants.java                  # Constantes (noms profil, stéréotypes, tags, enums)
├── rhapsody/
│   └── RhapsodyWrapper.java               # Utilitaires bas niveau pour l'API Rhapsody
└── service/
    ├── CalculationService.java            # Logique de calcul d'importance (value loops, matrix scoring)
    ├── DiagramService.java                # Gestion graphique (couleurs, labels, arcs dans les diagrammes)
    └── ProfileService.java                # Création / suppression du profil SVNProfile
```

### Commandes exposées dans le menu Rhapsody (`SVNPlugin`)

| Entrée de menu              | Classe déléguée                   |
| --------------------------- | --------------------------------- |
| `SVN Configure`             | `SVNConfigureCommand`             |
| `SVN Calculate`             | `SVNCalculateCommand`             |
| `SVN Update Arc Labels`     | `SVNLabelArcCommand`              |
| `SVN Set Arc Color`         | `SVNArcColorCommand`              |
| `SVN Colorize Stakeholders` | `SVNColorizeStakeholdersCommand`  |
| `SVN Create Arc`            | `SVNCreateArcCommand`             |
| `SVN Clean`                 | `SVNCleanCommand`                 |

---

### Work in progress

- Intégration complète à l'interface Rhapsody
- Tests de validation des calculs d'importance
- Ajout de tests unitaires et d'intégration