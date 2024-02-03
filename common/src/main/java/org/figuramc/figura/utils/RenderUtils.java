package org.figuramc.figura.utils;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.api.vanilla_model.VanillaPart;
import org.figuramc.figura.mixin.render.GlStateManagerAccessor;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.permissions.Permissions;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

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
        return layer != null ? Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(layer) : null;
    }

    public static TextureAtlasSprite secondFireLayer(Avatar avatar) {
        if (!vanillaModelAndScript(avatar))
            return null;

        ResourceLocation layer1 = avatar.luaRuntime.renderer.fireLayer1;
        ResourceLocation layer2 = avatar.luaRuntime.renderer.fireLayer2;

        if (layer2 != null)
            return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(layer2);
        if (layer1 != null)
            return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(layer1);

        return null;
    }

    public static VanillaPart partFromSlot(Avatar avatar, EquipmentSlot equipmentSlot) {
        if (!RenderUtils.vanillaModelAndScript(avatar))
            return null;

        switch (equipmentSlot) {
            case HEAD:
                return avatar.luaRuntime.vanilla_model.HELMET;
            case CHEST:
                return avatar.luaRuntime.vanilla_model.CHESTPLATE;
            case LEGS:
                return avatar.luaRuntime.vanilla_model.LEGGINGS;
            case FEET:
                return avatar.luaRuntime.vanilla_model.BOOTS;
            default:
                return null;
        }
    }

    public static EquipmentSlot slotFromPart(ParentType type) {
        switch (type) {
            case Head:
            case HelmetItemPivot:
            case HelmetPivot:
            case Skull:
                return EquipmentSlot.HEAD;
            case Body:
            case ChestplatePivot:
            case LeftShoulderPivot:
            case RightShoulderPivot:
            case LeftElytra:
            case RightElytra:
            case ElytraPivot:
                return EquipmentSlot.CHEST;
            case LeftArm:
            case LeftItemPivot:
            case LeftSpyglassPivot:
                return EquipmentSlot.OFFHAND;
            case RightArm:
            case RightItemPivot:
            case RightSpyglassPivot:
                return EquipmentSlot.MAINHAND;
            case LeftLeggingPivot:
            case RightLeggingPivot:
            case LeftLeg:
            case RightLeg:
            case LeggingsPivot:
                return EquipmentSlot.LEGS;
            case LeftBootPivot:
            case RightBootPivot:
                return EquipmentSlot.FEET;
            default:
                return null;
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
    public static <T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> ResourceLocation getArmorResource(HumanoidArmorLayer<T, M, A> armorLayer, Entity entity, ItemStack stack, ArmorItem item, EquipmentSlot slot, boolean isInner, String type) {
        throw new AssertionError();
    }

    public static class TextRenderType extends RenderType {
        public static Function<ResourceLocation, RenderType> TEXT_BACKGROUND_SEE_THROUGH = ResourceUtils.memoize((texture) -> {
            return create("text_background_see_through", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormatMode.QUADS.asGLMode, 256, false, true, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(texture, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(LIGHTMAP).setDepthTestState(NO_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).createCompositeState(false));
        });
        public static Function<ResourceLocation, RenderType> TEXT_BACKGROUND = ResourceUtils.memoize((texture) -> {
            return create("text_background", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormatMode.QUADS.asGLMode, 256, false, true, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(texture, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(LIGHTMAP).createCompositeState(false));
        });

        public TextRenderType(String name, VertexFormat vertexFormat, VertexFormatMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
            super(name, vertexFormat, drawMode.asGLMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
        }

    }

    public static final Vector3f INVENTORY_DIFFUSE_LIGHT_0 = Util.make(new Vector3f(0.2f, -1.0f, -1.0f), Vector3f::normalize);
    public static final Vector3f INVENTORY_DIFFUSE_LIGHT_1 = Util.make(new Vector3f(-0.2f, -1.0f, 0.0f), Vector3f::normalize);
    public static void setLights(Vector3f lightingVector1, Vector3f lightingVector2) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        Vector4f vector4f = new Vector4f(lightingVector1);
        GlStateManager._pushMatrix();
        GlStateManager._light(GL11.GL_LIGHT0, GL11.GL_POSITION, GlStateManagerAccessor.invokeGetFloatBuffer(vector4f.x(), vector4f.y(), vector4f.z(), 0.0f));
        float f = 0.6f;
        GlStateManager._light(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, GlStateManagerAccessor.invokeGetFloatBuffer(f, f, f, 1.0f));
        GlStateManager._light(GL11.GL_LIGHT0, GL11.GL_AMBIENT, GlStateManagerAccessor.invokeGetFloatBuffer(0.0f, 0.0f, 0.0f, 1.0f));
        GlStateManager._light(GL11.GL_LIGHT0, GL11.GL_SPECULAR, GlStateManagerAccessor.invokeGetFloatBuffer(0.0f, 0.0f, 0.0f, 1.0f));
        Vector4f vector4f2 = new Vector4f(lightingVector2);

        GlStateManager._light(GL11.GL_LIGHT1, GL11.GL_POSITION, GlStateManagerAccessor.invokeGetFloatBuffer(vector4f2.x(), vector4f2.y(), vector4f2.z(), 0.0f));
        GlStateManager._light(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, GlStateManagerAccessor.invokeGetFloatBuffer(f, f, f, 1.0f));
        GlStateManager._light(GL11.GL_LIGHT1, GL11.GL_AMBIENT, GlStateManagerAccessor.invokeGetFloatBuffer(0.0f, 0.0f, 0.0f, 1.0f));
        GlStateManager._light(GL11.GL_LIGHT1, GL11.GL_SPECULAR, GlStateManagerAccessor.invokeGetFloatBuffer(0.0f, 0.0f, 0.0f, 1.0f));
        GlStateManager._shadeModel(GL11.GL_FLAT);
        float g = 0.4f;
        GlStateManager._lightModel(GL11.GL_LIGHT_MODEL_AMBIENT, GlStateManagerAccessor.invokeGetFloatBuffer(g, g, g, 1.0f));
        GlStateManager._popMatrix();
    }


}
