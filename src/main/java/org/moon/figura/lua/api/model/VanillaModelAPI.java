package org.moon.figura.lua.api.model;

import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.avatars.vanilla.VanillaPartOffsetManager;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaPairsIterator;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.ducks.PlayerModelAccessor;
import org.moon.figura.mixin.render.layers.elytra.ElytraModelAccessor;
import org.terasology.jnlua.LuaRuntimeException;

import java.util.List;
import java.util.function.Function;

/**
 * This class is honestly decently organized, it just has... a lot of stuff in it
 */

@LuaWhitelist
@LuaTypeDoc(
        name = "VanillaModelAPI",
        description = "vanilla_model"
)
public class VanillaModelAPI {

    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.head")
    public final VanillaModelPart<HumanoidModel<?>> HEAD;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.body")
    public final VanillaModelPart<HumanoidModel<?>> BODY;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.left_arm")
    public final VanillaModelPart<HumanoidModel<?>> LEFT_ARM;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.right_arm")
    public final VanillaModelPart<HumanoidModel<?>> RIGHT_ARM;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.left_leg")
    public final VanillaModelPart<HumanoidModel<?>> LEFT_LEG;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.right_leg")
    public final VanillaModelPart<HumanoidModel<?>> RIGHT_LEG;

    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.hat")
    public final VanillaModelPart<HumanoidModel<?>> HAT;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.jacket")
    public final VanillaModelPart<PlayerModel<?>> JACKET;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.left_sleeve")
    public final VanillaModelPart<PlayerModel<?>> LEFT_SLEEVE;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.right_sleeve")
    public final VanillaModelPart<PlayerModel<?>> RIGHT_SLEEVE;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.left_pants")
    public final VanillaModelPart<PlayerModel<?>> LEFT_PANTS;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.right_pants")
    public final VanillaModelPart<PlayerModel<?>> RIGHT_PANTS;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.cape")
    public final VanillaModelPart<PlayerModel<?>> CAPE;

    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.helmet")
    public final VanillaModelPart<HumanoidModel<?>> HELMET;

    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.chestplate")
    public final VanillaModelPart<HumanoidModel<?>> CHESTPLATE;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.chestplate_body")
    public final VanillaModelPart<HumanoidModel<?>> CHESTPLATE_BODY;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.chestplate_left_arm")
    public final VanillaModelPart<HumanoidModel<?>> CHESTPLATE_LEFT_ARM;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.chestplate_right_arm")
    public final VanillaModelPart<HumanoidModel<?>> CHESTPLATE_RIGHT_ARM;


    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.leggings")
    public final VanillaModelPart<HumanoidModel<?>> LEGGINGS;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.leggings_body")
    public final VanillaModelPart<HumanoidModel<?>> LEGGINGS_BODY;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.leggings_left_leg")
    public final VanillaModelPart<HumanoidModel<?>> LEGGINGS_LEFT_LEG;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.leggings_right_leg")
    public final VanillaModelPart<HumanoidModel<?>> LEGGINGS_RIGHT_LEG;

    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.boots")
    public final VanillaModelPart<HumanoidModel<?>> BOOTS;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.boots_left_leg")
    public final VanillaModelPart<HumanoidModel<?>> BOOTS_LEFT_LEG;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.boots_right_leg")
    public final VanillaModelPart<HumanoidModel<?>> BOOTS_RIGHT_LEG;


    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.all")
    public final VanillaModelPart<HumanoidModel<?>> ALL;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.player")
    public final VanillaModelPart<HumanoidModel<?>> PLAYER;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.outer_layer")
    public final VanillaModelPart<HumanoidModel<?>> OUTER_LAYER;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.inner_layer")
    public final VanillaModelPart<HumanoidModel<?>> INNER_LAYER;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.armor")
    public final VanillaModelPart<HumanoidModel<?>> ARMOR;

    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.elytra")
    public final VanillaModelPart<ElytraModel<?>> ELYTRA;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.left_elytra")
    public final VanillaModelPart<ElytraModel<?>> LEFT_ELYTRA;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.right_elytra")
    public final VanillaModelPart<ElytraModel<?>> RIGHT_ELYTRA;

    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.held_items")
    public final VanillaModelPart<EntityModel<?>> HELD_ITEMS;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.left_item")
    public final VanillaModelPart<EntityModel<?>> LEFT_ITEM;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.left_item")
    public final VanillaModelPart<EntityModel<?>> RIGHT_ITEM;


