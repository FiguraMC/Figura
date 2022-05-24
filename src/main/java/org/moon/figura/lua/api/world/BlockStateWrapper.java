package org.moon.figura.lua.api.world;

import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaTable;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec6;
import org.moon.figura.mixin.BlockBehaviourAccessor;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.LuaUtils;
import org.terasology.jnlua.LuaRuntimeException;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Optional;

@LuaWhitelist
@LuaTypeDoc(
        name = "BlockState",
        description = "blockstate"
)
public class BlockStateWrapper {

    private final WeakReference<BlockState> blockState;
    private BlockPos pos;

    @LuaWhitelist
    @LuaFieldDoc(description = "blockstate.id")
    public final String id;

    public BlockStateWrapper(BlockState wrapped, BlockPos pos) {
        this.blockState = new WeakReference<>(wrapped);
        this.pos = pos;
        this.id = Registry.BLOCK.getKey(wrapped.getBlock()).toString();
        //TODO - properties
    }

    protected static BlockState getState(BlockStateWrapper blockState) {
        if (!exists(blockState)) throw new LuaRuntimeException("BlockState does not exist!");
        return blockState.blockState.get();
    }

    protected static BlockPos getBlockPos(BlockStateWrapper blockState) {
        return blockState.pos == null ? BlockPos.ZERO : blockState.pos;
    }

