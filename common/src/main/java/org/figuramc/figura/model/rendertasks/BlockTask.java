package org.figuramc.figura.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.world.BlockStateAPI;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.model.FiguraModelPart;
import org.figuramc.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "BlockTask",
        value = "block_task"
)
public class BlockTask extends RenderTask {

    private BlockState block;
    private int cachedComplexity;

    public BlockTask(String name, Avatar owner, FiguraModelPart parent) {
        super(name, owner, parent);
    }

    @Override
    public void render(PoseStack poseStack, SubmitNodeCollector buffer, int light, int overlay) {
        poseStack.scale(16, 16, 16);

        int newLight = this.customization.light != null ? this.customization.light : light;
        int newOverlay = this.customization.overlay != null ? this.customization.overlay : overlay;

        var state = new net.minecraft.client.renderer.block.BlockModelRenderState();
        Minecraft.getInstance().getModelManager().getBlockModelSet().get(block).update(
                state, block, net.minecraft.client.renderer.block.model.BlockDisplayContext.create(), 0L);
        state.submit(poseStack, buffer, newLight, newOverlay, 0);
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
        RandomSource random = client.level != null ? client.level.getRandom() : RandomSource.create();

        BlockStateModel blockModel = client.getModelManager().getBlockStateModelSet().get(this.block);
        var parts = new java.util.ArrayList<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart>();
        blockModel.collectParts(random, parts);
        cachedComplexity = parts.stream().mapToInt(p -> p.getQuads(null).size()).sum();
        for (Direction dir : Direction.values())
            cachedComplexity += parts.stream().mapToInt(p -> p.getQuads(dir).size()).sum();

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
