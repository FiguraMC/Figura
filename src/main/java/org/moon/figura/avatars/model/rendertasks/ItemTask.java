package org.moon.figura.avatars.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.utils.LuaUtils;
import org.terasology.jnlua.LuaRuntimeException;

@LuaWhitelist
public class ItemTask extends RenderTask {

    private ItemStack item;
    private ItemTransforms.TransformType renderType;

    @Override
    public void render(PoseStack stack, MultiBufferSource buffer, int light, int overlay) {
        if (!enabled || item == null || item.isEmpty())
            return;

        stack.pushPose();
        applyMatrices(stack);
        stack.scale(16, 16, 16);

        Minecraft.getInstance().getItemRenderer().renderStatic(item, renderType == null ? ItemTransforms.TransformType.NONE : renderType, emissive ? LightTexture.FULL_BRIGHT : light, overlay, stack, buffer, 0);

        stack.popPose();
    }

    @LuaWhitelist
    public static RenderTask item(@LuaNotNil ItemTask task, Object item) {
        task.item = LuaUtils.parseItemStack("item", item);
        return task;
    }

    @LuaWhitelist
    public static RenderTask renderType(@LuaNotNil ItemTask task, String type) {
        try {
            task.renderType = ItemTransforms.TransformType.valueOf(type);
            return task;
        } catch (Exception ignored) {
            throw new LuaRuntimeException("Illegal RenderType: \"" + type + "\".");
        }
    }
}
