package org.figuramc.figura.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.figuramc.figura.lua.api.vanilla_model.VanillaPart;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.permissions.Permissions;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class RenderUtils {

    public static boolean vanillaModel(Avatar avatar) {
        return avatar != null && avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) >= 1;
    }

    public static boolean vanillaModelAndScript(Avatar avatar) {
        return avatar != null && avatar.luaRuntime != null && avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) >= 1;
    }

    public static TextureAtlasSprite firstFireLayer(Avatar avatar) {
        if (!vanillaModelAndScript(avatar))
            return null;

        ResourceLocation layer = avatar.luaRuntime.renderer.fireLayer1;
        return layer != null ? Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS).getSprite(layer) : null;
    }

    public static TextureAtlasSprite secondFireLayer(Avatar avatar) {
        if (!vanillaModelAndScript(avatar))
            return null;

        ResourceLocation layer1 = avatar.luaRuntime.renderer.fireLayer1;
        ResourceLocation layer2 = avatar.luaRuntime.renderer.fireLayer2;

        if (layer2 != null)
            return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS).getSprite(layer2);
        if (layer1 != null)
            return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS).getSprite(layer1);

        return null;
    }

    public static VanillaPart partFromSlot(Avatar avatar, EquipmentSlot equipmentSlot) {
        if (!RenderUtils.vanillaModelAndScript(avatar))
            return null;

        return switch (equipmentSlot) {
            case HEAD -> avatar.luaRuntime.vanilla_model.HELMET;
            case CHEST -> avatar.luaRuntime.vanilla_model.CHESTPLATE;
            case LEGS -> avatar.luaRuntime.vanilla_model.LEGGINGS;
            case FEET -> avatar.luaRuntime.vanilla_model.BOOTS;
            default -> null;
        };
    }

    public static VanillaPart pivotToPart(Avatar avatar, ParentType type) {
        if (!RenderUtils.vanillaModelAndScript(avatar))
            return null;

        return switch (type) {
            case HelmetPivot -> avatar.luaRuntime.vanilla_model.HELMET;
            case ChestplatePivot -> avatar.luaRuntime.vanilla_model.CHESTPLATE;
            case LeftShoulderPivot -> avatar.luaRuntime.vanilla_model.CHESTPLATE_LEFT_ARM;
            case RightShoulderPivot -> avatar.luaRuntime.vanilla_model.CHESTPLATE_RIGHT_ARM;
            case LeggingsPivot -> avatar.luaRuntime.vanilla_model.LEGGINGS;
            case LeftLeggingPivot -> avatar.luaRuntime.vanilla_model.LEGGINGS_LEFT_LEG;
            case RightLeggingPivot -> avatar.luaRuntime.vanilla_model.LEGGINGS_RIGHT_LEG;
            case LeftBootPivot -> avatar.luaRuntime.vanilla_model.BOOTS_LEFT_LEG;
            case RightBootPivot -> avatar.luaRuntime.vanilla_model.BOOTS_RIGHT_LEG;
            case LeftElytraPivot -> avatar.luaRuntime.vanilla_model.LEFT_ELYTRA;
            case RightElytraPivot -> avatar.luaRuntime.vanilla_model.RIGHT_ELYTRA;
            default -> null;
        };
    }

    public static EquipmentSlot slotFromPart(ParentType type) {
        switch (type){
            case Head, HelmetItemPivot, HelmetPivot, Skull -> {
                return EquipmentSlot.HEAD;
            }
            case Body, ChestplatePivot, LeftShoulderPivot, RightShoulderPivot, LeftElytra, RightElytra, RightElytraPivot, LeftElytraPivot -> {
                return EquipmentSlot.CHEST;
            }
            case LeftArm, LeftItemPivot, LeftSpyglassPivot -> {
                return EquipmentSlot.OFFHAND;
            }
            case RightArm, RightItemPivot, RightSpyglassPivot -> {
                return EquipmentSlot.MAINHAND;
            }
            case LeftLeggingPivot, RightLeggingPivot, LeftLeg, RightLeg, LeggingsPivot -> {
                return EquipmentSlot.LEGS;
            }
            case LeftBootPivot, RightBootPivot -> {
                return EquipmentSlot.FEET;
            }
            default -> {
                return null;
            }
        }

    }

    public static boolean renderArmItem(Avatar avatar, boolean lefty, CallbackInfo ci) {
        if (!vanillaModel(avatar))
            return false;

        if (avatar.luaRuntime != null && (
                lefty && !avatar.luaRuntime.vanilla_model.LEFT_ITEM.checkVisible() ||
                !lefty && !avatar.luaRuntime.vanilla_model.RIGHT_ITEM.checkVisible()
        )) {
            ci.cancel();
            return false;
        }

        return true;
    }

    @ExpectPlatform
    public static ResourceLocation getPlayerSkinTexture(WingsLayer<?, ?> wingsLayer, HumanoidRenderState renderState) {
        throw new AssertionError();
    }

    static final ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
    public static void renderStatic(LivingEntity entity, ItemStack item, ItemDisplayContext displayMode, PoseStack poseStack, int newLight, int newOverlay) {
        Minecraft client = Minecraft.getInstance();
        FeatureRenderDispatcher featureRenderDispatcher = client.gameRenderer.getFeatureRenderDispatcher();
        SubmitNodeStorage submitNodeStorage = featureRenderDispatcher.getSubmitNodeStorage();
        if (entity != null)
            client.getItemModelResolver().updateForLiving(itemStackRenderState, item, displayMode, entity);
        else
            client.getItemModelResolver().updateForTopItem(itemStackRenderState, item, displayMode, client.level, null, 0);
        itemStackRenderState.submit(poseStack, submitNodeStorage, newLight, newOverlay, 0);
    }



    static PoseStack dummyPoseStack = new PoseStack();
    public static void createDummySubmission(BitSet selection, SubmitNodeCollector submitNodeStorage, BiFunction<MultiBufferSource, PoseStack, Boolean> preRender, Runnable postRender) {
        // otherwise something is very wrong and this will cause out of bounds exceptions and other bad things
        assert (selection.size() >= 3);


        AvatarRenderer<AbstractClientPlayer> avatarRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getPlayerRenderer(Minecraft.getInstance().player);

        if (selection.get(1)) {
            PlayerModel playerModel = avatarRenderer.getModel();
            ((FiguraSubmitCallBackExtension) playerModel).figura$addPreRenderingCallback(preRender);
            ((FiguraSubmitCallBackExtension) playerModel).figura$addPostRenderingCallback(postRender);
            submitNodeStorage.submitModel(playerModel, null, dummyPoseStack, RenderType.LINES, 0, 0, 0, null);
        }

        if (selection.get(2)) {
            ModelPart modelPart = avatarRenderer.getModel().leftArm;
            ((FiguraSubmitCallBackExtension) (Object)modelPart).figura$addPreRenderingCallback(preRender);
            ((FiguraSubmitCallBackExtension) (Object)modelPart).figura$addPostRenderingCallback(postRender);
            submitNodeStorage.submitModelPart(modelPart, dummyPoseStack, RenderType.LINES, 0, 0, null);
        }

        if (selection.get(3)) {
            // TODO ITEM SUBMISSION
            //  submitNodeStorage.submitItem(dummyStack, ItemDisplayContext.FIXED, 0, 0, 0, dummyArr, List.of(), RenderType.LINES, ItemStackRenderState.FoilType.NONE);
        }



    }


    public static <M extends Model<?>> Map<ModelPart, PartPose> captureModelState(M model) {
        HashMap<ModelPart, PartPose> map = new HashMap<>();
        for (ModelPart part : model.allParts()) {
            PartPose pose = part.storePose();
            map.put(part, pose);
        }
        return map;
    }

    public static <M extends Model<?>> void restoreModelPoseState(M model, Map<ModelPart, PartPose> map) {
        for (ModelPart part : model.allParts()) {
            PartPose pose = map.get(part);
            if (pose != null)
                part.loadPose(pose);
        }
    }

    public static boolean isEntityUpsideDown(LivingEntity livingEntity) {
        if (livingEntity instanceof Player || livingEntity.hasCustomName()) {
            String string = ChatFormatting.stripFormatting(livingEntity.getName().getString());
            if ("Dinnerbone".equals(string) || "Grumm".equals(string)) {
                return !(livingEntity instanceof Player player && !player.isModelPartShown(PlayerModelPart.CAPE));
            }
        }

        return false;
    }
}
