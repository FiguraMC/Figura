package org.figuramc.figura.gui.cards;

import java.util.Locale;

public enum CardBackground {
    DEFAULT,
    CHEESE,
    CLOUDS,
    COOKIE,
    RAINBOW,
    INSCRYPTION,
    SPACE,
    FADE;

    public static CardBackground parse(String string) {
        try {
            return CardBackground.valueOf(string.toUpperCase(Locale.US));
        } catch (Exception ignored) {
            return DEFAULT;
        }
    }
}
