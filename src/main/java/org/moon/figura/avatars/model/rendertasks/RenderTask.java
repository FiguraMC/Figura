package org.moon.figura.avatars.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaType;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;

@LuaType(typeName = "render_task")
@LuaTypeDoc(
        name = "Render Task",
        description = "render_task"
)
public abstract class RenderTask {

    protected boolean enabled = true;
    protected boolean emissive = false;
    protected FiguraVec3 pos, rot, scale;

    public abstract void render(PoseStack stack, MultiBufferSource buffer, int light, int overlay);

    public void apply(PoseStack stack) {
        if (rot != null) {
            stack.mulPose(Vector3f.XP.rotationDegrees((float) rot.x));
            stack.mulPose(Vector3f.YP.rotationDegrees((float) rot.y));
            stack.mulPose(Vector3f.ZP.rotationDegrees((float) rot.z));
        }

        if (pos != null)
            stack.translate(pos.x, pos.y, pos.z);

        if (scale != null)
            stack.scale((float) scale.x, (float) scale.y, (float) scale.z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "bool"
            ),
            description = "render_task.enabled"
    )
    public RenderTask enabled(@LuaNotNil Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "bool"
            ),
            description = "render_task.emissive"
    )
    public RenderTask emissive(@LuaNotNil Boolean emissive) {
        this.emissive = emissive;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "render_task.pos"
    )
    public RenderTask pos(Object x, Double y, Double z) {
        this.pos = LuaUtils.parseVec3("pos", x, y, z);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "rot"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "render_task.rot"
    )
    public RenderTask rot(Object x, Double y, Double z) {
        this.rot = LuaUtils.parseVec3("rot", x, y, z);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "scale"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "render_task.scale"
    )
    public RenderTask scale(Object x, Double y, Double z) {
        this.scale = LuaUtils.parseVec3("scale", x, y, z, 1, 1, 1);
        return this;
    }
}
