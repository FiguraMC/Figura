package org.figuramc.figura.lua.api;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaTypeManager;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.model.FiguraModelPart;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;

import java.util.Set;
import java.util.function.Supplier;

@LuaWhitelist
@LuaTypeDoc(name = "PartCollection", value = "models.collection")
public class PartCollection implements MutablePart<PartCollection> {
    private Supplier<LuaTypeManager> manager;
    public final Set<FiguraModelPart> parts;

    public PartCollection(Supplier<LuaTypeManager> manager, Set<FiguraModelPart> parts) {
        this.manager = manager;
        this.parts = parts;
    }

    @LuaWhitelist
    @LuaMethodDoc("models.collection.get_parts")
    public Set<FiguraModelPart> getParts() {
        return parts;
    }

    @LuaWhitelist
    @LuaMethodDoc(
        overloads = {
            @LuaMethodOverload(
                argumentTypes = FiguraModelPart.class,
                argumentNames = "part"
            ),
            @LuaMethodOverload(
                argumentTypes = PartCollection.class,
                argumentNames = "parts"
            ),
        },
        value = "models.collection.union",
        aliases = "+"
    )
    public PartCollection union(Object parts) {
        if (parts instanceof PartCollection c) {
            return new PartCollection(manager, Sets.union(this.parts, c.parts));
        } else if (parts instanceof FiguraModelPart p) {
            return new PartCollection(manager, Sets.union(this.parts, Set.of(p)));
        } else {
            throw new LuaError("Expected argument of either modelpart or collection for 'PartCollection:union'");
        }
    }
    
    @LuaWhitelist
    @LuaMethodDoc(
        overloads = {
            @LuaMethodOverload(
                argumentTypes = FiguraModelPart.class,
                argumentNames = "part"
            ),
            @LuaMethodOverload(
                argumentTypes = PartCollection.class,
                argumentNames = "parts"
            ),
        },
        value = "models.collection.subtract",
        aliases = "-"
    )
    public PartCollection subtract(Object parts) {
        if (parts instanceof PartCollection c) {
            return new PartCollection(manager, Sets.difference(this.parts, c.parts));
        } else if (parts instanceof FiguraModelPart p) {
            return new PartCollection(manager, Sets.difference(this.parts, Set.of(p)));
        } else {
            throw new LuaError("Expected argument of either modelpart or collection for 'PartCollection:subtract'");
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
        overloads = {
            @LuaMethodOverload(
                argumentTypes = FiguraModelPart.class,
                argumentNames = "part"
            ),
            @LuaMethodOverload(
                argumentTypes = PartCollection.class,
                argumentNames = "parts"
            ),
        },
        value = "models.collection.intersect",
        aliases = "*"
    )
    public PartCollection intersect(Object parts) {
        if (parts instanceof PartCollection c) {
            return new PartCollection(manager, Sets.intersection(this.parts, c.parts));
        } else if (parts instanceof FiguraModelPart p) {
            return new PartCollection(manager, Sets.intersection(this.parts, Set.of(p)));
        } else {
            throw new LuaError("Expected argument of either modelpart or collection for 'PartCollection:intersect'");
        }
    }


    @LuaWhitelist public int __len() { return parts.size(); }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof PartCollection other && parts.equals(other.parts);
    }

    @LuaWhitelist
    public boolean __eq(PartCollection other) {
        return this.equals(other);
    }
    
    @Override
    public int hashCode() {
        return parts.hashCode();
    }

    @LuaWhitelist
    @LuaMethodDoc("models.collection.has")
    public boolean has(Object parts) {
        if (parts instanceof PartCollection c) {
            return this.parts.containsAll(c.parts);
        } else if (parts instanceof FiguraModelPart p) {
            return this.parts.contains(p);
        } else {
            throw new LuaError("Expected argument of either modelpart or collection for 'PartCollection:has'");
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("models.collection.filter")
    public PartCollection filter(@LuaNotNil LuaFunction arg) {
        ImmutableSet.Builder<FiguraModelPart> builder = ImmutableSet.builder();
        Iterable<FiguraModelPart> iter = parts;
        for (var part: iter)
			if (arg.invoke(manager.get().javaToLua(part)).toboolean(1))
				builder.add(part);
        return new PartCollection(manager, builder.build());
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("model_part.set_visible")
    public PartCollection setVisible(boolean bool) {
        getParts().forEach(p -> p.setVisible(bool));
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("model_part.set_primary_render_type")
    public PartCollection setPrimaryRenderType(String type) {
        getParts().forEach(p -> p.setPrimaryRenderType(type));
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("model_part.set_secondary_render_type")
    public PartCollection setSecondaryRenderType(String type) {
        getParts().forEach(p -> p.setSecondaryRenderType(type));
        return this;
    }

    @LuaMethodDoc("model_part.set_primary_texture")
    @Override
    public PartCollection setPrimaryTexture(String type, Object x) {
        getParts().forEach(p -> p.setPrimaryTexture(type, x));
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("model_part.set_secondary_texture")
    public PartCollection setSecondaryTexture(String type, Object x) {
        getParts().forEach(p -> p.setSecondaryTexture(type, x));
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("model_part.set_color")
    public PartCollection setColor(Object r, Double g, Double b) {
        getParts().forEach(p -> p.setColor(r, g, b));
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("model_part.set_primary_color")
    public PartCollection setPrimaryColor(Object r, Double g, Double b) {
        getParts().forEach(p -> p.setPrimaryColor(r, g, b));
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("model_part.set_secondary_color")
    public PartCollection setSecondaryColor(Object r, Double g, Double b) {
        getParts().forEach(p -> p.setSecondaryColor(r, g, b));
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("model_part.set_opacity")
    public PartCollection setOpacity(Float opacity) {
        getParts().forEach(p -> p.setOpacity(opacity));
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("model_part.set_light")
    public PartCollection setLight(Object light, Double skyLight) {
        getParts().forEach(p -> p.setLight(light, skyLight));
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("model_part.set_overlay")
    public PartCollection setOverlay(Object whiteOverlay, Double hurtOverlay) {
        getParts().forEach(p -> p.setOverlay(whiteOverlay, hurtOverlay));
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("model_part.set_parent_type")
    public PartCollection setParentType(String parent) {
        getParts().forEach(p -> p.setParentType(parent));
        return this;
    }

    // reasonable tostring
    @Override
    public String toString() {
        return "PartCollection" + parts.toString();
    }
}
