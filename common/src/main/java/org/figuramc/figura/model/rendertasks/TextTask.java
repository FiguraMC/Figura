package org.figuramc.figura.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.Badges;
import org.figuramc.figura.font.Emojis;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.model.FiguraModelPart;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.LuaUtils;
import org.figuramc.figura.utils.TextUtils;
import org.joml.Matrix4f;
import org.luaj.vm2.LuaError;

import java.util.List;
import java.util.Locale;

@LuaWhitelist
@LuaTypeDoc(
        name = "TextTask",
        value = "text_task"
)
public class TextTask extends RenderTask {

    private String textCached;
    private List<Component> text;
    private TextUtils.Alignment alignment = TextUtils.Alignment.LEFT;
    private boolean shadow = false, outline = false;
    private boolean background = false, seeThrough = false;
    private Integer outlineColor, backgroundColor;
    private int opacity = 0xFF;
    private int width = 0;
    private boolean wrap = true;

    private int cachedComplexity, cacheWidth, cacheHeight;

    public TextTask(String name, Avatar owner, FiguraModelPart parent) {
        super(name, owner, parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        if (opacity == 0) return; // lol

        // prepare matrices
        Matrix4f matrix = poseStack.last().pose();
        matrix.scale(-1, -1, -1);

        // prepare variables
        Font font = Minecraft.getInstance().font;
        int l = this.customization.light != null ? this.customization.light : light;
        int bg = backgroundColor != null ? ColorUtils.intRGBAToIntARGB(backgroundColor) : background ? (int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25f) * 0xFF) << 24 : 0;
        int out = outlineColor != null ? outlineColor : 0x202020;
        int op = opacity << 24 | 0xFFFFFF;
        Font.DisplayMode displayMode = seeThrough ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET;
        float vertexOffset = outline ? FiguraMod.VERTEX_OFFSET : 0f;

        // background
        if (bg != 0) {
            int offset = alignment.apply(cacheWidth);
            float x1 = -1 - offset;
            float x2 = cacheWidth - offset;
            VertexConsumer vertexConsumer = buffer.getBuffer(seeThrough ? RenderType.textBackgroundSeeThrough() : RenderType.textBackground());
            vertexConsumer.vertex(matrix, x1, -1f, vertexOffset).color(bg).uv2(l).endVertex();
            vertexConsumer.vertex(matrix, x1, cacheHeight, vertexOffset).color(bg).uv2(l).endVertex();
            vertexConsumer.vertex(matrix, x2, cacheHeight, vertexOffset).color(bg).uv2(l).endVertex();
            vertexConsumer.vertex(matrix, x2, -1f, vertexOffset).color(bg).uv2(l).endVertex();
        }

        // text
        for (int i = 0, j = 0; i < text.size(); i++, j += (font.lineHeight + 1)) {
            Component text = this.text.get(i);
            int x = -alignment.apply(font, text);

            if (outline) {
                font.drawInBatch8xOutline(text.getVisualOrderText(), x, j, -1, out, matrix, buffer, l);
                if (seeThrough)
                    font.drawInBatch(text, x, j, op, shadow, matrix, buffer, displayMode, 0, l);
            } else {
                font.drawInBatch(text, x, j, op, shadow, matrix, buffer, displayMode, 0, l);
            }
        }
    }

    @Override
    public int getComplexity() {
        return cachedComplexity;
    }

    @Override
    public boolean shouldRender() {
        return super.shouldRender() && text != null && text.size() != 0;
    }

    private void updateText() {
        if (this.textCached == null) {
            this.text = null;
            return;
        }

        Component component = TextUtils.tryParseJson(this.textCached);
        component = Badges.noBadges4U(component);
        component = Emojis.applyEmojis(component);
        component = Emojis.removeBlacklistedEmojis(component);
        this.text = TextUtils.formatInBounds(component, Minecraft.getInstance().font, width, wrap);

        Font font = Minecraft.getInstance().font;
        cacheWidth = TextUtils.getWidth(this.text, font);
        cacheHeight = TextUtils.getHeight(this.text, font);
    }


    // -- lua -- // 


