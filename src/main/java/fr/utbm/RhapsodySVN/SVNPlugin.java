package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;

public class SVNPlugin extends RPUserPlugin {

    private IRPApplication app;
    private IRPProject project;

    @Override
    public void RhpPluginInit(IRPApplication rpyApplication) {
        this.app = rpyApplication;
        this.project = app.activeProject();//To fix
        System.out.println("[SVN] Plugin initialisé.");
    }

    @Override
    public void RhpPluginInvokeItem() {
        SVNConfigureCommand.run(app);
    }

    @Override
    public void OnMenuItemSelect(String menuItem) {
        System.out.println("[SVN] Commande : " + menuItem);
        switch (menuItem) {
            case "SVN Configure":             SVNConfigureCommand.run(app);             break;
            case "SVN Calculate":             SVNCalculateCommand.run(app);             break;
            case "SVN Update Arc Labels":     SVNLabelArcCommand.run(project);              break;
            case "SVN Set Arc Color":         SVNArcColorCommand.run(app);              break;
            case "SVN Colorize Stakeholders": SVNColorizeStakeholdersCommand.run(app);  break;
            case "SVN Create Arc":            SVNCreateArcCommand.run(app);             break;
            case "SVN Clean":                 SVNCleanCommand.run(app);                 break;
            default: System.err.println("[SVN] Commande inconnue : " + menuItem);
        }
    }

    @Override public void OnTrigger(String trigger) {}
    @Override public boolean RhpPluginCleanup() { return true; }
    @Override public void RhpPluginFinalCleanup() { System.out.println("[SVN] Plugin déchargé."); }
}