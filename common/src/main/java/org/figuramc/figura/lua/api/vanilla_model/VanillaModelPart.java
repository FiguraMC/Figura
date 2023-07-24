package org.figuramc.figura.lua.api.vanilla_model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.model.ParentType;

import java.util.function.Function;

@LuaWhitelist
@LuaTypeDoc(
        name = "VanillaModelPart",
        value = "vanilla_model_part"
)
public class VanillaModelPart extends VanillaPart {

    private final ParentType parentType;
    private final Function<EntityModel<?>, ModelPart> provider;

    // backup
    private float backupPosX, backupPosY, backupPosZ;
    private float backupRotX, backupRotY, backupRotZ;
    private float backupScaleX, backupScaleY, backupScaleZ;
    private boolean originVisible;
    private boolean saved;

    // part getters
    private final FiguraVec3 originRot = FiguraVec3.of();
    private final FiguraVec3 originPos = FiguraVec3.of();
    private final FiguraVec3 originScale = FiguraVec3.of();

    public VanillaModelPart(Avatar owner, String name, ParentType parentType, Function<EntityModel<?>, ModelPart> provider) {
        super(owner, name);
        this.parentType = parentType;
        this.provider = provider;
    }

    private ModelPart getPart(EntityModel<?> model) {
        return provider == null ? null : provider.apply(model);
    }

    @Override
    public void save(EntityModel<?> model) {
        saved = false;
        ModelPart part = getPart(model);
        if (part == null) return;

        // set getters
        originRot.set(-part.xRot, -part.yRot, part.zRot);
        originRot.scale(180 / Math.PI);

        FiguraVec3 pivot = parentType.offset.copy();
        pivot.subtract(part.x, part.y, part.z);
        pivot.multiply(1, -1, -1);
        originPos.set(pivot);

        originScale.set(part.xScale, part.yScale, part.zScale);

        // save visible
        originVisible = part.visible;

        // save pos
        backupPosX = part.x;
        backupPosY = part.y;
        backupPosZ = part.z;

        // save rot
        backupRotX = part.xRot;
        backupRotY = part.yRot;
        backupRotZ = part.zRot;

        // save scale
        backupScaleX = part.xScale;
        backupScaleY = part.yScale;
        backupScaleZ = part.zScale;

        saved = true;
    }

    @Override
    public void preTransform(EntityModel<?> model) {
        if (!saved) return;

        ModelPart part = getPart(model);
        if (part == null) return;

        // pos
        if (pos != null) {
            part.x += (float) -pos.x;
            part.y += (float) -pos.y;
            part.z += (float) pos.z;
        }

        // rot
        if (rot != null) {
            FiguraVec3 rot = this.rot.toRad();
            part.setRotation((float) -rot.x, (float) -rot.y, (float) rot.z);
        }
        if (offsetRot != null)
            part.offsetRotation(offsetRot.toRad().mul(-1, -1, 1).asVec3f());

        // scale
        if (scale != null) {
            part.xScale = (float) scale.x;
            part.yScale = (float) scale.y;
            part.zScale = (float) scale.z;
        }
        if (offsetScale != null)
            part.offsetScale(offsetScale.asVec3f());
    }

    @Override
    public void posTransform(EntityModel<?> model) {
        if (visible == null)
            return;

        ModelPart part = getPart(model);
        if (part != null)
            part.visible = visible;
    }

    @Override
    public void restore(EntityModel<?> model) {
        ModelPart part = getPart(model);
        if (part == null) return;

        // restore visible
        part.visible = originVisible;

        if (!saved) return;

        // restore pos
        part.x = backupPosX;
        part.y = backupPosY;
        part.z = backupPosZ;

        // restore rot
        part.xRot = backupRotX;
        part.yRot = backupRotY;
        part.zRot = backupRotZ;

        // restore scale
        part.xScale = backupScaleX;
        part.yScale = backupScaleY;
        part.zScale = backupScaleZ;
    }

    @LuaWhitelist
    @LuaMethodDoc("vanilla_model_part.get_origin_visible")
    public boolean getOriginVisible() {
        return this.originVisible;
    }

    @LuaWhitelist
    @LuaMethodDoc("vanilla_model_part.get_origin_rot")
    public FiguraVec3 getOriginRot() {
        return this.originRot.copy();
    }

    @LuaWhitelist
    @LuaMethodDoc("vanilla_model_part.get_origin_pos")
    public FiguraVec3 getOriginPos() {
        return this.originPos.copy();
    }

    @LuaWhitelist
    @LuaMethodDoc("vanilla_model_part.get_origin_scale")
    public FiguraVec3 getOriginScale() {
        return this.originScale.copy();
    }

    @Override
    public String toString() {
        return "VanillaModelPart";
    }
}
