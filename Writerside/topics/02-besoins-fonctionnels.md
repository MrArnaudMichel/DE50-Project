# 2. Besoins fonctionnels

Le plugin expose **sept cas d'utilisation**, tous déclenchés par l'ingénieur système depuis le menu contextuel de
Rhapsody. Ils sont regroupés en trois grandes fonctionnalités : **initialisation**, **analyse SVN**, et **visualisation**.

> **Remarque** :
> Il est également possible de déclencher ces cas d'utilisation en ligne de commande lors du développement, mais l'usage
> principal est via le menu de Rhapsody.

Les différents cas d'utilisation sont répartis en quatre groupes :

| Groupe            | Cas d'utilisation                                    |
|-------------------|------------------------------------------------------|
| Initialisation    | UC1 — SVN Configure, UC7 — SVN Clean                 |
| Édition du modèle | UC2 — SVN Create Arc, UC3 — SVN Edit Arc             |
| Analyse SVN       | UC4 — SVN Calculate                                  |
| Visualisation     | UC5 — SVN Colorize Elements, UC6 — SVN Set Arc Label |

---

## UC1 — Configurer le profil SVN

| Champ                           | Contenu                                                                                                                                                                                                                                      |
|---------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Nom**                         | SVN Configure                                                                                                                                                                                                                                |
| **Description**                 | Crée ou recrée le profil `SVNProfile` dans le projet Rhapsody actif. Initialise les stéréotypes (`«stakeholder»`, `«system»`, `«valuearc»`, `«SVNDiagram»`), les types énumérés (`BenefitRanking`, `SupplyImportance`) et les tags associés. |
| **Entrées et provenances**      | Projet Rhapsody actif (via `IRPApplication.activeProject()`)                                                                                                                                                                                 |
| **Traitement**                  | `ProfileService.configureProfile(true)` — supprime l'ancien profil s'il existe, puis recrée tous les éléments du profil.                                                                                                                     |
| **Sorties**                     | Le profil `SVNProfile` est présent dans le projet. Message console `[SVN] Plugin initialisé`. Aucun retour visuel à l'utilisateur.                                                                                                           |
| **Classe ou instance associée** | `SVNPlugin` (init), `RhapsodyWrapper` (création des stéréotypes et tags)                                                                                                                                                                     |

---

## UC2 — Créer un arc de valeur

