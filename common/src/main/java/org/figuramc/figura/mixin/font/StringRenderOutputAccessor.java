package org.figuramc.figura.mixin.font;

import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(Font.StringRenderOutput.class)
public interface StringRenderOutputAccessor {
    @Accessor("dimFactor")
    float getDimFactor();
    @Accessor("bufferSource")
    MultiBufferSource getBufferSource();
    @Accessor("dropShadow")
    boolean getDropShadow();
    @Accessor("r")
    float getR();
    @Accessor("g")
    float getG();
    @Accessor("b")
    float getB();
    @Accessor("a")
    float getA();
    @Accessor("x")
    float getX();
    @Accessor("y")
    float getY();
    @Accessor("packedLightCoords")
    int getPackedLightCoords();
    @Accessor("pose")
    Matrix4f getPose();
    @Accessor("x")
    void setX(float x);
    @Accessor("x")
    void setY(float y);

    @Accessor("effects")
    List<BakedGlyph.Effect> getEffects();

    @Invoker("addEffect")
    void invokeAddEffect(BakedGlyph.Effect effect);

}
