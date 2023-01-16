package org.moon.figura.lua.api.vanilla_model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.model.ParentType;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.trust.Trust;

import java.util.function.Function;

@LuaWhitelist
@LuaTypeDoc(
        name = "VanillaModelPart",
        value = "vanilla_part"
)
public class VanillaModelPart extends VanillaPart {

    private final ParentType parentType;
    private final Function<EntityModel<?>, ModelPart> provider;

    private final FiguraVec3 originRot = FiguraVec3.of();
    private final FiguraVec3 originPos = FiguraVec3.of();
    private final FiguraVec3 originScale = FiguraVec3.of();
    private boolean originVisible;

    public VanillaModelPart(Avatar owner, String name, ParentType parentType, Function<EntityModel<?>, ModelPart> provider) {
        super(owner, name);
        this.parentType = parentType;
        this.provider = provider;
    }

    @Override
    public void change(EntityModel<?> model) {
        if (visible == null || provider == null)
            return;

        ModelPart part = provider.apply(model);
        if (part == null)
            return;

        part.visible = visible;
    }

    @Override
    public void save(EntityModel<?> model) {
        if (provider == null)
            return;

        ModelPart part = provider.apply(model);
        if (part == null)
            return;

        originRot.set(-part.xRot, -part.yRot, part.zRot);
        originRot.scale(180 / Math.PI);

        FiguraVec3 pivot = parentType.offset.copy();
        pivot.subtract(part.x, part.y, part.z);
        pivot.multiply(1, -1, -1);
        originPos.set(pivot);
        pivot.free();

        originScale.set(part.xScale, part.yScale, part.zScale);

        originVisible = part.visible;
    }

    @Override
    public void restore(EntityModel<?> model) {
        if (provider == null)
            return;

        ModelPart part = provider.apply(model);
        if (part == null)
            return;

        part.visible = originVisible;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "visible"
            ),
            value = "vanilla_part.set_visible"
    )
    public VanillaModelPart setVisible(Boolean visible) {
        this.visible = visible;
        if (visible == null) {
            owner.trustsToTick.remove(Trust.VANILLA_MODEL_EDIT);
        } else {
            owner.trustsToTick.add(Trust.VANILLA_MODEL_EDIT);
        }
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("vanilla_part.get_visible")
    public Boolean getVisible() {
        return this.visible;
    }

    @LuaWhitelist
    @LuaMethodDoc("vanilla_part.get_origin_visible")
    public boolean getOriginVisible() {
        return this.originVisible;
    }

    @LuaWhitelist
    @LuaMethodDoc("vanilla_part.get_origin_rot")
    public FiguraVec3 getOriginRot() {
        return this.originRot.copy();
    }

    @LuaWhitelist
    @LuaMethodDoc("vanilla_part.get_origin_pos")
    public FiguraVec3 getOriginPos() {
        return this.originPos.copy();
    }

    @LuaWhitelist
    @LuaMethodDoc("vanilla_part.get_origin_scale")
    public FiguraVec3 getOriginScale() {
        return this.originScale.copy();
    }

    @Override
    public String toString() {
        return "VanillaModelPart";
    }
}
