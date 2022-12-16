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

    public void build() {
        System.out.println("a");
    }

    public enum WizardEntry {
        //metadata
        Meta(Type.CATEGORY),
        NAME(Type.TEXT),
        //model stuff
        Model(Type.CATEGORY),
        SLIM,
        CAPE,
        ELYTRA,
        //pivots
        Pivots(Type.CATEGORY),
        ITEMS_PIVOT,
        SPYGLASS_PIVOT,
        HELMET_ITEM_PIVOT,
        PARROTS_PIVOT,
        //scripting
        Scripting(Type.CATEGORY),
        HIDE_PLAYER,
        HIDE_ARMOR,
        HIDE_CAPE,
        HIDE_ELYTRA,
        EMPTY_EVENTS;

        private final Type type;

        WizardEntry() {
            this(Type.TOGGLE);
        }

        WizardEntry(Type type) {
            this.type = type;
        }

        public Type getType() {
            return type;
        }

        public enum Type {
            TOGGLE,
            TEXT,
            CATEGORY
        }
    }
}
