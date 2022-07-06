package org.moon.figura.lua.api.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.ducks.PlayerModelAccessor;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaPairsIterator;
import org.moon.figura.mixin.render.layers.elytra.ElytraModelAccessor;

import java.util.List;
import java.util.function.Function;

@LuaWhitelist
@LuaTypeDoc(
        name = "VanillaModelAPI",
        description = "vanilla_model"
)
public class VanillaModelAPI {

    // -- body -- //

    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.head")
    public final VanillaModelPart HEAD;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.body")
    public final VanillaModelPart BODY;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.left_arm")
    public final VanillaModelPart LEFT_ARM;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.right_arm")
    public final VanillaModelPart RIGHT_ARM;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.left_leg")
    public final VanillaModelPart LEFT_LEG;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.right_leg")
    public final VanillaModelPart RIGHT_LEG;

    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.hat")
    public final VanillaModelPart HAT;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.jacket")
    public final VanillaModelPart JACKET;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.left_sleeve")
    public final VanillaModelPart LEFT_SLEEVE;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.right_sleeve")
    public final VanillaModelPart RIGHT_SLEEVE;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.left_pants")
    public final VanillaModelPart LEFT_PANTS;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.right_pants")
    public final VanillaModelPart RIGHT_PANTS;

    // -- cape -- //

    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.cape_model")
    public final VanillaModelPart CAPE_MODEL;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.fake_cape")
    public final VanillaModelPart FAKE_CAPE;

    // -- armor -- //

    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.helmet_head")
    public final VanillaModelPart HELMET_HEAD;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.helmet_hat")
    public final VanillaModelPart HELMET_HAT;

    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.chestplate_body")
    public final VanillaModelPart CHESTPLATE_BODY;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.chestplate_left_arm")
    public final VanillaModelPart CHESTPLATE_LEFT_ARM;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.chestplate_right_arm")
    public final VanillaModelPart CHESTPLATE_RIGHT_ARM;


    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.leggings_body")
    public final VanillaModelPart LEGGINGS_BODY;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.leggings_left_leg")
    public final VanillaModelPart LEGGINGS_LEFT_LEG;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.leggings_right_leg")
    public final VanillaModelPart LEGGINGS_RIGHT_LEG;

    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.boots_left_leg")
    public final VanillaModelPart BOOTS_LEFT_LEG;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.boots_right_leg")
    public final VanillaModelPart BOOTS_RIGHT_LEG;

    // -- elytra -- //

    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.left_elytra")
    public final VanillaModelPart LEFT_ELYTRA;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.right_elytra")
    public final VanillaModelPart RIGHT_ELYTRA;

    // -- held items -- //

    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.left_item")
    public final VanillaModelPart LEFT_ITEM;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.right_item")
    public final VanillaModelPart RIGHT_ITEM;


    // -- groups -- //


    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.all")
    public final VanillaGroupPart ALL;

    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.player")
    public final VanillaGroupPart PLAYER;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.outer_layer")
    public final VanillaGroupPart OUTER_LAYER;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.inner_layer")
    public final VanillaGroupPart INNER_LAYER;

    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.cape")
    public final VanillaGroupPart CAPE;

    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.armor")
    public final VanillaGroupPart ARMOR;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.helmet")
    public final VanillaGroupPart HELMET;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.chestplate")
    public final VanillaGroupPart CHESTPLATE;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.leggings")
    public final VanillaGroupPart LEGGINGS;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.boots")
    public final VanillaGroupPart BOOTS;

    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.elytra")
    public final VanillaGroupPart ELYTRA;

    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.held_items")
    public final VanillaGroupPart HELD_ITEMS;

