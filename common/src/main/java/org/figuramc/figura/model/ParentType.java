<<<<<<< HEAD:common/src/main/java/org/figuramc/figura/model/ParentType.java
package org.moon.figura.model;
=======
package org.figuramc.figura.model;
>>>>>>> 8b2c2606120aac4f05d8dd5820ea17317422dc93:src/main/java/org/moon/figura/avatars/model/ParentType.java

import org.figuramc.figura.math.vector.FiguraVec3;

public enum ParentType {
    None("NONE", "Model", "MODEL"),

    Head(VanillaModelProvider.HEAD, "HEAD"),
    Body(VanillaModelProvider.BODY, "BODY"),
    LeftArm(VanillaModelProvider.LEFT_ARM, FiguraVec3.of(5, 2, 0), "LEFT_ARM"),
    RightArm(VanillaModelProvider.RIGHT_ARM, FiguraVec3.of(-5, 2, 0), "RIGHT_ARM"),
    LeftLeg(VanillaModelProvider.LEFT_LEG, FiguraVec3.of(1.9, 12, 0), "LEFT_LEG"),
    RightLeg(VanillaModelProvider.RIGHT_LEG, FiguraVec3.of(-1.9, 12, 0), "RIGHT_LEG"),

    LeftElytra(true, VanillaModelProvider.LEFT_ELYTRON, FiguraVec3.of(5, 0, 0), "LEFT_ELYTRA", "LeftElytron", "LEFT_ELYTRON"),
    RightElytra(true, VanillaModelProvider.RIGHT_ELYTRON, FiguraVec3.of(-5, 0, 0), "RIGHT_ELYTRA", "RightElytron", "RIGHT_ELYTRON"),

    Cape(true, VanillaModelProvider.FAKE_CAPE, FiguraVec3.of(), "CAPE"),

    World(true, false, "WORLD"),
    Hud(true, false, "HUD", "HeadsUpDisplay", "Gui", "GUI", "GraphicalUserInterface", "JraficalUserInterface"),
    Camera("CAMERA", "Billboard", "BILLBOARD"),
    Skull(true, false, "SKULL", "☠"),
    Portrait(true, false, "PORTRAIT"),
    Arrow(true, false, "ARROW"),
    Item(true, false, "ITEM"),

    LeftItemPivot(false, true,"LEFT_ITEM_PIVOT"),
    RightItemPivot(false, true,"RIGHT_ITEM_PIVOT"),
    LeftSpyglassPivot(false, true,"LEFT_SPYGLASS_PIVOT"),
    RightSpyglassPivot(false, true,"RIGHT_SPYGLASS_PIVOT"),
    HelmetItemPivot(false, true,"HELMET_ITEM_PIVOT"),
    LeftParrotPivot(false, true,"LEFT_PARROT_PIVOT"),
    RightParrotPivot(false, true,"RIGHT_PARROT_PIVOT");

    public final VanillaModelProvider provider;
    public final FiguraVec3 offset;
    public final String[] aliases;

    //If this parent part renders separately from the rest of the model
    public final boolean isSeparate;

    //If this parent part serves as a modification for a vanilla rendering feature, and *not* to actually render blockbench cubes
    public final boolean isPivot;

    //If this parent part is a render layer, were parenting matrices should be kept
    public final boolean isRenderLayer;

    ParentType(String... aliases) {
        this(false, false, false, null, FiguraVec3.of(), aliases);
    }

    ParentType(VanillaModelProvider provider, String... aliases) {
        this(false, false, false, provider, FiguraVec3.of(), aliases);
    }

    ParentType(boolean isSeparate, boolean isPivot, String... aliases) {
        this(isSeparate, isPivot, false, null, FiguraVec3.of(), aliases);
    }

    ParentType(VanillaModelProvider provider, FiguraVec3 offset, String... aliases) {
        this(false, false, false, provider, offset, aliases);
    }

    ParentType(boolean isSeparate, VanillaModelProvider provider, FiguraVec3 offset, String... aliases) {
        this(isSeparate, false, true, provider, offset, aliases);
    }

    ParentType(boolean isSeparate, boolean isPivot, boolean isRenderLayer, VanillaModelProvider provider, FiguraVec3 offset, String... aliases) {
        this.isSeparate = isSeparate;
        this.isPivot = isPivot;
        this.isRenderLayer = isRenderLayer;
        this.provider = provider;
        this.offset = offset;
        this.aliases = aliases;
    }

    public static ParentType get(String name) {
        if (name == null)
            return None;

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
