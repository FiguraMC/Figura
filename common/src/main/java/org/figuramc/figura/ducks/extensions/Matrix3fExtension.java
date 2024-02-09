package org.figuramc.figura.ducks.extensions;

import java.nio.FloatBuffer;

public interface Matrix3fExtension {
    void figura$store(FloatBuffer buf);
    void figura$load(FloatBuffer buf);
}
