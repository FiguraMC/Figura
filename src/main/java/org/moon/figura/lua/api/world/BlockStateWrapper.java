package org.moon.figura.lua.api.world;

import net.minecraft.world.level.block.state.BlockState;
import org.moon.figura.lua.LuaWhitelist;

import java.lang.ref.WeakReference;

@LuaWhitelist
public class BlockStateWrapper {

    private final WeakReference<BlockState> blockState;

    public BlockStateWrapper(BlockState wrapped) {
        blockState = new WeakReference<>(wrapped);
        name = wrapped.getBlock().getName().getString();
    }

    @LuaWhitelist
    public final String name;

}
