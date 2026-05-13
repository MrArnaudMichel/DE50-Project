package fr.utbm.RhapsodySVN.rhapsody;

import com.telelogic.rhapsody.core.*;

public class RhapsodyWrapper {

    public static IRPModelElement findInCollection(IRPCollection collection, String name) {
        if (collection == null) return null;
        for (int i = 1; i <= collection.getCount(); i++) {
            IRPModelElement el = (IRPModelElement) collection.getItem(i);
            if (name.equals(el.getName())) return el;
        }
        return null;
    }

    public static boolean hasStereotype(IRPModelElement el, String stereotypeName) {
        if (el == null) return false;
        IRPCollection stereotypes = el.getStereotypes();
        for (int i = 1; i <= stereotypes.getCount(); i++) {
            IRPModelElement st = (IRPModelElement) stereotypes.getItem(i);
            if (st.getName().equals(stereotypeName)) return true;
        }
        return false;
    }

    public static void deleteElement(IRPModelElement el) {
        if (el != null) {
            System.out.println("[SVN] Suppression de : " + el.getName() + " (" + el.getMetaClass() + ")");
            el.deleteFromProject();
        }
    }

    public static IRPStereotype getOrCreateStereotype(IRPPackage pkg, String name, String metaclass, boolean isNewTerm) {
        IRPStereotype st = (IRPStereotype) findInCollection(pkg.getStereotypes(), name);
        if (st == null) {
            try {
                // Pour éviter l'ambiguïté, on crée d'abord le stéréotype sans métaclasse via addNewAggr
                st = (IRPStereotype) pkg.addNewAggr("Stereotype", name);
                System.out.println("[SVN] Stéréotype créé : " + name);
            } catch (Exception e) {
                System.err.println("[SVN] Échec création stéréotype " + name + " : " + e.getMessage());
                return null;
            }
        }
        
        if (st != null) {
            try {
                // On ne définit la métaclasse que si elle n'est pas déjà correcte
                if (!metaclass.equals(st.getMetaClass())) {
                    st.addMetaClass(metaclass);
                }
                if (isNewTerm) st.setIsNewTerm(1);
            } catch (Exception e) {
                // On log mais on continue si possible
                System.out.println("[SVN] Note : Configuration métaclasse pour " + name + " : " + e.getMessage());
            }
        }
        return st;
    }

    public static IRPTag getOrCreateTag(IRPStereotype st, String name, IRPType type) {
        IRPTag tag = st.getTag(name);
        if (tag == null) {
            try {
                tag = (IRPTag) st.addNewAggr("Tag", name);
            } catch (Exception e) {
                System.err.println("[SVN] Échec création tag " + name + " : " + e.getMessage());
            }
        }
        if (tag != null && type != null) {
            tag.setType(type);
        }
        return tag;
    }

    public static IRPType getOrCreateEnumType(IRPPackage pkg, String name, String[] literals) {
        IRPType type = (IRPType) findInCollection(pkg.getTypes(), name);
        if (type == null) {
            try {
                // Tentative via addNewAggr d'abord, plus générique
                type = (IRPType) pkg.addNewAggr("Type", name);
                if (type != null) {
                    type.setKind("Enumeration");
                    for (String lit : literals) {
                        type.addEnumerationLiteral(lit);
                    }
                    System.out.println("[SVN] Type énuméré créé : " + name);
                }
            } catch (Exception e) {
                try {
                    // Repli sur addType
                    type = pkg.addType(name);
                    type.setKind("Enumeration");
                    for (String lit : literals) {
                        type.addEnumerationLiteral(lit);
                    }
                    System.out.println("[SVN] Type énuméré créé (via addType) : " + name);
                } catch (Exception e2) {
                    System.err.println("[SVN] Échec final pour le type " + name + ". " + e2.getMessage());
                }
            }
        }
        return type;
    }
}
