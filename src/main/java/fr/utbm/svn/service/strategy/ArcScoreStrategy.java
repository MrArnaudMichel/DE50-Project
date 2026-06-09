package fr.utbm.svn.service.strategy;

public class ArcScoreStrategy {
    /**
     * Matrice de score des arcs (Figure 3, INCOSE 2018).
     * Score = f(BenefitRanking, SupplyImportance)
     *
     *              Might Be  Should Be  Must Be
     * High           0.3       0.5       0.95
     * Medium         0.2       0.4       0.8
     * Low            0.1       0.2       0.4
     */
    public static double getArcScore(String benefit, String supply) {
        if ("MUST_BE".equals(benefit)) {
            if ("HIGH".equals(supply))   return 0.95;
            if ("MEDIUM".equals(supply)) return 0.8;
            if ("LOW".equals(supply))    return 0.4;
        } else if ("SHOULD_BE".equals(benefit)) {
            if ("HIGH".equals(supply))   return 0.5;
            if ("MEDIUM".equals(supply)) return 0.4;
            if ("LOW".equals(supply))    return 0.2;
        } else { // MIGHT_BE ou non défini
            if ("HIGH".equals(supply))   return 0.3;
            if ("MEDIUM".equals(supply)) return 0.2;
            if ("LOW".equals(supply))    return 0.1;
        }
        return 0.2;
    }
}
