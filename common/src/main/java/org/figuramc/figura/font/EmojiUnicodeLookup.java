package org.figuramc.figura.font;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EmojiUnicodeLookup {
    private final Map<String, String> unicodeLookup = new HashMap<>(); // <EmojiName, Unicode>
    private final HashMap<String, String[]> reverseUnicodeLookup = new HashMap<>(); // <Unicode, EmojiNames[]>
    private final Map<Integer, EmojiMetadata> metadataLookup = new HashMap<>(); // <Codepoint, EmojiMetadata>
    private final Map<String, String> shortcutLookup = new HashMap<>(); // <Shortcut, Unicode>

    public void putAliases(String[] aliases, String unicode) {
        for (String alias : aliases) {
            unicodeLookup.put(alias, unicode);
        }
        reverseUnicodeLookup.put(unicode, aliases);
    }

    public void putShortcuts(String[] aliases, String unicode) {
        for (String alias : aliases) {
            shortcutLookup.put(alias, unicode);
        }
    }

    public void putMetadata(int codepoint, EmojiMetadata metadata) {
        metadataLookup.put(codepoint, metadata);
    }

    public Collection<String> getNames() {
        return unicodeLookup.keySet();
    }

    public Collection<String> getShortcuts() {
        return shortcutLookup.keySet();
    }

    public @Nullable EmojiMetadata getMetadata(int codepoint) {
        return metadataLookup.getOrDefault(codepoint, null);
    }

    public @Nullable String getUnicode(String emojiAlias) {
        return unicodeLookup.getOrDefault(emojiAlias, null);
    }

    public @Nullable String getUnicodeForShortcut(String shortcut) {
        return shortcutLookup.getOrDefault(shortcut, null);
    }

    public @Nullable String[] getAliases(String unicode) {
        return reverseUnicodeLookup.getOrDefault(unicode, null);
    }

    public Collection<EmojiMetadata> metadataValues() {
        return metadataLookup.values();
    }

    public Set<String> aliasValues() {
        return unicodeLookup.keySet();
    }

    public Collection<String> unicodeValues() {
        return unicodeLookup.values();
    }
}
