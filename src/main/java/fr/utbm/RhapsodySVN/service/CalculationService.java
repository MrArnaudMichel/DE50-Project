package fr.utbm.RhapsodySVN.service;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.constants.SVNConstants;
import fr.utbm.RhapsodySVN.rhapsody.RhapsodyWrapper;

import java.util.ArrayList;
import java.util.List;

public class CalculationService {

    public void calculateImportance(IRPModelElement root) {
        System.out.println("[SVN] Début du calcul d'importance pour : " + root.getName());
        
        List<IRPClass> stakeholders = findStakeholders(root);
        if (stakeholders.isEmpty()) {
            System.out.println("[SVN] Aucun stakeholder trouvé.");
            return;
        }

        List<StakeholderScore> scores = new ArrayList<>();
        double totalValue = 0;

        for (IRPClass sh : stakeholders) {
            double score = calculateValueForStakeholder(sh);
            scores.add(new StakeholderScore(sh, score));
            totalValue += score;
        }

        if (totalValue > 0) {
            for (StakeholderScore ss : scores) {
                double relativeImportance = ss.score / totalValue;
                updateImportanceTag(ss.element, relativeImportance);
            }
            System.out.println("[SVN] Calcul terminé. " + stakeholders.size() + " acteurs mis à jour.");
        } else {
            System.out.println("[SVN] Valeur totale nulle, aucune mise à jour effectuée.");
        }
    }

    private List<IRPClass> findStakeholders(IRPModelElement root) {
        List<IRPClass> result = new ArrayList<>();
        IRPCollection descendants = root.getNestedElementsRecursive();
        for (int i = 1; i <= descendants.getCount(); i++) {
            IRPModelElement el = (IRPModelElement) descendants.getItem(i);
            if (el instanceof IRPClass && RhapsodyWrapper.hasStereotype(el, SVNConstants.STEREOTYPE_STAKEHOLDER)) {
                result.add((IRPClass) el);
            }
        }
        return result;
    }

    private double calculateValueForStakeholder(IRPClass stakeholder) {
        double score = 0;
        IRPCollection relations = stakeholder.getRelations();
        for (int i = 1; i <= relations.getCount(); i++) {
            IRPRelation rel = (IRPRelation) relations.getItem(i);
            if (RhapsodyWrapper.hasStereotype(rel, SVNConstants.STEREOTYPE_VALUE_ARC)) {
                score += getArcWeight(rel);
            }
        }
        return score;
    }

    private double getArcWeight(IRPRelation arc) {
        double benefit = 1.0;
        double supply = 1.0;

        IRPTag benefitTag = arc.getTag(SVNConstants.TAG_BENEFIT_RANKING);
        if (benefitTag != null) {
            String val = benefitTag.getValue();
            if ("MUST_BE".equals(val)) benefit = 3.0;
            else if ("SHOULD_BE".equals(val)) benefit = 2.0;
        }

        IRPTag supplyTag = arc.getTag(SVNConstants.TAG_SUPPLY_IMPORTANCE);
        if (supplyTag != null) {
            String val = supplyTag.getValue();
            if ("HIGH".equals(val)) supply = 3.0;
            else if ("MEDIUM".equals(val)) supply = 2.0;
        }

        return benefit * supply;
    }

    private void updateImportanceTag(IRPClass el, double score) {
        IRPTag tag = el.getTag(SVNConstants.TAG_IMPORTANCE_SCORE);
        if (tag == null) {
            try {
                tag = (IRPTag) el.addNewAggr("Tag", SVNConstants.TAG_IMPORTANCE_SCORE);
            } catch (Exception ignored) {}
        }
        if (tag != null) {
            tag.setValue(String.format("%.4f", score));
        }
    }

    private static class StakeholderScore {
        IRPClass element;
        double score;
        StakeholderScore(IRPClass el, double s) {
            this.element = el;
            this.score = s;
        }
    }
}
