package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsable des calculs SVN (Stakeholder Value Network).
 * Implémente les formules de priorisation basées sur les arcs de valeur.
 */
public class SVNCalculator {

    public static void main(String[] args) {
        IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
        calculate(app);
    }

    public static void calculate(IRPApplication app) {
        IRPModelElement selected = app.getSelectedElement();
        if (selected == null) {
            System.err.println("[SVN] Veuillez sélectionner un élément (Package ou Projet) pour lancer le calcul.");
            return;
        }

        System.out.println("[SVN] Lancement du calcul sur : " + selected.getName());
        
        // Liste tous les stakeholders dans le contexte sélectionné
        List<IRPClass> stakeholders = findAllStakeholders(selected);
        if (stakeholders.isEmpty()) {
            System.out.println("[SVN] Aucun stéréotype <<stakeholder>> trouvé.");
            return;
        }

        double totalScore = 0;
        // Map pour stocker les scores individuels
        List<StakeholderScore> scores = new ArrayList<>();

        for (IRPClass sh : stakeholders) {
            double shScore = calculateStakeholderImportance(sh);
            scores.add(new StakeholderScore(sh, shScore));
            totalScore += shScore;
        }

        // Normalisation et affichage/mise à jour
        System.out.println("\n[SVN] --- Résultats du classement ---");
        for (StakeholderScore score : scores) {
            double normalized = (totalScore > 0) ? (score.score / totalScore) : 0;
            System.out.printf("[SVN] Actor: %-20s | Score Brut: %6.2f | Importance: %6.2f%%%n", 
                    score.element.getName(), score.score, normalized * 100);
            
            // On peut stocker le résultat dans un Tag "importanceScore" par exemple
            updateImportanceTag(score.element, normalized);
        }
    }

    private static List<IRPClass> findAllStakeholders(IRPModelElement root) {
        List<IRPClass> result = new ArrayList<>();
        IRPCollection descendants = root.getNestedElementsRecursive();
        for (int i = 1; i <= descendants.getCount(); i++) {
            IRPModelElement el = (IRPModelElement) descendants.getItem(i);
            if (el instanceof IRPClass && hasStereotype(el, "stakeholder")) {
                result.add((IRPClass) el);
            }
        }
        return result;
    }

    private static double calculateStakeholderImportance(IRPClass stakeholder) {
        double score = 0;
        // On parcourt les relations pour trouver les arcs de valeur sortants
        // Note : Dans un SVN, on peut aussi considérer les arcs entrants ou les chemins complexes (Value Paths)
        IRPCollection relations = stakeholder.getRelations();
        for (int i = 1; i <= relations.getCount(); i++) {
            IRPRelation rel = (IRPRelation) relations.getItem(i);
            if (hasStereotype(rel, "valueArc")) {
                score += getArcWeight(rel);
            }
        }
        return score;
    }

    private static double getArcWeight(IRPRelation arc) {
        // Calcul du poids basé sur les Tags benefitRanking et supplyImportance
        int benefit = getTagValueAsInt(arc, "benefitRanking");
        int supply = getTagValueAsInt(arc, "supplyImportance");
        
        // Formule d'exemple (à ajuster selon Cameron 2007)
        return benefit * supply;
    }

    private static int getTagValueAsInt(IRPModelElement el, String tagName) {
        IRPTag tag = el.getTag(tagName);
        if (tag == null) return 1;
        
        String val = tag.getValue();
        // MIGHT_BE=1, SHOULD_BE=2, MUST_BE=3
        if (val.contains("MUST") || val.contains("HIGH")) return 3;
        if (val.contains("SHOULD") || val.contains("MEDIUM")) return 2;
        return 1; // Default for MIGHT_BE, LOW or empty
    }

    private static void updateImportanceTag(IRPModelElement el, double score) {
        IRPTag tag = el.getTag("importanceScore");
        if (tag == null) {
            // On l'ajoute si nécessaire (le profil devrait idéalement le définir)
            try {
                tag = (IRPTag) el.addNewAggr("Tag", "importanceScore");
            } catch (Exception ignored) {}
        }
        if (tag != null) {
            tag.setValue(String.format("%.4f", score));
        }
    }

    private static boolean hasStereotype(IRPModelElement el, String stereotypeName) {
        IRPCollection stereotypes = el.getStereotypes();
        for (int i = 1; i <= stereotypes.getCount(); i++) {
            IRPModelElement st = (IRPModelElement) stereotypes.getItem(i);
            if (st.getName().equals(stereotypeName)) return true;
        }
        return false;
    }

    private static class StakeholderScore {
        IRPClass element;
        double score;
        StakeholderScore(IRPClass e, double s) { this.element = e; this.score = s; }
    }
}
