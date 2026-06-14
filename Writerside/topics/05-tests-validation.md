# 5. Tests et validation

## Stratégie de test

La stratégie de test adoptée sur ce projet est **centrée sur la logique métier pure**, c'est-à-dire les composants indépendants de l'API IBM Rhapsody. Les couches d'accès à Rhapsody (`Listener`, `RhapsodyWrapper`, `RhapsodyElementUpdater`) ne peuvent pas être testées sans une instance en cours d'exécution de l'application IBM, qui nécessite une licence et un environnement Windows dédié. Ces couches sont donc exclues des tests automatisés.

Les classes modèles (`ValueArc`, `ValueLoop`, `SearchState`) dépendent elles aussi transitivement de l'API Rhapsody. Pour contourner cette contrainte, chaque fichier de test contient une **reproduction locale simplifiée** de la classe testée, qui isole la logique pure sans aucun import Rhapsody.

La couverture automatisée porte sur trois domaines :

- **Le modèle de données** — score des arcs (`ValueArc`), score des loops (`ValueLoop`), état du parcours DFS (`SearchState`)
- **L'algorithme de détection des loops** — parcours DFS, construction des cycles
- **Le calcul d'importance** — implémentation des équations de Cameron via `ValueLoopStrategy`

Les tests sont écrits avec **JUnit 4** et organisés dans deux packages :
- `src/test/java/fr/utbm/svn/model/` — tests unitaires des modèles
- `src/test/java/fr/utbm/svn/service/strategy/` — tests d'intégration algorithmique

---

## Tests du modèle de données

### Matrice de score des arcs — `ValueArcScoreTest`

Ce fichier de test couvre l'ensemble des 9 cellules de la matrice de score définie à la section [A. Fondements théoriques](A-theorie-svn.md), plus le cas du score par défaut. La logique de `ValueArc.getArcScore()` est reproduite localement dans le test pour s'affranchir de la dépendance Rhapsody.

| Test | Combinaison testée | Score attendu |
|---|---|---|
| `mustBe_High` | `MUST_BE` / `HIGH` | 0.95 |
| `mustBe_Medium` | `MUST_BE` / `MEDIUM` | 0.80 |
| `mustBe_Low` | `MUST_BE` / `LOW` | 0.40 |
| `shouldBe_High` | `SHOULD_BE` / `HIGH` | 0.50 |
| `shouldBe_Medium` | `SHOULD_BE` / `MEDIUM` | 0.40 |
| `shouldBe_Low` | `SHOULD_BE` / `LOW` | 0.20 |
| `mightBe_High` | `MIGHT_BE` / `HIGH` | 0.30 |
| `mightBe_Medium` | `MIGHT_BE` / `MEDIUM` | 0.20 |
| `mightBe_Low` | `MIGHT_BE` / `LOW` | 0.10 |
| `unknownValue_returnsDefault` | Valeurs inconnues (défaut) | 0.20 |

### Modèle `ValueLoop` — `ValueLoopTest`

