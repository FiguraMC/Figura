package org.figuramc.figura.lua.api.world;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.NbtToLua;
import org.figuramc.figura.lua.ReadOnlyLuaTable;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.mixin.BlockBehaviourAccessor;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.LuaUtils;
import org.luaj.vm2.LuaTable;

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
        this.id = BuiltInRegistries.BLOCK.getKey(blockstate.getBlock()).toString();

        CompoundTag tag = NbtUtils.writeBlockState(blockstate);
        this.properties = new ReadOnlyLuaTable(tag.contains("Properties") ? NbtToLua.convert(tag.get("Properties")) : new LuaTable());
    }

    protected BlockPos getBlockPos() {
        return pos == null ? BlockPos.ZERO : pos;
    }

    protected static List<List<FiguraVec3>> voxelShapeToTable(VoxelShape shape) {
        List<List<FiguraVec3>> shapes = new ArrayList<>();
        for (AABB aabb : shape.toAabbs())
            shapes.add(List.of(FiguraVec3.of(aabb.minX, aabb.minY, aabb.minZ), FiguraVec3.of(aabb.maxX, aabb.maxY, aabb.maxZ)));
        return shapes;
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.get_id")
    public String getID() {
        return id;
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.get_properties")
    public LuaTable getProperties() {
        return properties;
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
            aliases = "pos",
            value = "blockstate.set_pos"
    )
    public BlockStateAPI setPos(Object x, Double y, Double z) {
        FiguraVec3 newPos = LuaUtils.parseVec3("setPos", x, y, z);
        pos = newPos.asBlockPos();
        return this;
    }

    @LuaWhitelist
    public BlockStateAPI pos(Object x, Double y, Double z) {
        return setPos(x, y, z);
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
    @LuaMethodDoc("blockstate.has_emissive_lighting")
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

        Registry<Block> registry = WorldAPI.getCurrentWorld().registryAccess().registryOrThrow(Registries.BLOCK);
        Optional<ResourceKey<Block>> key = registry.getResourceKey(blockState.getBlock());

        if (key.isEmpty())
            return list;

        for (TagKey<Block> blockTagKey : registry.getHolderOrThrow(key.get()).tags().toList())
            list.add(blockTagKey.location().toString());

        return list;
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.has_collision")
    public boolean hasCollision() {
        return ((BlockBehaviourAccessor) blockState.getBlock()).hasCollision();
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.get_collision_shape")
    public List<List<FiguraVec3>> getCollisionShape() {
        return voxelShapeToTable(blockState.getCollisionShape(WorldAPI.getCurrentWorld(), getBlockPos()));
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.get_outline_shape")
    public List<List<FiguraVec3>> getOutlineShape() {
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
        sounds.put("place", snd.getPlaceSound().getLocation().toString());
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
    @LuaMethodDoc("blockstate.get_textures")
    public HashMap<String, Set<String>> getTextures() {
        HashMap<String, Set<String>> map = new HashMap<>();

        RenderShape renderShape = blockState.getRenderShape();

        if (renderShape == RenderShape.MODEL) {
            BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

            BakedModel bakedModel = blockRenderer.getBlockModel(blockState);
            RandomSource randomSource = RandomSource.create();
            long seed = 42L;

            for (Direction direction : Direction.values())
                map.put(direction.name(), getTexturesForFace(blockState, direction, randomSource, bakedModel, seed));
            map.put("NONE", getTexturesForFace(blockState, null, randomSource, bakedModel, seed));

            TextureAtlasSprite particle = blockRenderer.getBlockModelShaper().getParticleIcon(blockState);
            map.put("PARTICLE", Set.of(getTextureName(particle)));
        } else if (renderShape == RenderShape.ENTITYBLOCK_ANIMATED) {
            map.put("PARTICLE", Set.of(getTextureName(Minecraft.getInstance().getItemRenderer().getModel(blockState.getBlock().asItem().getDefaultInstance(), WorldAPI.getCurrentWorld(), null, 42).getParticleIcon())));
        }
        return map;
    }

    @LuaWhitelist
    @LuaMethodDoc("blockstate.is_air")
    public boolean isAir() {
        return blockState.isAir();
    }

    private static Set<String> getTexturesForFace(BlockState blockState, Direction direction, RandomSource randomSource, BakedModel bakedModel, long seed) {
        randomSource.setSeed(seed);
        List<BakedQuad> quads = bakedModel.getQuads(blockState, direction, randomSource);
        Set<String> textures = new HashSet<>();

        for (BakedQuad quad : quads)
            textures.add(getTextureName(quad.getSprite()));

        return textures;
    }

    private static String getTextureName(TextureAtlasSprite sprite) {
        ResourceLocation location = sprite.contents().name(); // do not close it
        return location.getNamespace() + ":textures/" + location.getPath();
    }

    @LuaWhitelist
    public boolean __eq(BlockStateAPI other) {
        return this.blockState.equals(other.blockState);
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
