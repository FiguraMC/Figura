package org.moon.figura.lua.api.vanilla_model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.model.ParentType;
import org.moon.figura.trust.Trust;
import org.moon.figura.utils.LuaUtils;

import java.util.function.Function;

@LuaWhitelist
@LuaTypeDoc(
        name = "VanillaModelPart",
        value = "vanilla_part"
)
public class VanillaModelPart extends VanillaPart {

    private final ParentType parentType;
    private final Function<EntityModel<?>, ModelPart> provider;

    //backup
    private float backupPosX, backupPosY, backupPosZ;
    private float backupRotX, backupRotY, backupRotZ;
    private float backupScaleX, backupScaleY, backupScaleZ;
    private boolean originVisible;
    private boolean saved;

    //part getters
    private final FiguraVec3 originRot = FiguraVec3.of();
    private final FiguraVec3 originPos = FiguraVec3.of();
    private final FiguraVec3 originScale = FiguraVec3.of();

    //transforms
    private FiguraVec3 pos, rot, scale;
    private FiguraVec3 offsetRot, offsetScale;

    public VanillaModelPart(Avatar owner, String name, ParentType parentType, Function<EntityModel<?>, ModelPart> provider) {
        super(owner, name);
        this.parentType = parentType;
        this.provider = provider;
    }

    private ModelPart getPart(EntityModel<?> model) {
        return provider == null ? null : provider.apply(model);
    }

    @Override
    public void change(EntityModel<?> model) {
        if (visible == null)
            return;

        ModelPart part = getPart(model);
        if (part != null)
            part.visible = visible;
    }

    @Override
    public void save(EntityModel<?> model) {
        saved = false;
        ModelPart part = getPart(model);
        if (part == null) return;

        //set getters
        originRot.set(-part.xRot, -part.yRot, part.zRot);
        originRot.scale(180 / Math.PI);

        FiguraVec3 pivot = parentType.offset.copy();
        pivot.subtract(part.x, part.y, part.z);
        pivot.multiply(1, -1, -1);
        originPos.set(pivot);
        pivot.free();

        originScale.set(part.xScale, part.yScale, part.zScale);

        //save visible
        originVisible = part.visible;

        //save pos
        backupPosX = part.x;
        backupPosY = part.y;
        backupPosZ = part.z;

        //save rot
        backupRotX = part.xRot;
        backupRotY = part.yRot;
        backupRotZ = part.zRot;

        //save scale
        backupScaleX = part.xScale;
        backupScaleY = part.yScale;
        backupScaleZ = part.zScale;

        saved = true;
    }

    @Override
    public void restore(EntityModel<?> model) {
        ModelPart part = getPart(model);
        if (part == null) return;

        //restore visible
        part.visible = originVisible;

        if (!saved) return;

        //restore pos
        part.x = backupPosX;
        part.y = backupPosY;
        part.z = backupPosZ;

        //restore rot
        part.xRot = backupRotX;
        part.yRot = backupRotY;
        part.zRot = backupRotZ;

        //restore scale
        part.xScale = backupScaleX;
        part.yScale = backupScaleY;
        part.zScale = backupScaleZ;
    }

    @Override
    public void transform(EntityModel<?> model) {
        if (!saved) return;

        ModelPart part = getPart(model);
        if (part == null) return;

        //pos
        if (pos != null) {
            part.x += (float) -pos.x;
            part.y += (float) -pos.y;
            part.z += (float) pos.z;
        }

        //rot
        if (rot != null) {
            FiguraVec3 rot = this.rot.toRad();
            part.setRotation((float) -rot.x, (float) -rot.y, (float) rot.z);
        }
        if (offsetRot != null)
            part.offsetRotation(offsetRot.toRad().mul(-1, -1, 1).asVec3f());

        //scale
        if (scale != null) {
            part.xScale = (float) scale.x;
            part.yScale = (float) scale.y;
            part.zScale = (float) scale.z;
        }
        if (offsetScale != null)
            part.offsetScale(offsetScale.asVec3f());
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

    @LuaWhitelist
    @LuaMethodDoc("vanilla_part.get_pos")
    public FiguraVec3 getPos() {
        return pos;
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
            value = "vanilla_part.set_pos"
    )
    public VanillaModelPart setPos(Object x, Double y, Double z) {
        pos = x == null ? null : LuaUtils.parseVec3("setPos", x, y, z);
        return this;
    }

    @LuaWhitelist
    public VanillaModelPart pos(Object x, Double y, Double z) {
        setPos(x, y, z);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("vanilla_part.get_rot")
    public FiguraVec3 getRot() {
        return rot;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "rot"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "rot",
            value = "vanilla_part.set_rot"
    )
    public VanillaModelPart setRot(Object x, Double y, Double z) {
        rot = x == null ? null : LuaUtils.parseVec3("setRot", x, y, z);
        return this;
    }

    @LuaWhitelist
    public VanillaModelPart rot(Object x, Double y, Double z) {
        setRot(x, y, z);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("vanilla_part.get_offset_rot")
    public FiguraVec3 getOffsetRot() {
        return offsetRot;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "offsetRot"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "offsetRot",
            value = "vanilla_part.set_offset_rot"
    )
    public VanillaModelPart setOffsetRot(Object x, Double y, Double z) {
        offsetRot = x == null ? null : LuaUtils.parseVec3("setOffsetRot", x, y, z);
        return this;
    }

    @LuaWhitelist
    public VanillaModelPart offsetRot(Object x, Double y, Double z) {
        return setOffsetRot(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("vanilla_part.get_scale")
    public FiguraVec3 getScale() {
        return scale;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "scale"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "scale",
            value = "vanilla_part.set_scale"
    )
    public VanillaModelPart setScale(Object x, Double y, Double z) {
        scale = x == null ? null : LuaUtils.parseVec3("setScale", x, y, z, 1, 1, 1);
        return this;
    }

    @LuaWhitelist
    public VanillaModelPart scale(Object x, Double y, Double z) {
        setScale(x, y, z);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("vanilla_part.get_offset_scale")
    public FiguraVec3 getOffsetScale() {
        return offsetScale;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "offsetScale"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "offsetScale",
            value = "vanilla_part.set_offset_scale"
    )
    public VanillaModelPart setOffsetScale(Object x, Double y, Double z) {
        offsetScale = x == null ? null : LuaUtils.parseVec3("setOffsetScale", x, y, z, 1, 1, 1);
        return this;
    }

    @LuaWhitelist
    public VanillaModelPart offsetScale(Object x, Double y, Double z) {
        return setOffsetScale(x, y, z);
    }

    @Override
    public String toString() {
        return "VanillaModelPart";
    }
}
