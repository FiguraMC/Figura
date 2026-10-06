package org.figuramc.figura.parsers;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapterFactory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.parsers.BlockbenchCommonTypes.*;
import org.figuramc.figura.parsers.BlockbenchCommonTypes.Collection;
import org.figuramc.figura.parsers.BlockbenchParser2.Intermediary.AnimationRepresentation;
import org.figuramc.figura.parsers.BlockbenchParser2.Intermediary.CollectionRepresentation;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BlockbenchV4Model extends ModelFormat {
    private static final FiguraVec3 ZERO = FiguraVec3.of(0, 0, 0);

    IntPair resolution;

    List<Element> elements;

    List<OutlinerItem> outliner;

    List<Texture> textures;

    List<Animation> animations;

    List<Collection> collections;

    public static final IllegalStateException WRONG_FORMAT =
            new IllegalStateException("Tried to execute the v4 parser on a model file of a different version");

    public static final JsonDeserializer<BlockbenchV4Model> MODEL_DESERIALIZER = (json, typeOfT, context) -> {
        JsonObject obj = json.getAsJsonObject();
        BlockbenchV4Model instance = new BlockbenchV4Model();

        JsonObject meta = obj.getAsJsonObject("meta");
        instance.formatVersion = meta.get("format_version").getAsString();
        instance.modelFormat = meta.get("model_format").getAsString();

        if (!instance.formatVersion.startsWith("4.")) throw WRONG_FORMAT;

        JsonObject resolution = obj.getAsJsonObject("resolution");
        instance.resolution = new IntPair(
                resolution.get("width").getAsInt(),
                resolution.get("height").getAsInt()
        );

        instance.elements = new ArrayList<>();
        if (obj.has("elements"))
            for (JsonElement item : obj.getAsJsonArray("elements")) {
                instance.elements.add(context.deserialize(item, Element.class));
            }

        instance.outliner = new ArrayList<>();
        if (obj.has("outliner"))
            for (JsonElement item : obj.getAsJsonArray("outliner")) {
                instance.outliner.add(context.deserialize(item, OutlinerItem.class));
            }

        instance.textures = new ArrayList<>();
        if (obj.has("textures"))
            for (JsonElement item : obj.getAsJsonArray("textures")) {
                instance.textures.add(context.deserialize(item, Texture.class));
            }

        instance.animations = new ArrayList<>();
        if (obj.has("animations"))
            for (JsonElement item : obj.getAsJsonArray("animations")) {
                instance.animations.add(context.deserialize(item, Animation.class));
            }

        instance.collections = new ArrayList<>();
        if (obj.has("collections"))
            for (JsonElement item : obj.getAsJsonArray("collections")) {
                instance.collections.add(context.deserialize(item, Collection.class));
            }

        return instance;
    };

    @Override
    public Map<String, UUIDReferable> getAllReferences() {
        Map<String, UUIDReferable> refs = new HashMap<>();
        for (Element element : elements)
            refs.put(element.uuid, element);
        for (OutlinerItem item : outliner)
            item.fillContainedRefs(refs);

        return refs;
    }

    @Override
    public CompoundTag convert(BlockbenchParser2.Intermediary target) {
        target.defaultRes = resolution;
        target.loadTextures(textures);
        for (Element element : elements) {
            target.elements.put(element.uuid, element);
        }
        target.referents.putAll(getAllReferences());
        target.loadAnimations(animations);
        target.loadCollections(collections);

        CompoundTag tag = new CompoundTag();

        tag.putString("name", target.name);
        BlockbenchCommonTypes.parseParent(target.name, tag);

        ListTag chld = new ListTag();
        for (OutlinerItem item : outliner) {
            CompoundTag itemTag = item.toNBT(target);
            if (itemTag != null)
                chld.add(itemTag);
        }
        tag.put("chld", chld);

        ListTag cn = new ListTag();
        for (CollectionRepresentation collRep : target.collections) {
            cn.add(StringTag.valueOf(collRep.name));
        }
        tag.put("cn", cn);

        return tag;
    }

    public static abstract class OutlinerItem implements NBTRepresentation<CompoundTag> {
        @Override
        public abstract @Nullable CompoundTag toNBT(BlockbenchParser2.Intermediary context);

        public static final TypeAdapterFactory ADAPTER_FACTORY =
                new GsonTypeByField<>(
                        OutlinerItem.class, e -> {
                    if (e.isJsonObject()) return "group";
                    else return "element";
                }
                )
                        .bind("group", Group.class).bind("element", Element.class);

        final String uuid;

        public OutlinerItem(String uuid) {
            this.uuid = uuid;
        }

        public static final class Element extends OutlinerItem {
            public static final JsonDeserializer<Element> DESERIALIZER = (json, typeOfT, context) -> new Element(json.getAsString());

            public Element(String uuid) {
                super(uuid);
            }

            @Override
            public void fillContainedRefs(Map<String, UUIDReferable> to) {
                // This is not a good object to refer to here.
                // Its UUID should be in the elements list.
            }

            @Override
            public @Nullable CompoundTag toNBT(BlockbenchParser2.Intermediary context) {
                BlockbenchCommonTypes.Element ref = context.elements.get(uuid);
                if (ref == null) return null;
                return ref.toNBT(context);
            }
        }

        public static final class Group extends OutlinerItem implements UUIDReferable {

            @Nullable
            final List<OutlinerItem> children = new ArrayList<>();
            String name;
            @Nullable Boolean visibility;
            @Nullable Boolean export;
            FiguraVec3 origin;

            FiguraVec3 rotation;

            public Group(String uuid) {
                super(uuid);
            }

            @Override
            public void fillContainedRefs(Map<String, UUIDReferable> to) {
                // in this model format, all the group information is in fact in the outliner struct
                to.put(uuid, this);
                if (children != null) {
                    children.forEach(it -> it.fillContainedRefs(to));
                }
            }

            @Override
            public String getUUID() {
                return uuid;
            }

            @Override
            public @Nullable CompoundTag toNBT(BlockbenchParser2.Intermediary context) {
                if (Boolean.FALSE.equals(export)) return null;

                CompoundTag tag = new CompoundTag();
                tag.putString("name", name);

                if (Boolean.FALSE.equals(visibility))
                    tag.putBoolean("vsb", false);

                if (origin != null && !origin.equals(ZERO)) tag.put("piv", BlockbenchCommonTypes.vecToList(origin));
                if (rotation != null && !rotation.equals(ZERO))
                    tag.put("rot", BlockbenchCommonTypes.vecToList(rotation));

                BlockbenchCommonTypes.parseParent(name, tag);

                if (children != null) {
                    ListTag chld = new ListTag();
                    for (OutlinerItem child : children) {
                        CompoundTag childTag = child.toNBT(context);
                        if (childTag != null) {
                            // do not propagate 'vsb' tag for children with same property
                            // this causes them to be "overriding" their parents' visibility
                            if (childTag.contains("vsb") && Objects.equals(visibility, childTag.getBoolean("vsb")))
                                childTag.remove("vsb");
                            chld.add(childTag);
                        }
                    }
                    if (!chld.isEmpty())
                        tag.put("chld", chld);
                }

                Set<AnimationRepresentation> animations = context.animationsByElement.get(uuid);
                if (animations != null) {
                    ListTag anim = new ListTag();
                    for (AnimationRepresentation animation : animations) {
                        Animator animator = animation.partAnimators.get(uuid);
                        if (animator == null) throw new RuntimeException(
                                "inconsistent state!! animationsByElement indicated an animator, but none actually present");
                        CompoundTag attachment = animator.getNBT(animation, false);
                        if (attachment != null) anim.add(attachment);
                    }
                    tag.put("anim", anim);
                }

                BlockbenchCommonTypes.attachCollections(context, uuid, tag);

                return tag;
            }
        }

        public abstract void fillContainedRefs(Map<String, UUIDReferable> to);

    }
}
