package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;

/**
 * Point d'entrée principal du plugin SVN pour IBM Rhapsody.
 *
 * Commandes disponibles via OnMenuItemSelect :
 *   "SVN Configure"             → configure le profil SVN
 *   "SVN Calculate"             → calcule l'importance des stakeholders
 *   "SVN Update Arc Labels"     → affiche B:/S: sur les arcs
 *   "SVN Set Arc Color"         → sélecteur de couleur pour l'arc sélectionné
 *   "SVN Colorize Stakeholders" → code couleur automatique après calcul
 *   "SVN Create Arc"            → crée un valuearc entre deux éléments sélectionnés
 *   "SVN Clean"                 → supprime et recrée le profil SVN
 *   "SVN Apply Toolbar"         → tente de forcer la toolbar SVN sur les diagrammes SVNDiagram
 */
public class SVNPlugin extends RPUserPlugin {

    private IRPApplication app;
    private IRPProject project;
    public static void main(String[] args) {
        IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
        SVNPlugin plugin = new SVNPlugin();
        plugin.run(app);
    }
    public void run(IRPApplication app) {
        this.app = app;
        project = app.activeProject();

        SVNPlugin plugin = new SVNPlugin();
        plugin.OnMenuItemSelect("SVN Configure");
        plugin.OnMenuItemSelect("SVN Calculate");
        plugin.OnMenuItemSelect("SVN Update Arc Labels");
        plugin.OnMenuItemSelect("SVN Set Arc Color");
        //     plugin.OnMenuItemSelect("SVN Configure");
        //     plugin.OnMenuItemSelect("SVN Configure");
        // plugin.OnMenuItemSelect("SVN Configure");
    }

    @Override
    public void RhpPluginInit(IRPApplication rpyApplication) {
        this.app = rpyApplication;
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
            case "SVN Apply Toolbar":         applyToolbarToSVNDiagrams();              break;
            default: System.err.println("[SVN] Commande inconnue : " + menuItem);
        }
    }

    @Override public void OnTrigger(String trigger) {}
    @Override public boolean RhpPluginCleanup() { return true; }
    @Override public void RhpPluginFinalCleanup() { System.out.println("[SVN] Plugin déchargé."); }

    // -------------------------------------------------------------------------
    // Piste alternative pour la palette SVNDiagram
    // -------------------------------------------------------------------------

    /**
     * Cherche tous les ObjectModelDiagram du projet portant le stéréotype SVNDiagram
     * et tente de forcer leur toolbar via setPropertyValue, puis addProperty en fallback.
     *
     * IRPProject hérite de IRPModelElement, donc getNestedElementsByMetaClass est disponible.
     */
    private void applyToolbarToSVNDiagrams() {
        IRPProject project = app.activeProject();
        if (project == null) return;

        String toolbarValue = "Select,stakeholder,system,valuearc,Note,Anchor";
        int applied = 0;

        // getNestedElementsByMetaClass est défini sur IRPModelElement (dont IRPProject hérite)
        IRPCollection diags = project.getNestedElementsByMetaClass("ObjectModelDiagram", 1);
        for (int i = 1; i <= diags.getCount(); i++) {
            Object item = diags.getItem(i);
            if (!(item instanceof IRPObjectModelDiagram)) continue;
            IRPObjectModelDiagram d = (IRPObjectModelDiagram) item;

            if (!isSVNDiagram(d)) continue;

            try {
                d.setPropertyValue("General.ObjectModelDiagram.Toolbar", toolbarValue);
                System.out.println("[SVN] Toolbar appliquée sur : " + d.getName());
                applied++;
            } catch (Exception e) {
                try {
                    d.addProperty("General.ObjectModelDiagram.Toolbar", "String", toolbarValue);
                    System.out.println("[SVN] Toolbar (addProperty) sur : " + d.getName());
                    applied++;
                } catch (Exception e2) {
                    System.err.println("[SVN] Toolbar échouée sur " + d.getName() + " : " + e2.getMessage());
                }
            }
        }

        if (applied == 0) {
            System.out.println("[SVN] Aucun diagramme SVNDiagram trouvé dans le projet.");
        }
    }

    private boolean isSVNDiagram(IRPObjectModelDiagram diagram) {
        try {
            IRPCollection stereotypes = diagram.getStereotypes();
            for (int i = 1; i <= stereotypes.getCount(); i++) {
                IRPModelElement st = (IRPModelElement) stereotypes.getItem(i);
                if ("SVNDiagram".equals(st.getName())) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }
}