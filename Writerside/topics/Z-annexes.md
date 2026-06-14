# Annexes

## Annexe A — Méthode SVN et équations de Cameron (2007)

### Présentation de la méthode

La méthode **Stakeholder Value Network (SVN)** est une approche d'ingénierie système permettant d'analyser les relations de valeur entre les parties prenantes d'un système. Elle représente ces relations sous forme d'un réseau orienté (graphe) où :

- Les **nœuds** sont des parties prenantes (`«stakeholder»`) et le système central (`«system»`)
- Les **arcs** (`«valuearc»`) représentent des flux de valeur orientés entre deux entités

### Scores des arcs

Chaque arc est caractérisé par deux attributs :

- **`benefitRanking`** : importance du bénéfice apporté par le flux du point de vue du destinataire (`MIGHT_BE`, `SHOULD_BE`, `MUST_BE`)
- **`supplyImportance`** : capacité du fournisseur à assurer ce flux (`LOW`, `MEDIUM`, `HIGH`)

Le score d'un arc est obtenu par la matrice INCOSE 2018 (voir Annexe B).

### Notion de Value Loop

Un **value loop** est un cycle dans le graphe SVN qui commence et se termine sur le nœud `«system»`. Il représente un circuit de création de valeur impliquant le système et un sous-ensemble de parties prenantes.

Exemple : `system → stakeholder_A → stakeholder_B → system`

### Équation 1 — Score d'un value loop

Le score d'un value loop L est le **produit des scores de ses arcs** :

```
score(L) = ∏ score(arc_i)    pour tout arc_i appartenant à L
```

Cette formule traduit le fait qu'un loop n'est efficace que si tous ses arcs sont de bonne qualité.

### Équation 2 — Importance d'un stakeholder

L'importance relative d'un stakeholder S est calculée comme le **rapport entre la somme des scores des loops contenant S et la somme des scores de tous les loops** :

```
importance(S) = Σ score(L_i) [pour L_i contenant S]
               ─────────────────────────────────────
                        Σ score(L_j) [tous L_j]
```

Le score obtenu est normalisé entre 0 et 1. Un stakeholder impliqué dans de nombreux loops de haute valeur obtient un score proche de 1.

### Fallback — Calcul simplifié par somme des arcs

En l'absence de nœud `«system»`, aucun value loop ne peut être détecté. Le plugin bascule sur un calcul simplifié :

```
score_brut(S) = Σ score(arc_i) [pour tout arc_i adjacent à S]

importance(S) = score_brut(S) / Σ score_brut(S_j) [tous les stakeholders]
```

Ce calcul est moins précis que la méthode principale mais reste fonctionnel pour des modèles sans système central défini.

---

## Annexe B — Matrice de score des arcs (INCOSE 2018, Figure 3)

La matrice ci-dessous définit le score numérique associé à chaque combinaison (`benefitRanking`, `supplyImportance`). Elle est implémentée dans `ValueArc.getArcScore()`.

| `supplyImportance` ↓ \ `benefitRanking` → | `MIGHT_BE` | `SHOULD_BE` | `MUST_BE` |
|---|---|---|---|
| `HIGH` | 0.30 | 0.50 | **0.95** |
| `MEDIUM` | 0.20 | 0.40 | 0.80 |
| `LOW` | 0.10 | 0.20 | 0.40 |

**Lecture de la matrice :**
- Un arc dont le bénéfice est critique (`MUST_BE`) et dont la capacité de fourniture est élevée (`HIGH`) obtient le score maximal de **0.95**
- Un arc dont le bénéfice est optionnel (`MIGHT_BE`) avec une faible capacité de fourniture (`LOW`) obtient le score minimal de **0.10**
- La valeur par défaut (tags absents ou invalides) est **0.20** (équivalent à `MIGHT_BE / MEDIUM`)

---

## Annexe C — Structure du projet Maven

```
RhapsodySVN/
├── pom.xml                          # Configuration Maven (Java 8, dépendance rhapsody.jar)
├── lib/
│   ├── rhapsody.jar                 # API IBM Rhapsody 9.0 (dépendance locale)
│   └── 64Bit/
│       └── rhapsody.dll             # Bibliothèque native Windows
└── src/
    ├── main/java/fr/utbm/svn/
    │   ├── SVNPlugin.java           # Point d'entrée plugin (RPUserPlugin)
    │   ├── Main.java                # Point d'entrée ligne de commande (hors Rhapsody)
    │   ├── Logger.java              # Singleton de journalisation
    │   ├── constants/
    │   │   └── SVNConstants.java    # Centralisation des noms de stéréotypes et tags
    │   ├── controller/
    │   │   └── Listener.java        # Écoute les événements Rhapsody, déclenche le recalcul
    │   ├── model/
    │   │   ├── Stakeholder.java     # Wrapper d'un acteur «stakeholder»
    │   │   ├── SVNSystem.java       # Wrapper du nœud «system»
    │   │   ├── ValueArc.java        # Wrapper d'une dépendance «valuearc»
    │   │   ├── ValueLoop.java       # Représente un cycle du graphe SVN
    │   │   └── SearchState.java     # État intermédiaire du parcours DFS
    │   ├── rhapsody/
    │   │   ├── RhapsodyWrapper.java         # Accès bas niveau à l'API Rhapsody (lecture)
    │   │   └── RhapsodyElementUpdater.java  # Écriture des résultats dans le modèle
    │   └── service/
    │       ├── ICalculationService.java     # Interface du service de calcul
    │       ├── ICalculationStrategy.java    # Interface des stratégies de calcul
    │       ├── impl/
    │       │   └── CalculationService.java  # Orchestrateur du calcul d'importance
    │       └── strategy/
    │           ├── ValueLoopStrategy.java   # Calcul par DFS (équations Cameron)
    │           └── ArcSumStrategy.java      # Calcul simplifié (fallback)
    └── test/java/fr/utbm/svn/
        ├── model/
        │   ├── SearchStateTest.java         # Tests du conteneur d'état DFS
        │   ├── ValueArcScoreTest.java       # Tests de la matrice de score INCOSE
        │   └── ValueLoopTest.java           # Tests du calcul de score de loop
        └── service/strategy/
            └── ImportanceCalculationTest.java  # Tests end-to-end du calcul Cameron
```

---

## Annexe D — Procédure d'installation et de premier lancement

1. **Pré-requis** : IBM Rhapsody 9.0 installé sur Windows, JDK 1.8, Maven 3.x
2. **Compilation** : `mvn clean package` — produit `target/RhapsodySVN-1.0-SNAPSHOT.jar`
3. **Déploiement** : placer le JAR dans le répertoire plugins de Rhapsody et configurer le fichier de configuration Rhapsody pour déclarer `fr.utbm.RhapsodySVN.SVNPlugin` comme plugin actif
4. **Premier lancement** :
   - Ouvrir IBM Rhapsody avec un projet actif
   - Exécuter `SVN Configure` depuis le menu pour initialiser le profil
   - Créer les éléments `«stakeholder»`, `«system»` et `«valuearc»` manuellement dans le diagramme
   - Renseigner les pondérations via `SVN Edit Arc` (UC3) pour chaque arc
   - Lancer `SVN Calculate` (UC4) pour calculer les scores
   - Visualiser les résultats via `SVN Colorize Stakeholders` (UC5) et `SVN Update Arc Labels` (UC7)
