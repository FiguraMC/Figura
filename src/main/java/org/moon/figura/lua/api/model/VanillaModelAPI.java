package org.moon.figura.lua.api.model;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.avatars.vanilla.VanillaPartOffsetManager;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaPairsIterator;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;
import org.terasology.jnlua.LuaRuntimeException;

import java.util.List;
import java.util.function.Function;

@LuaWhitelist
@LuaTypeDoc(
        name = "VanillaModelAPI",
        description = "vanilla_model"
)
public class VanillaModelAPI {

    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.head")
    public final VanillaModelPart HEAD;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.torso")
    public final VanillaModelPart TORSO;
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

    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.all")
    public final VanillaModelPart ALL;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.outer_layer")
    public final VanillaModelPart OUTER_LAYER;
    @LuaWhitelist
    @LuaFieldDoc(description = "vanilla_model.inner_layer")
    public final VanillaModelPart INNER_LAYER;

    public void alterModel(PlayerModel<?> playerModel) {
        HEAD.alter(playerModel);
        TORSO.alter(playerModel);
        LEFT_ARM.alter(playerModel);
        RIGHT_ARM.alter(playerModel);
        LEFT_LEG.alter(playerModel);
        RIGHT_LEG.alter(playerModel);
        HAT.alter(playerModel);
        JACKET.alter(playerModel);
        LEFT_SLEEVE.alter(playerModel);
        RIGHT_SLEEVE.alter(playerModel);
        LEFT_PANTS.alter(playerModel);
        RIGHT_PANTS.alter(playerModel);
        //We don't call alter on multi-parts, since their individual parts are already altered.
    }

    public void restoreModel(PlayerModel<?> playerModel) {
        HEAD_CONSUMER.restore(playerModel);
        TORSO_CONSUMER.restore(playerModel);
        LEFT_ARM_CONSUMER.restore(playerModel);
        RIGHT_ARM_CONSUMER.restore(playerModel);
        LEFT_LEG_CONSUMER.restore(playerModel);
        RIGHT_LEG_CONSUMER.restore(playerModel);
        HAT_CONSUMER.restore(playerModel);
        JACKET_CONSUMER.restore(playerModel);
        LEFT_SLEEVE_CONSUMER.restore(playerModel);
        RIGHT_SLEEVE_CONSUMER.restore(playerModel);
        LEFT_PANTS_CONSUMER.restore(playerModel);
        RIGHT_PANTS_CONSUMER.restore(playerModel);
        //We don't call restore on multi-parts, since the individual parts are already restored.
    }

    //CONSUMERS
    //TODO: Change default visibility depending on circumstances.
    //TODO: For example, HAT_CONSUMER should be invisible by default if 3d Skin Layers is installed.
    private final ModelConsumer HEAD_CONSUMER = new ModelConsumer(model -> model.head, true);
    private final ModelConsumer TORSO_CONSUMER = new ModelConsumer(model -> model.body, true);
    private final ModelConsumer LEFT_ARM_CONSUMER = new ModelConsumer(model -> model.leftArm, true);
    private final ModelConsumer RIGHT_ARM_CONSUMER = new ModelConsumer(model -> model.rightArm, true);
    private final ModelConsumer LEFT_LEG_CONSUMER = new ModelConsumer(model -> model.leftLeg, true);
    private final ModelConsumer RIGHT_LEG_CONSUMER = new ModelConsumer(model -> model.rightLeg, true);
    private final ModelConsumer HAT_CONSUMER = new ModelConsumer(model -> model.hat, true);
    private final ModelConsumer JACKET_CONSUMER = new ModelConsumer(model -> model.jacket, true);
    private final ModelConsumer LEFT_SLEEVE_CONSUMER = new ModelConsumer(model -> model.leftSleeve, true);
    private final ModelConsumer RIGHT_SLEEVE_CONSUMER = new ModelConsumer(model -> model.rightSleeve, true);
    private final ModelConsumer LEFT_PANTS_CONSUMER = new ModelConsumer(model -> model.leftPants, true);
    private final ModelConsumer RIGHT_PANTS_CONSUMER = new ModelConsumer(model -> model.rightPants, true);

