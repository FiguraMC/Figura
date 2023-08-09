package org.figuramc.figura.ducks;

import org.figuramc.figura.font.EmojiContainer;

public interface BakedGlyphAccessor {
    void figura$setupEmoji(EmojiContainer container, int codepoint);
}