    @LuaWhitelist
    @LuaMethodDoc("text_task.get_text")
    public String getText() {
        return textCached;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "text"
            ),
            aliases = "text",
            value = "text_task.set_text"
    )
    public TextTask setText(String text) {
        this.textCached = text;
        updateText();
        if (text != null) this.cachedComplexity = text.length() + 1;
        return this;
    }

    @LuaWhitelist
    public TextTask text(String text) {
        return setText(text);
    }

    @LuaWhitelist
    @LuaMethodDoc("text_task.get_alignment")
    public String getAlignment() {
        return this.alignment.name();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "alignment"
            ),
            aliases = "alignment",
            value = "text_task.set_alignment"
    )
    public TextTask setAlignment(@LuaNotNil String alignment) {
        try {
            this.alignment = TextUtils.Alignment.valueOf(alignment.toUpperCase(Locale.US));
        } catch (Exception ignored) {
            throw new LuaError("Invalid alignment type \"" + alignment + "\"");
        }
        return this;
    }

    @LuaWhitelist
    public TextTask alignment(@LuaNotNil String alignment) {
        return setAlignment(alignment);
    }

    @LuaWhitelist
    @LuaMethodDoc("text_task.has_shadow")
    public boolean hasShadow() {
        return this.shadow;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "shadow"
            ),
            aliases = "shadow",
            value = "text_task.set_shadow"
    )
    public TextTask setShadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    @LuaWhitelist
    public TextTask shadow(boolean shadow) {
        return setShadow(shadow);
    }

    @LuaWhitelist
    @LuaMethodDoc("text_task.has_outline")
    public boolean hasOutline() {
        return this.outline;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "outline"
            ),
            aliases = "outline",
            value = "text_task.set_outline"
    )
    public TextTask setOutline(boolean outline) {
        this.outline = outline;
        return this;
    }

    @LuaWhitelist
    public TextTask outline(boolean outline) {
        return setOutline(outline);
    }

    @LuaWhitelist
    @LuaMethodDoc("text_task.get_outline_color")
    public FiguraVec3 getOutlineColor() {
        return this.outlineColor != null ? ColorUtils.intToRGB(this.outlineColor) : FiguraVec3.of();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "color"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b"}
                    )
            },
            aliases = "outlineColor",
            value = "text_task.set_outline_color"
    )
    public TextTask setOutlineColor(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setOutlineColor", x, y, z);
        this.outlineColor = ColorUtils.rgbToInt(vec);
        return this;
    }

    @LuaWhitelist
    public TextTask outlineColor(Object x, Double y, Double z) {
        return setOutlineColor(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("text_task.get_width")
    public int getWidth() {
        return width;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Integer.class,
                    argumentNames = "width"
            ),
            aliases = "width",
            value = "text_task.set_width"
    )
    public TextTask setWidth(int width) {
        this.width = width;
        updateText();
        return this;
    }

    @LuaWhitelist
    public TextTask width(int width) {
        return setWidth(width);
    }

    @LuaWhitelist
    @LuaMethodDoc("text_task.has_wrap")
    public boolean hasWrap() {
        return wrap;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "wrap"
            ),
            aliases = "wrap",
            value = "text_task.set_wrap"
    )
    public TextTask setWrap(boolean wrap) {
        this.wrap = wrap;
        updateText();
        return this;
    }

    @LuaWhitelist
    public TextTask wrap(boolean wrap) {
        return setWrap(wrap);
    }

    @LuaWhitelist
    @LuaMethodDoc("text_task.is_see_through")
    public boolean isSeeThrough() {
        return this.seeThrough;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "seeThrough"
            ),
            aliases = "seeThrough",
            value = "text_task.set_see_through"
    )
    public TextTask setSeeThrough(boolean seeThrough) {
        this.seeThrough = seeThrough;
        return this;
    }

    @LuaWhitelist
    public TextTask seeThrough(boolean seeThrough) {
        return setSeeThrough(seeThrough);
    }

    @LuaWhitelist
    @LuaMethodDoc("text_task.has_background")
    public boolean hasBackground() {
        return this.background;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "background"
            ),
            aliases = "background",
            value = "text_task.set_background"
    )
    public TextTask setBackground(boolean background) {
        this.background = background;
        return this;
    }

    @LuaWhitelist
    public TextTask background(boolean background) {
        return setBackground(background);
    }

    @LuaWhitelist
    @LuaMethodDoc("text_task.get_background_color")
    public FiguraVec4 getBackgroundColor() {
        return this.backgroundColor == null ? null : ColorUtils.intToRGBA(this.backgroundColor);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec4.class,
                            argumentNames = "rgba"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b", "a"}
                    )
            },
            aliases = "backgroundColor",
            value = "text_task.set_background_color"
    )
    public TextTask setBackgroundColor(Object r, Double g, Double b, Double a) {
        FiguraVec4 vec = LuaUtils.parseVec4("setBackgroundColor", r, g, b, a, 0, 0, 0,  Minecraft.getInstance().options.getBackgroundOpacity(0.25f));
        this.backgroundColor = ColorUtils.rgbaToInt(vec);
        return this;
    }

    @LuaWhitelist
    public TextTask backgroundColor(Object r, Double g, Double b, Double a) {
        return setBackgroundColor(r, g, b, a);
    }

    @LuaWhitelist
    @LuaMethodDoc("text_task.get_opacity")
    public float getOpacity() {
        return opacity / (float) 0xFF;
    }

    @LuaWhitelist
    @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = Float.class, argumentNames = "opacity"), aliases = "opacity", value = "text_task.set_opacity")
    public TextTask setOpacity(float opacity) {
        this.opacity = (int) (opacity * 0xFF);
        return this;
    }

    @LuaWhitelist
    public TextTask opacity(float opacity) {
        return setOpacity(opacity);
    }

    @Override
    public String toString() {
        return name + " (Text Render Task)";
    }
}
