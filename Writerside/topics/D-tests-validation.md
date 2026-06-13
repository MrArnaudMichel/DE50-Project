# D. Tests et validation

> **Note** : partie à modifier
{style=warning}

## Stratégie de test

La stratégie de test adoptée sur ce projet est **centrée sur la logique métier pure**, c'est-à-dire les composants indépendants de l'API IBM Rhapsody. Les couches d'accès à Rhapsody (`Listener`, `RhapsodyWrapper`, `RhapsodyElementUpdater`) ne peuvent pas être testées sans une instance en cours d'exécution de l'application IBM, qui nécessite une licence et un environnement Windows dédié. Ces couches sont donc exclues des tests automatisés.

La couverture automatisée porte sur trois domaines :

- **Le modèle de données** — score des arcs (`ValueArc`), score des loops (`ValueLoop`), état du parcours DFS (`SearchState`)
- **L'algorithme de détection des loops** — parcours DFS, construction des cycles
- **Le calcul d'importance** — implémentation des équations de Cameron via `ValueLoopStrategy`

Les tests sont écrits avec **JUnit 4** et placés dans `src/test/java/fr/utbm/svn/`.

---

## Tests du modèle de données

### Matrice de score des arcs — `ValueArcScoreTest`

Ce fichier de test couvre l'ensemble des 9 cellules de la matrice de score définie à la section [A. Fondements théoriques](A-theorie-svn.md), plus le cas du score par défaut pour des valeurs inconnues.

| Combinaison testée | Score attendu |
|---|---|
| `HIGH` / `MUST_BE` | 0.95 |
| `MEDIUM` / `MUST_BE` | 0.80 |
| `LOW` / `MUST_BE` | 0.40 |
| `HIGH` / `SHOULD_BE` | 0.50 |
| `MEDIUM` / `SHOULD_BE` | 0.40 |
| `LOW` / `SHOULD_BE` | 0.20 |
| `HIGH` / `MIGHT_BE` | 0.30 |
| `MEDIUM` / `MIGHT_BE` | 0.20 |
| `LOW` / `MIGHT_BE` | 0.10 |
| Valeurs inconnues (défaut) | 0.20 |

Ces tests garantissent la conformité de l'implémentation avec les valeurs de la matrice et documentent le comportement du chemin par défaut (score 0.20 renvoyé lorsque les attributs ne correspondent à aucune combinaison connue).

### Modèle `ValueLoop` — `ValueLoopTest`

Ces tests vérifient le comportement du conteneur de value loop, notamment le calcul du score via l'**Équation 1** (produit des scores des arcs) :

| Test | Ce qui est vérifié |
|---|---|
| `constructor_storeArcs` | Le constructeur stocke correctement la liste des arcs |
| `getScore_returnsZero_initially` | Le score vaut 0 en l'absence de calcul préalable |
| `getScore_singleArc` | Loop à un seul arc : score = score de l'arc |
| `getScore_multipleArcs_product` | Loop à plusieurs arcs : score = produit des scores individuels |
| `getScore_afterUpdate` | Mise à jour du score après modification des arcs |

Le test multi-arcs est particulièrement important : il valide que l'implémentation applique bien le **produit** et non une somme. Par exemple, pour une loop de scores `[0.5, 0.4, 0.8]`, le score attendu est `0.5 × 0.4 × 0.8 = 0.16`.

### État du parcours DFS — `SearchStateTest`

`SearchState` est le conteneur immuable qui représente un état intermédiaire de la recherche DFS lors de la détection des value loops. Il stocke :

- le **nœud courant** du parcours,
- le **chemin parcouru** depuis le nœud de départ,
- les **scores accumulés** sur le chemin,
- l'ensemble des **nœuds visités**.

| Test | Ce qui est vérifié |
|---|---|
| `constructor_storesAllFields` | Tous les champs sont correctement initialisés |
| `currentField` | L'accesseur `getCurrent()` renvoie le nœud courant |
| `emptyFields` | Comportement correct avec des listes et ensembles vides |

Ces tests garantissent l'intégrité de l'état transmis d'une itération DFS à la suivante.

---

## Tests d'intégration algorithmique — `ImportanceCalculationTest`

Ce fichier de test valide l'ensemble de la chaîne de calcul : détection des loops par DFS + calcul de l'importance via l'**Équation 2** de Cameron. Il utilise des graphes construits en mémoire (sans Rhapsody) et appelle directement la stratégie `ValueLoopStrategy`.

| Test | Graphe | Résultat attendu |
|---|---|---|
| `singleLoop` | S → SH1 → S (1 loop, 1 arc de score connu) | Score stakeholder = 1.0 (seule loop du réseau) |
| `twoLoops_stakeholderInBoth` | S → SH1 → S et S → SH2 → SH1 → S | SH1 présent dans les 2 loops, SH2 dans 1 seule |
| `noLoop_returnsZero` | Graphe acyclique sans nœud `«system»` | Score = 0 pour tous les stakeholders |
| `threeNodeLoop` | S → SH1 → SH2 → S (loop de 3 nœuds) | Score = produit des 3 arcs |
| `stakeholderNotInLoop` | SH2 non connecté au cycle principal | Importance(SH2) = 0 |
| `emptyGraph` | Aucun nœud | Aucune exception, résultat vide |

Le test `noLoop_returnsZero` couvre un cas d'usage réel : si l'utilisateur n'a pas encore ajouté de nœud `«system»` dans son modèle, aucune loop ne peut être détectée et tous les scores restent à zéro. Ce comportement est géré proprement sans erreur.

Le test `emptyGraph` valide la robustesse de l'algorithme face à un modèle vide, cas pouvant survenir au démarrage du plugin avant tout ajout d'éléments.

---

## Limites de la couverture

| Composant | Couverture | Raison |
|---|---|---|
| `ValueArc.getArcScore()` | Complète (10/10 cas) | Logique pure, sans dépendance externe |
| `ValueLoop.getScore()` | Complète | Logique pure |
| `SearchState` | Complète | Structure de données simple |
| `ValueLoopStrategy` | Fonctionnelle (6 scénarios) | Testable avec graphes synthétiques |
| `ArcSumStrategy` | Non couverte | Stratégie de secours, logique simple non critique |
| `Listener` | Non couverte | Nécessite une instance Rhapsody en cours d'exécution |
| `RhapsodyWrapper` | Non couverte | API Rhapsody non mocable sans licence |
| `RhapsodyElementUpdater` | Non couverte | API Rhapsody non mocable sans licence |
| `CalculationService` | Non couverte | Orchestrateur dépendant de Rhapsody |

L'isolation de l'API Rhapsody dans les classes dédiées (voir section [C. Conception et architecture](C-conception-architecture.md)) a été la condition préalable à la mise en place de ces tests : en concentrant tous les appels à Rhapsody dans `RhapsodyWrapper` et `RhapsodyElementUpdater`, les algorithmes métier (`ValueLoopStrategy`, `ValueLoop`, `ValueArc`) peuvent être testés indépendamment de toute infrastructure IBM.