    public void alterPlayerModel(PlayerModel<?> playerModel) {
        alterByPart(playerModel, HEAD);
        alterByPart(playerModel, BODY);
        alterByPart(playerModel, LEFT_ARM);
        alterByPart(playerModel, RIGHT_ARM);
        alterByPart(playerModel, LEFT_LEG);
        alterByPart(playerModel, RIGHT_LEG);
        alterByPart(playerModel, HAT);
        alterByPart(playerModel, JACKET);
        alterByPart(playerModel, LEFT_SLEEVE);
        alterByPart(playerModel, RIGHT_SLEEVE);
        alterByPart(playerModel, LEFT_PANTS);
        alterByPart(playerModel, RIGHT_PANTS);
    }

    public void copyPlayerModel(PlayerModel<?> playerModel) {
        copyByPart(playerModel, HEAD);
        copyByPart(playerModel, BODY);
        copyByPart(playerModel, LEFT_ARM);
        copyByPart(playerModel, RIGHT_ARM);
        copyByPart(playerModel, LEFT_LEG);
        copyByPart(playerModel, RIGHT_LEG);
        copyByPart(playerModel, HAT);
        copyByPart(playerModel, JACKET);
        copyByPart(playerModel, LEFT_SLEEVE);
        copyByPart(playerModel, RIGHT_SLEEVE);
        copyByPart(playerModel, LEFT_PANTS);
        copyByPart(playerModel, RIGHT_PANTS);
    }

    public void restorePlayerModel(PlayerModel<?> playerModel) {
        restoreByPart(playerModel, PLAYER);
        //We don't need to call restore on individual parts, as restoring does not need to set origin rot and pos.
    }


    public <T extends EntityModel<?>> void copyByPart(T model, VanillaModelPart<T> vanillaModelPart) {
        if (vanillaModelPart != null)
            vanillaModelPart.copyInfo(model);
    }

    public <T extends EntityModel<?>> void alterByPart(T model, VanillaModelPart<T> vanillaModelPart) {
        if (vanillaModelPart != null)
            vanillaModelPart.alter(model);
    }

    public <T extends EntityModel<?>> void restoreByPart(T model, VanillaModelPart<T> vanillaModelPart) {
        if (vanillaModelPart != null)
            vanillaModelPart.restore(model);
    }