    protected static LuaTable voxelShapeToTable(VoxelShape shape) {
        LuaTable shapes = new LuaTable();
        int i = 1;
        for (AABB aabb : shape.toAabbs()) {
            shapes.put(i, FiguraVec6.of(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ));
            i++;
        }
        return shapes;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.get_pos"
    )
    public static FiguraVec3 getPos(@LuaNotNil BlockStateWrapper blockState) {
        BlockPos pos = getBlockPos(blockState);
        return FiguraVec3.of(pos.getX(), pos.getY(), pos.getZ());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, FiguraVec3.class},
                            argumentNames = {"blockState", "pos"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, Double.class, Double.class, Double.class},
                            argumentNames = {"blockState", "x", "y", "z"}
                    )
            },
            description = "blockstate.set_pos"
    )
    public static void setPos(@LuaNotNil BlockStateWrapper blockState, Object x, Double y, Double z) {
        FiguraVec3 newPos = LuaUtils.parseVec3("setPos", x, y, z);
        blockState.pos = newPos.asBlockPos();
        newPos.free();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.exists"
    )
    public static boolean exists(@LuaNotNil BlockStateWrapper blockState) {
        return blockState.blockState.get() != null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.is_translucent"
    )
    public static boolean isTranslucent(@LuaNotNil BlockStateWrapper blockState) {
        return getState(blockState).propagatesSkylightDown(WorldAPI.getCurrentWorld(), getBlockPos(blockState));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.get_opacity"
    )
    public static int getOpacity(@LuaNotNil BlockStateWrapper blockState) {
        return getState(blockState).getLightBlock(WorldAPI.getCurrentWorld(), getBlockPos(blockState));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.get_map_color"
    )
    public static FiguraVec3 getMapColor(@LuaNotNil BlockStateWrapper blockState) {
        return ColorUtils.intToRGB(getState(blockState).getMapColor(WorldAPI.getCurrentWorld(), getBlockPos(blockState)).col);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.is_solid_block"
    )
    public static boolean isSolidBlock(@LuaNotNil BlockStateWrapper blockState) {
        return getState(blockState).isRedstoneConductor(WorldAPI.getCurrentWorld(), getBlockPos(blockState));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.is_full_cube"
    )
    public static boolean isFullCube(@LuaNotNil BlockStateWrapper blockState) {
        return getState(blockState).isCollisionShapeFullBlock(WorldAPI.getCurrentWorld(), getBlockPos(blockState));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.has_emissive_lightning"
    )
    public static boolean hasEmissiveLighting(@LuaNotNil BlockStateWrapper blockState) {
        return getState(blockState).emissiveRendering(WorldAPI.getCurrentWorld(), getBlockPos(blockState));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.get_hardness"
    )
    public static float getHardness(@LuaNotNil BlockStateWrapper blockState) {
        return getState(blockState).getDestroySpeed(WorldAPI.getCurrentWorld(), getBlockPos(blockState));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.get_comparator_output"
    )
    public static int getComparatorOutput(@LuaNotNil BlockStateWrapper blockState) {
        return getState(blockState).getAnalogOutputSignal(WorldAPI.getCurrentWorld(), getBlockPos(blockState));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.has_block_entity"
    )
    public static boolean hasBlockEntity(@LuaNotNil BlockStateWrapper blockState) {
        return getState(blockState).hasBlockEntity();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.is_opaque"
    )
    public static boolean isOpaque(@LuaNotNil BlockStateWrapper blockState) {
        return getState(blockState).canOcclude();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.emits_redstone_power"
    )
    public static boolean emitsRedstonePower(@LuaNotNil BlockStateWrapper blockState) {
        return getState(blockState).isSignalSource();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.get_luminance"
    )
    public static int getLuminance(@LuaNotNil BlockStateWrapper blockState) {
        return getState(blockState).getLightEmission();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.get_friction"
    )
    public static float getFriction(@LuaNotNil BlockStateWrapper blockState) {
        return getState(blockState).getBlock().getFriction();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.get_velocity_multiplier"
    )
    public static float getVelocityMultiplier(@LuaNotNil BlockStateWrapper blockState) {
        return getState(blockState).getBlock().getSpeedFactor();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.get_jump_velocity_multiplier"
    )
    public static float getJumpVelocityMultiplier(@LuaNotNil BlockStateWrapper blockState) {
        return getState(blockState).getBlock().getJumpFactor();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.get_blast_resistance"
    )
    public static float getBlastResistance(@LuaNotNil BlockStateWrapper blockState) {
        return getState(blockState).getBlock().getExplosionResistance();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.as_item"
    )
    public static ItemStackWrapper asItem(@LuaNotNil BlockStateWrapper blockState) {
        return ItemStackWrapper.verify(getState(blockState).getBlock().asItem().getDefaultInstance());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.get_tags"
    )
    public static LuaTable getTags(@LuaNotNil BlockStateWrapper blockState) {
        LuaTable table = new LuaTable();

        Registry<Block> registry = WorldAPI.getCurrentWorld().registryAccess().registryOrThrow(Registry.BLOCK_REGISTRY);
        Optional<ResourceKey<Block>> key = registry.getResourceKey(getState(blockState).getBlock());

        if (key.isEmpty())
            return table;

        int i = 1;
        for (TagKey<Block> blockTagKey : registry.getHolderOrThrow(key.get()).tags().toList()) {
            table.put(i, blockTagKey.location().toString());
            i++;
        }

        return table;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.get_material"
    )
    public static String getMaterial(@LuaNotNil BlockStateWrapper blockState) {
        BlockState state = getState(blockState);

        for (Field field : Material.class.getFields()) {
            try {
                if (field.get(null) == state.getMaterial())
                    return field.getName();
            } catch (Exception ignored) {}
        }

        return null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.get_has_collision"
    )
    public static boolean hasCollision(@LuaNotNil BlockStateWrapper blockState) {
        return ((BlockBehaviourAccessor) getState(blockState).getBlock()).hasCollision();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.get_collision_shape"
    )
    public static LuaTable getCollisionShape(@LuaNotNil BlockStateWrapper blockState) {
        return voxelShapeToTable(getState(blockState).getCollisionShape(WorldAPI.getCurrentWorld(), getBlockPos(blockState)));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.get_outline_shape"
    )
    public static LuaTable getOutlineShape(@LuaNotNil BlockStateWrapper blockState) {
        return voxelShapeToTable(getState(blockState).getShape(WorldAPI.getCurrentWorld(), getBlockPos(blockState)));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.get_sounds"
    )
    public static LuaTable getSounds(@LuaNotNil BlockStateWrapper blockState) {
        LuaTable sounds = new LuaTable();
        SoundType snd = getState(blockState).getSoundType();

        sounds.put("pitch", snd.getPitch());
        sounds.put("volume", snd.getVolume());
        sounds.put("break", snd.getBreakSound().getLocation().toString());
        sounds.put("fall", snd.getFallSound().getLocation().toString());
        sounds.put("hit", snd.getHitSound().getLocation().toString());
        sounds.put("plate", snd.getPlaceSound().getLocation().toString());
        sounds.put("step", snd.getStepSound().getLocation().toString());

        return sounds;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.get_entity_data"
    )
    public static void getEntityData(@LuaNotNil BlockStateWrapper blockState) {
        BlockEntity entity = WorldAPI.getCurrentWorld().getBlockEntity(getBlockPos(blockState));
        CompoundTag tag = entity != null ? entity.saveWithoutMetadata() : new CompoundTag();
        //TODO - NBT of "tag"
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.to_state_string"
    )
    public static String toStateString(@LuaNotNil BlockStateWrapper blockState) {
        BlockEntity entity = WorldAPI.getCurrentWorld().getBlockEntity(getBlockPos(blockState));
        CompoundTag tag = entity != null ? entity.saveWithoutMetadata() : new CompoundTag();

        return BlockStateParser.serialize(getState(blockState)) + tag;
    }

    @Override
    public String toString() {
        return id + " (BlockState)";
    }
}
