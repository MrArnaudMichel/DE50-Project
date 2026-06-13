# Introduction

Ce projet s'inscrit dans le contexte de l'UV DE50 de l'Université de Technologie de Belfort-Montbéliard (UTBM).

Il a été réalisé par **Guillaume RETTER**, **Hugo CHARCOT**, **Arnaud MICHEL**, **Benjamin GROISNE** et **Antoine LAURANT** sous la supervision de **Vincent HILAIRE**

## Objectifs et apport du projet

**IBM Rational Rhapsody** est un environnement de référence pour la modélisation de systèmes complexes. Il permet aux ingénieurs systèmes de concevoir leurs architectures en SysML/UML dans un cadre MBSE (Model-Based Systems Engineering). Cependant, Rhapsody ne propose aucun support natif de la méthode **Stakeholder Value Network (SVN)**.

Le projet **RhapsodySVN** vise à résoudre ce problème en développant un **plugin Java pour IBM Rhapsody 9.0** qui intègre nativement la méthode SVN dans l'environnement de modélisation. Concrètement, le plugin permet de :

- **modéliser** graphiquement les arcs de valeur entre acteurs et système directement dans un diagramme Rhapsody, sans recourir à un outil externe ;
- **quantifier** automatiquement l'importance relative de chaque partie prenante selon les équations de Cameron (2007), avec mise à jour en temps réel à chaque modification du modèle ;
- **informer** les choix d'architecture en propageant les scores calculés directement dans les éléments du modèle SysML, sous forme de tag values.

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