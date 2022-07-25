package org.moon.figura.avatars.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.LuaUtils;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.ui.UIHelper;

import java.util.List;

@LuaWhitelist
@LuaTypeDoc(
        name = "TextTask",
        description = "text_task"
)
public class TextTask extends RenderTask {

    private List<Component> text;
    private boolean centred = false;
    private boolean shadow = false;
    private boolean outline = false;
    private FiguraVec3 outlineColor;

    @Override
    public void render(PoseStack stack, MultiBufferSource buffer, int light, int overlay) {
        if (!enabled || text == null || text.size() == 0)
            return;

        stack.pushPose();
        this.apply(stack);
        stack.scale(-1, -1, 1);

        Font font = Minecraft.getInstance().font;

        for (int i = 0; i < text.size(); i++) {
            Component text = this.text.get(i);
            float x = centred ? -font.width(text) / 2f : 0f;
            float y = i * font.lineHeight;

            if (outline) {
                UIHelper.renderOutlineText(stack, font, text, x, y, 0xFFFFFF, outlineColor == null ? 0 : ColorUtils.rgbToInt(outlineColor));
            } else {
                font.drawInBatch(text, x, y, 0xFFFFFF, shadow, stack.last().pose(), buffer, false, 0, emissive ? LightTexture.FULL_BRIGHT : light);
            }
        }

        stack.popPose();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {TextTask.class, String.class},
                            argumentNames = {"task", "text"}
                    )
            },
            description = "text_task.text"
    )
    public static RenderTask text(@LuaNotNil TextTask task, String text) {
        task.text = text == null ? null : TextUtils.splitText(TextUtils.tryParseJson(text), "\n");
        return task;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {TextTask.class, Boolean.class},
                            argumentNames = {"task", "centred"}
                    )
            },
            description = "text_task.centred"
    )
    public static RenderTask centred(@LuaNotNil TextTask task, @LuaNotNil Boolean centred) {
        task.centred = centred;
        return task;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {TextTask.class, Boolean.class},
                            argumentNames = {"task", "shadow"}
                    )
            },
            description = "text_task.shadow"
    )
    public static RenderTask shadow(@LuaNotNil TextTask task, @LuaNotNil Boolean shadow) {
        task.shadow = shadow;
        return task;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {TextTask.class, Boolean.class},
                            argumentNames = {"task", "outline"}
                    )
            },
            description = "text_task.outline"
    )
    public static RenderTask outline(@LuaNotNil TextTask task, @LuaNotNil Boolean outline) {
        task.outline = outline;
        return task;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {TextTask.class, FiguraVec3.class},
                            argumentNames = {"task", "color"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {TextTask.class, Double.class, Double.class, Double.class},
                            argumentNames = {"task", "r", "g", "b"}
                    )
            },
            description = "text_task.outline_color"
    )
    public static TextTask outlineColor(@LuaNotNil TextTask task, Object x, Double y, Double z) {
        task.outlineColor = LuaUtils.oldParseVec3("outlineColor", x, y, z);
        return task;
    }
}
