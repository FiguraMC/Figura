package org.moon.figura.avatars.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "RenderTask",
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
                    argumentTypes = {RenderTask.class, Boolean.class},
                    argumentNames = {"task", "bool"}
            ),
            description = "render_task.enabled"
    )
    public static RenderTask enabled(@LuaNotNil RenderTask task, @LuaNotNil Boolean enabled) {
        task.enabled = enabled;
        return task;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {RenderTask.class, Boolean.class},
                    argumentNames = {"task", "bool"}
            ),
            description = "render_task.emissive"
    )
    public static RenderTask emissive(@LuaNotNil RenderTask task, @LuaNotNil Boolean emissive) {
        task.emissive = emissive;
        return task;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {RenderTask.class, FiguraVec3.class},
                            argumentNames = {"task", "pos"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {RenderTask.class, Double.class, Double.class, Double.class},
                            argumentNames = {"task", "x", "y", "z"}
                    )
            },
            description = "render_task.pos"
    )
    public static RenderTask pos(@LuaNotNil RenderTask task, Object x, Double y, Double z) {
        task.pos = LuaUtils.parseVec3("pos", x, y, z);
        return task;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {RenderTask.class, FiguraVec3.class},
                            argumentNames = {"task", "rot"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {RenderTask.class, Double.class, Double.class, Double.class},
                            argumentNames = {"task", "x", "y", "z"}
                    )
            },
            description = "render_task.rot"
    )
    public static RenderTask rot(@LuaNotNil RenderTask task, Object x, Double y, Double z) {
        task.rot = LuaUtils.parseVec3("rot", x, y, z);
        return task;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {RenderTask.class, FiguraVec3.class},
                            argumentNames = {"task", "scale"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {RenderTask.class, Double.class, Double.class, Double.class},
                            argumentNames = {"task", "x", "y", "z"}
                    )
            },
            description = "render_task.scale"
    )
    public static RenderTask scale(@LuaNotNil RenderTask task, Object x, Double y, Double z) {
        task.scale = LuaUtils.parseVec3("scale", x, y, z, 1, 1, 1);
        return task;
    }
}
