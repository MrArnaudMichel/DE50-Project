# 5. Spécifications des interfaces externes

## Interface matériel / logiciel

| Composant              | Configuration minimale                               |
|------------------------|------------------------------------------------------|
| Système d'exploitation | Windows (7, 10 ou 11) — **Windows uniquement**       |
| Ressources matérielles | Ne consome pas plus que Rhapsody sans plugin         |
| JDK                    | JDK 1.8 (Java 8) installé et accessible dans le PATH |

---

## Interface logiciel / logiciel

### IBM Rhapsody 9.0

| Propriété             | Valeur                                                                                                                    |
|-----------------------|---------------------------------------------------------------------------------------------------------------------------|
| **Nom**               | IBM Rational Rhapsody                                                                                                     |
| **Version**           | 9.0 (obligatoire — l'API COM utilisée est spécifique à cette version)                                                     |
| **Provenance**        | Fourni par IBM. *Nécésite une liscence spécifique*                                                                        |
| **But d'utilisation** | Application hôte du plugin. Fournit l'API Java via `rhapsody.jar`.                                                        |
| **Interface**         | `IRPApplication`, `IRPProject`, `IRPProfile`, `IRPFlow`, `IRPActor`, `IRPCollection`, `RPUserPlugin`, `RhapsodyAppServer` |

Le JAR `rhapsody.jar` est inclus comme dépendance locale dans `lib/rhapsody.jar` et déclaré dans `pom.xml`.

### Maven 3.x

| Propriété   | Valeur                                                  |
|-------------|---------------------------------------------------------|
| **Nom**     | Apache Maven                                            |
| **Version** | 3.x                                                     |
| **But**     | Outil de build et de gestion des dépendances            |
| **Usage**   | Compilation du projet, packaging en JAR (`mvn package`) |

---

## Interface Homme / logiciel

### Interaction via Rhapsody

Le plugin ne possède **aucune interface graphique propre**. Il est entièrement réactif : l'utilisateur interagit
exclusivement via les outils natifs de Rhapsody, et le plugin répond automatiquement à ces actions.

| Action utilisateur dans Rhapsody                                                                 | Réaction automatique du plugin                                                |
|--------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------|
| Création d'un élément `«valuearc»` dans le diagramme                                             | Initialisation des tags par défaut, mise à jour du label, recalcul des scores |
| Modification du tag `benefitRanking` ou `supplyImportance` via le panneau de propriétés Rhapsody | Mise à jour du label de l'arc, recalcul des scores d'importance               |
| Suppression d'un élément SVN                                                                     | Recalcul des scores d'importance                                              |

### Messages console

Le plugin utilise un `Logger` singleton qui écrit sur la sortie standard. Trois niveaux sont définis :

| Niveau        | Format                           | Condition d'affichage                  |
|---------------|----------------------------------|----------------------------------------|
| Info          | `[SVN] <message>`                | Uniquement si le mode debug est activé |
| Avertissement | `[SVN][WARN] <message>` (jaune)  | Uniquement si le mode debug est activé |
| Erreur        | `[SVN][ERROR] <message>` (rouge) | Toujours affiché                       |

Le mode debug s'active en passant l'argument `debug` au lancement via `Main`, ou via `logger.setDebug(true)`.

Exemples de messages produits :

- `[SVN] Plugin init success` — à l'ouverture du plugin
- `[SVN] Stakeholder found : Stakeholder1` — lors du calcul UC4
- `[SVN] Value loops found : 3` — lors du calcul UC4
- `[SVN] Importance 0.3750` — score calculé pour un stakeholder
- `[SVN][ERROR] Error in afterAddElement: <détail>` — en cas d'exception dans le Listener

### Spécification des messages d'erreur

| Situation                                  | Message affiché                                               |
|--------------------------------------------|---------------------------------------------------------------|
| Projet Rhapsody non actif au démarrage     | `[SVN] Project is null`                                       |
| Aucun stakeholder trouvé dans le diagramme | `[SVN] No stakeholders found.`                                |
| Aucun arc `«valuearc»` trouvé              | `[SVN] No arcs, can't calculate.`                             |
| Aucun value loop détecté (fallback)        | `[SVN] No «SVNsystem» found — switching to arc sum strategy.` |
| Exception dans `afterAddElement`           | `[SVN][ERROR] Error in afterAddElement: <détail>`             |
| Exception dans `onElementsChanged`         | `[SVN][ERROR] Error on onElementsChanged : <détail>`          |
