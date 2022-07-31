package org.moon.figura.lua.api.world;

import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.luaj.vm2.LuaTable;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec6;
import org.moon.figura.mixin.BlockBehaviourAccessor;
import org.moon.figura.lua.LuaType;
import org.moon.figura.lua.LuaTypeManager;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.NbtToLua;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.LuaUtils;

import java.lang.reflect.Field;
import java.util.Optional;

@LuaType(typeName = "blockstate")
@LuaTypeDoc(
        name = "BlockState",
        description = "blockstate"
)
public class BlockStateAPI {

    public final BlockState blockState;
    private BlockPos pos;

    @LuaFieldDoc(description = "blockstate.id")
    public final String id;
    @LuaFieldDoc(description = "blockstate.properties")
    public final LuaTable properties;

    public BlockStateAPI(BlockState blockstate, BlockPos pos) {
        this.blockState = blockstate;
        this.pos = pos;
        this.id = Registry.BLOCK.getKey(blockstate.getBlock()).toString();

        CompoundTag tag = NbtUtils.writeBlockState(blockstate);
        this.properties = (LuaTable) NbtToLua.convert(tag.contains("Properties") ? tag.get("Properties") : null);
    }

    protected BlockPos getBlockPos() {
        return pos == null ? BlockPos.ZERO : pos;
    }

    protected static LuaTable voxelShapeToTable(VoxelShape shape) {
        LuaTable shapes = new LuaTable();
        int i = 1;
        for (AABB aabb : shape.toAabbs()) {
            shapes.set(i, LuaTypeManager.wrap(FiguraVec6.of(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ)));
            i++;
        }
        return shapes;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.get_pos")
    public FiguraVec3 getPos() {
        BlockPos pos = getBlockPos();
        return FiguraVec3.of(pos.getX(), pos.getY(), pos.getZ());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "blockstate.set_pos"
    )
    public void setPos(Object x, Double y, Double z) {
        FiguraVec3 newPos = LuaUtils.parseVec3("setPos", x, y, z);
        pos = newPos.asBlockPos();
        newPos.free();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.is_translucent")
    public boolean isTranslucent() {
        return blockState.propagatesSkylightDown(WorldAPI.getCurrentWorld(), getBlockPos());
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.get_opacity")
    public int getOpacity() {
        return blockState.getLightBlock(WorldAPI.getCurrentWorld(), getBlockPos());
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.get_map_color")
    public FiguraVec3 getMapColor() {
        return ColorUtils.intToRGB(blockState.getMapColor(WorldAPI.getCurrentWorld(), getBlockPos()).col);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.is_solid_block")
    public boolean isSolidBlock() {
        return blockState.isRedstoneConductor(WorldAPI.getCurrentWorld(), getBlockPos());
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.is_full_cube")
    public boolean isFullCube() {
        return blockState.isCollisionShapeFullBlock(WorldAPI.getCurrentWorld(), getBlockPos());
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.has_emissive_lightning")
    public boolean hasEmissiveLighting() {
        return blockState.emissiveRendering(WorldAPI.getCurrentWorld(), getBlockPos());
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.get_hardness")
    public float getHardness() {
        return blockState.getDestroySpeed(WorldAPI.getCurrentWorld(), getBlockPos());
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.get_comparator_output")
    public int getComparatorOutput() {
        return blockState.getAnalogOutputSignal(WorldAPI.getCurrentWorld(), getBlockPos());
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.has_block_entity")
    public boolean hasBlockEntity() {
        return blockState.hasBlockEntity();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.is_opaque")
    public boolean isOpaque() {
        return blockState.canOcclude();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.emits_redstone_power")
    public boolean emitsRedstonePower() {
        return blockState.isSignalSource();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.get_luminance")
    public int getLuminance() {
        return blockState.getLightEmission();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.get_friction")
    public float getFriction() {
        return blockState.getBlock().getFriction();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.get_velocity_multiplier")
    public float getVelocityMultiplier() {
        return blockState.getBlock().getSpeedFactor();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.get_jump_velocity_multiplier")
    public float getJumpVelocityMultiplier() {
        return blockState.getBlock().getJumpFactor();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.get_blast_resistance")
    public float getBlastResistance() {
        return blockState.getBlock().getExplosionResistance();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.as_item")
    public ItemStackAPI asItem() {
        return ItemStackAPI.verify(blockState.getBlock().asItem().getDefaultInstance());
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.get_tags")
    public LuaTable getTags() {
        LuaTable table = new LuaTable();

        Registry<Block> registry = WorldAPI.getCurrentWorld().registryAccess().registryOrThrow(Registry.BLOCK_REGISTRY);
        Optional<ResourceKey<Block>> key = registry.getResourceKey(blockState.getBlock());

        if (key.isEmpty())
            return table;

        int i = 1;
        for (TagKey<Block> blockTagKey : registry.getHolderOrThrow(key.get()).tags().toList()) {
            table.set(i, blockTagKey.location().toString());
            i++;
        }

        return table;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.get_material")
    public String getMaterial() {
        for (Field field : Material.class.getFields()) {
            try {
                if (field.get(null) == blockState.getMaterial())
                    return field.getName();
            } catch (Exception ignored) {}
        }

        return null;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.get_has_collision")
    public boolean hasCollision() {
        return ((BlockBehaviourAccessor) blockState.getBlock()).hasCollision();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.get_collision_shape")
    public LuaTable getCollisionShape() {
        return voxelShapeToTable(blockState.getCollisionShape(WorldAPI.getCurrentWorld(), getBlockPos()));
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.get_outline_shape")
    public LuaTable getOutlineShape() {
        return voxelShapeToTable(blockState.getShape(WorldAPI.getCurrentWorld(), getBlockPos()));
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.get_sounds")
    public LuaTable getSounds() {
        LuaTable sounds = new LuaTable();
        SoundType snd = blockState.getSoundType();

        sounds.set("pitch", snd.getPitch());
        sounds.set("volume", snd.getVolume());
        sounds.set("break", snd.getBreakSound().getLocation().toString());
        sounds.set("fall", snd.getFallSound().getLocation().toString());
        sounds.set("hit", snd.getHitSound().getLocation().toString());
        sounds.set("plate", snd.getPlaceSound().getLocation().toString());
        sounds.set("step", snd.getStepSound().getLocation().toString());

        return sounds;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.get_fluid_tags")
    public LuaTable getFluidTags() {
        LuaTable table = new LuaTable();

        int i = 1;
        for (TagKey<Fluid> fluidTagKey : blockState.getFluidState().getTags().toList()) {
            table.set(i, fluidTagKey.location().toString());
            i++;
        }

        return table;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.get_entity_data")
    public LuaTable getEntityData() {
        BlockEntity entity = WorldAPI.getCurrentWorld().getBlockEntity(getBlockPos());
        return (LuaTable) NbtToLua.convert(entity != null ? entity.saveWithoutMetadata() : null);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "blockstate.to_state_string")
    public String toStateString() {
        BlockEntity entity = WorldAPI.getCurrentWorld().getBlockEntity(getBlockPos());
        CompoundTag tag = entity != null ? entity.saveWithoutMetadata() : new CompoundTag();

        return BlockStateParser.serialize(blockState) + tag;
    }
}