`ValueLoop` stocke un graphe de nœuds sous la forme d'une `Map<String, String>` (GUID → nom) et une liste de scores d'arcs. Le score de la loop est calculé **en dehors du modèle** par `ValueLoopStrategy` (produit des scores d'arcs), puis stocké via `setScore()`. Ces tests vérifient la structure de données et la cohérence du getter/setter.

| Test | Ce qui est vérifié |
|---|---|
| `constructor_storesNodesAndScores` | Le constructeur stocke correctement les nœuds et les scores d'arcs |
| `setScore_getScore` | `setScore()` et `getScore()` fonctionnent en aller-retour |
| `initialScoreIsZero` | Le score est 0.0 avant tout appel à `setScore()` |
| `calculateArcScoresProduct` | Produit de 3 scores : `0.5 × 0.8 × 0.4 = 0.16` |
| `calculateProductWithSingleArc` | Loop à un seul arc : score = score de l'arc (`0.95`) |

Le test `calculateArcScoresProduct` simule le calcul effectué par `ValueLoopStrategy` : il itère sur les scores d'arcs, les multiplie, et vérifie le résultat via `setScore()` / `getScore()`.

### État du parcours DFS — `SearchStateTest`

`SearchState` est le conteneur qui représente un état intermédiaire de la recherche DFS lors de la détection des value loops. Il stocke :

- le **nœud courant** (`String current`)
- le **chemin parcouru** (`List<String> path` — liste de GUIDs)
- les **scores accumulés** sur le chemin (`List<Double> scores`)
- l'ensemble des **nœuds visités** (`Set<String> visited`)

| Test | Ce qui est vérifié |
|---|---|
| `constructor_storesAllFields` | Les quatre champs sont correctement initialisés |
| `currentField` | `getCurrent()` renvoie le nœud courant |
| `emptyFields` | Path, scores et visited sont bien vides si construits avec des collections vides |

---

## Tests d'intégration algorithmique — `ImportanceCalculationTest`

Ce fichier de test valide l'ensemble de la chaîne de calcul : détection des loops par DFS + calcul de l'importance via l'**Équation 2** de Cameron. Il utilise des graphes construits en mémoire (sans Rhapsody) sous la forme d'une `Map<String, List<ArcEdge>>` (GUID source → liste d'arcs sortants) et appelle directement la logique de `ValueLoopStrategy`.

| Test | Graphe | Résultat attendu |
|---|---|---|
| `singleLoop_oneStakeholder` | S → A → S (arcs 0.5 et 0.8, score loop = 0.40) | importance(A) = 1.0 |
| `twoLoops_twoStakeholders` | S → A → S (score 0.40) et S → B → S (score 0.12), total 0.52 | importance(A) ≈ 0.769, importance(B) ≈ 0.231 |
| `noLoop_zeroImportance` | S → A sans arc retour (graphe acyclique) | aucune loop détectée, importance(A) = 0.0 |
| `loopWithThreeNodes` | S → A → B → S (arcs 0.5, 0.8, 0.4, score loop = 0.16) | importance(A) = 1.0, importance(B) = 1.0 |
| `stakeholderNotInLoop` | S → A → S, C non connecté au cycle | importance(A) = 1.0, importance(C) = 0.0 |
| `emptyGraph` | Aucun arc | aucune loop, importance = 0.0, aucune exception |

Le test `twoLoops_twoStakeholders` valide la normalisation de l'Équation 2 : avec deux loops de scores 0.40 et 0.12 (total 0.52), le stakeholder présent dans la loop de score 0.40 obtient `0.40 / 0.52 ≈ 0.769`.

Le test `noLoop_zeroImportance` couvre le cas où l'utilisateur n'a pas encore bouclé son graphe SVN — aucune loop n'est détectée et tous les scores restent à zéro, sans erreur.

---

## Limites de la couverture

| Composant | Couverture | Raison |
|---|---|---|
| `ValueArc.getArcScore()` | Complète (9/9 cas + défaut) | Logique reproduite localement, sans dépendance Rhapsody |
| `ValueLoop` | Complète (structure + calcul produit) | Reproduction locale de la structure de données |
| `SearchState` | Complète (3 tests) | Reproduction locale du conteneur DFS |
| `ValueLoopStrategy` | Fonctionnelle (6 scénarios end-to-end) | Logique reproduite sans Rhapsody via graphes synthétiques |
| `ArcSumStrategy` | Non couverte | Stratégie de secours, logique simple non critique |
| `Listener` | Non couverte | Nécessite une instance Rhapsody en cours d'exécution |
| `RhapsodyWrapper` | Non couverte | API Rhapsody non accessible sans licence |
| `RhapsodyElementUpdater` | Non couverte | API Rhapsody non accessible sans licence |
| `CalculationService` | Non couverte | Orchestrateur dépendant de Rhapsody |

L'isolation de l'API Rhapsody dans les classes dédiées (voir section [C. Conception et architecture](C-conception-architecture.md)) a été la condition préalable à la mise en place de ces tests : en concentrant tous les appels à Rhapsody dans `RhapsodyWrapper` et `RhapsodyElementUpdater`, les algorithmes métier peuvent être testés indépendamment de toute infrastructure IBM.