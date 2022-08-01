package org.moon.figura.avatars.model;

import org.moon.figura.math.vector.FiguraVec3;

import java.util.Set;

public enum ParentType {
    None("NONE"),

    Head(VanillaModelProvider.HEAD, "HEAD"),
    Body(VanillaModelProvider.BODY, "BODY"),
    LeftArm(VanillaModelProvider.LEFT_ARM, FiguraVec3.of(5, 2, 0), "LEFT_ARM"),
    RightArm(VanillaModelProvider.RIGHT_ARM, FiguraVec3.of(-5, 2, 0), "RIGHT_ARM"),
    LeftLeg(VanillaModelProvider.LEFT_LEG, FiguraVec3.of(1.9, 12, 0), "LEFT_LEG"),
    RightLeg(VanillaModelProvider.RIGHT_LEG, FiguraVec3.of(-1.9, 12, 0), "RIGHT_LEG"),

    LeftElytra(VanillaModelProvider.LEFT_ELYTRON, FiguraVec3.of(5, 0, 0), "LEFT_ELYTRA", "LeftElytron", "LEFT_ELYTRON"),
    RightElytra(VanillaModelProvider.RIGHT_ELYTRON, FiguraVec3.of(-5, 0, 0), "RIGHT_ELYTRA", "RightElytron", "RIGHT_ELYTRON"),

    Cape(VanillaModelProvider.FAKE_CAPE, "CAPE"),

    World("WORLD"),
    Hud("HUD", "Gui", "GUI"),
    Camera("CAMERA"),
    Skull("SKULL"),

    LeftItemPivot("LEFT_ITEM_PIVOT"),
    RightItemPivot("RIGHT_ITEM_PIVOT"),
    LeftSpyglassPivot("LEFT_SPYGLASS_PIVOT"),
    RightSpyglassPivot("RIGHT_SPYGLASS_PIVOT"),
    HelmetItemPivot("HELMET_ITEM_PIVOT"),
    LeftParrotPivot("LEFT_PARROT_PIVOT"),
    RightParrotPivot("RIGHT_PARROT_PIVOT");

    public final VanillaModelProvider provider;
    public final FiguraVec3 offset;
    public final String[] aliases;
    public static final Set<ParentType> SPECIAL_PARTS = Set.of(World, Hud, Skull);
    public static final Set<ParentType> PIVOT_PARTS = Set.of(
            LeftItemPivot, RightItemPivot,
            LeftSpyglassPivot, RightSpyglassPivot,
            HelmetItemPivot,
            LeftParrotPivot, RightParrotPivot
    );

    ParentType(String... aliases) {
        this(null, aliases);
    }

    ParentType(VanillaModelProvider provider, String... aliases) {
        this(provider, FiguraVec3.of(), aliases);
    }

    ParentType(VanillaModelProvider provider, FiguraVec3 offset, String... aliases) {
        this.provider = provider;
        this.offset = offset;
        this.aliases = aliases;
    }

    public static ParentType get(String name) {
        for (ParentType parentType : values()) {
            if (name.startsWith(parentType.name()))
                return parentType;
            for (String alias : parentType.aliases) {
                if (name.startsWith(alias))
                    return parentType;
            }
        }
        return None;
    }
}
