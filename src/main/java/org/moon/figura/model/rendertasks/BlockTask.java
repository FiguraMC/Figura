package org.moon.figura.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.BlockStateAPI;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.model.PartCustomization;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "BlockTask",
        value = "block_task"
)
public class BlockTask extends RenderTask {

    private BlockState block;
    private int cachedComplexity;

    public BlockTask(String name, Avatar owner) {
        super(name, owner);
    }

    @Override
    public void render(PartCustomization.PartCustomizationStack stack, MultiBufferSource buffer, int light, int overlay) {
        this.pushOntoStack(stack); //push
        PoseStack poseStack = stack.peek().copyIntoGlobalPoseStack();
        poseStack.scale(16, 16, 16);

        int newLight = this.customization.light != null ? this.customization.light : light;
        int newOverlay = this.customization.overlay != null ? this.customization.overlay : overlay;

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(block, poseStack, buffer, newLight, newOverlay);

        stack.pop(); //pop
    }

    @Override
    public int getComplexity() {
        return cachedComplexity;
    }

    @Override
    public boolean shouldRender() {
        return super.shouldRender() && block != null && !block.isAir();
    }

    // -- lua -- //


    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "block"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = BlockStateAPI.class,
                            argumentNames = "block"
                    )
            },
            aliases = "block",
            value = "block_task.set_block"
    )
    public BlockTask setBlock(Object block) {
        this.block = LuaUtils.parseBlockState("block", block);
        Minecraft client = Minecraft.getInstance();
        RandomSource random = client.level != null ? client.level.random : RandomSource.create();

        BakedModel blockModel = client.getBlockRenderer().getBlockModel(this.block);
        cachedComplexity = blockModel.getQuads(this.block, null, random).size();
        for (Direction dir : Direction.values())
            cachedComplexity += blockModel.getQuads(this.block, dir, random).size();

        return this;
    }

    @LuaWhitelist
    public BlockTask block(Object block) {
        return setBlock(block);
    }

    @Override
    public String toString() {
        return name + " (Block Render Task)";
    }
}