    public VanillaModelAPI() {
        //REUSED LAMBDAS
        Function<HumanoidModel<?>, ModelPart> HEAD_FUNC = model -> model.head;
        Function<HumanoidModel<?>, ModelPart> BODY_FUNC = model -> model.body;
        Function<HumanoidModel<?>, ModelPart> LEFT_ARM_FUNC = model -> model.leftArm;
        Function<HumanoidModel<?>, ModelPart> RIGHT_ARM_FUNC = model -> model.rightArm;
        Function<HumanoidModel<?>, ModelPart> LEFT_LEG_FUNC = model -> model.leftLeg;
        Function<HumanoidModel<?>, ModelPart> RIGHT_LEG_FUNC = model -> model.rightLeg;

        Function<HumanoidModel<?>, ModelPart> HAT_FUNC = model -> model.hat;
        Function<PlayerModel<?>, ModelPart> JACKET_FUNC = model -> model.jacket;
        Function<PlayerModel<?>, ModelPart> LEFT_SLEEVE_FUNC = model -> model.leftSleeve;
        Function<PlayerModel<?>, ModelPart> RIGHT_SLEEVE_FUNC = model -> model.rightSleeve;
        Function<PlayerModel<?>, ModelPart> LEFT_PANTS_FUNC = model -> model.leftPants;
        Function<PlayerModel<?>, ModelPart> RIGHT_PANTS_FUNC = model -> model.rightPants;
        Function<PlayerModel<?>, ModelPart> CAPE_FUNC = model -> ((PlayerModelAccessor) model).figura$getCloak();
        Function<PlayerModel<?>, ModelPart> FAKE_CAPE_FUNC = model -> ((PlayerModelAccessor) model).figura$getFakeCloak();

        Function<ElytraModel<?>, ModelPart> LEFT_ELYTRON_FUNC = model -> ((ElytraModelAccessor) model).getLeftWing();
        Function<ElytraModel<?>, ModelPart> RIGHT_ELYTRON_FUNC = model -> ((ElytraModelAccessor) model).getRightWing();

        Function<EntityModel<?>, ModelPart> NULL_FUNC = model -> null;

        //TRACKERS
        PartTracker<HumanoidModel<?>> HEAD_TRACKER = new PartTracker<>(HEAD_FUNC, true);
        PartTracker<HumanoidModel<?>> BODY_TRACKER = new PartTracker<>(BODY_FUNC, true);
        PartTracker<HumanoidModel<?>> LEFT_ARM_TRACKER = new PartTracker<>(LEFT_ARM_FUNC, true);
        PartTracker<HumanoidModel<?>> RIGHT_ARM_TRACKER = new PartTracker<>(RIGHT_ARM_FUNC, true);
        PartTracker<HumanoidModel<?>> LEFT_LEG_TRACKER = new PartTracker<>(LEFT_LEG_FUNC, true);
        PartTracker<HumanoidModel<?>> RIGHT_LEG_TRACKER = new PartTracker<>(RIGHT_LEG_FUNC, true);
        PartTracker<HumanoidModel<?>> HAT_TRACKER = new PartTracker<>(HAT_FUNC, true);
        PartTracker<PlayerModel<?>> JACKET_TRACKER = new PartTracker<>(JACKET_FUNC, true);
        PartTracker<PlayerModel<?>> LEFT_SLEEVE_TRACKER = new PartTracker<>(LEFT_SLEEVE_FUNC, true);
        PartTracker<PlayerModel<?>> RIGHT_SLEEVE_TRACKER = new PartTracker<>(RIGHT_SLEEVE_FUNC, true);
        PartTracker<PlayerModel<?>> LEFT_PANTS_TRACKER = new PartTracker<>(LEFT_PANTS_FUNC, true);
        PartTracker<PlayerModel<?>> RIGHT_PANTS_TRACKER = new PartTracker<>(RIGHT_PANTS_FUNC, true);
        PartTracker<PlayerModel<?>> CAPE_TRACKER = new PartTracker<>(CAPE_FUNC, FAKE_CAPE_FUNC, true);

        PartTracker<HumanoidModel<?>> HELMET_TRACKER = new PartTracker<>(HEAD_FUNC, true);
        PartTracker<HumanoidModel<?>> HELMET_HAT_TRACKER = new PartTracker<>(HAT_FUNC, true);
        PartTracker<HumanoidModel<?>> CHESTPLATE_BODY_TRACKER = new PartTracker<>(BODY_FUNC, true);
        PartTracker<HumanoidModel<?>> CHESTPLATE_LEFT_ARM_TRACKER = new PartTracker<>(LEFT_ARM_FUNC, true);
        PartTracker<HumanoidModel<?>> CHESTPLATE_RIGHT_ARM_TRACKER = new PartTracker<>(RIGHT_ARM_FUNC, true);
        PartTracker<HumanoidModel<?>> LEGGINGS_BODY_TRACKER = new PartTracker<>(BODY_FUNC, true);
        PartTracker<HumanoidModel<?>> LEGGINGS_LEFT_LEG_TRACKER = new PartTracker<>(LEFT_LEG_FUNC, true);
        PartTracker<HumanoidModel<?>> LEGGINGS_RIGHT_LEG_TRACKER = new PartTracker<>(RIGHT_LEG_FUNC, true);
        PartTracker<HumanoidModel<?>> BOOTS_LEFT_LEG_TRACKER = new PartTracker<>(LEFT_LEG_FUNC, true);
        PartTracker<HumanoidModel<?>> BOOTS_RIGHT_LEG_TRACKER = new PartTracker<>(RIGHT_LEG_FUNC, true);

        PartTracker<ElytraModel<?>> LEFT_ELYTRON_TRACKER = new PartTracker<>(LEFT_ELYTRON_FUNC, true);
        PartTracker<ElytraModel<?>> RIGHT_ELYTRON_TRACKER = new PartTracker<>(RIGHT_ELYTRON_FUNC, true);

        PartTracker<EntityModel<?>> LEFT_ITEM_TRACKER = new PartTracker<>(NULL_FUNC, true);
        PartTracker<EntityModel<?>> RIGHT_ITEM_TRACKER = new PartTracker<>(NULL_FUNC, true);

        //INIT VanillaModelPart FIELDS
        HEAD = new VanillaModelPart<>(List.of(HEAD_TRACKER), FiguraModelPart.ParentType.Head);
        BODY = new VanillaModelPart<>(List.of(BODY_TRACKER), FiguraModelPart.ParentType.Body);
        LEFT_ARM = new VanillaModelPart<>(List.of(LEFT_ARM_TRACKER), FiguraModelPart.ParentType.LeftArm);
        RIGHT_ARM = new VanillaModelPart<>(List.of(RIGHT_ARM_TRACKER), FiguraModelPart.ParentType.RightArm);
        LEFT_LEG = new VanillaModelPart<>(List.of(LEFT_LEG_TRACKER), FiguraModelPart.ParentType.LeftLeg);
        RIGHT_LEG = new VanillaModelPart<>(List.of(RIGHT_LEG_TRACKER), FiguraModelPart.ParentType.RightLeg);

        HAT = new VanillaModelPart<>(List.of(HAT_TRACKER), FiguraModelPart.ParentType.Head);
        JACKET = new VanillaModelPart<>(List.of(JACKET_TRACKER), FiguraModelPart.ParentType.Body);
        LEFT_SLEEVE = new VanillaModelPart<>(List.of(LEFT_SLEEVE_TRACKER), FiguraModelPart.ParentType.LeftArm);
        RIGHT_SLEEVE = new VanillaModelPart<>(List.of(RIGHT_SLEEVE_TRACKER), FiguraModelPart.ParentType.RightArm);
        LEFT_PANTS = new VanillaModelPart<>(List.of(LEFT_PANTS_TRACKER), FiguraModelPart.ParentType.LeftLeg);
        RIGHT_PANTS = new VanillaModelPart<>(List.of(RIGHT_PANTS_TRACKER), FiguraModelPart.ParentType.RightLeg);
        CAPE = new VanillaModelPart<>(List.of(CAPE_TRACKER), FiguraModelPart.ParentType.Cape);

        HELMET = new VanillaModelPart<>(List.of(HELMET_TRACKER, HELMET_HAT_TRACKER), null);

        CHESTPLATE = new VanillaModelPart<>(List.of(CHESTPLATE_BODY_TRACKER, CHESTPLATE_LEFT_ARM_TRACKER, CHESTPLATE_RIGHT_ARM_TRACKER), null);
        CHESTPLATE_BODY = new VanillaModelPart<>(List.of(CHESTPLATE_BODY_TRACKER), FiguraModelPart.ParentType.Body);
        CHESTPLATE_LEFT_ARM = new VanillaModelPart<>(List.of(CHESTPLATE_LEFT_ARM_TRACKER), FiguraModelPart.ParentType.LeftArm);
        CHESTPLATE_RIGHT_ARM = new VanillaModelPart<>(List.of(CHESTPLATE_RIGHT_ARM_TRACKER), FiguraModelPart.ParentType.RightArm);

        LEGGINGS = new VanillaModelPart<>(List.of(LEGGINGS_BODY_TRACKER, LEGGINGS_LEFT_LEG_TRACKER, LEGGINGS_RIGHT_LEG_TRACKER), null);
        LEGGINGS_BODY = new VanillaModelPart<>(List.of(LEGGINGS_BODY_TRACKER), FiguraModelPart.ParentType.Body);
        LEGGINGS_LEFT_LEG = new VanillaModelPart<>(List.of(LEGGINGS_LEFT_LEG_TRACKER), FiguraModelPart.ParentType.LeftLeg);
        LEGGINGS_RIGHT_LEG = new VanillaModelPart<>(List.of(LEGGINGS_RIGHT_LEG_TRACKER), FiguraModelPart.ParentType.RightLeg);

        BOOTS = new VanillaModelPart<>(List.of(BOOTS_LEFT_LEG_TRACKER, BOOTS_RIGHT_LEG_TRACKER), null);
        BOOTS_LEFT_LEG = new VanillaModelPart<>(List.of(BOOTS_LEFT_LEG_TRACKER), FiguraModelPart.ParentType.LeftLeg);
        BOOTS_RIGHT_LEG = new VanillaModelPart<>(List.of(BOOTS_RIGHT_LEG_TRACKER), FiguraModelPart.ParentType.RightLeg);

        ELYTRA = new VanillaModelPart<>(List.of(LEFT_ELYTRON_TRACKER, RIGHT_ELYTRON_TRACKER), null);
        LEFT_ELYTRA = new VanillaModelPart<>(List.of(LEFT_ELYTRON_TRACKER), FiguraModelPart.ParentType.LeftElytra);
        RIGHT_ELYTRA = new VanillaModelPart<>(List.of(RIGHT_ELYTRON_TRACKER), FiguraModelPart.ParentType.RightElytra);

        HELD_ITEMS = new VanillaModelPart<>(List.of(LEFT_ITEM_TRACKER, RIGHT_ITEM_TRACKER), null);
        LEFT_ITEM = new VanillaModelPart<>(List.of(LEFT_ITEM_TRACKER), FiguraModelPart.ParentType.LeftArm);
        RIGHT_ITEM = new VanillaModelPart<>(List.of(RIGHT_ITEM_TRACKER), FiguraModelPart.ParentType.RightArm);

        ALL = new VanillaModelPart<>(List.of(
                HEAD_TRACKER, BODY_TRACKER, LEFT_ARM_TRACKER, RIGHT_ARM_TRACKER, LEFT_LEG_TRACKER, RIGHT_LEG_TRACKER,
                HAT_TRACKER, JACKET_TRACKER, LEFT_SLEEVE_TRACKER, RIGHT_SLEEVE_TRACKER, LEFT_PANTS_TRACKER, RIGHT_PANTS_TRACKER,
                CAPE_TRACKER,

                HELMET_TRACKER, HELMET_HAT_TRACKER,
                CHESTPLATE_BODY_TRACKER, CHESTPLATE_LEFT_ARM_TRACKER, CHESTPLATE_RIGHT_ARM_TRACKER,
                LEGGINGS_BODY_TRACKER, LEGGINGS_LEFT_LEG_TRACKER, LEGGINGS_RIGHT_LEG_TRACKER,
                BOOTS_LEFT_LEG_TRACKER, BOOTS_RIGHT_LEG_TRACKER,

                LEFT_ELYTRON_TRACKER, RIGHT_ELYTRON_TRACKER,

                LEFT_ITEM_TRACKER, RIGHT_ITEM_TRACKER
        ), null);

        PLAYER = new VanillaModelPart<>(List.of(
                HEAD_TRACKER, BODY_TRACKER, LEFT_ARM_TRACKER, RIGHT_ARM_TRACKER, LEFT_LEG_TRACKER, RIGHT_LEG_TRACKER,
                HAT_TRACKER, JACKET_TRACKER, LEFT_SLEEVE_TRACKER, RIGHT_SLEEVE_TRACKER, LEFT_PANTS_TRACKER, RIGHT_PANTS_TRACKER,
                CAPE_TRACKER
        ), null);
        OUTER_LAYER = new VanillaModelPart<>(List.of(
                HAT_TRACKER, JACKET_TRACKER, LEFT_SLEEVE_TRACKER, RIGHT_SLEEVE_TRACKER, LEFT_PANTS_TRACKER, RIGHT_PANTS_TRACKER
        ), null);
        INNER_LAYER = new VanillaModelPart<>(List.of(
                HEAD_TRACKER, BODY_TRACKER, LEFT_ARM_TRACKER, RIGHT_ARM_TRACKER, LEFT_LEG_TRACKER, RIGHT_LEG_TRACKER
        ), null);

        ARMOR = new VanillaModelPart<>(List.of(
                HELMET_TRACKER, HELMET_HAT_TRACKER,
                CHESTPLATE_BODY_TRACKER, CHESTPLATE_LEFT_ARM_TRACKER, CHESTPLATE_RIGHT_ARM_TRACKER,
                LEGGINGS_BODY_TRACKER, LEGGINGS_LEFT_LEG_TRACKER, LEGGINGS_RIGHT_LEG_TRACKER,
                BOOTS_LEFT_LEG_TRACKER, BOOTS_RIGHT_LEG_TRACKER
        ), null);

    }

