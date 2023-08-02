package org.figuramc.figura.ducks;

import org.figuramc.figura.font.Emojis;

public interface BakedGlyphAccessor {
    void figura$setupEmoji(Emojis.EmojiContainer container, int codepoint);
}
