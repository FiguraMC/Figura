package org.figuramc.figura.parsers;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.Gson;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.utils.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.figuramc.figura.parsers.BlockbenchCommonTypes.parseFloatOr;

// holdout until the Codec PR comes through
// or the permanent solution, as fate tends to have it
public class BlockbenchParser2 {
    private static final Gson GSON = BlockbenchCommonTypes.getGson();

    // Multiple models can be loaded.
    private int nextTexture = 0;
    private int nextAnimation = 0;

    public ModelParseResult parseModel(Path avatarRoot,
                                       Path modelPath,
                                       String json,
                                       String modelName,
                                       String locatedWithin,
                                       @Nullable LoadOptions options) throws Exception {
        try {
            BlockbenchCommonTypes.ModelFormat model = GSON.fromJson(json, BlockbenchCommonTypes.ModelFormat.class);

            boolean validFormat = model.modelFormat.equals("free") || model.modelFormat.contains(FiguraMod.MOD_ID);
            if (!validFormat) throw new Exception(String.format(
                    "Model \"%s\" has an incompatible model format: \"%s\".\n" +
                            "Compatibility is limited to the \"Generic Model\" format and third-party %s-specific formats.",
                    modelName,
                    model.modelFormat,
                    FiguraMod.MOD_NAME
            ));

            Intermediary container = new Intermediary(this, options == null ? LoadOptions.DEFAULT : options);
            container.name = modelName;
            container.avatarRoot = avatarRoot;
            container.modelPath = modelPath;
            container.locatedWithin = locatedWithin;

            CompoundTag nbt = model.convert(container);

            return new ModelParseResult(
                    container.getTexturesNBT(),
                    container.getAnimationsNBT(),
                    nbt
            );
        } catch (Exception e) {
            throw new Exception(
                    String.format(
                            "model \"%s\": %s",
                            modelName,
                            e.getMessage()
                    ), e
            );
        }
    }

    /**
     * Data container for all model versions.
     */
    public static class Intermediary {
        private final BlockbenchParser2 parser;
        public final LoadOptions options;

        public Intermediary(BlockbenchParser2 parser, LoadOptions options) {
            this.parser = parser;
            this.options = options;
        }

        // supplied externally
        public String name;
        public Path modelPath;
        public Path avatarRoot;
        public String locatedWithin;
        // model file's 'resolution' property
        public BlockbenchCommonTypes.IntPair defaultRes;

        private final List<TextureRepresentation> textures = new ArrayList<>();
        private final HashMap<String, Map<String, TextureRepresentation>> textureNames = new HashMap<>();
        public final Map<String, BlockbenchCommonTypes.Element> elements = new HashMap<>();
        public final Map<String, BlockbenchCommonTypes.UUIDReferable> referents = new HashMap<>();
        public final List<AnimationRepresentation> animations = new ArrayList<>();
        public final Map<String, Set<AnimationRepresentation>> animationsByElement = new HashMap<>();
        public final List<CollectionRepresentation> collections = new ArrayList<>();
        public final Multimap<String, Integer> collectionsByElement =
                MultimapBuilder.hashKeys().hashSetValues().build();

        public void loadTextures(List<BlockbenchCommonTypes.Texture> textures) {
            int i = 0;
            for (BlockbenchCommonTypes.Texture texture : textures) {
                TextureRepresentation texRep = new TextureRepresentation(texture);
                texRep.localID = i++;
                // assign the same global index as others with the same name, or
                // add a new index
                textureNames.compute(
                        texRep.name, (k, v) -> {
                            if (v == null) {
                                texRep.globalID = parser.nextTexture++;
                                v = new HashMap<>();
                            } else {
                                texRep.globalID = v.values().iterator().next().globalID;
                            }
                            v.put(texRep.textureType, texRep);
                            return v;
                        }
                );
                this.textures.add(texRep);
            }
        }

        public void loadAnimations(List<BlockbenchCommonTypes.Animation> animations) {
            for (BlockbenchCommonTypes.Animation animation : animations) {
                AnimationRepresentation animRep = new AnimationRepresentation(parser.nextAnimation++, animation);
                this.animations.add(animRep);
            }
        }

        public void loadCollections(List<BlockbenchCommonTypes.Collection> collections) {
            int i = 0;
            for (BlockbenchCommonTypes.Collection collection : collections) {
                CollectionRepresentation coll = new CollectionRepresentation(i++, collection);
                this.collections.add(coll);
            }
        }

        public @Nullable Integer getTextureGlobalID(int localID) {
            if (localID < 0 || textures.size() <= localID) return null;
            return textures.get(localID).globalID;
        }

