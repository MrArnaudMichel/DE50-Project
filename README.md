# RhapsodySVN

Plugin Java pour **IBM Rhapsody 9.0** qui intègre la méthode **Stakeholder Value Network (SVN)** directement dans l'environnement de modélisation.

Le plugin écoute les modifications du modèle en temps réel et recalcule automatiquement l'importance de chaque partie prenante à chaque changement.

---

## Table des matières

- [Prérequis](#prérequis)
- [Installation](#installation)
- [Utilisation](#utilisation)
- [Profil SVN](#profil-svn)
- [Algorithme de calcul](#algorithme-de-calcul)
- [Architecture du code](#architecture-du-code)
- [Tests](#tests)

---

## Prérequis

| Composant        | Version requise                                |
|------------------|------------------------------------------------|
| **IBM Rhapsody** | 9.0 (avec un projet actif ouvert)              |
| **Java (JDK)**   | 1.8 (ex. Amazon Corretto 1.8)                  |
| **OS**           | Windows (requis pour les DLLs COM de Rhapsody) |
| **Maven**        | 3.x                                            |

### Fichiers natifs (inclus dans `lib/`)

```
lib/
├── rhapsody.jar        # API Java Rhapsody
├── rhapsody.dll        # DLL native 32 bits
└── 64Bit/
    └── rhapsody.dll    # DLL native 64 bits
```

> `rhapsody.jar` est également disponible dans `C:\Program Files\IBM\Rhapsody\9.0\Share\JavaAPI\`.

---

## Installation

### 1. Cloner le dépôt

```bash
git clone https://github.com/MrArnaudMichel/DE50-Project.git
cd DE50-Project
```

### 2. Compiler et packager

```bash
mvn package
```

En cas de recompilation :

```bash
mvn clean package
```

Le JAR produit est `target/RhapsodySVN-1.0-SNAPSHOT.jar`.

---

## Utilisation

### En tant que plugin Rhapsody (mode normal)

Charger le JAR dans Rhapsody via le gestionnaire de plugins. Lors du chargement, `SVNPlugin` s'enregistre comme listener et commence à écouter les changements du modèle automatiquement.

### En ligne de commande (mode développement)

Rhapsody doit être ouvert avec un projet actif.

```bash
java "-Djava.library.path=lib\64Bit" -cp "target\RhapsodySVN-1.0-SNAPSHOT.jar;lib\rhapsody.jar" fr.utbm.svn.Main
```

Pour activer les logs détaillés, ajouter l'argument `debug` :

```bash
java "-Djava.library.path=lib\64Bit" -cp "target\RhapsodySVN-1.0-SNAPSHOT.jar;lib\rhapsody.jar" fr.utbm.svn.Main debug
```

Appuyer sur **Entrée** pour arrêter proprement le plugin.

### Workflow dans Rhapsody

1. Créer un diagramme avec le stéréotype `«SVNDiagram»`
2. Ajouter des acteurs avec le stéréotype `«stakeholder»`
3. Ajouter une classe avec le stéréotype `«system»`
4. Relier les éléments avec des dépendances portant le stéréotype `«valuearc»`
5. Définir les propriétés de chaque arc (`benefitRanking`, `supplyImportance`) dans le panneau Properties
6. Les scores se calculent et s'affichent **automatiquement** dès qu'un arc est modifié

---

## Profil SVN

Le profil doit être défini dans Rhapsody avant d'utiliser le plugin. Il repose sur les éléments suivants.

### Stéréotypes

| Stéréotype      | Métaclasse cible     | Rôle                                       |
|-----------------|----------------------|--------------------------------------------|
| `«stakeholder»` | `Actor`              | Partie prenante du réseau de valeur        |
| `«system»`      | `Class`              | Système central (nœud de départ des boucles) |
| `«valuearc»`    | `Dependency`         | Arc de valeur entre deux éléments          |
| `«SVNDiagram»`  | `ObjectModelDiagram` | Diagramme SVN                              |

### Tags

| Tag                | Porté par       | Description                                      |
|--------------------|-----------------|--------------------------------------------------|
| `importanceScore`  | `«stakeholder»` | Score d'importance calculé (entre 0.0 et 1.0)   |
| `benefitRanking`   | `«valuearc»`    | Niveau de bénéfice : `MIGHT_BE`, `SHOULD_BE`, `MUST_BE` |
| `supplyImportance` | `«valuearc»`    | Niveau de fourniture : `LOW`, `MEDIUM`, `HIGH`   |
| `totalLoopScore`   | `«system»`      | Score total des boucles de valeur détectées      |

---

## Algorithme de calcul

Le plugin implémente les équations de Cameron (2007) combinées à la matrice de scoring INCOSE 2018.

### Matrice de score des arcs (INCOSE 2018)

|              | MIGHT_BE | SHOULD_BE | MUST_BE |
|--------------|----------|-----------|---------|
| **HIGH**     | 0.30     | 0.50      | 0.95    |
| **MEDIUM**   | 0.20     | 0.40      | 0.80    |
| **LOW**      | 0.10     | 0.20      | 0.40    |

### Stratégie principale — ValueLoopStrategy

Lorsqu'un nœud `«system»` est présent, le plugin détecte toutes les **boucles de valeur** (cycles passant par le système) via un parcours en profondeur (DFS) :

```
score(boucle) = ∏ score(arc_i)   pour tous les arcs de la boucle

importance(S) = Σ score(L | S ∈ L)  /  Σ score(toutes les boucles L)
```

### Stratégie de secours — ArcSumStrategy

Si aucune boucle n'est détectée (pas de nœud `«system»` ou graphe non cyclique), le plugin bascule sur une somme normalisée des arcs directement connectés à chaque partie prenante :

```
importance(S) = Σ score(arc | arc connecté à S)  /  Σ score(tous les arcs)
```

### Résultats écrits dans le modèle

- `importanceScore` mis à jour sur chaque `«stakeholder»`
- Le nom affiché de chaque acteur est suffixé par son score : `NomActeur : 0.4231`
- Le score de chaque arc est affiché comme label sur la dépendance
- Les tags `Loop_N`, `totalLoopScore`, et `mostImportantVL` sont mis à jour sur le `«system»`
- Le stéréotype `bestValueArc` est appliqué aux arcs de la boucle au score le plus élevé

---

## Architecture du code

```
src/main/java/fr/utbm/svn/
│
├── Main.java                              # Point d'entrée CLI
├── SVNPlugin.java                         # Point d'entrée plugin Rhapsody (RPUserPlugin)
├── Logger.java                            # Singleton de log avec niveaux colorés
│
├── controller/
│   └── Listener.java                      # Listener Rhapsody (afterAddElement, onElementsChanged)
│
├── service/
│   ├── ICalculationService.java           # Contrat du service de calcul
│   ├── ICalculationStrategy.java          # Contrat des stratégies de calcul
│   ├── impl/
│   │   └── CalculationService.java        # Orchestration : extraction → stratégie → écriture
│   └── strategy/
│       ├── ValueLoopStrategy.java         # Stratégie principale (DFS + équations Cameron)
│       └── ArcSumStrategy.java            # Stratégie de secours (somme normalisée des arcs)
│
├── model/
│   ├── Stakeholder.java                   # Wrapper IRPActor avec score d'importance
│   ├── ValueArc.java                      # Wrapper IRPDependency avec lecture de tags et score
│   ├── SVNSystem.java                     # Wrapper IRPClass (nœud système)
│   ├── ValueLoop.java                     # Représentation d'un cycle dans le graphe SVN
│   └── SearchState.java                   # État immutable du DFS
│
├── rhapsody/
│   ├── RhapsodyWrapper.java               # Utilitaires bas niveau (stéréotypes, tags, recherche)
│   └── RhapsodyElementUpdater.java        # Écriture des résultats dans le modèle Rhapsody
│
└── constants/
    └── SVNConstants.java                  # Noms des stéréotypes, tags et littéraux enum
```

### Flux d'exécution

```
Rhapsody change event
        │
        ▼
   Listener.onElementsChanged / afterAddElement
        │
        ├── (arc modifié) → RhapsodyElementUpdater.updateArcLabel
        │
        └── scheduleRecalculation (debounce 300 ms)
                │
                ▼
         CalculationService.calculateImportance
                │
                ├── RhapsodyWrapper.findStakeholders / findValueArcs / findSystem
                │
                ├── ValueLoopStrategy.computeScores  (ou ArcSumStrategy si pas de boucle)
                │
                └── RhapsodyElementUpdater.updateStakeholderImportance
                                         .updateSystemTags
```

---

## Tests

Les tests unitaires couvrent la logique métier sans nécessiter une instance Rhapsody.

```bash
mvn test
```

| Classe de test                 | Ce qui est testé                                      | Cas |
|-------------------------------|-------------------------------------------------------|-----|
| `ValueArcScoreTest`           | Matrice de score INCOSE (toutes les combinaisons)     | 10  |
| `ValueLoopTest`               | Calcul du score d'une boucle (produit des arcs)       | 5   |
| `SearchStateTest`             | État DFS : construction, immuabilité, transitions     | 3   |
| `ImportanceCalculationTest`   | Calcul d'importance de bout en bout sur des graphes   | 6   |

Les couches `Listener`, `RhapsodyWrapper` et `RhapsodyElementUpdater` ne sont pas testées unitairement car elles requièrent une connexion COM à Rhapsody.
