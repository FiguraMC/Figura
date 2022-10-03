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
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.NbtToLua;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec6;
import org.moon.figura.mixin.BlockBehaviourAccessor;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.LuaUtils;

import java.lang.reflect.Field;
import java.util.*;

@LuaWhitelist
@LuaTypeDoc(
        name = "BlockState",
        value = "blockstate"
)
public class BlockStateAPI {

    public final BlockState blockState;
    private BlockPos pos;

    @LuaWhitelist
    @LuaFieldDoc("blockstate.id")
    public final String id;
    @LuaWhitelist
    @LuaFieldDoc("blockstate.properties")
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

    protected static List<FiguraVec6> voxelShapeToTable(VoxelShape shape) {
        List<FiguraVec6> shapes = new ArrayList<>();
        for (AABB aabb : shape.toAabbs())
            shapes.add(FiguraVec6.of(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ));
        return shapes;
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.get_pos")
    public FiguraVec3 getPos() {
        return FiguraVec3.fromBlockPos(getBlockPos());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "blockstate.set_pos"
    )
    public void setPos(Object x, Double y, Double z) {
        FiguraVec3 newPos = LuaUtils.parseVec3("setPos", x, y, z);
        pos = newPos.asBlockPos();
        newPos.free();
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.is_translucent")
    public boolean isTranslucent() {
        return blockState.propagatesSkylightDown(WorldAPI.getCurrentWorld(), getBlockPos());
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.get_opacity")
    public int getOpacity() {
        return blockState.getLightBlock(WorldAPI.getCurrentWorld(), getBlockPos());
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.get_map_color")
    public FiguraVec3 getMapColor() {
        return ColorUtils.intToRGB(blockState.getMapColor(WorldAPI.getCurrentWorld(), getBlockPos()).col);
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.is_solid_block")
    public boolean isSolidBlock() {
        return blockState.isRedstoneConductor(WorldAPI.getCurrentWorld(), getBlockPos());
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.is_full_cube")
    public boolean isFullCube() {
        return blockState.isCollisionShapeFullBlock(WorldAPI.getCurrentWorld(), getBlockPos());
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.has_emissive_lightning")
    public boolean hasEmissiveLighting() {
        return blockState.emissiveRendering(WorldAPI.getCurrentWorld(), getBlockPos());
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.get_hardness")
    public float getHardness() {
        return blockState.getDestroySpeed(WorldAPI.getCurrentWorld(), getBlockPos());
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.get_comparator_output")
    public int getComparatorOutput() {
        return blockState.getAnalogOutputSignal(WorldAPI.getCurrentWorld(), getBlockPos());
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.has_block_entity")
    public boolean hasBlockEntity() {
        return blockState.hasBlockEntity();
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.is_opaque")
    public boolean isOpaque() {
        return blockState.canOcclude();
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.emits_redstone_power")
    public boolean emitsRedstonePower() {
        return blockState.isSignalSource();
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.get_luminance")
    public int getLuminance() {
        return blockState.getLightEmission();
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.get_friction")
    public float getFriction() {
        return blockState.getBlock().getFriction();
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.get_velocity_multiplier")
    public float getVelocityMultiplier() {
        return blockState.getBlock().getSpeedFactor();
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.get_jump_velocity_multiplier")
    public float getJumpVelocityMultiplier() {
        return blockState.getBlock().getJumpFactor();
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.get_blast_resistance")
    public float getBlastResistance() {
        return blockState.getBlock().getExplosionResistance();
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.as_item")
    public ItemStackAPI asItem() {
        return ItemStackAPI.verify(blockState.getBlock().asItem().getDefaultInstance());
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.get_tags")
    public List<String> getTags() {
        List<String> list = new ArrayList<>();

        Registry<Block> registry = WorldAPI.getCurrentWorld().registryAccess().registryOrThrow(Registry.BLOCK_REGISTRY);
        Optional<ResourceKey<Block>> key = registry.getResourceKey(blockState.getBlock());

        if (key.isEmpty())
            return list;

        for (TagKey<Block> blockTagKey : registry.getHolderOrThrow(key.get()).tags().toList())
            list.add(blockTagKey.location().toString());

        return list;
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.get_material")
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
    @LuaMethodDoc("blockstate.get_has_collision")
    public boolean hasCollision() {
        return ((BlockBehaviourAccessor) blockState.getBlock()).hasCollision();
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.get_collision_shape")
    public List<FiguraVec6> getCollisionShape() {
        return voxelShapeToTable(blockState.getCollisionShape(WorldAPI.getCurrentWorld(), getBlockPos()));
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.get_outline_shape")
    public List<FiguraVec6> getOutlineShape() {
        return voxelShapeToTable(blockState.getShape(WorldAPI.getCurrentWorld(), getBlockPos()));
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.get_sounds")
    public Map<String, Object> getSounds() {
        Map<String, Object> sounds = new HashMap<>();
        SoundType snd = blockState.getSoundType();

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
    @LuaMethodDoc("blockstate.get_fluid_tags")
    public List<String> getFluidTags() {
        List<String> list = new ArrayList<>();
        for (TagKey<Fluid> fluidTagKey : blockState.getFluidState().getTags().toList())
            list.add(fluidTagKey.location().toString());
        return list;
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.get_entity_data")
    public LuaTable getEntityData() {
        BlockEntity entity = WorldAPI.getCurrentWorld().getBlockEntity(getBlockPos());
        return (LuaTable) NbtToLua.convert(entity != null ? entity.saveWithoutMetadata() : null);
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.to_state_string")
    public String toStateString() {
        BlockEntity entity = WorldAPI.getCurrentWorld().getBlockEntity(getBlockPos());
        CompoundTag tag = entity != null ? entity.saveWithoutMetadata() : new CompoundTag();

        return BlockStateParser.serialize(blockState) + tag;
    }

    @LuaWhitelist
    public Object __index(String arg) {
        if (arg == null) return null;
        return switch (arg) {
            case "id" -> id;
            case "properties" -> properties;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return id + " (BlockState)";
    }
}