        public FiguraVec2 getTextureFixedSize(int localID) {
            return textures.get(localID).fixedSize;
        }

        public CompoundTag getTexturesNBT() {
            CompoundTag tag = new CompoundTag();
            CompoundTag sources = new CompoundTag();
            // ordering is important!
            LinkedHashMap<String, CompoundTag> buildData = new LinkedHashMap<>();

            for (TextureRepresentation texture : textures) {
                sources.put(texture.path, texture.getSourceNBT());
                buildData.compute(
                        texture.name, (k, it) -> {
                            if (it == null) it = new CompoundTag();
                            if (it.contains(texture.textureType))
                                throw new RuntimeException("Model \"" + name + "\" contains texture with duplicate name \"" + texture.name + "\"");
                            it.putString(texture.textureType, texture.path);
                            return it;
                        }
                );
            }

            ListTag data = new ListTag();
            data.addAll(buildData.values());
            tag.put("src", sources);
            tag.put("data", data);
            return tag;
        }

        public List<CompoundTag> getAnimationsNBT() {
            List<CompoundTag> list = new ArrayList<>();
            for (AnimationRepresentation animation : animations) {
                list.add(animation.getAnimNBT());
            }
            return list;
        }

        /* not static */
        public class TextureRepresentation {
            public String name;
            public int globalID;
            public int localID;

            /// 'd', 'e', 'n', 's'
            public final String textureType;

            public String path;
            public byte[] source;

            public final FiguraVec2 fixedSize;

            public TextureRepresentation(BlockbenchCommonTypes.Texture texture) {
                name = texture.name;
                if (name.endsWith(".png")) name = name.substring(0, name.length() - 4);

                // ugh why do we have to perpetuate this garbage
                if (name.endsWith("_e")) textureType = "e";
                else if (name.endsWith("_n")) textureType = "n";
                else if (name.endsWith("_s")) textureType = "s";
                else textureType = "d";

                try {
                    // exceptions as control flow ._.
                    if (texture.relative_path == null) throw new RuntimeException("load from source");
                    Path p = modelPath.getParent().resolve(texture.relative_path);
                    if (p.getFileSystem() == FileSystems.getDefault()) {
                        p = p.toFile().getCanonicalFile().toPath();
                    }
                    p = p.normalize();

                    if (!Files.exists(p) || (avatarRoot.getNameCount() > 1 && !p.startsWith(avatarRoot))) {
                        // <=4.9
                        if (texture.relative_path.startsWith("../")) {
                            p = modelPath.resolve(texture.relative_path);
                            if (p.getFileSystem() == FileSystems.getDefault()) {
                                p = p.toFile().getCanonicalFile().toPath();
                            }
                            p = p.normalize();
                        }
                    }

                    if (!Files.exists(p))
                        throw new FileNotFoundException("Could not locate texture '" + texture.name + "'");

                    if ((avatarRoot.getNameCount() > 1 && !p.startsWith(avatarRoot)) || p.getFileSystem() != avatarRoot.getFileSystem()) {
                        throw new IllegalStateException("Texture '" + texture.name + "' is a reference outside the avatar folder");
                    }

                    FiguraMod.debug("Loading texture {}: path is {}", texture.name, p.toString());
                    source = IOUtils.readFileBytes(p);
                    path = avatarRoot.relativize(p).toString().replace(p.getFileSystem().getSeparator(), ".");
                    path = path.substring(0, path.length() - 4); // (file extension)
                    name = locatedWithin + name;
                    FiguraMod.debug("Loaded {} texture \"{}\" as path {} (from {})", textureType, name, path, p);
                } catch (Exception e) {
                    if (e instanceof IOException || e instanceof NullPointerException)
                        FiguraMod.LOGGER.error("", e);

                    // Try to load from base64
                    if (texture.source == null || !texture.source.startsWith("data:image/png;base64,"))
                        throw new IllegalStateException(String.format(
                                "Failed to find the %s texture: %s\n(and the bundled texture data was missing or bad)",
                                texture.name,
                                e
                        ));
                    source = Base64.getDecoder().decode(texture.source.substring("data:image/png;base64,".length()));
                    path = locatedWithin + Intermediary.this.name + "." + name;
                    FiguraMod.debug("Loaded {} texture \"{}\" as path {} (bundled)", textureType, name, path);
                }

                if (!textureType.equals("d"))
                    name = name.substring(0, name.length() - 2);

                if (texture.width != null) {
                    fixedSize = FiguraVec2.of(
                            (float) texture.width / texture.uv_width,
                            (float) texture.height / texture.uv_height
                    );
                } else {
                    BlockbenchCommonTypes.IntPair imageSize = BlockbenchCommonTypes.getPNGDimensions(source);
                    fixedSize = FiguraVec2.of(
                            (float) imageSize.x / defaultRes.x,
                            (float) imageSize.y / defaultRes.y
                    );
                }
            }