    @LuaWhitelist
    public static LuaPairsIterator<VanillaModelAPI, String> __pairs(@LuaNotNil VanillaModelAPI arg) {
        return PAIRS_ITERATOR;
    }
    private static final LuaPairsIterator<VanillaModelAPI, String> PAIRS_ITERATOR =
            new LuaPairsIterator<>(List.of(

                    "HEAD", "BODY", "LEFT_ARM", "RIGHT_ARM", "LEFT_LEG", "RIGHT_LEG",
                    "HAT", "JACKET", "LEFT_SLEEVE", "RIGHT_SLEEVE", "LEFT_PANTS", "RIGHT_PANTS",
                    "CAPE",

                    "HELMET",
                    "CHESTPLATE_BODY", "CHESTPLATE_LEFT_ARM", "CHESTPLATE_RIGHT_ARM",
                    "LEGGINGS", "LEGGINGS_BODY", "LEGGINGS_LEFT_LEG", "LEGGINGS_RIGHT_LEG",
                    "BOOTS", "BOOTS_LEFT_LEG", "BOOTS_RIGHT_LEG",

                    "LEFT_ELYTRA", "RIGHT_ELYTRA",

                    "LEFT_ITEM", "RIGHT_ITEM"

            ), VanillaModelAPI.class, String.class);

