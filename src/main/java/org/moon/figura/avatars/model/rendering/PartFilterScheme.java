package org.moon.figura.avatars.model.rendering;

import org.moon.figura.avatars.model.ParentType;

public enum PartFilterScheme {
    MODEL(true, null),
    WORLD(false, ParentType.World, true),
    HEAD(false, ParentType.Head),
    LEFT_ARM(false, ParentType.LeftArm),
    RIGHT_ARM(false, ParentType.RightArm),
    HUD(false, ParentType.Hud, true),
    SKULL(false, ParentType.Skull, true);

    public final boolean initialValue;
    public final ParentType parent;
    private final boolean special;

    PartFilterScheme(boolean initialValue, ParentType parent) {
        this(initialValue, parent, false);
    }

    PartFilterScheme(boolean initialValue, ParentType parent, boolean special) {
        this.initialValue = initialValue;
        this.parent = parent;
        this.special = special;
    }

    public Boolean test(ParentType toTest, boolean prevResult) {
        if (this.parent != null && this.parent == toTest)
            return true;

        if (!this.special && toTest.isSpecial)
            return null;

        return prevResult;
    }
}
