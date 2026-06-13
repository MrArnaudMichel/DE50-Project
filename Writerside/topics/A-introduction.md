# 1. Introduction

## Contexte

**IBM Rational Rhapsody** est un environnement de référence pour la modélisation de systèmes complexes. Il permet aux ingénieurs systèmes de concevoir leurs architectures en SysML/UML dans un cadre MBSE (Model-Based Systems Engineering). Cependant, Rhapsody ne propose aucun support natif de la méthode **Stakeholder Value Network (SVN)**.

Aujourd'hui, les ingénieurs souhaitant analyser les relations de valeur entre un système et ses parties prenantes sont contraints de réaliser ces calculs manuellement, en dehors de leur outil de modélisation. Cette démarche est source de plusieurs problèmes :

- les calculs sont effectués hors modèle, dans des tableurs ou des outils externes non connectés à Rhapsody ;
- toute modification du modèle (ajout d'un acteur, changement de pondération) nécessite une ressaisie complète ;
- le risque d'erreurs de calcul est élevé, en particulier sur des réseaux de grande taille.

## Objectifs du projet

Le projet **RhapsodySVN** vise à résoudre ce problème en développant un **plugin Java pour IBM Rhapsody 9.0** qui intègre nativement la méthode SVN dans l'environnement de modélisation. L'objectif est triple :

| Axe | Objectif | Résultat attendu |
|---|---|---|
| **Modéliser** | Représenter graphiquement les arcs de valeur entre acteurs et système directement dans Rhapsody | Graphe orienté pondéré intégré au modèle SysML |
| **Quantifier** | Calculer automatiquement l'importance relative de chaque stakeholder (équations Cameron 2007) | Classement objectif (score 0–1) mis à jour en temps réel |
| **Informer** | Propager les résultats dans le modèle pour guider les choix d'architecture | Base quantitative pour les arbitrages systèmes |

## Apport du plugin

Le tableau ci-dessous illustre concrètement la valeur ajoutée du plugin par rapport à la situation actuelle :

| Critère | Sans plugin | Avec RhapsodySVN |
|---|---|---|
| Modélisation SVN | Outil externe, déconnecté du modèle | Natif dans Rhapsody, intégré au modèle SysML |
| Calcul des scores | Manuel, source d'erreurs | Automatique (équation Cameron Eq.2) |
| Mise à jour | Ressaisie complète à chaque modification | Dynamique, event-driven (Listener) |
| Visualisation | Aucune priorisation visuelle | Colorisation automatique par rang d'importance |
| Intégration modèle | Copie manuelle des résultats | Tag values directement dans les éléments SysML |

## Structure du document

Ce rapport est organisé en deux parties distinctes :

- **Partie I — Rapport** : présente les fondements théoriques de la méthode SVN, la planification et le déroulement du projet, les décisions d'architecture, et la stratégie de tests.
- **Partie II — Spécification technique** : décrit le système de façon formelle — description générale, besoins fonctionnels, structures de données, interfaces et avancement du projet.

---

## Définitions et abréviations

| Terme / Abréviation | Définition |
|---|---|
| SVN | Stakeholder Value Network — méthode d'analyse des parties prenantes et de leurs échanges de valeur |
| SysML | Systems Modeling Language — langage de modélisation de systèmes basé sur UML |
| UML | Unified Modeling Language — langage de modélisation objet standard |
| IBM Rhapsody | Outil de modélisation SysML/UML développé par IBM (Rational) |
| MBSE | Model-Based Systems Engineering — ingénierie système pilotée par le modèle |
| INCOSE | International Council on Systems Engineering |
| API | Application Programming Interface |
| JDK | Java Development Kit |
| JAR | Java ARchive — format d'empaquetage d'applications Java |
| UC | Use Case — cas d'utilisation |
| DFS | Depth-First Search — algorithme de parcours en profondeur d'un graphe |
| Value Loop | Cycle dans le graphe SVN passant par le nœud système central |
| RPUserPlugin | Interface Java de l'API Rhapsody permettant de déclarer un plugin |
| IRPApplication | Interface Rhapsody représentant l'application en cours d'exécution |
| IRPProject | Interface Rhapsody représentant le projet ouvert |
| IRPActor | Interface Rhapsody représentant un acteur |
| IRPDependency | Interface Rhapsody représentant une dépendance UML (utilisée pour les arcs `«valuearc»`) |
| Stéréotype | Extension du métamodèle UML/SysML permettant de typer un élément |
| Tag | Propriété supplémentaire associée à un élément via un stéréotype |