    private static class PartTracker<T extends EntityModel<?>> {
        private final Function<T, ModelPart> partProvider, fakePartProvider;

        private boolean visible, storedVisibility;

        public PartTracker(Function<T, ModelPart> partProvider, boolean defaultVisibility) {
            this(partProvider, partProvider, defaultVisibility);
        }

        public PartTracker(Function<T, ModelPart> realPartProvider, Function<T, ModelPart> fakePartProvider, boolean defaultVisibility) {
            this.partProvider = realPartProvider;
            this.fakePartProvider = fakePartProvider;
            visible = defaultVisibility;
        }

        public void storeOriginData(VanillaModelPart<? extends T> vanillaModelPart, T model) {
            ModelPart part = fakePartProvider.apply(model);
            if (part == null) return;
            vanillaModelPart.savedOriginRot.set(-part.xRot, -part.yRot, part.zRot);
            vanillaModelPart.savedOriginRot.scale(180 / Math.PI);

            FiguraVec3 pivot = VanillaPartOffsetManager.getVanillaOffset(model, vanillaModelPart.parentType);
            pivot.subtract(part.x, part.y, part.z);
            pivot.multiply(1, -1, -1);
            vanillaModelPart.savedOriginPos.set(pivot);
            pivot.free();
        }

        public void alter(T model) {
            ModelPart part = partProvider.apply(model);
            if (part == null) return;
            storedVisibility = part.visible;
            part.visible = visible;
        }

