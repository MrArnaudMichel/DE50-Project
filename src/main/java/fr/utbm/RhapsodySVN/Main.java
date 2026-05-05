package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;

import static fr.utbm.RhapsodySVN.SVNProfileCreator.run;

public class Main {

    public static void main(String[] args) {
        IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
        run(app);
    }
}