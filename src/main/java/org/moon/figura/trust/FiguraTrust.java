package org.moon.figura.trust;

import java.util.Collection;

public interface FiguraTrust {

    /**
     * @return a string with this mod's title, used in the trust screen
     * the string will be attempted to be translated
     */
    String getTitle();

    /**
     * @return returns a list with all trust settings implemented by this mod
     * insertion order matters
     */
    Collection<Trust> getTrusts();
}