        public void restore(T model) {
            ModelPart part = partProvider.apply(model);
            if (part == null) return;
            part.visible = storedVisibility;
        }
    }

    @Override
    public String toString() {
        return "VanillaModelAPI";
    }

    @LuaWhitelist
    @LuaTypeDoc(
            name = "VanillaModelPart",
            description = "vanilla_part"
    )
    public static class VanillaModelPart<T extends EntityModel<?>> {

        private final List<PartTracker> partModifiers;
        private final FiguraModelPart.ParentType parentType;

        private final FiguraVec3 savedOriginRot = FiguraVec3.of();
        private final FiguraVec3 savedOriginPos = FiguraVec3.of();

        public VanillaModelPart(List<PartTracker> modelParts, FiguraModelPart.ParentType parentType) {
            this.partModifiers = modelParts;
            this.parentType = parentType;
        }

        public void copyInfo(T model) {
            if (partModifiers.size() > 1) return;
            partModifiers.get(0).storeOriginData(this, model);
        }

        public void alter(T model) {
            for (PartTracker<T> tracker : partModifiers)
                tracker.alter(model);
        }

        public void restore(T model) {
            for (PartTracker<T> tracker : partModifiers)
                tracker.restore(model);
        }

        public boolean isVisible() {
            if (partModifiers.size() > 1)
                throw new IllegalArgumentException("Tried to call isVisible on multi-part!");
            return partModifiers.get(0).visible;
        }

