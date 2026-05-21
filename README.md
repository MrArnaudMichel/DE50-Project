# DE50-Project — RhapsodySVN

Plugin Java pour **IBM Rhapsody 9.0** permettant de modéliser et calculer un **Stakeholder Value Network (SVN)** directement dans un modèle SysML/UML.

---

## Table des matières

- [Contexte](#contexte)
- [Prérequis (cible)](#prérequis-cible)
- [Installation](#installation)
- [Lancement](#lancement)
- [Définition du profil SVN](#définition-du-profil-svn)
- [Algorithme de calcul](#algorithme-de-calcul)
- [Architecture du code](#architecture-du-code)

---

## Contexte

Le **Stakeholder Value Network (SVN)** est un outil d'analyse permettant d'évaluer l'importance des parties prenantes (stakeholders) d'un système. Ce plugin Rhapsody automatise :

1. La **création d'un profil SysML** dédié au SVN (stéréotypes, tags, types énumérés)
2. Le **calcul de l'importance** de chaque stakeholder en fonction de ses arcs de valeur (en cours)

Le plugin interagit avec une instance Rhapsody **déjà ouverte** via l'API Java COM (`RhapsodyAppServer`).

---

## Prérequis (cible)

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

### 3. Ajouter le .JAR dans \target

```bash
mvn package
```

### 3.1. Dans le cas où vous avez déjà compiler (clean)

```bash
mvn clean
```

---

## Lancement

### Prérequis de lancement

1. **IBM Rhapsody doit être ouvert** avec un projet actif

### Via ligne de commande

```bash
# Configurer le profil SVN dans le modèle Rhapsody ouvert
java "-Djava.library.path=lib\64Bit" -cp "target\RhapsodySVN-1.0-SNAPSHOT.jar;lib\rhapsody.jar" fr.utbm.RhapsodySVN.Main -clean
```

### Sur Rhapsody

1. Vous pouvez créer un SVN Diagram avec un clique droit sur le projet    

![](C:\Users\hchacrot\AppData\Roaming\marktext\images\2026-05-21-19-59-41-image.png)

2. Vous pouvez vérifier le profile SVN depuis l'explorateur de projet à gauche
   
   ![](C:\Users\hchacrot\AppData\Roaming\marktext\images\2026-05-21-20-02-42-image.png)
   
   **Note** : Pour l'instant, l'intégration graphique n'est pas visible.

---

## Définition du profil SVN

Le plugin crée un profil Rhapsody nommé **`SVNProfile`** contenant les éléments suivants :

### Stéréotypes

| Stéréotype      | Métaclasse cible     | New Term | Description                                      |
| --------------- | -------------------- | -------- | ------------------------------------------------ |
| `«stakeholder»` | `Class`              | Oui      | Représente un acteur du réseau de valeur         |
| `«svnSystem»`   | `Class`              | Oui      | Représente le système évalué                     |
| `«valueArc»`    | `Association`        | Oui      | Arc de valeur entre un stakeholder et le système |
| `«SVNDiagram»`  | `ObjectModelDiagram` | Oui      | Diagramme dédié à la visualisation du SVN        |

### Tags (Tagged Values)

| Tag                | Porté par       | Type                      | Description                                |
| ------------------ | --------------- | ------------------------- | ------------------------------------------ |
| `importanceScore`  | `«stakeholder»` | Texte libre               | Score d'importance calculé (entre 0 et 1)  |
| `benefitRanking`   | `«valueArc»`    | `BenefitRanking` (enum)   | Niveau de bénéfice de l'arc                |
| `supplyImportance` | `«valueArc»`    | `SupplyImportance` (enum) | Niveau d'importance de l'approvisionnement |

### Types énumérés

| Type               | Littéraux                          | Poids associé (dans le calcul) |
| ------------------ | ---------------------------------- | ------------------------------ |
| `BenefitRanking`   | `MIGHT_BE`, `SHOULD_BE`, `MUST_BE` | 1.0, 2.0, 3.0                  |
| `SupplyImportance` | `LOW`, `MEDIUM`, `HIGH`            | 1.0, 2.0, 3.0                  |

### Localisation dans le code

Les définitions du profil sont réparties dans deux fichiers :

- **Noms et constantes** → [`SVNConstants.java`](src/main/java/fr/utbm/RhapsodySVN/constants/SVNConstants.java)
- **Logique de création** → [`ProfileService.java`](src/main/java/fr/utbm/RhapsodySVN/service/ProfileService.java)

---

## Algorithme de calcul

Le `CalculationService` calcule l'**importance relative** de chaque stakeholder :

```
Pour chaque «stakeholder» trouvé (recherche récursive) :
    score = 0
    Pour chaque relation (association) du stakeholder :
        Si la relation porte le stéréotype «valueArc» :
            benefit = poids(benefitRanking)    // MIGHT_BE=1, SHOULD_BE=2, MUST_BE=3
            supply  = poids(supplyImportance)  // LOW=1, MEDIUM=2, HIGH=3
            score += benefit × supply

totalValue = somme de tous les scores
Pour chaque «stakeholder» :
    importanceScore = score / totalValue
```

**Résultat** : chaque stakeholder reçoit un `importanceScore` normalisé (la somme de tous les scores = 1.0).

---

## Architecture du code

```
src/main/java/fr/utbm/RhapsodySVN/
├── Main.java                        # Point d'entrée CLI (configure / configure+clean)
├── SVNConfigureCommand.java         # Commande Rhapsody : configure le profil
├── SVNCalculateCommand.java         # Commande Rhapsody : calcule sur l'élément sélectionné
├── SVNCleanCommand.java             # Commande Rhapsody : nettoyage complet
├── constants/
│   └── SVNConstants.java            # Constantes (noms profil, stéréotypes, tags, enums)
├── rhapsody/
│   └── RhapsodyWrapper.java         # Wrapper de l'API Rhapsody
└── service/
    ├── CalculationService.java      # Logique de calcul d'importance des stakeholders
    └── ProfileService.java          # Création / suppression du profil SVNProfile
```

### Work in progress

- Intégrer à l'interface Rhapsody (visuels)

- Test des calculs d'importances

- Intégration en continu de l'outil

- Documentation utilisateurs et technique

- Ajout de tests