    public VanillaModelAPI() {
        // -- body -- //

        HEAD = new VanillaModelPart(FiguraModelPart.ParentType.Head, ModelFunction.HEAD.func);
        BODY = new VanillaModelPart(FiguraModelPart.ParentType.Body, ModelFunction.BODY.func);
        LEFT_ARM = new VanillaModelPart(FiguraModelPart.ParentType.LeftArm, ModelFunction.LEFT_ARM.func);
        RIGHT_ARM = new VanillaModelPart(FiguraModelPart.ParentType.RightArm, ModelFunction.RIGHT_ARM.func);
        LEFT_LEG = new VanillaModelPart(FiguraModelPart.ParentType.LeftLeg, ModelFunction.LEFT_LEG.func);
        RIGHT_LEG = new VanillaModelPart(FiguraModelPart.ParentType.RightLeg, ModelFunction.RIGHT_LEG.func);

        HAT = new VanillaModelPart(FiguraModelPart.ParentType.Head, ModelFunction.HAT.func);
        JACKET = new VanillaModelPart(FiguraModelPart.ParentType.Body, ModelFunction.JACKET.func);
        LEFT_SLEEVE = new VanillaModelPart(FiguraModelPart.ParentType.LeftArm, ModelFunction.LEFT_SLEEVE.func);
        RIGHT_SLEEVE = new VanillaModelPart(FiguraModelPart.ParentType.RightArm, ModelFunction.RIGHT_SLEEVE.func);
        LEFT_PANTS = new VanillaModelPart(FiguraModelPart.ParentType.LeftLeg, ModelFunction.LEFT_PANTS.func);
        RIGHT_PANTS = new VanillaModelPart(FiguraModelPart.ParentType.RightLeg, ModelFunction.RIGHT_PANTS.func);

        // -- cape -- //

        CAPE_MODEL = new VanillaModelPart(FiguraModelPart.ParentType.Cape, ModelFunction.CAPE.func);
        FAKE_CAPE = new VanillaModelPart(FiguraModelPart.ParentType.Cape, ModelFunction.FAKE_CAPE.func);

        // -- armor -- //

        HELMET_HEAD = new VanillaModelPart(FiguraModelPart.ParentType.Head, ModelFunction.HEAD.func);
        HELMET_HAT = new VanillaModelPart(FiguraModelPart.ParentType.Head, ModelFunction.HAT.func);

        CHESTPLATE_BODY = new VanillaModelPart(FiguraModelPart.ParentType.Body, ModelFunction.BODY.func);
        CHESTPLATE_LEFT_ARM = new VanillaModelPart(FiguraModelPart.ParentType.LeftArm, ModelFunction.LEFT_ARM.func);
        CHESTPLATE_RIGHT_ARM = new VanillaModelPart(FiguraModelPart.ParentType.RightArm, ModelFunction.RIGHT_ARM.func);

        LEGGINGS_BODY = new VanillaModelPart(FiguraModelPart.ParentType.Body, ModelFunction.BODY.func);
        LEGGINGS_LEFT_LEG = new VanillaModelPart(FiguraModelPart.ParentType.LeftLeg, ModelFunction.LEFT_LEG.func);
        LEGGINGS_RIGHT_LEG = new VanillaModelPart(FiguraModelPart.ParentType.RightLeg, ModelFunction.RIGHT_LEG.func);

        BOOTS_LEFT_LEG = new VanillaModelPart(FiguraModelPart.ParentType.LeftLeg, ModelFunction.LEFT_LEG.func);
        BOOTS_RIGHT_LEG = new VanillaModelPart(FiguraModelPart.ParentType.RightLeg, ModelFunction.RIGHT_LEG.func);

        // -- elytra -- //

        LEFT_ELYTRA = new VanillaModelPart(FiguraModelPart.ParentType.LeftElytra, ModelFunction.LEFT_ELYTRON.func);
        RIGHT_ELYTRA = new VanillaModelPart(FiguraModelPart.ParentType.RightElytra, ModelFunction.RIGHT_ELYTRON.func);

        // -- held items -- //

        LEFT_ITEM = new VanillaModelPart(FiguraModelPart.ParentType.LeftArm, null);
        RIGHT_ITEM = new VanillaModelPart(FiguraModelPart.ParentType.RightArm, null);


        // -- groups -- //


        INNER_LAYER = new VanillaGroupPart(HEAD, BODY, LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG);
        OUTER_LAYER = new VanillaGroupPart(HAT, JACKET, LEFT_SLEEVE, RIGHT_SLEEVE, LEFT_PANTS, RIGHT_PANTS);
        PLAYER = new VanillaGroupPart(INNER_LAYER, OUTER_LAYER);

        CAPE = new VanillaGroupPart(CAPE_MODEL, FAKE_CAPE);

        HELMET = new VanillaGroupPart(HELMET_HEAD, HELMET_HAT);
        CHESTPLATE = new VanillaGroupPart(CHESTPLATE_BODY, CHESTPLATE_LEFT_ARM, CHESTPLATE_RIGHT_ARM);
        LEGGINGS = new VanillaGroupPart(LEGGINGS_BODY, LEGGINGS_LEFT_LEG, LEGGINGS_RIGHT_LEG);
        BOOTS = new VanillaGroupPart(BOOTS_LEFT_LEG, BOOTS_RIGHT_LEG);
        ARMOR = new VanillaGroupPart(HELMET, CHESTPLATE, LEGGINGS, BOOTS);

        ELYTRA = new VanillaGroupPart(LEFT_ELYTRA, RIGHT_ELYTRA);

        HELD_ITEMS = new VanillaGroupPart(LEFT_ITEM, RIGHT_ITEM);

        ALL = new VanillaGroupPart(PLAYER, CAPE, ARMOR, ELYTRA, HELD_ITEMS);
    }