            public ByteArrayTag getSourceNBT() {
                return new ByteArrayTag(source);
            }
        }

        public class AnimationRepresentation {
            // assign externally
            public final int globalID;

            final String mdl = locatedWithin.isBlank() ? Intermediary.this.name : locatedWithin + Intermediary.this.name;
            final String name;
            // skip if 'once'
            final String loop;
            // skip if false
            final boolean override;
            final float length;
            final float offset;
            final float blend;
            final float startDelay;
            final float loopDelay;

            public final Map<String, BlockbenchCommonTypes.Animator> partAnimators;
            public final @Nullable BlockbenchCommonTypes.Animator fxAnimator;

            public AnimationRepresentation(int globalID, BlockbenchCommonTypes.Animation animation) {
                this.globalID = globalID;
                name = animation.name;
                loop = animation.loop == null ? "once" : animation.loop;
                override = Boolean.TRUE.equals(animation.override);
                length = animation.length;
                offset = parseFloatOr(animation.anim_time_update, 0f);
                blend = parseFloatOr(animation.blend_weight, 1f);
                startDelay = parseFloatOr(animation.start_delay, 0f);
                loopDelay = parseFloatOr(animation.loop_delay, 0f);

                if (animation.animators != null) {
                    Map<String, BlockbenchCommonTypes.Animator> filtered = new HashMap<>(animation.animators);
                    fxAnimator = filtered.remove("effects");
                    partAnimators = filtered;
                } else {
                    fxAnimator = null;
                    partAnimators = new HashMap<>();
                }

                for (Map.Entry<String, BlockbenchCommonTypes.Animator> entry : partAnimators.entrySet()) {
                    animationsByElement.compute(
                            entry.getKey(),
                            (k, v) -> {
                                if (v == null) v = new LinkedHashSet<>();
                                v.add(this);
                                return v;
                            }
                    );
                }
            }

            public CompoundTag getAnimNBT() {
                CompoundTag tag = new CompoundTag();

                tag.putString("mdl", mdl);
                tag.putString("name", name);
                if (!loop.equals("once")) tag.putString("loop", loop);
                if (override) tag.putBoolean("ovr", true);
                if (length != 0f) tag.putFloat("len", length);
                if (offset != 0f) tag.putFloat("off", offset);
                if (blend != 1f) tag.putFloat("bld", blend);
                if (startDelay != 0f) tag.putFloat("sdel", startDelay);
                if (loopDelay != 0f) tag.putFloat("ldel", loopDelay);

                // We also need to attach the instruction keyframe animator here
                // (it's not bound to any parts.)
                ListTag code = getEffectsNBT();
                if (code != null) tag.put("code", code);

                return tag;
            }

            private @Nullable ListTag getEffectsNBT() {
                if (fxAnimator == null) return null;

                // this code _only_ handles Instruction Keyframes.

                ListTag fxData = new ListTag();

                if (fxAnimator.keyframes != null)
                    for (BlockbenchCommonTypes.Keyframe keyframe : fxAnimator.keyframes) {
                        if (!keyframe.channel.equalsIgnoreCase("timeline"))
                            continue;
                        if (!(keyframe instanceof BlockbenchCommonTypes.Keyframe.InstructionKeyframe ik))
                            continue;
                        CompoundTag kfTag = new CompoundTag();
                        kfTag.putFloat("time", ik.time);
                        kfTag.putString("src", ik.data_points[0].script);
                        fxData.add(kfTag);
                    }


                return fxData;
            }
        }

        public class CollectionRepresentation {
            public final String name;
            public final int localID;

            public CollectionRepresentation(int localID, BlockbenchCommonTypes.Collection source) {
                name = source.name;
                this.localID = localID;

                source.children.forEach(it -> collectionsByElement.put(it, localID));
            }
        }
    }

    /**
     * part of avatar metadata
     */
    public static class LoadOptions {
        public static final LoadOptions DEFAULT = new LoadOptions();

        /**
         * Whether to include part UUIDs when compiling models.
         * Most people don't need these, so disabling them by default will save ~16B per part
         * (even through compression)
         */
        public boolean partsWithUUIDs;
    }
}
