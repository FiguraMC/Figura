package org.figuramc.figura.ducks;

import org.figuramc.figura.font.EmojiContainer;

public interface BakedGlyphAccessor {
    void figura$setupEmoji(EmojiContainer container, int codepoint);
    float figura$getU0();
    float figura$getU1();
    float figura$getV0();
    float figura$getV1();
}
