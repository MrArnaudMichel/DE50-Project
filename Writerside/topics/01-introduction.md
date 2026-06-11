# 1. Introduction

## Buts et destinataires du document

Ce document constitue le rapport de spécification du projet **RhapsodySVN**, réalisé dans le cadre de l'UV DE50. L'objectif de ce projet est la conception et le développement d'un plugin Java pour le logiciel de modélisation **IBM Rational Rhapsody 9.0**, permettant de créer, d'annoter et d'analyser des diagrammes de type **Stakeholder Value Network (SVN)**.

Le projet est réalisé dans un cadre académique et n'a pas vocation à être distribué ou commercialisé en dehors de l'établissement. Ce document s'adresse :

- aux **étudiants** membres de l'équipe de développement, comme référence de conception et d'implémentation ;
- aux **encadrants** du module DE50, pour la validation des choix techniques et fonctionnels ;
- à toute personne souhaitant **comprendre, utiliser ou faire évoluer** le plugin RhapsodySVN.

## Définitions et abréviations

| Terme / Abréviation | Définition |
|---|---|
| SVN | Stakeholder Value Network — méthode d'analyse des parties prenantes et de leurs échanges de valeur |
| SysML | Systems Modeling Language — langage de modélisation de systèmes basé sur UML |
| UML | Unified Modeling Language — langage de modélisation objet standard |
| IBM Rhapsody | Outil de modélisation SysML/UML développé par IBM (Rational) |
| INCOSE | International Council on Systems Engineering |
| API | Application Programming Interface |
| JDK | Java Development Kit |
| JAR | Java ARchive — format d'empaquetage d'applications Java |
| UC | Use Case — cas d'utilisation |
| DFS | Depth-First Search — algorithme de parcours en profondeur d'un graphe |
| RPUserPlugin | Interface Java de l'API Rhapsody permettant de déclarer un plugin |
| IRPApplication | Interface Rhapsody représentant l'application en cours d'exécution |
| IRPProject | Interface Rhapsody représentant le projet ouvert |
| IRPProfile | Interface Rhapsody représentant un profil SysML |
| IRPFlow | Interface Rhapsody représentant un flux (arc orienté) |
| IRPActor | Interface Rhapsody représentant un acteur |
| IRPCollection | Interface Rhapsody représentant une collection d'éléments du modèle |
| Stéréotype | Extension du métamodèle UML/SysML permettant de typer un élément |
| Tag | Propriété supplémentaire associée à un élément via un stéréotype |
| Value Loop | Cycle dans le graphe SVN passant par le nœud système central |