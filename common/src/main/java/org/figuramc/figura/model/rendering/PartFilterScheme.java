package org.figuramc.figura.model.rendering;

import org.figuramc.figura.model.ParentType;

public enum PartFilterScheme {

    // Assume that, when rendering model, everything is good to go in the beginning, and prune off things that aren't connected to main model.
    // Cancel when we find a part that's separate (special)
    MODEL(true, SchemeFunction.cancelOnSeparate(), ParentType.None),


    HEAD(false, SchemeFunction.onlyThis(ParentType.Head), ParentType.Head),
    LEFT_ARM(false, SchemeFunction.onlyThis(ParentType.LeftArm), ParentType.LeftArm),
    RIGHT_ARM(false, SchemeFunction.onlyThis(ParentType.RightArm), ParentType.RightArm),


    CAPE(false, SchemeFunction.onlyThisSeparate(ParentType.Cape), ParentType.Cape),
    LEFT_ELYTRA(false, SchemeFunction.onlyThisSeparate(ParentType.LeftElytra), ParentType.LeftElytra),
    RIGHT_ELYTRA(false, SchemeFunction.onlyThisSeparate(ParentType.RightElytra), ParentType.RightElytra),


    WORLD(false, SchemeFunction.onlyThisSeparate(ParentType.World), ParentType.World),
    HUD(false, SchemeFunction.onlyThisSeparate(ParentType.Hud), ParentType.Hud),
    SKULL(false, SchemeFunction.onlyThisSeparate(ParentType.Skull), ParentType.Skull),
    PORTRAIT(false, SchemeFunction.onlyThisSeparate(ParentType.Portrait), ParentType.Portrait),
    ARROW(false, SchemeFunction.onlyThisSeparate(ParentType.Arrow), ParentType.Arrow),
    TRIDENT(false, SchemeFunction.onlyThisSeparate(ParentType.Trident), ParentType.Trident),
    ITEM(false, SchemeFunction.onlyThisSeparate(ParentType.Item), ParentType.Item),

    PIVOTS(false, SchemeFunction.onlyPivotsAndCancelOnSeparate(), ParentType.HelmetItemPivot);

    public final boolean initialValue;
    public final SchemeFunction predicate;
    public final ParentType parentType;

    PartFilterScheme(boolean initialValue, SchemeFunction predicate, ParentType parentType) {
        this.initialValue = initialValue;
        this.predicate = predicate;
        this.parentType = parentType;
    }

    public Boolean test(ParentType toTest, boolean prevResult) {
        return predicate.test(toTest, prevResult);
    }


    /**
     * Return true: this part should render, and also continue and render children, and pass true to the scheme function next time
     * Return false: this part should not render, however try to render children, and pass false to the scheme function next time
     * Return null: this part should not render, and also do not try children, so scheme function is not called again
     *
     * the second parameter cannot be null, since if the previous call returned null then
     * the function is not called again on the children.
     */
    @FunctionalInterface
    private interface SchemeFunction {
        Boolean test(ParentType typeOfThis, boolean previousSucceeded);

        static SchemeFunction onlyThisSeparate(ParentType typeToAllow) {
            return (parent, prev) -> {
                if (parent == typeToAllow) // If it's our allowed type, we're good to go
                    return !prev;
                if (parent.isSeparate) // If it is separate, but not this type, then we want to not render but continue
                    return false;
                return prev; // Pass it along
            };
        }

        static SchemeFunction cancelOnSeparate() {
            return (parent, prev) -> {
                // Cancel everything if we find something that's not attached to the model itself
                if (parent.isSeparate)
                    return null;
                // If the part is attached to the model, then allow it to render.
                return true;
            };
        }

        static SchemeFunction onlyThis(ParentType typeToAllow) {
            return (parent, prev) -> {
                if (parent == typeToAllow)
                    return true;
                else if (parent == ParentType.None)
                    return prev;
                else
                    return null;
            };
        }

        static SchemeFunction onlyPivotsAndCancelOnSeparate() {
            return (parent, prev) -> {
                if (parent.isPivot)
                    return true;
                // Cancel everything if we find something that's not attached to the model itself
                if (parent.isSeparate)
                    return null;
                // If the part is attached to the model, skip it.
                return false;
            };
        }
    }
}
