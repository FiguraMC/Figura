package org.moon.figura.gui.cards;

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
            return CardBackground.valueOf(string.toUpperCase());
        } catch (Exception ignored) {
            return DEFAULT;
        }
    }
}
