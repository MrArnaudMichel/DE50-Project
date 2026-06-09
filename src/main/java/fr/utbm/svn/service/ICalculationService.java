package fr.utbm.svn.service;

import com.telelogic.rhapsody.core.IRPDiagram;
import com.telelogic.rhapsody.core.IRPModelElement;

public interface ICalculationService {
    void calculateImportance(IRPModelElement root, IRPDiagram diagram);
}
