package org.moon.figura.avatars.model.rendering;

import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.avatars.model.ParentType;

import java.util.function.BiPredicate;

public enum PartFilterScheme {
    MODEL(true, (part, previousPassed) -> {
        if (ParentType.SPECIAL_PARTS.contains(part.parentType))
            return false;
        return previousPassed;
    }),
    WORLD(false, (part, previousPassed) -> {
        if (part.parentType == ParentType.World)
            return true;
        return previousPassed;
    }),
    HEAD(false, (part, previousPassed) -> {
        if (part.parentType == ParentType.Head)
            return true;
        return previousPassed;
    }),
    LEFT_ARM(false, (part, previousPassed) -> {
        if (part.parentType == ParentType.LeftArm)
            return true;
        return previousPassed;
    }),
    RIGHT_ARM(false, (part, previousPassed) -> {
        if (part.parentType == ParentType.RightArm)
            return true;
        return previousPassed;
    }),
    HUD(false, (part, previousPassed) -> {
        if (part.parentType == ParentType.Hud)
            return true;
        return previousPassed;
    });

    public final boolean initialValue;
    public final BiPredicate<FiguraModelPart, Boolean> predicate;

    PartFilterScheme(boolean initialValue, BiPredicate<FiguraModelPart, Boolean> predicate) {
        this.initialValue = initialValue;
        this.predicate = predicate;
    }
}
