package org.moon.figura.lua.api.vanilla_model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import org.moon.figura.avatars.model.ParentType;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaType;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;

import java.util.function.Function;

@LuaType(typeName = "vanilla_part")
@LuaTypeDoc(
        name = "VanillaModelPart",
        description = "vanilla_part"
)
public class VanillaModelPart extends VanillaPart {

    private final ParentType parentType;
    private final Function<EntityModel<?>, ModelPart> provider;

    private final FiguraVec3 savedOriginRot = FiguraVec3.of();
    private final FiguraVec3 savedOriginPos = FiguraVec3.of();

    private boolean visible = true;
    private boolean storedVisibility;

    public VanillaModelPart(String name, ParentType parentType, Function<EntityModel<?>, ModelPart> provider) {
        super(name);
        this.parentType = parentType;
        this.provider = provider;
    }

    @Override
    public void alter(EntityModel<?> model) {
        if (provider == null)
            return;

        ModelPart part = provider.apply(model);
        storedVisibility = part.visible;
        part.visible = visible;
    }

    @Override
    public void store(EntityModel<?> model) {
        if (provider == null)
            return;

        ModelPart part = provider.apply(model);
        savedOriginRot.set(-part.xRot, -part.yRot, part.zRot);
        savedOriginRot.scale(180/Math.PI);

        FiguraVec3 pivot = parentType.offset.copy();
        pivot.subtract(part.x, part.y, part.z);
        pivot.multiply(1, -1, -1);
        savedOriginPos.set(pivot);
        pivot.free();
    }

    @Override
    public void restore(EntityModel<?> model) {
        if (provider != null)
            provider.apply(model).visible = storedVisibility;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "visible"
            ),
            description = "vanilla_part.set_visible"
    )
    public void setVisible(@LuaNotNil Boolean visible) {
        this.visible = visible;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(description = "vanilla_part.get_visible")
    public Boolean getVisible() {
        return this.visible;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "vanilla_part.get_origin_rot")
    public FiguraVec3 getOriginRot() {
        return this.savedOriginRot.copy();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "vanilla_part.get_origin_pos")
    public FiguraVec3 getOriginPos() {
        return this.savedOriginPos.copy();
    }
}
