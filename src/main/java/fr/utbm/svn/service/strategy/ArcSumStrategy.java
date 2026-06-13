package fr.utbm.svn.service.strategy;

import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPPort;
import fr.utbm.svn.Logger;
import fr.utbm.svn.model.Stakeholder;
import fr.utbm.svn.model.ValueArc;
import fr.utbm.svn.service.ICalculationStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArcSumStrategy implements ICalculationStrategy {
    private final Logger logger = Logger.getInstance();

    @Override
    public Map<Stakeholder, Double> computeScores(List<Stakeholder> stakeholders, List<ValueArc> valueArcs) {
        double total = 0;

        for (Stakeholder sh : stakeholders) {
            double score = 0;
            for (ValueArc arc : valueArcs) {
                try {
                    IRPModelElement dependent = arc.getDependent();
                    IRPModelElement dependsOn = arc.getDependsOn();
                    IRPModelElement out = dependsOn instanceof IRPPort ? (dependsOn).getOwner() : dependsOn;
                    IRPModelElement in = dependent instanceof IRPPort ? (dependent).getOwner() : dependent;
                    if ((in != null && sh.getGUID().equals(in.getGUID())) || (out != null && sh.getGUID().equals(out.getGUID()))) {
                        score += arc.getScore();
                    }
                } catch (Exception ignored) {}
            }
            sh.setScore(score);
            total += score;
        }

        Map<Stakeholder, Double> scores = new HashMap<>();

        for (Stakeholder sh : stakeholders) {
            double importance = (total > 0) ? sh.getScore() / total : 0;
            scores.put(sh, importance);
            logger.log("Importance " + sh.getName()
                    + " = " + String.format("%.4f", importance));
        }

        return scores;
    }
}