    public VanillaModelAPI() {

        HEAD = new VanillaModelPart(List.of(HEAD_CONSUMER), FiguraModelPart.ParentType.Head);
        TORSO = new VanillaModelPart(List.of(TORSO_CONSUMER), FiguraModelPart.ParentType.Body);
        LEFT_ARM = new VanillaModelPart(List.of(LEFT_ARM_CONSUMER), FiguraModelPart.ParentType.LeftArm);
        RIGHT_ARM = new VanillaModelPart(List.of(RIGHT_ARM_CONSUMER), FiguraModelPart.ParentType.RightArm);
        LEFT_LEG = new VanillaModelPart(List.of(LEFT_LEG_CONSUMER), FiguraModelPart.ParentType.LeftLeg);
        RIGHT_LEG = new VanillaModelPart(List.of(RIGHT_LEG_CONSUMER), FiguraModelPart.ParentType.RightLeg);

        HAT = new VanillaModelPart(List.of(HAT_CONSUMER), FiguraModelPart.ParentType.Head);
        JACKET = new VanillaModelPart(List.of(JACKET_CONSUMER), FiguraModelPart.ParentType.Body);
        LEFT_SLEEVE = new VanillaModelPart(List.of(LEFT_SLEEVE_CONSUMER), FiguraModelPart.ParentType.LeftArm);
        RIGHT_SLEEVE = new VanillaModelPart(List.of(RIGHT_SLEEVE_CONSUMER), FiguraModelPart.ParentType.RightArm);
        LEFT_PANTS = new VanillaModelPart(List.of(LEFT_PANTS_CONSUMER), FiguraModelPart.ParentType.LeftLeg);
        RIGHT_PANTS = new VanillaModelPart(List.of(RIGHT_PANTS_CONSUMER), FiguraModelPart.ParentType.RightLeg);

        ALL = new VanillaModelPart(List.of(
                HEAD_CONSUMER, TORSO_CONSUMER, LEFT_ARM_CONSUMER, RIGHT_ARM_CONSUMER, LEFT_LEG_CONSUMER, RIGHT_LEG_CONSUMER,
                HAT_CONSUMER, JACKET_CONSUMER, LEFT_SLEEVE_CONSUMER, RIGHT_SLEEVE_CONSUMER, LEFT_PANTS_CONSUMER, RIGHT_PANTS_CONSUMER
        ), FiguraModelPart.ParentType.None);
        OUTER_LAYER = new VanillaModelPart(List.of(
                HAT_CONSUMER, JACKET_CONSUMER, LEFT_SLEEVE_CONSUMER, RIGHT_SLEEVE_CONSUMER, LEFT_PANTS_CONSUMER, RIGHT_PANTS_CONSUMER
        ), FiguraModelPart.ParentType.None);
        INNER_LAYER = new VanillaModelPart(List.of(
                HEAD_CONSUMER, TORSO_CONSUMER, LEFT_ARM_CONSUMER, RIGHT_ARM_CONSUMER, LEFT_LEG_CONSUMER, RIGHT_LEG_CONSUMER
        ), FiguraModelPart.ParentType.None);
    }

    @LuaWhitelist
    public static LuaPairsIterator<VanillaModelAPI, String> __pairs(VanillaModelAPI arg) {
        return PAIRS_ITERATOR;
    }
    private static final LuaPairsIterator<VanillaModelAPI, String> PAIRS_ITERATOR =
            new LuaPairsIterator<>(List.of(
                    "HEAD", "TORSO",
                    "LEFT_ARM", "RIGHT_ARM",
                    "LEFT_LEG", "RIGHT_LEG",
                    "HAT", "JACKET",
                    "LEFT_SLEEVE", "RIGHT_SLEEVE",
                    "LEFT_PANTS", "RIGHT_PANTS"), VanillaModelAPI.class, String.class);

    private static class ModelConsumer {

        private final Function<PlayerModel<?>, ModelPart> partProvider;
        private boolean visible;

        private boolean storedVisibility;

        public ModelConsumer(Function<PlayerModel<?>, ModelPart> partProvider, boolean defaultVisibility) {
            this.partProvider = partProvider;
            visible = defaultVisibility;
        }

