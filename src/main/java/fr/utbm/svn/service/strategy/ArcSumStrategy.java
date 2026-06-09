package fr.utbm.svn.service.strategy;


import com.telelogic.rhapsody.core.IRPModelElement;
import fr.utbm.svn.Logger;
import fr.utbm.svn.model.SVNSystem;
import fr.utbm.svn.model.Stakeholder;
import fr.utbm.svn.model.ValueArc;
import fr.utbm.svn.service.ICalculationStrategy;

import java.util.List;

public class ArcSumStrategy implements ICalculationStrategy {
    private Logger logger = Logger.getInstance();

    @Override
    public void computeScores(List<Stakeholder> stakeholders, List<ValueArc> valueArcs, IRPModelElement root, SVNSystem svnSystem) {
        double total = 0;

        for (Stakeholder sh : stakeholders) {
            double score = 0;
            for (ValueArc arc : valueArcs) {
                try {
                    IRPModelElement dependent = arc.getDependent();
                    IRPModelElement dependsOn = arc.getDependsOn();
                    if ((dependent != null && sh.getName().equals(dependent.getName()))
                            || (dependsOn != null && sh.getName().equals(dependsOn.getName()))) {
                        score += arc.getScore();
                    }
                } catch (Exception ignored) {}
            }
            sh.setScore(score);
            total += score;
        }

        for (Stakeholder sh : stakeholders) {
            double importance = (total > 0) ? sh.getScore() / total : 0;
            updateImportanceTag(sh, importance);
            logger.log("Importance (simplifié) " + sh.getName()
                    + " = " + String.format("%.4f", importance));
        }
    }
}
