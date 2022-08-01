package org.moon.figura.avatars.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaType;
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

@LuaType(typeName = "text_task")
@LuaTypeDoc(
        name = "Text Task",
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
                            argumentTypes = String.class,
                            argumentNames = "text"
                    )
            },
            description = "text_task.text"
    )
    public RenderTask text(String text) {
        this.text = text == null ? null : TextUtils.splitText(TextUtils.tryParseJson(text), "\n");
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = Boolean.class,
                            argumentNames = "centred"
                    )
            },
            description = "text_task.centred"
    )
    public RenderTask centred(@LuaNotNil Boolean centred) {
        this.centred = centred;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = Boolean.class,
                            argumentNames = "shadow"
                    )
            },
            description = "text_task.shadow"
    )
    public RenderTask shadow(@LuaNotNil Boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = Boolean.class,
                            argumentNames = "outline"
                    )
            },
            description = "text_task.outline"
    )
    public RenderTask outline(@LuaNotNil Boolean outline) {
        this.outline = outline;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "color"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b"}
                    )
            },
            description = "text_task.outline_color"
    )
    public TextTask outlineColor(Object x, Double y, Double z) {
        this.outlineColor = LuaUtils.parseVec3("outlineColor", x, y, z);
        return this;
    }

    @Override
    public String toString() {
        return "Text Render Task";
    }
}
