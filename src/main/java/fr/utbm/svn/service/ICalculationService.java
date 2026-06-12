package fr.utbm.svn.service;

import com.telelogic.rhapsody.core.IRPApplication;
import com.telelogic.rhapsody.core.IRPProject;

public interface ICalculationService {
    void calculateImportance(IRPProject project, IRPApplication app);
}