        @LuaWhitelist
        @LuaMethodDoc(
                overloads = @LuaFunctionOverload(
                        argumentTypes = {VanillaModelPart.class, Boolean.class},
                        argumentNames = {"vanillaPart", "visible"}
                ),
                description = "vanilla_part.set_visible"
        )
        public static <T extends EntityModel<?>> void setVisible(@LuaNotNil VanillaModelPart<T> vanillaPart, @LuaNotNil Boolean visible) {
            for (PartTracker<T> tracker : vanillaPart.partModifiers)
                tracker.visible = visible;
        }

        @LuaWhitelist
        @LuaMethodDoc(
                overloads = @LuaFunctionOverload(
                        argumentTypes = VanillaModelPart.class,
                        argumentNames = "vanillaPart"
                ),
                description = "vanilla_part.get_visible"
        )
        public static <T extends EntityModel<?>> boolean getVisible(@LuaNotNil VanillaModelPart<T> vanillaPart) {
            if (vanillaPart.partModifiers.size() > 1)
                throw new LuaRuntimeException("Cannot get visibility of vanilla multi-part!");
            return vanillaPart.partModifiers.get(0).visible;
        }

        @LuaWhitelist
        @LuaMethodDoc(
                overloads = @LuaFunctionOverload(
                        argumentTypes = VanillaModelPart.class,
                        argumentNames = "vanillaPart"
                ),
                description = "vanilla_part.get_origin_rot"
        )
        public static <T extends EntityModel<?>> FiguraVec3 getOriginRot(@LuaNotNil VanillaModelPart<T> vanillaPart) {
            if (vanillaPart.partModifiers.size() > 1)
                throw new LuaRuntimeException("Cannot get origin rotation of vanilla multi-part!");
            return vanillaPart.savedOriginRot.copy();
        }

        @LuaWhitelist
        @LuaMethodDoc(
                overloads = @LuaFunctionOverload(
                        argumentTypes = VanillaModelPart.class,
                        argumentNames = "vanillaPart"
                ),
                description = "vanilla_part.get_origin_pos"
        )
        public static <T extends EntityModel<?>> FiguraVec3 getOriginPos(@LuaNotNil VanillaModelPart<T> vanillaPart) {
            if (vanillaPart.partModifiers.size() > 1)
                throw new LuaRuntimeException("Cannot get origin position of vanilla multi-part!");
            return vanillaPart.savedOriginPos.copy();
        }

        @Override
        public String toString() {
            return "VanillaModelPart";
        }
    }
}
