package fr.utbm.RhapsodySVN;
import com.telelogic.rhapsody.core.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        final IRPApplication app =
                RhapsodyAppServer.getActiveRhapsodyApplication();
        IRPProject prj = app.activeProject();
        IRPPackage pkg = prj.addPackage("Test8");
    }
}