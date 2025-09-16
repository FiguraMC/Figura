package org.figuramc.figura.ducks;

import org.figuramc.figura.config.Configs;
import org.figuramc.figura.lua.docs.FiguraListDocs;
import org.figuramc.figura.mixin.input.InputConstantsTypeMixin;

import java.util.LinkedHashSet;

/**
 * Prevents a nasty circular dependency between {@link InputConstantsTypeMixin}, {@link FiguraListDocs},
 * and {@link Configs}.
 */
public class FiguraKeyStorage {
    private FiguraKeyStorage() {}

    public static final LinkedHashSet<String> allKeys = new LinkedHashSet<>();
}
