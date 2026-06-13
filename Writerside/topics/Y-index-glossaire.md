# Index

## Index alphabétique des termes

| Terme | Section(s) |
|---|---|
| API | §1, §2, §5 |
| Arc de valeur (`«valuearc»`) | §2, §3 (UC2, UC3, UC6, UC7), §4, §5 |
| `BenefitRanking` | §3 (UC3, UC4), §4, Annexe B |
| `CalculationService` | §2, §3 (UC4) |
| Cameron (2007) | §3 (UC4), §7, §8, Annexe A |
| `DiagramService` | §2, §3 (UC2, UC5, UC6, UC7) |
| DFS (Depth-First Search) | §3 (UC4), §6, Annexe A |
| Diagramme SVN (`«SVNDiagram»`) | §2, §3 (UC2, UC5, UC6, UC7), §4 |
| `importanceScore` | §3 (UC4, UC5), §4 |
| IBM Rhapsody | §1, §2, §5, §7 |
| INCOSE 2018 | §3 (UC4), §7, §8, Annexe B |
| `IRPActor` | §4, §5 |
| `IRPApplication` | §2, §5 |
| `IRPCollection` | §1, §4 |
| `IRPFlow` | §3 (UC2, UC3), §4, §5 |
| `IRPProject` | §2, §5 |
| Java 8 / JDK 1.8 | §2, §5, §7 |
| `JColorChooser` | §3 (UC6), §5 |
| `JOptionPane` | §3 (UC3), §5 |
| Maven | §2, §5, §7 |
| Nœud système (`«system»`) | §2, §3 (UC4), §4 |
| `ProfileService` | §2, §3 (UC1, UC8) |
| `RhapsodyWrapper` | §2, §7 |
| `RPUserPlugin` | §1, §2 |
| `supplyImportance` | §3 (UC3, UC4), §4, Annexe B |
| `SupplyImportance` | §3 (UC3, UC4), §4, Annexe B |
| `SVNCalculateCommand` | §2, §3 (UC4) |
| `SVNCleanCommand` | §2, §3 (UC8) |
| `SVNConfigureCommand` | §2, §3 (UC1) |
| `SVNConstants` | §2, §7 |
| `SVNPlugin` | §1, §2, §5 |
| `SVNProfile` | §2, §3 (UC1, UC8), §4 |
| Stakeholder (`«stakeholder»`) | §2, §3 (UC4, UC5), §4 |
| Stéréotype | §1, §2, §3, §4 |
| SysML | §1, §2 |
| Tag | §1, §3, §4 |
| Value loop | §1, §3 (UC4), §6, Annexe A |
| Windows | §2, §5 |

## Index des fonctions et classes

| Classe / Méthode | Rôle | Section(s) |
|---|---|---|
| `SVNPlugin` | Point d'entrée du plugin, route les commandes | §2, §5 |
| `SVNConfigureCommand.run()` | Lance la configuration du profil SVN | §3 (UC1) |
| `SVNCreateArcCommand.run()` | Crée un arc «valuearc» entre deux éléments sélectionnés | §3 (UC2) |
| `SVNEditArcCommand.run()` | Édite les pondérations d'un arc sélectionné | §3 (UC3) |
| `SVNCalculateCommand.run()` | Lance le calcul d'importance SVN | §3 (UC4) |
| `SVNColorizeStakeholdersCommand.run()` | Colorise les stakeholders par rang | §3 (UC5) |
| `SVNArcColorCommand.run()` | Applique une couleur à un arc | §3 (UC6) |
| `SVNLabelArcCommand.run()` | Met à jour les labels d'arcs | §3 (UC7) |
| `SVNCleanCommand.run()` | Nettoie le modèle SVN | §3 (UC8) |
| `ProfileService.configureProfile()` | Crée/recrée le profil SVNProfile | §3 (UC1) |
| `ProfileService.cleanDefaultPackage()` | Supprime les éléments SVN du package Default | §3 (UC8) |
| `CalculationService.calculateImportance()` | Orchestre le calcul d'importance | §3 (UC4) |
| `CalculationService.calculateByValueLoops()` | Calcul principal par DFS | §3 (UC4), Annexe A |
| `CalculationService.calculateByArcSum()` | Calcul simplifié (fallback) | §3 (UC4) |
| `DiagramService.createArcBetweenSelected()` | Crée un arc entre éléments graphiques | §3 (UC2) |
| `DiagramService.colorizeStakeholdersByRank()` | Colorise les nœuds par tier | §3 (UC5) |
| `DiagramService.setArcColor()` | Change la couleur d'un arc | §3 (UC6) |
| `DiagramService.updateArcLabels()` | Met à jour les labels d'arcs | §3 (UC7) |
| `RhapsodyWrapper.hasStereotype()` | Vérifie si un élément porte un stéréotype donné | §2, §3 |
| `RhapsodyWrapper.getOrCreateStereotype()` | Crée ou récupère un stéréotype | §3 (UC1) |
| `RhapsodyWrapper.getOrCreateEnumType()` | Crée ou récupère un type énuméré | §3 (UC1) |
