package fr.utbm.RhapsodySVN.service;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.constants.SVNConstants;
import fr.utbm.RhapsodySVN.rhapsody.RhapsodyWrapper;

import java.util.ArrayList;
import java.util.List;

public class CalculationService {

    public void calculateImportance(IRPModelElement root) {
        System.out.println("[SVN] Début du calcul d'importance pour : " + root.getName());

        List<IRPActor> stakeholders = findStakeholders(root);
        if (stakeholders.isEmpty()) {
            System.out.println("[SVN] Aucun stakeholder trouvé.");
            return;
        }

        List<IRPFlow> allArcs = findValueArcs(root);
        System.out.println("[SVN] ValueArcs (Flow) trouvés : " + allArcs.size());

        List<StakeholderScore> scores = new ArrayList<>();
        double totalValue = 0;

        for (IRPActor sh : stakeholders) {
            double score = calculateValueForStakeholder(sh, allArcs);
            scores.add(new StakeholderScore(sh, score));
            totalValue += score;
            System.out.println("[SVN] Score brut " + sh.getName() + " = " + score);
        }

        if (totalValue > 0) {
            for (StakeholderScore ss : scores) {
                double relativeImportance = ss.score / totalValue;
                updateImportanceTag(ss.element, relativeImportance);
                System.out.println("[SVN] Importance " + ss.element.getName()
                        + " = " + String.format("%.4f", relativeImportance));
            }
            System.out.println("[SVN] Calcul terminé. " + stakeholders.size() + " acteurs mis à jour.");
        } else {
            System.out.println("[SVN] Valeur totale nulle, aucune mise à jour effectuée.");
        }
    }

    private List<IRPActor> findStakeholders(IRPModelElement root) {
        List<IRPActor> result = new ArrayList<>();
        IRPCollection descendants = root.getNestedElementsRecursive();
        for (int i = 1; i <= descendants.getCount(); i++) {
            IRPModelElement el = (IRPModelElement) descendants.getItem(i);
            if (el instanceof IRPActor
                    && RhapsodyWrapper.hasStereotype(el, SVNConstants.STEREOTYPE_STAKEHOLDER)) {
                result.add((IRPActor) el);
                System.out.println("[SVN] Stakeholder trouvé : " + el.getName());
            }
        }
        return result;
    }

    private List<IRPFlow> findValueArcs(IRPModelElement root) {
        List<IRPFlow> result = new ArrayList<>();
        IRPCollection descendants = root.getNestedElementsRecursive();
        for (int i = 1; i <= descendants.getCount(); i++) {
            IRPModelElement el = (IRPModelElement) descendants.getItem(i);
            if (el instanceof IRPFlow
                    && RhapsodyWrapper.hasStereotype(el, SVNConstants.STEREOTYPE_VALUE_ARC)) {
                result.add((IRPFlow) el);
                System.out.println("[SVN] ValueArc trouvé : " + el.getName());
            }
        }
        return result;
    }

    private double calculateValueForStakeholder(IRPActor stakeholder, List<IRPFlow> allArcs) {
        double score = 0;
        String name = stakeholder.getName();
        for (IRPFlow arc : allArcs) {
            try {
                // IRPFlow utilise getEnd1()/getEnd2() au lieu de getOfClass()/getOtherClass()
                IRPModelElement end1 = arc.getEnd1();
                IRPModelElement end2 = arc.getEnd2();
                boolean involves = (end1 != null && name.equals(end1.getName()))
                        || (end2 != null && name.equals(end2.getName()));
                if (involves) {
                    double w = getArcWeight(arc);
                    score += w;
                    System.out.println("[SVN]   Arc " + arc.getName()
                            + " implique " + name + " poids=" + w);
                }
            } catch (Exception e) {
                System.err.println("[SVN] Erreur arc " + arc.getName() + " : " + e.getMessage());
            }
        }
        return score;
    }

    private double getArcWeight(IRPFlow arc) {
        double benefit = 1.0;
        double supply  = 1.0;

        // IRPFlow extends IRPModelElement donc getTag() est disponible
        IRPTag benefitTag = arc.getTag(SVNConstants.TAG_BENEFIT_RANKING);
        if (benefitTag != null) {
            String val = benefitTag.getValue();
            if ("MUST_BE".equals(val))        benefit = 3.0;
            else if ("SHOULD_BE".equals(val)) benefit = 2.0;
        }

        IRPTag supplyTag = arc.getTag(SVNConstants.TAG_SUPPLY_IMPORTANCE);
        if (supplyTag != null) {
            String val = supplyTag.getValue();
            if ("HIGH".equals(val))           supply = 3.0;
            else if ("MEDIUM".equals(val))    supply = 2.0;
        }

        return benefit * supply;
    }

    private void updateImportanceTag(IRPModelElement el, double score) {
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
        IRPActor element;
        double score;
        StakeholderScore(IRPActor el, double s) {
            this.element = el;
            this.score   = s;
        }
    }
}