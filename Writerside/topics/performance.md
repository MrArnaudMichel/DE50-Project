# 6. Les besoins en performance

## Temps de réponse

Le plugin est conçu pour une utilisation interactive au sein d'IBM Rhapsody. Les temps de réponse acceptables sont définis par rapport à la perception utilisateur :

| Opération | Temps de réponse attendu | Commentaire |
|---|---|---|
| UC1 — SVN Configure | < 5 secondes | Création du profil, des stéréotypes et des types dans Rhapsody |
| UC2 — SVN Create Arc | < 2 secondes | Création d'un flow et insertion graphique dans le diagramme |
| UC3 — SVN Edit Arc | < 1 seconde (hors saisie utilisateur) | Lecture/écriture de tags uniquement |
| UC4 — SVN Calculate | < 10 secondes pour ≤ 50 nœuds | Dépend de la complexité du graphe (recherche DFS de cycles) |
| UC5 — SVN Colorize Stakeholders | < 5 secondes | Parcours des éléments graphiques des diagrammes |
| UC6 — SVN Set Arc Color | < 2 secondes (hors saisie utilisateur) | Modification d'une propriété graphique |
| UC7 — SVN Update Arc Labels | < 5 secondes | Parcours de tous les arcs et mise à jour des labels |
| UC8 — SVN Clean | < 10 secondes | Suppression de tous les éléments SVN du modèle |

## Taille du modèle

| Paramètre | Valeur maximale recommandée |
|---|---|
| Nombre de nœuds `«stakeholder»` | 50 |
| Nombre d'arcs `«valuearc»` | 200 |
| Nombre de diagrammes `«SVNDiagram»` | 10 |
| Profondeur maximale des value loops | Non limitée, mais performance dégradée au-delà de 20 nœuds |

## Complexité algorithmique

L'algorithme de recherche de value loops (UC4) est basé sur un **DFS itératif** depuis le nœud `«system»`. Sa complexité est :

- **Cas favorable** : O(N + A) avec N nœuds et A arcs — graphe sans cycles ou avec peu de cycles
- **Cas défavorable** : O(N! / (N-k)!) avec k = longueur maximale d'un cycle — graphe fortement connexe

Pour les cas d'usage académiques typiques (< 20 stakeholders), les performances sont acceptables sans optimisation particulière. Pour des modèles plus grands, le fallback par somme des arcs reste disponible.

## Contraintes liées à l'environnement

- Le plugin s'exécute **dans le même processus JVM** qu'IBM Rhapsody. Il ne peut pas monopoliser le thread principal sous peine de bloquer l'interface Rhapsody.
- Les opérations de longue durée (UC4 sur grand modèle) s'exécutent de manière **synchrone** dans la version actuelle — aucun thread séparé n'est utilisé. L'interface Rhapsody sera temporairement non réactive pendant le calcul.
- Le plugin ne gère pas de **transactions simultanées** : une seule commande peut être en cours d'exécution à la fois.
