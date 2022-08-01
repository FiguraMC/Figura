package org.moon.figura.avatars.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.state.BlockState;
import org.moon.figura.lua.LuaType;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.BlockStateAPI;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.utils.LuaUtils;

@LuaType(typeName = "block_task")
@LuaTypeDoc(
        name = "Block Task",
        description = "block_task"
)
public class BlockTask extends RenderTask {

    private BlockState block;

    @Override
    public void render(PoseStack stack, MultiBufferSource buffer, int light, int overlay) {
        if (!enabled || block == null || block.isAir())
            return;

        stack.pushPose();
        this.apply(stack);
        stack.scale(16, 16, 16);

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(block, stack, buffer, emissive ? LightTexture.FULL_BRIGHT : light, overlay);

        stack.popPose();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = String.class,
                            argumentNames = "block"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = BlockStateAPI.class,
                            argumentNames = "block"
                    )
            },
            description = "block_task.block"
    )
    public RenderTask block(Object block) {
        this.block = LuaUtils.parseBlockState("block", block);
        return this;
    }

    @Override
    public String toString() {
        return "Block Render Task";
    }
}
