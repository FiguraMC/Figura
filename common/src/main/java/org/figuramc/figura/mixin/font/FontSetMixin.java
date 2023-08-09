package org.figuramc.figura.mixin.font;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.FontTexture;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.providers.BitmapProvider;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura.ducks.BakedGlyphAccessor;
import org.figuramc.figura.ducks.BitmapProviderGlyphAccessor;
import org.figuramc.figura.font.Emojis;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Mixin(FontSet.class)
public abstract class FontSetMixin {

    @Shadow
    @Final
    private ResourceLocation name;
    @Unique
    int figura$codePoint = -1;

    //method_27545 for fabric intermediary, m_232558_ for SRG, lambda$setGlyphProviders$5 unmapped for OF, i had dig at the bytecode for this one
    @Inject(method = {"method_27545", "m_232558_", "lambda$setGlyphProviders$5"}, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/font/GlyphInfo;getAdvance(Z)F", shift = At.Shift.BEFORE, remap = true), locals = LocalCapture.CAPTURE_FAILEXCEPTION, remap = false)
    public void thing(List<?> list, Set<?> set, int i, CallbackInfo ci, Iterator<?> var4, GlyphProvider glyphProvider, GlyphInfo glyphInfo) {
        if (figura$isEmojiFont() && glyphInfo instanceof BitmapProvider.Glyph) {
            ((BitmapProviderGlyphAccessor) glyphInfo).figura$setAdvance(8);
        }
    }

    @Inject(method = "computeBakedGlyph", at = @At("HEAD"))
    public void computeBakedGlyphMixin(int codePoint, CallbackInfoReturnable<BakedGlyph> cir) {
        figura$codePoint = codePoint;
    }

    @Redirect(method = "stitch", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/FontTexture;add(Lcom/mojang/blaze3d/font/SheetGlyphInfo;)Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;"))
    public BakedGlyph insertDataIntoBakedGlyph(FontTexture instance, SheetGlyphInfo glyphInfo) {
        BakedGlyph glyph = instance.add(glyphInfo);

        if (figura$isEmojiFont() && glyph != null) {
            ((BakedGlyphAccessor) glyph).figura$setupEmoji(Emojis.getCategoryByFont(name), figura$codePoint);
        }

        return glyph;
    }

    @Inject(method = "reload", at = @At("HEAD"))
    public void reload(List<GlyphProvider> fonts, CallbackInfo ci) {
        figura$codePoint = -1;
    }

    @Unique
    private boolean figura$isEmojiFont() {
        return name.getNamespace().equals("figura");
    }
}
