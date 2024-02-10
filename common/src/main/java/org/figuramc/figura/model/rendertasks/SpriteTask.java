package org.figuramc.figura.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.model.FiguraModelPart;
import org.figuramc.figura.model.rendering.Vertex;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.figuramc.figura.model.rendering.texture.RenderTypes;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.LuaUtils;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.luaj.vm2.LuaError;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@LuaWhitelist
@LuaTypeDoc(
        name = "SpriteTask",
        value = "sprite_task"
)
public class SpriteTask extends RenderTask {

    private ResourceLocation texture;
    private int textureW = -1, textureH = -1;
    private int width, height;
    private int regionW, regionH;
    private float u = 0f, v = 0f;
    private int r = 0xFF, g = 0xFF, b = 0xFF, a = 0xFF;
    private RenderTypes renderType = RenderTypes.TRANSLUCENT;
    private final List<Vertex> vertices = new ArrayList<>(4);

    public SpriteTask(String name, Avatar owner, FiguraModelPart parent) {
        super(name, owner, parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        if (a == 0) return;
        poseStack.scale(-1, -1, 1);

        // prepare variables
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        int newLight = this.customization.light != null ? this.customization.light : light;
        int newOverlay = this.customization.overlay != null ? this.customization.overlay : overlay;

        // setup texture render
        VertexConsumer consumer = buffer.getBuffer(renderType.get(texture));

        // create vertices
        for (Vertex v : vertices) {
            consumer.vertex(pose, v.x, v.y, v.z)
                    .color(r, g, b, a)
                    .uv(v.u, v.v)
                    .overlayCoords(newOverlay)
                    .uv2(newLight)
                    .normal(normal, v.nx, v.ny, v.nz)
                    .endVertex();
        }
    }

    @Override
    public int getComplexity() {
        return 1; // 1 face, 1 complexity
    }

    @Override
    public boolean shouldRender() {
        return super.shouldRender() && texture != null && renderType != RenderTypes.NONE;
    }

    private void recalculateVertices() {
        float u2 = u + regionW / (float) textureW;
        float v2 = v + regionH / (float) textureH;

        vertices.clear();
        vertices.add(new Vertex(0f, height, 0f, u, v2, 0f, 0f, -1f));
        vertices.add(new Vertex(width, height, 0f, u2, v2, 0f, 0f, -1f));
        vertices.add(new Vertex(width, 0f, 0f, u2, v, 0f, 0f, -1f));
        vertices.add(new Vertex(0f, 0f, 0f, u, v, 0f, 0f, -1f));
    }


    // -- lua -- // 


    @LuaWhitelist
    @LuaMethodDoc("sprite_task.get_texture")
    public String getTexture() {
        return texture == null ? null : texture.toString();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {String.class, Integer.class, Integer.class},
                            argumentNames = {"textureLocation", "width", "height"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = FiguraTexture.class,
                            argumentNames = "texture"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraTexture.class, Integer.class, Integer.class},
                            argumentNames = {"texture", "width", "height"}
                    )
            },
            aliases = "texture",
            value = "sprite_task.set_texture"
    )
    public SpriteTask setTexture(Object texture, Integer width, Integer height) {
        if (texture == null) {
            this.texture = null;
            return this;
        }

        if (texture instanceof String s) {
            try {
                this.texture = new ResourceLocation(s);
            } catch (Exception e) {
                this.texture = MissingTextureAtlasSprite.getLocation();
            }
            if (width == null || height == null)
                throw new LuaError("Texture dimensions cannot be null");
        } else if (texture instanceof FiguraTexture tex) {
            this.texture = tex.getLocation();
            if (width == null || height == null) {
                width = tex.getWidth();
                height = tex.getHeight();
            }
        } else {
            throw new LuaError("Illegal argument to setTexture(): " + texture.getClass().getSimpleName());
        }

        if (width <= 0 || height <= 0)
            throw new LuaError("Invalid texture size: " + width + "x" + height);

        this.textureW = this.regionW = this.width = width;
        this.textureH = this.regionH = this.height = height;
        recalculateVertices();
        return this;
    }

    @LuaWhitelist
    public SpriteTask texture(Object texture, Integer width, Integer height) {
        return setTexture(texture, width, height);
    }

    @LuaWhitelist
    @LuaMethodDoc("sprite_task.get_dimensions")
    public FiguraVec2 getDimensions() {
        return FiguraVec2.of(textureW, textureH);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "dimensions"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Integer.class},
                            argumentNames = {"width", "height"}
                    )
            },
            aliases = "dimensions",
            value = "sprite_task.set_dimensions"
    )
    public SpriteTask setDimensions(Object w, Double h) {
        FiguraVec2 vec = LuaUtils.parseVec2("setDimensions", w, h);
        if (vec.x <= 0 || vec.y <= 0)
            throw new LuaError("Invalid dimensions: " + vec.x + "x" + vec.y);
        this.textureW = (int) Math.round(vec.x);
        this.textureH = (int) Math.round(vec.y);
        recalculateVertices();
        return this;
    }

    @LuaWhitelist
    public SpriteTask dimensions(Object w, Double h) {
        return setDimensions(w, h);
    }

    @LuaWhitelist
    @LuaMethodDoc("sprite_task.get_size")
    public FiguraVec2 getSize() {
        return FiguraVec2.of(width, height);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "size"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Integer.class},
                            argumentNames = {"width", "height"}
                    )
            },
            aliases = "size",
            value = "sprite_task.set_size"
    )
    public SpriteTask setSize(Object w, Double h) {
        FiguraVec2 vec = LuaUtils.parseVec2("setSize", w, h);
        this.width = (int) Math.round(vec.x);
        this.height = (int) Math.round(vec.y);
        recalculateVertices();
        return this;
    }

    @LuaWhitelist
    public SpriteTask size(Object w, Double h) {
        return setSize(w, h);
    }

    @LuaWhitelist
    @LuaMethodDoc("sprite_task.get_region")
    public FiguraVec2 getRegion() {
        return FiguraVec2.of(regionW, regionH);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "region"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Integer.class},
                            argumentNames = {"width", "height"}
                    )
            },
            aliases = "region",
            value = "sprite_task.set_region"
    )
    public SpriteTask setRegion(Object w, Double h) {
        FiguraVec2 vec = LuaUtils.parseVec2("setRegion", w, h);
        this.regionW = (int) Math.round(vec.x);
        this.regionH = (int) Math.round(vec.y);
        recalculateVertices();
        return this;
    }

    @LuaWhitelist
    public SpriteTask region(Object w, Double h) {
        return setRegion(w, h);
    }

    @LuaWhitelist
    @LuaMethodDoc("sprite_task.get_uv")
    public FiguraVec2 getUV() {
        return FiguraVec2.of(u, v);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "uv"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class},
                            argumentNames = {"u", "v"}
                    )
            },
            aliases = "uv",
            value = "sprite_task.set_uv"
    )
    public SpriteTask setUV(Object u, Double v) {
        FiguraVec2 vec = LuaUtils.parseVec2("setUV", u, v);
        this.u = (float) vec.x;
        this.v = (float) vec.y;
        recalculateVertices();
        return this;
    }

    @LuaWhitelist
    public SpriteTask uv(Object u, Double v) {
        return setUV(u, v);
    }

    @LuaWhitelist
    @LuaMethodDoc("sprite_task.get_uv_pixels")
    public FiguraVec2 getUVPixels() {
        if (this.textureW == -1 || this.textureH == -1)
            throw new LuaError("Cannot call getUVPixels before defining the texture dimensions!");
        return getUV().multiply(this.textureW, this.textureH);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "uv"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class},
                            argumentNames = {"u", "v"}
                    )
            },
            aliases = "uvPixels",
            value = "sprite_task.set_uv_pixels")
    public SpriteTask setUVPixels(Object u, Double v) {
        if (this.textureW == -1 || this.textureH == -1)
            throw new LuaError("Cannot call setUVPixels before defining the texture dimensions!");

        FiguraVec2 uv = LuaUtils.parseVec2("setUVPixels", u, v);
        uv.divide(this.textureW, this.textureH);
        setUV(uv.x, uv.y);

        return this;
    }

    @LuaWhitelist
    public SpriteTask uvPixels(Object u, Double v) {
        return setUVPixels(u, v);
    }

    @LuaWhitelist
    @LuaMethodDoc("sprite_task.get_color")
    public FiguraVec4 getColor() {
        return FiguraVec4.of(r, g, b, a).scale(1f / 255f);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "rgb"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec4.class,
                            argumentNames = "rgba"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b", "a"}
                    )
            },
            aliases = "color",
            value = "sprite_task.set_color"
    )
    public SpriteTask setColor(Object r, Double g, Double b, Double a) {
        FiguraVec4 vec = LuaUtils.parseVec4("setColor", r, g, b, a, 0, 0, 0, 1);
        int i = ColorUtils.rgbaToInt(vec);
        this.r = i >> 24 & 0xFF;
        this.g = i >> 16 & 0xFF;
        this.b = i >> 8 & 0xFF;
        this.a = i & 0xFF;
        return this;
    }

    @LuaWhitelist
    public SpriteTask color(Object r, Double g, Double b, Double a) {
        return setColor(r, g, b, a);
    }

    @LuaWhitelist
    @LuaMethodDoc("sprite_task.get_render_type")
    public String getRenderType() {
        return renderType.name();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "renderType"
            ),
            aliases = "renderType",
            value = "sprite_task.set_render_type"
    )
    public SpriteTask setRenderType(@LuaNotNil String renderType) {
        try {
            this.renderType = RenderTypes.valueOf(renderType.toUpperCase(Locale.US));
            return this;
        } catch (Exception ignored) {
            throw new LuaError("Illegal RenderType: \"" + renderType + "\".");
        }
    }

    @LuaWhitelist
    public SpriteTask renderType(@LuaNotNil String renderType) {
        return setRenderType(renderType);
    }

    @LuaWhitelist
    @LuaMethodDoc("sprite_task.get_vertices")
    public List<Vertex> getVertices() {
        return vertices;
    }

    @Override
    public String toString() {
        return name + " (Sprite Render Task)";
    }
}
