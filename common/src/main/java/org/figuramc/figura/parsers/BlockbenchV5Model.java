package org.figuramc.figura.parsers;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.parsers.BlockbenchCommonTypes.*;
import org.figuramc.figura.parsers.BlockbenchCommonTypes.Collection;
import org.figuramc.figura.parsers.BlockbenchParser2.Intermediary.AnimationRepresentation;
import org.figuramc.figura.parsers.BlockbenchParser2.Intermediary.CollectionRepresentation;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

import static org.figuramc.figura.parsers.BlockbenchCommonTypes.FORMAT_V5;

public class BlockbenchV5Model extends ModelFormat {
    private static final FiguraVec3 ZERO = FiguraVec3.of(0, 0, 0);

    IntPair resolution;

    List<Element> elements;

    List<Group> groups;

    List<OutlinerItem> outliner;

    List<Texture> textures;

    List<Animation> animations;

    List<Collection> collections;

    public static final IllegalStateException WRONG_FORMAT =
            new IllegalStateException("Tried to execute the v5 parser on a model file of a different version");
    public static final JsonDeserializer<BlockbenchV5Model> MODEL_DESERIALIZER = (json, typeOfT, context) -> {
        JsonObject obj = json.getAsJsonObject();
        BlockbenchV5Model instance = new BlockbenchV5Model();

        JsonObject meta = obj.getAsJsonObject("meta");
        instance.formatVersion = meta.get("format_version").getAsString();
        instance.modelFormat = meta.get("model_format").getAsString();

        if (!instance.formatVersion.startsWith("5.")) throw WRONG_FORMAT;

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

        instance.groups = new ArrayList<>();
        if (obj.has("groups"))
            for (JsonElement item : obj.getAsJsonArray("groups")) {
                instance.groups.add(context.deserialize(item, Group.class));
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
        for (Group group : groups)
            refs.put(group.uuid, group);
        return refs;
    }

    /**
     * Pattern for Lua code that is definitely a statement and not an expression.
     * <ul>
     *     <li>{@code [:;]} - labels and semicolons</li>
     *     <li>{@code function\\s*[^\\s(]} - non-anonymous functions</li>
     * </ul>
     */
    public static final Pattern DEFINITELY_STMT = Pattern.compile(
            "^\\s*([:;]|break|goto|do|while|repeat|if|for|function\\s*[^\\s(]|local)");

    public static final Pattern RETURN_BOUNDARY = Pattern.compile("\\breturn\\b");

    /**
     * @return source code that returns the opposite, hopefully?
     */
    public static String negateLua(String source) {
        if (RETURN_BOUNDARY.matcher(source).find() || DEFINITELY_STMT.matcher(source).find()) {
            // don't bother
            return source;
        }
        // if it is truly an expression, then this will work
        // otherwise... oops?
        return String.format("-(%s)", source);
    }

    @Override
    public CompoundTag convert(BlockbenchParser2.Intermediary target) {
        target.defaultRes = resolution;
        target.loadTextures(textures);
        target.loadAnimations(animations);
        for (Element element : elements) {
            target.elements.put(element.uuid, element);
        }
        target.referents.putAll(getAllReferences());
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

        // FORMAT major << 4 + minor (i.e. 0xXY)
        // 5.0 -> 0x50
        tag.put("_v", ByteTag.valueOf(FORMAT_V5));

        return tag;
    }

    public static class Group implements UUIDReferable {
        String name;
        String uuid;

        @Nullable Boolean visibility;
        @Nullable Boolean export;

        FiguraVec3 origin;
        FiguraVec3 rotation;

        @Override
        public String getUUID() {
            return uuid;
        }
    }

    public static abstract class OutlinerItem implements NBTRepresentation<CompoundTag> {
        @Override
        public abstract @Nullable CompoundTag toNBT(BlockbenchParser2.Intermediary context);

        public static final JsonDeserializer<OutlinerItem> DESERIALIZER = (json, typeOfT, context) -> {
            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                return new Element(json.getAsString());
            }
            if (json.isJsonObject()) {
                JsonObject theObject = json.getAsJsonObject();
                Container instance = new Container(theObject.get("uuid").getAsString());
                for (JsonElement child : theObject.getAsJsonArray("children")) {
                    instance.children.add(context.deserialize(child, OutlinerItem.class));
                }
                return instance;
            }
            throw new JsonParseException("Expecting an outliner item (string or object), but found " + json.getClass()
                    .getSimpleName() + " instead");
        };

        final String uuid;

        public OutlinerItem(String uuid) {
            this.uuid = uuid;
        }

        public static final class Element extends OutlinerItem {
            public Element(String uuid) {
                super(uuid);
            }

            @Override
            public CompoundTag toNBT(BlockbenchParser2.Intermediary context) {
                return context.elements.get(uuid).toNBT(context);
            }
        }

        public static final class Container extends OutlinerItem {
            final List<OutlinerItem> children = new ArrayList<>();

            public Container(String uuid) {
                super(uuid);
            }

            @Override
            public @Nullable CompoundTag toNBT(BlockbenchParser2.Intermediary context) {
                UUIDReferable groupProbably = context.referents.get(uuid);
                if (!(groupProbably instanceof Group group)) {
                    FiguraMod.LOGGER.warn(
                            "Broken reference (in model '{}'): expected a group at UUID {} but found {} instead",
                            context.name,
                            uuid,
                            groupProbably == null ? "(nothing with that UUID!)" : groupProbably.getClass()
                                    .getSimpleName()
                    );
                    return null;
                }
                if (Boolean.FALSE.equals(group.export)) return null;

                CompoundTag tag = new CompoundTag();
                tag.putString("name", group.name);

                if (Boolean.FALSE.equals(group.visibility))
                    tag.putBoolean("vsb", false);

                if (group.origin != null && !group.origin.equals(ZERO))
                    tag.put("piv", BlockbenchCommonTypes.vecToList(group.origin));
                if (group.rotation != null && !group.rotation.equals(ZERO))
                    tag.put("rot", BlockbenchCommonTypes.vecToList(group.rotation));

                BlockbenchCommonTypes.parseParent(group.name, tag);

                ListTag chld = new ListTag();
                for (OutlinerItem child : children) {
                    CompoundTag childTag = child.toNBT(context);
                    if (childTag != null) {
                        if (childTag.contains("vsb") && Objects.equals(group.visibility, childTag.getBoolean("vsb")))
                            childTag.remove("vsb");
                        chld.add(childTag);
                    }
                }
                if (!chld.isEmpty())
                    tag.put("chld", chld);

                Set<AnimationRepresentation> animations = context.animationsByElement.get(uuid);
                if (animations != null) {
                    ListTag anim = new ListTag();
                    for (AnimationRepresentation animation : animations) {
                        Animator animator = animation.partAnimators.get(uuid);
                        if (animator == null) throw new RuntimeException(
                                "inconsistent state!! animationsByElement indicated an animator, but none actually present");
                        CompoundTag attachment = animator.getNBT(animation, true);
                        if (attachment != null) anim.add(attachment);
                    }
                    tag.put("anim", anim);
                }

                BlockbenchCommonTypes.attachCollections(context, group.uuid, tag);

                return tag;
            }
        }
    }
}
