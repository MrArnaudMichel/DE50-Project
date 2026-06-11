# 3. Besoins fonctionnels

Le plugin expose **huit cas d'utilisation**, tous déclenchés par l'ingénieur système depuis le menu contextuel de
Rhapsody. Ils sont regroupés en trois grandes fonctionnalités : **initialisation**, **analyse SVN**, et **visualisation
**.

> **Remarque** :
> Il est également possible de déclancher ses cas d'utilisation en ligne de commande lors du développement, mais l'usage
> principal est via le menu de Rhapsody.

Les différents cas d'utilisation sont répartis en quatres groupes :

| Groupe            | Cas d'utilisation                                                                     |
|-------------------|---------------------------------------------------------------------------------------|
| Initialisation    | UC1 — SVN Configure, UC8 — SVN Clean                                                  |
| Édition du modèle | UC2 — SVN Create Arc, UC3 — SVN Edit Arc                                              |
| Analyse SVN       | UC4 — SVN Calculate                                                                   |
| Visualisation     | UC5 — SVN Colorize Stakeholders, UC6 — SVN Set Arc Color, UC7 — SVN Update Arc Labels |

---

## UC1 — Configurer le profil SVN

| Champ                      | Contenu                                                                                                                                                                                                                                      |
|----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Nom**                          | SVN Configure                                                                                                                                                                                                                                |
| **Description**                  | Crée ou recrée le profil `SVNProfile` dans le projet Rhapsody actif. Initialise les stéréotypes (`«stakeholder»`, `«system»`, `«valuearc»`, `«SVNDiagram»`), les types énumérés (`BenefitRanking`, `SupplyImportance`) et les tags associés. |
| **Entrées et provenances**       | Projet Rhapsody actif (via `IRPApplication.activeProject()`)                                                                                                                                                                                 |
| **Traitement**                   | `ProfileService.configureProfile(true)` — supprime l'ancien profil s'il existe, puis recrée tous les éléments du profil.                                                                                                                     |
| **Sorties**                      | Le profil `SVNProfile` est présent dans le projet. Message console `[SVN] Plugin initialisé`. Aucun retour visuel à l'utilisateur.                                                                                                           |
| **Classe ou instance associée**  | `SVNPlugin` (init), `RhapsodyWrapper` (création des stéréotypes et tags)                                                                                                                                                                     |

---

## UC2 — Créer un arc de valeur

