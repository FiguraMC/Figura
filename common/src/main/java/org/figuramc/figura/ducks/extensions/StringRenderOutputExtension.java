package org.figuramc.figura.ducks.extensions;

import net.minecraft.network.chat.Style;

public interface StringRenderOutputExtension {
    boolean polygonOffset$accept(int i, Style style, int j);

    float polyonOffset$finish(int i, float f);
}
