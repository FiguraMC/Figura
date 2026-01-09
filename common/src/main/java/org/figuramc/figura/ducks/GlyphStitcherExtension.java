package org.figuramc.figura.ducks;

import com.mojang.blaze3d.font.GlyphInfo;

public interface GlyphStitcherExtension {
    void addCodePoint(GlyphInfo info, int codePoint);
}
