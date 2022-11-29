package org.moon.figura.model.rendering;

import org.moon.figura.model.FiguraModelPart;
import org.moon.figura.model.ParentType;

public enum PartFilterScheme {

    //Assume that, when rendering model, everything is good to go in the beginning, and prune off things that aren't connected to main model.
    //Cancel when we find a part that's separate (special)
    MODEL(true, SchemeFunction.cancelOnSeparate()),


    HEAD(false, SchemeFunction.allowOnThisAndCancelOnSeparate(ParentType.Head)),
    LEFT_ARM(false, SchemeFunction.allowOnThisAndCancelOnSeparate(ParentType.LeftArm)),
    RIGHT_ARM(false, SchemeFunction.allowOnThisAndCancelOnSeparate(ParentType.RightArm)),


    CAPE(false, SchemeFunction.onlyThisSeparate(ParentType.Cape)),
    LEFT_ELYTRA(false, SchemeFunction.onlyThisSeparate(ParentType.LeftElytra)),
    RIGHT_ELYTRA(false, SchemeFunction.onlyThisSeparate(ParentType.RightElytra)),


    WORLD(false, SchemeFunction.onlyThisSeparate(ParentType.World)),
    HUD(false, SchemeFunction.onlyThisSeparate(ParentType.Hud)),
    SKULL(false, SchemeFunction.onlyThisSeparate(ParentType.Skull)),
    PORTRAIT(false, SchemeFunction.onlyThisSeparate(ParentType.Portrait)),

    PIVOTS(false, SchemeFunction.onlyPivotsAndCancelOnSeparate());


    private final boolean initialValue;
    private final SchemeFunction predicate;

    PartFilterScheme(boolean initialValue, SchemeFunction predicate) {
        this.initialValue = initialValue;
        this.predicate = predicate;
    }

    public Boolean initialValue(FiguraModelPart root) {
        return test(root.parentType, initialValue);
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
                if (parent == typeToAllow) //If it's our allowed type, we're good to go
                    return true;
                if (parent.isSeparate) //If it is separate, but not this type, then we want to not render but continue
                    return false;
                return prev; //Pass it along
            };
        }

        static SchemeFunction cancelOnSeparate() {
            return (parent, prev) -> {
                //Cancel everything if we find something that's not attached to the model itself
                if (parent.isSeparate)
                    return null;
                //If the part is attached to the model, then allow it to render.
                return true;
            };
        }

        static SchemeFunction allowOnThisAndCancelOnSeparate(ParentType typeToAllow) {
            return (parent, prev) -> {
                if (parent == typeToAllow)
                    return true;
                if (parent.isSeparate)
                    return null;
                return prev;
            };
        }

        static SchemeFunction onlyPivotsAndCancelOnSeparate() {
            return (parent, prev) -> {
                if (parent.isPivot)
                    return true;
                //Cancel everything if we find something that's not attached to the model itself
                if (parent.isSeparate)
                    return null;
                //If the part is attached to the model, skip it.
                return false;
            };
        }
    }
}
