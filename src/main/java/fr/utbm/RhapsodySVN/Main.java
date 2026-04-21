package fr.utbm.RhapsodySVN;
import com.telelogic.rhapsody.core.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        final IRPApplication app =
                RhapsodyAppServer.getActiveRhapsodyApplication();
        IRPProject prj = app.activeProject();
        // If the project has Test in packages - remove it
        IRPCollection packages = prj.getPackages();
        if (packages != null) {
            for (int i = 1; i <= packages.getCount(); i++) {
                IRPPackage pkg = (IRPPackage) packages.getItem(i);
                if (pkg.getName().equals("Test")) {
                    pkg.deletePackage();
                    break;
                }
            }
        }

        IRPPackage pkg = prj.addPackage("Test");
    }
}