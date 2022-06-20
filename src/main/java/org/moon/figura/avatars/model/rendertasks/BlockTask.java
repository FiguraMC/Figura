package org.moon.figura.avatars.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.state.BlockState;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
public class BlockTask extends RenderTask {

    private BlockState block;

    @Override
    public void render(PoseStack stack, MultiBufferSource buffer, int light, int overlay) {
        if (!enabled || block == null || block.isAir())
            return;

        stack.pushPose();
        applyMatrices(stack);
        stack.scale(16, 16, 16);

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(block, stack, buffer, emissive ? LightTexture.FULL_BRIGHT : light, overlay);

        stack.popPose();
    }

    @LuaWhitelist
    public static RenderTask block(@LuaNotNil BlockTask task, Object block) {
        task.block = LuaUtils.parseBlockState("block", block);
        return task;
    }
}
