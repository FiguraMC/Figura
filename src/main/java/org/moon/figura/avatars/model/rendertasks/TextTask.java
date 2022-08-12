package org.moon.figura.avatars.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.moon.figura.avatars.model.PartCustomization;
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
    private boolean centered = false;
    private boolean shadow = false;
    private boolean outline = false;
    private FiguraVec3 outlineColor;

    private int cachedComplexity;

    @Override
    public boolean render(PartCustomization.Stack stack, MultiBufferSource buffer, int light, int overlay) {
        if (!enabled || text == null || text.size() == 0)
            return false;

        this.pushOntoStack(stack);
        PoseStack poseStack = stack.peek().copyIntoGlobalPoseStack();
        poseStack.scale(-1, -1, 1);

        Font font = Minecraft.getInstance().font;

        for (int i = 0; i < text.size(); i++) {
            Component text = this.text.get(i);
            int x = centered ? -font.width(text) / 2 : 0;
            int y = i * font.lineHeight;

            if (outline) {
                UIHelper.renderOutlineText(poseStack, font, text, x, y, 0xFFFFFF, outlineColor == null ? 0 : ColorUtils.rgbToInt(outlineColor));
            } else {
                font.drawInBatch(text, x, y, 0xFFFFFF, shadow, poseStack.last().pose(), buffer, false, 0, emissive ? LightTexture.FULL_BRIGHT : light);
            }
        }

        stack.pop();
        return true;
    }

    @Override
    public int getComplexity() {
        return cachedComplexity;
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
        this.text = text == null ? null : TextUtils.splitText(TextUtils.noBadges4U(TextUtils.tryParseJson(text)), "\n");
        if (text != null)
            this.cachedComplexity = text.length() + 1;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = Boolean.class,
                            argumentNames = "centered"
                    )
            },
            description = "text_task.centered"
    )
    public RenderTask centered(boolean centered) {
        this.centered = centered;
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
    public RenderTask shadow(boolean shadow) {
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
    public RenderTask outline(boolean outline) {
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