| Champ                           | Contenu                                                                                                                                                                                                                                                                                                               |
|---------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Nom**                         | SVN Create Arc                                                                                                                                                                                                                                                                                                        |
| **Description**                 | Crée un arc `«valuearc»` entre deux éléments sélectionnés dans le diagramme Rhapsody.                                                                                                                                                                                                                                 |
| **Entrées et provenances**      | Deux éléments sélectionnés graphiquement dans Rhapsody (source et cible de l'arc)                                                                                                                                                                                                                                     |
| **Traitement**                  | `DiagramService.createArcBetweenSelected()` — récupère la sélection courante via `IRPApplication.getSelectedGraphElements()`, vérifie que les deux nœuds portent un stéréotype SVN (`«stakeholder»` ou `«system»`), crée un `IRPDependency` avec le stéréotype `«valuearc»`, puis l'insère graphiquement dans le diagramme. |
| **Sorties**                     | Un arc `«valuearc»` apparaît dans le diagramme entre les deux éléments sélectionnés. Message console confirmant la création.                                                                                                                                                                                          |
| **Classe ou instance associée** | `Listener.afterAddElement()`, `ValueArc` (initialisation des tags par défaut), `RhapsodyElementUpdater.updateArcLabel()`                                                                                                                                                                                              |

---

## UC3 — Éditer les pondérations d'un arc

| Champ                           | Contenu                                                                                                                                                                                                                     |
|---------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Nom**                         | SVN Edit Arc                                                                                                                                                                                                                |
| **Description**                 | Permet à l'utilisateur de renseigner les deux tags de pondération d'un arc `«valuearc»` sélectionné : `benefitRanking` et `supplyImportance`.                                                                               |
| **Entrées et provenances**      | A définir                                                                                                                                                                                                                   |
| **Traitement**                  | Le `Listener.onElementsChanged()` détecte la modification d'un tag `benefitRanking` ou `supplyImportance` via le panneau de propriétés de Rhapsody. Il écrit la valeur mise à jour dans le modèle et déclenche le recalcul. |
| **Sorties**                     | Les tags `benefitRanking` et `supplyImportance` de l'arc sont mis à jour. Message d'avertissement si l'élément sélectionné n'est pas un `«valuearc»`.                                                                       |
| **Classe ou instance associée** | A définir                                                                                                                                                                                                                   |

**Valeurs possibles :**

| Tag                | Valeurs                            |
|--------------------|------------------------------------|
| `benefitRanking`   | `MIGHT_BE`, `SHOULD_BE`, `MUST_BE` |
| `supplyImportance` | `LOW`, `MEDIUM`, `HIGH`            |

---

## UC4 — Calculer l'importance des stakeholders

| Champ                           | Contenu                                                                                                                                                                                                                                                                                                                                                                        |
|---------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Nom**                         | SVN Calculate                                                                                                                                                                                                                                                                                                                                                                  |
| **Description**                 | Calcule le score d'importance normalisé de chaque `«stakeholder»` du modèle en appliquant les équations SVN ([Cameron 2007](X-references.md#cameron) / [INCOSE 2018](X-references.md#incose)).                                                                                                                                                                                     |
| **Entrées et provenances**      | Projet Rhapsody actif contenant des éléments `«stakeholder»`, `«system»` et `«valuearc»` avec leurs tags `benefitRanking` et `supplyImportance` renseignés                                                                                                                                                                                                                     |
| **Traitement**                  | `CalculationService.calculateImportance()` — parcourt récursivement le modèle, construit le graphe des arcs, recherche les *value loops* par DFS depuis le nœud `«system»`, puis calcule l'importance relative de chaque stakeholder (Équation 2, [Cameron 2007](X-references.md#cameron)). En l'absence de nœud `«system»`, bascule sur un calcul simplifié par somme des arcs. |
| **Sorties**                     | Le tag `importanceScore` (valeur entre 0 et 1) de chaque `«stakeholder»` est mis à jour dans le modèle. Messages console détaillant les loops trouvés et les scores calculés.                                                                                                                                                                                                  |
| **Classe ou instance associée** | `CalculationService`, `ValueLoopStrategy` / `ArcSumStrategy`, `ValueLoop`, `SearchState`, `RhapsodyElementUpdater.updateStakeholderImportance()`                                                                                                                                                                                                                               |

**Algorithme de calcul (méthode principale) :**

1. Construire le graphe orienté des arcs `«valuearc»`
2. Trouver tous les cycles (value loops) depuis le nœud `«system»` par DFS
3. **Équation 1** : score d'un loop = produit des scores de ses arcs
4. **Équation 2** : importance(stakeholder S) = Σ scores des loops contenant S / Σ scores de tous les loops

**Matrice de score des arcs ([INCOSE 2018](X-references.md#incose), Figure 3) :**

| `supplyImportance` ↓ \ `benefitRanking` → | `MIGHT_BE` | `SHOULD_BE` | `MUST_BE` |
|-------------------------------------------|------------|-------------|-----------|
| `HIGH`                                    | 0.30       | 0.50        | 0.95      |
| `MEDIUM`                                  | 0.20       | 0.40        | 0.80      |
| `LOW`                                     | 0.10       | 0.20        | 0.40      |

---

## UC5 — Coloriser les éléments SVN

| Champ                           | Contenu                                                                                                                                                                                                              |
|---------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Nom**                         | SVN Colorize Elements                                                                                                                                                                                                |
| **Description**                 | Applique une couleur à chaque arc `«valuearc»` dans les diagrammes SVN en fonction du score calculé. La coloration des nœuds `«stakeholder»` est prévue mais non encore implémentée.                                |
| **Entrées et provenances**      | Modèle Rhapsody avec des `«stakeholder»` dont le tag `importanceScore` est renseigné (nécessite l'exécution préalable de UC4)                                                                                        |
| **Traitement**                  | Trie les arcs `«valuearc»` par score décroissant, les divise en trois tiers, applique les couleurs rouge/orange/jaune via `RhapsodyWrapper` (propriété graphique `LineColor`). La logique de tiers sur les nœuds `«stakeholder»` (propriété `FillColor`) est conçue mais non encore intégrée. |
| **Sorties**                     | Les arcs `«valuearc»` sont colorisés dans les diagrammes SVN (rouge = arcs critiques, orange = importants, jaune = secondaires).                                                                                     |
| **Classe ou instance associée** | `RhapsodyElementUpdater.updateArcLabel()`, `ValueArc`, `RhapsodyWrapper`                                                                                                                                             |

**Code couleur appliqué :**

| Tier                                    | Couleur            | Signification       |
|-----------------------------------------|--------------------|---------------------|
| Tiers supérieur (1/3 le plus important) | Rouge (`#FF4444`)  | Arcs critiques      |
| Tiers médian                            | Orange (`#FFA500`) | Arcs importants     |
| Tiers inférieur                         | Jaune (`#FFFF00`)  | Arcs secondaires    |

---

## UC6 — SVN Set Arc Label

| Champ                           | Contenu                                                                                                                                                                                                                              |
|---------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Nom**                         | SVN Set Arc Label                                                                                                                                                                                                                    |
| **Description**                 | Met à jour manuellement le label d'un arc `«valuearc»` sélectionné pour afficher son score numérique calculé depuis ses tags `benefitRanking` et `supplyImportance`.                                                                 |
| **Entrées et provenances**      | Arc `«valuearc»` sélectionné dans Rhapsody avec ses tags `benefitRanking` et `supplyImportance` renseignés                                                                                                                           |
| **Traitement**                  | `RhapsodyElementUpdater.updateArcLabel()` — calcule le score numérique de l'arc via `ValueArc.getScore()` et met à jour son `displayName`. En cas d'échec, tente une mise à jour via `IRPGraphElement.setGraphicalPropertyOfText()`. |
| **Sorties**                     | L'arc affiche son score numérique dans le diagramme (ex. : `0.95`). Message console confirmant la mise à jour.                                                                                                                       |
| **Classe ou instance associée** | `RhapsodyElementUpdater.updateArcLabel()`, `ValueArc.getScore()`                                                                                                                                                                     |

---

## UC7 — Nettoyer le modèle

| Champ                           | Contenu                                                                                                                                                                                                                                                   |
|---------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Nom**                         | SVN Clean                                                                                                                                                                                                                                                 |
| **Description**                 | Supprime tous les éléments SVN du package `Default` du projet Rhapsody (stakeholders, systems, valuearcs, diagrammes SVN) ainsi que le profil `SVNProfile`. Remet le modèle dans un état vierge.                                                          |
| **Entrées et provenances**      | Projet Rhapsody actif                                                                                                                                                                                                                                     |
| **Traitement**                  | `ProfileService.cleanDefaultPackage()` puis `ProfileService.deleteProfile()` — collecte tous les éléments SVN avant suppression, supprime d'abord les arcs, puis les nœuds, puis les diagrammes, dans le bon ordre pour éviter les erreurs de dépendance. |
| **Sorties**                     | Le modèle ne contient plus d'éléments SVN. Le profil `SVNProfile` est supprimé. Message console indiquant le nombre d'éléments supprimés.                                                                                                                 |
| **Classe ou instance associée** | `RhapsodyWrapper`, `SVNConstants`                                                                                                                                                                                                                         |