| Champ                      | Contenu                                                                                                                                                                                                                                                                                                               |
|----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Nom**                          | SVN Create Arc                                                                                                                                                                                                                                                                                                        |
| **Description**                  | Crée un arc `«valuearc»` entre deux éléments sélectionnés dans le diagramme Rhapsody.                                                                                                                                                                                                                                 |
| **Entrées et provenances**       | Deux éléments sélectionnés graphiquement dans Rhapsody (source et cible de l'arc)                                                                                                                                                                                                                                     |
| **Traitement**                   | `DiagramService.createArcBetweenSelected()` — récupère la sélection courante via `IRPApplication.getSelectedGraphElements()`, vérifie que les deux nœuds portent un stéréotype SVN (`«stakeholder»` ou `«system»`), crée un `IRPFlow` avec le stéréotype `«valuearc»`, puis l'insère graphiquement dans le diagramme. |
| **Sorties**                      | Un arc `«valuearc»` apparaît dans le diagramme entre les deux éléments sélectionnés. Message console confirmant la création.                                                                                                                                                                                          |
| **Classe ou instance associée**  | `Listener.afterAddElement()`, `ValueArc` (initialisation des tags par défaut), `UpdateElementService.updateArcLabel()`                                                                                                                                                                                                |

---

## UC3 — Éditer les pondérations d'un arc

| Champ                      | Contenu                                                                                                                                                                                                                                        |
|----------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Nom**                          | SVN Edit Arc                                                                                                                                                                                                                                   |
| **Description**                  | Permet à l'utilisateur de renseigner les deux tags de pondération d'un arc `«valuearc»` sélectionné : `benefitRanking` et `supplyImportance`.                                                                                                  |
| **Entrées et provenances**       | Arc `«valuearc»` sélectionné dans Rhapsody ; choix de l'utilisateur via deux boîtes de dialogue Swing (`JOptionPane`)                                                                                                                          |
| **Traitement**                   | Vérifie que l'élément sélectionné est bien un `IRPFlow` portant le stéréotype `«valuearc»`. Ouvre deux dialogues de sélection successifs (listes déroulantes), puis écrit les valeurs choisies dans les tags de l'arc et sauvegarde le projet. |
| **Sorties**                      | Les tags `benefitRanking` et `supplyImportance` de l'arc sont mis à jour. Message d'avertissement si l'élément sélectionné n'est pas un `«valuearc»`.                                                                                          |
| **Classe ou instance associée**  | `Listener.onElementsChanged()`, `ValueArc.getTagValue()`, `RhapsodyWrapper.setOrCreateTag()`                                                                                                                                                   |

**Valeurs possibles :**

| Tag                | Valeurs                            |
|--------------------|------------------------------------|
| `benefitRanking`   | `MIGHT_BE`, `SHOULD_BE`, `MUST_BE` |
| `supplyImportance` | `LOW`, `MEDIUM`, `HIGH`            |

---

## UC4 — Calculer l'importance des stakeholders

| Champ                      | Contenu                                                                                                                                                                                                                                                                                                                                               |
|----------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Nom**                          | SVN Calculate                                                                                                                                                                                                                                                                                                                                         |
| **Description**                  | Calcule le score d'importance normalisé de chaque `«stakeholder»` du modèle en appliquant les équations SVN ([Cameron 2007](references.md#cameron) / [INCOSE 2018](references.md#incose)).                                                                                                                                                                                                             |
| **Entrées et provenances**       | Projet Rhapsody actif contenant des éléments `«stakeholder»`, `«system»` et `«valuearc»` avec leurs tags `benefitRanking` et `supplyImportance` renseignés                                                                                                                                                                                            |
| **Traitement**                   | `CalculationService.calculateImportance()` — parcourt récursivement le modèle, construit le graphe des arcs, recherche les *value loops* par DFS depuis le nœud `«system»`, puis calcule l'importance relative de chaque stakeholder (Équation 2, [Cameron 2007](references.md#cameron)). En l'absence de nœud `«system»`, bascule sur un calcul simplifié par somme des arcs. |
| **Sorties**                      | Le tag `importanceScore` (valeur entre 0 et 1) de chaque `«stakeholder»` est mis à jour dans le modèle. Messages console détaillant les loops trouvés et les scores calculés.                                                                                                                                                                         |
| **Classe ou instance associée**  | `CalculationService`, `ValueLoopStrategy` / `ArcSumStrategy`, `ValueLoop`, `SearchState`, `UpdateElementService.updateStakeholderImportance()`                                                                                                                                                                                                        |

**Algorithme de calcul (méthode principale) :**

1. Construire le graphe orienté des arcs `«valuearc»`
2. Trouver tous les cycles (value loops) depuis le nœud `«system»` par DFS
3. **Équation 1** : score d'un loop = produit des scores de ses arcs
4. **Équation 2** : importance(stakeholder S) = Σ scores des loops contenant S / Σ scores de tous les loops

**Matrice de score des arcs ([INCOSE 2018](references.md#incose), Figure 3) :**

| `supplyImportance` ↓ \ `benefitRanking` → | `MIGHT_BE` | `SHOULD_BE` | `MUST_BE` |
|-------------------------------------------|------------|-------------|-----------|
| `HIGH`                                    | 0.30       | 0.50        | 0.95      |
| `MEDIUM`                                  | 0.20       | 0.40        | 0.80      |
| `LOW`                                     | 0.10       | 0.20        | 0.40      |

---

## UC5 — Coloriser les stakeholders

| Champ                      | Contenu                                                                                                                                                                                                                       |
|----------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Nom**                          | SVN Colorize Stakeholders                                                                                                                                                                                                     |
| **Description**                  | Applique une couleur de remplissage à chaque nœud `«stakeholder»` dans les diagrammes SVN, en fonction de leur rang d'importance calculé.                                                                                     |
| **Entrées et provenances**       | Modèle Rhapsody avec des `«stakeholder»` dont le tag `importanceScore` est renseigné (nécessite l'exécution préalable de UC4)                                                                                                 |
| **Traitement**                   | `DiagramService.colorizeStakeholdersByRank()` — trie les stakeholders par score décroissant, les divise en trois tiers, applique les couleurs rouge/orange/jaune selon l'importance, et affiche le score dans le nom affiché. |
| **Sorties**                      | Les nœuds `«stakeholder»` sont colorisés dans les diagrammes SVN (rouge = plus importants, orange = importants, jaune = moins importants).                                                                                    |
| **Classe ou instance associée**  | `UpdateElementService.updateStakeholderImportance()`, `Stakeholder`                                                                                                                                                           |

**Code couleur appliqué :**

| Tier                                    | Couleur            | Signification            |
|-----------------------------------------|--------------------|--------------------------|
| Tiers supérieur (1/3 le plus important) | Rouge (`#FF4444`)  | Stakeholders critiques   |
| Tiers médian                            | Orange (`#FFA500`) | Stakeholders importants  |
| Tiers inférieur                         | Jaune (`#FFFF00`)  | Stakeholders secondaires |

---

## UC6 — Coloriser un arc de valeur

| Champ                      | Contenu                                                                                                                                                                                                                                |
|----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Nom**                          | SVN Set Arc Color                                                                                                                                                                                                                      |
| **Description**                  | Permet à l'utilisateur de choisir une couleur personnalisée pour un arc `«valuearc»` sélectionné dans le diagramme.                                                                                                                    |
| **Entrées et provenances**       | Arc `«valuearc»` sélectionné dans Rhapsody ; couleur choisie via une boîte de dialogue `JColorChooser`                                                                                                                                 |
| **Traitement**                   | `DiagramService.setArcColor()` — vérifie que l'élément sélectionné est un `«valuearc»`, puis applique la couleur choisie (propriété graphique `LineColor`) sur toutes les représentations graphiques de l'arc dans les diagrammes SVN. |
| **Sorties**                      | La couleur de l'arc est mise à jour dans le ou les diagrammes SVN. Message console confirmant l'application de la couleur.                                                                                                             |
| **Classe ou instance associée**  | `ValueArc`, `RhapsodyWrapper`                                                                                                                                                                                                          |

---

## UC7 — Mettre à jour les labels d'arcs

| Champ                            | Contenu                                                                                                                                                |
|----------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Nom**                          | SVN Update Arc Labels                                                                                                                                  |
| **Description**                  | Affiche les valeurs de pondération (`benefitRanking` et `supplyImportance`) directement sur chaque arc `«valuearc»` dans les diagrammes SVN.           |
| **Entrées et provenances**       | Projet Rhapsody actif contenant des arcs `«valuearc»` avec leurs tags renseignés                                                                       |
| **Traitement**                   | `UpdateElementService.updateArcLabel()` — calcule le score numérique de l'arc et met à jour son `displayName`. En cas d'échec, tente une mise à jour via les propriétés graphiques des éléments de diagramme. |
| **Sorties**                      | Les arcs affichent leur score numérique dans le diagramme (ex. : `0.95`).                                                                              |
| **Classe ou instance associée**  | `UpdateElementService.updateArcLabel()`, `ValueArc.getScore()`                                                                                         |

---

## UC8 — Nettoyer le modèle

| Champ                      | Contenu                                                                                                                                                                                                                                                   |
|----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Nom**                          | SVN Clean                                                                                                                                                                                                                                                 |
| **Description**                  | Supprime tous les éléments SVN du package `Default` du projet Rhapsody (stakeholders, systems, valuearcs, diagrammes SVN) ainsi que le profil `SVNProfile`. Remet le modèle dans un état vierge.                                                          |
| **Entrées et provenances**       | Projet Rhapsody actif                                                                                                                                                                                                                                     |
| **Traitement**                   | `ProfileService.cleanDefaultPackage()` puis `ProfileService.deleteProfile()` — collecte tous les éléments SVN avant suppression, supprime d'abord les arcs, puis les nœuds, puis les diagrammes, dans le bon ordre pour éviter les erreurs de dépendance. |
| **Sorties**                      | Le modèle ne contient plus d'éléments SVN. Le profil `SVNProfile` est supprimé. Message console indiquant le nombre d'éléments supprimés.                                                                                                                 |
| **Classe ou instance associée**  | `RhapsodyWrapper`, `SVNConstants`                                                                                                                                                                                                                         |
