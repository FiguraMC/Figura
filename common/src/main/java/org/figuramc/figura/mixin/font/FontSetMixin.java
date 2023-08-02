package org.figuramc.figura.mixin.font;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.FontTexture;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.providers.BitmapProvider;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura.ducks.BakedGlyphAccessor;
import org.figuramc.figura.ducks.BitmapProviderGlyphAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FontSet.class)
public abstract class FontSetMixin {

    @Shadow
    @Final
    private ResourceLocation name;
    @Unique
    boolean figura$isEmojiFont;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(TextureManager textureManager, ResourceLocation id, CallbackInfo ci) {
        figura$isEmojiFont = id.getNamespace().contains("figura");
    }

    @Redirect(method = "method_27545", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/font/GlyphInfo;getAdvance(Z)F"))
    public float makeEmojisWidthCorrect(GlyphInfo instance, boolean bold) {
        if (figura$isEmojiFont && instance instanceof BitmapProvider.Glyph) {
            ((BitmapProviderGlyphAccessor) instance).figura$setAdvance(8);
        }
        return instance.getAdvance(bold);
    }

    @Redirect(method = "stitch", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/FontTexture;add(Lcom/mojang/blaze3d/font/SheetGlyphInfo;)Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;"))
    public BakedGlyph insertDataIntoBakedGlyph(FontTexture instance, SheetGlyphInfo glyphInfo) {
        BakedGlyph glyph = instance.add(glyphInfo);

        if (figura$isEmojiFont && glyph != null) {
            ((BakedGlyphAccessor) glyph).figura$setupEmoji(this.name);
        }

        return glyph;
    }
}
