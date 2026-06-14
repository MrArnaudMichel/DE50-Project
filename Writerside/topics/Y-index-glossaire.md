# Index

Les références de section utilisent les abréviations suivantes :
**Intro** = Introduction · **A–D** = sections de la Partie I · **1–5** = sections de la Partie II · **Ann.** = Annexes

## Index alphabétique des termes

| Terme | Section(s) |
|---|---|
| API | Intro, 1, 4 |
| Arc de valeur (`«valuearc»`) | A, 2 (UC2, UC3, UC5, UC6), 3, 4, 5 |
| `ArcSumStrategy` | C, 2 (UC4), 5 |
| `BenefitRanking` | A, 2 (UC3, UC4), 3, Ann. B |
| `CalculationService` | C, 1, 2 (UC4), 5 |
| Cameron (2007) | A, 1, 2 (UC4), Ann. A |
| COM | Intro, 1, 4 |
| DFS (Depth-First Search) | A, 2 (UC4), 5, Ann. A |
| Diagramme SVN (`«SVNDiagram»`) | 2, 3 |
| IBM Rhapsody | Intro, 1, 4, 5 |
| IBD / BDD | C, 5 |
| `ICalculationStrategy` | C, 1, 5 |
| `importanceScore` | 2 (UC4, UC5), 3 |
| INCOSE 2018 | A, 1, 2 (UC4), Ann. B |
| `IRPApplication` | 1, 4 |
| `IRPActor` | 3, 4 |
| `IRPDependency` | Intro, C, 2 (UC2), 3, 4 |
| `IRPProject` | 1, 4 |
| Java 8 / JDK 1.8 | 1, 4 |
| `Listener` | C, 1, 5 |
| `Logger` | C, 1, 4, 5 |
| Maven | 1, 4 |
| MBSE | Intro, A |
| Nœud système (`«system»`) | A, 2 (UC4), 3 |
| OMG | 1 |
| POC | B |
| `RhapsodyElementUpdater` | C, 1, 2 (UC2, UC6), 5 |
| `RhapsodyWrapper` | C, 1, 2, 5 |
| `RPUserPlugin` | Intro, 1 |
| `SearchState` | 2 (UC4), 5 |
| Stakeholder (`«stakeholder»`) | A, 2 (UC4, UC5), 3 |
| Stéréotype | Intro, 1, 2, 3 |
| `supplyImportance` / `SupplyImportance` | A, 2 (UC3, UC4), 3, Ann. B |
| `SVNConstants` | C, 1 |
| `SVNPlugin` | 1, 4 |
| `SVNProfile` | 2 (UC1, UC7), 3 |
| SysML | Intro, 1 |
| Tag | Intro, 3, 4 |
| Value loop | A, 2 (UC4), 5, Ann. A |
| `ValueArc` | 2, 3, 5 |
| `ValueLoop` | A, 2 (UC4), 5 |
| `ValueLoopStrategy` | C, 2 (UC4), 5 |
| Windows | 1, 4, 5 |

## Index des fonctions et classes

| Classe / Méthode | Rôle | Section(s) |
|---|---|---|
| `SVNPlugin` | Point d'entrée du plugin, connexion à Rhapsody | 1 |
| `Main` | Point d'entrée ligne de commande (hors Rhapsody) | 1 |
| `Listener` | Réception des événements Rhapsody, orchestration des mises à jour | C, 1 |
| `CalculationService.calculateImportance()` | Orchestre le calcul d'importance SVN | C, 2 (UC4) |
| `ValueLoopStrategy` | Calcul principal par DFS — équations Cameron | C, 2 (UC4), 5 |
| `ArcSumStrategy` | Calcul simplifié par somme des arcs (fallback sans `«system»`) | C, 2 (UC4) |
| `Logger` | Singleton de journalisation (niveaux info / warn / error) | C, 4 |
| `RhapsodyWrapper.hasStereotype()` | Vérifie si un élément porte un stéréotype donné | C |
| `RhapsodyWrapper.findStakeholders()` | Collecte les `«stakeholder»` d'un diagramme | 2 (UC4) |
| `RhapsodyWrapper.findValueArcs()` | Collecte les arcs `«valuearc»` d'un diagramme | 2 (UC4) |
| `RhapsodyWrapper.initTagIfAbsent()` | Initialise un tag avec sa valeur par défaut si absent | 5 |
| `RhapsodyElementUpdater.updateArcLabel()` | Met à jour le label d'un arc avec son score calculé | 2 (UC2, UC6) |
| `RhapsodyElementUpdater.updateStakeholderImportance()` | Écrit le score d'importance d'un stakeholder dans le modèle | 2 (UC4) |
| `RhapsodyElementUpdater.updateSystemTags()` | Met à jour les tags du nœud `«system»` après calcul | 2 (UC4) |
| `SVNConstants` | Centralisation des noms de stéréotypes et de tags | C |