    private static final LuaPairsIterator<VanillaModelAPI, String> PAIRS_ITERATOR =
            new LuaPairsIterator<>(List.of(
                    "HEAD", "BODY", "LEFT_ARM", "RIGHT_ARM", "LEFT_LEG", "RIGHT_LEG",
                    "HAT", "JACKET", "LEFT_SLEEVE", "RIGHT_SLEEVE", "LEFT_PANTS", "RIGHT_PANTS",

                    "CAPE_MODEL", "FAKE_CAPE",

                    "HELMET_HEAD", "HELMET_HAT",
                    "CHESTPLATE_BODY", "CHESTPLATE_LEFT_ARM", "CHESTPLATE_RIGHT_ARM",
                    "LEGGINGS_BODY", "LEGGINGS_LEFT_LEG", "LEGGINGS_RIGHT_LEG",
                    "BOOTS_LEFT_LEG", "BOOTS_RIGHT_LEG",

                    "LEFT_ELYTRA", "RIGHT_ELYTRA",

                    "LEFT_ITEM", "RIGHT_ITEM"
            ), VanillaModelAPI.class, String.class);
    @LuaWhitelist
    public static LuaPairsIterator<VanillaModelAPI, String> __pairs(@LuaNotNil VanillaModelAPI arg) {
        return PAIRS_ITERATOR;
    }

    @Override
    public String toString() {
        return "VanillaModelAPI";
    }

    public enum ModelFunction {
        HEAD(model -> ((HumanoidModel<?>) model).head),
        BODY(model -> ((HumanoidModel<?>) model).body),
        LEFT_ARM(model -> ((HumanoidModel<?>) model).leftArm),
        RIGHT_ARM(model -> ((HumanoidModel<?>) model).rightArm),
        LEFT_LEG(model -> ((HumanoidModel<?>) model).leftLeg),
        RIGHT_LEG(model -> ((HumanoidModel<?>) model).rightLeg),

        HAT(model -> ((HumanoidModel<?>) model).hat),
        JACKET(model -> ((PlayerModel<?>) model).jacket),
        LEFT_SLEEVE(model -> ((PlayerModel<?>) model).leftSleeve),
        RIGHT_SLEEVE(model -> ((PlayerModel<?>) model).rightSleeve),
        LEFT_PANTS(model -> ((PlayerModel<?>) model).leftPants),
        RIGHT_PANTS(model -> ((PlayerModel<?>) model).rightPants),
        CAPE(model -> ((PlayerModelAccessor) model).figura$getCloak()),
        FAKE_CAPE(model -> ((PlayerModelAccessor) model).figura$getFakeCloak()),

        LEFT_ELYTRON(model -> ((ElytraModelAccessor) model).getLeftWing()),
        RIGHT_ELYTRON(model -> ((ElytraModelAccessor) model).getRightWing());

        public final Function<EntityModel<?>, ModelPart> func;

        ModelFunction(Function<EntityModel<?>, ModelPart> func) {
            this.func = func;
        }
    }
}
