package org.figuramc.figura.wizards;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class WizardEntry {
    private static final HashMap<String, WizardEntry> ENTRY_LOOKUP = new LinkedHashMap<>();

    public static final WizardEntry Meta = register("Meta", WizardEntry.Type.CATEGORY);
    public static final WizardEntry NAME = register("NAME", WizardEntry.Type.TEXT);
    public static final WizardEntry DESCRIPTION = register("DESCRIPTION", WizardEntry.Type.TEXT, NAME);
    public static final WizardEntry AUTHORS = register("AUTHORS", WizardEntry.Type.TEXT, NAME);
    public static final WizardEntry Model = register("Model", WizardEntry.Type.CATEGORY, NAME);
    public static final WizardEntry DUMMY_MODEL = register("DUMMY_MODEL", Model);
    public static final WizardEntry PLAYER_MODEL = register("PLAYER_MODEL", DUMMY_MODEL);
    public static final WizardEntry SLIM = register("SLIM", PLAYER_MODEL);
    public static final WizardEntry CAPE = register("CAPE", DUMMY_MODEL);
    public static final WizardEntry ELYTRA = register("ELYTRA", DUMMY_MODEL);
    public static final WizardEntry Pivots = register("Pivots", WizardEntry.Type.CATEGORY, DUMMY_MODEL);
    public static final WizardEntry ITEMS_PIVOT = register("ITEMS_PIVOT", Pivots);
    public static final WizardEntry SPYGLASS_PIVOT = register("SPYGLASS_PIVOT", Pivots);
    public static final WizardEntry HELMET_ITEM_PIVOT = register("HELMET_ITEM_PIVOT", Pivots);
    public static final WizardEntry PARROTS_PIVOT = register("PARROTS_PIVOT", Pivots);
    public static final WizardEntry ARMOR_PIVOTS = register("ARMOR_PIVOTS", Pivots);
    public static final WizardEntry Scripting = register("Scripting", WizardEntry.Type.CATEGORY, NAME);
    public static final WizardEntry DUMMY_SCRIPT = register("DUMMY_SCRIPT", Scripting);
    public static final WizardEntry HIDE_PLAYER = register("HIDE_PLAYER", PLAYER_MODEL, DUMMY_SCRIPT);
    public static final WizardEntry HIDE_ARMOR = register("HIDE_ARMOR", DUMMY_SCRIPT);
    public static final WizardEntry HIDE_CAPE = register("HIDE_CAPE", DUMMY_SCRIPT);
    public static final WizardEntry HIDE_ELYTRA = register("HIDE_ELYTRA", DUMMY_SCRIPT);
    public static final WizardEntry EMPTY_EVENTS = register("EMPTY_EVENTS", DUMMY_SCRIPT);

    public static WizardEntry get(String name) {
        return ENTRY_LOOKUP.get(name);
    }

    public static boolean exists(String name) {
        return ENTRY_LOOKUP.containsKey(name);
    }

    public static Collection<WizardEntry> all() {
        return ENTRY_LOOKUP.values();
    }

    public final String name;
    public final WizardEntry.Type type;
    public final WizardEntry[] dependencies;

    WizardEntry(String name, WizardEntry... dependencies) {
        this(name, WizardEntry.Type.TOGGLE, dependencies);
    }

    WizardEntry(String name, WizardEntry.Type type, WizardEntry... dependencies) {
        this.name = name;
        this.type = type;
        this.dependencies = dependencies;
    }

    public boolean validate(Object object) {
        return switch (type) {
            case TOGGLE -> object instanceof Boolean;
            case TEXT -> object instanceof String;
            case CATEGORY -> true;
        };
    }

    public boolean asBool(HashMap<WizardEntry, Object> map) {
        Object object = map.get(this);
        return type == WizardEntry.Type.TOGGLE && object != null && (boolean) object;
    }

    public enum Type {
        TOGGLE,
        TEXT,
        CATEGORY
    }

    public static WizardEntry register(String name, WizardEntry.Type type, WizardEntry... dependencies) {
        WizardEntry entry = new WizardEntry(name, type, dependencies);
        ENTRY_LOOKUP.put(name, entry);
        return entry;
    }

    public static WizardEntry register(String name, WizardEntry... dependencies) {
        return register(name, Type.TOGGLE, dependencies);
    }
}