        public void storeOriginData(VanillaModelPart vanillaModelPart, PlayerModel<?> playerModel) {
            ModelPart part = partProvider.apply(playerModel);
            vanillaModelPart.savedOriginRot.set(-part.xRot, -part.yRot, part.zRot);
            vanillaModelPart.savedOriginRot.scale(180 / Math.PI);

            FiguraVec3 pivot = VanillaPartOffsetManager.getVanillaOffset(playerModel, vanillaModelPart.parentType);
            pivot.subtract(part.x, part.y, part.z);
            pivot.multiply(1, -1, -1);
            vanillaModelPart.savedOriginPos.set(pivot);
            pivot.free();
        }

        public void alter(PlayerModel<?> playerModel) {
            ModelPart part = partProvider.apply(playerModel);
            storedVisibility = part.visible;
            part.visible = visible;
        }

        public void restore(PlayerModel<?> playerModel) {
            partProvider.apply(playerModel).visible = storedVisibility;
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
    public static class VanillaModelPart {

        private final List<ModelConsumer> partModifiers;
        private final FiguraModelPart.ParentType parentType;

        private final FiguraVec3 savedOriginRot = FiguraVec3.of();
        private final FiguraVec3 savedOriginPos = FiguraVec3.of();

        public VanillaModelPart(List<ModelConsumer> modelParts, FiguraModelPart.ParentType parentType) {
            this.partModifiers = modelParts;
            this.parentType = parentType;
        }

        public void alter(PlayerModel<?> playerModel) {
            if (partModifiers.size() == 1) {
                partModifiers.get(0).storeOriginData(this, playerModel);
                partModifiers.get(0).alter(playerModel);
            }
        }

        @LuaWhitelist
        @LuaMethodDoc(
                overloads = @LuaFunctionOverload(
                        argumentTypes = {VanillaModelPart.class, Boolean.class},
                        argumentNames = {"vanillaPart", "visible"},
                        returnType = void.class
                ),
                description = "vanilla_part.set_visible"
        )
        public static void setVisible(VanillaModelPart vanillaPart, Boolean visible) {
            LuaUtils.nullCheck("setVisible", "vanillaPart", vanillaPart);
            for (ModelConsumer consumer : vanillaPart.partModifiers)
                consumer.visible = visible;
        }

        @LuaWhitelist
        @LuaMethodDoc(
                overloads = @LuaFunctionOverload(
                        argumentTypes = VanillaModelPart.class,
                        argumentNames = "vanillaPart",
                        returnType = Boolean.class
                ),
                description = "vanilla_part.get_visible"
        )
        public static boolean getVisible(VanillaModelPart vanillaPart) {
            LuaUtils.nullCheck("getOriginRot", "vanillaPart", vanillaPart);
            if (vanillaPart.partModifiers.size() > 1)
                throw new LuaRuntimeException("Cannot get visibility of vanilla multi-part!");
            return vanillaPart.partModifiers.get(0).visible;
        }

        @LuaWhitelist
        @LuaMethodDoc(
                overloads = @LuaFunctionOverload(
                        argumentTypes = VanillaModelPart.class,
                        argumentNames = "vanillaPart",
                        returnType = FiguraVec3.class
                ),
                description = "vanilla_part.get_origin_rot"
        )
        public static FiguraVec3 getOriginRot(VanillaModelPart vanillaPart) {
            LuaUtils.nullCheck("getOriginRot", "vanillaPart", vanillaPart);
            if (vanillaPart.partModifiers.size() > 1)
                throw new LuaRuntimeException("Cannot get origin rotation of vanilla multi-part!");
            return vanillaPart.savedOriginRot.copy();
        }

        @LuaWhitelist
        @LuaMethodDoc(
                overloads = @LuaFunctionOverload(
                        argumentTypes = VanillaModelPart.class,
                        argumentNames = "vanillaPart",
                        returnType = FiguraVec3.class
                ),
                description = "vanilla_part.get_origin_pos"
        )
        public static FiguraVec3 getOriginPos(VanillaModelPart vanillaPart) {
            LuaUtils.nullCheck("getOriginPos", "vanillaPart", vanillaPart);
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
