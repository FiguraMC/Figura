package org.moon.figura.wizards;

import java.util.HashMap;

public class AvatarWizard {

    private final HashMap<WizardEntry, Object> map = new HashMap<>();

    public void changeEntry(WizardEntry entry, Object value) {
        map.put(entry, value);
    }

    public boolean canBuild() {
        String name = (String) map.get(WizardEntry.NAME);
        return name != null && !name.isBlank();
    }

    public boolean checkDependency(WizardEntry entry) {
        if (entry.dependencies == null)
            return true;

        for (WizardEntry dependency : entry.dependencies) {
            if (!checkDependency(dependency))
                return false;

            if (dependency.type == WizardEntry.Type.CATEGORY)
                continue;

            Object obj = map.get(dependency);
            if (obj == null || !dependency.validade(obj))
                return false;

            boolean bl = switch (dependency.type) {
                case TOGGLE -> (boolean) obj;
                case TEXT -> !((String) obj).isBlank();
                default -> true;
            };
            if (!bl) return false;
        }

        return true;
    }

    public void build() {

    }

    public enum WizardEntry {
        //metadata
        Meta(Type.CATEGORY),
        NAME(Type.TEXT),
        AUTHORS(Type.TEXT, NAME),
        //model stuff
        Model(Type.CATEGORY, NAME),
        DUMMY_MODEL(Model),
        CUSTOM_PLAYER(DUMMY_MODEL),
        SLIM(CUSTOM_PLAYER),
        CAPE(DUMMY_MODEL),
        ELYTRA(DUMMY_MODEL),
        //pivots
        Pivots(Type.CATEGORY, DUMMY_MODEL),
        ITEMS_PIVOT(Pivots),
        SPYGLASS_PIVOT(Pivots),
        HELMET_ITEM_PIVOT(Pivots),
        PARROTS_PIVOT(Pivots),
        //scripting
        Scripting(Type.CATEGORY, NAME),
        DUMMY_SCRIPT(Scripting),
        HIDE_PLAYER(CUSTOM_PLAYER, DUMMY_SCRIPT),
        HIDE_ARMOR(DUMMY_SCRIPT),
        HIDE_CAPE(DUMMY_SCRIPT),
        HIDE_ELYTRA(DUMMY_SCRIPT),
        EMPTY_EVENTS(DUMMY_SCRIPT);

        private final Type type;
        private final WizardEntry[] dependencies;

        WizardEntry() {
            this(Type.TOGGLE);
        }
        WizardEntry(WizardEntry... dependencies) {
            this(Type.TOGGLE, dependencies);
        }
        WizardEntry(Type type, WizardEntry... dependencies) {
            this.type = type;
            this.dependencies = dependencies;
        }

        public Type getType() {
            return type;
        }

        public boolean validade(Object object) {
            return switch (type) {
                case TOGGLE -> object instanceof Boolean;
                case TEXT -> object instanceof String;
                case CATEGORY -> true;
            };
        }

        public enum Type {
            TOGGLE,
            TEXT,
            CATEGORY
        }
    }
}
