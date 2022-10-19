package org.moon.figura.lua;

import org.moon.figura.avatar.Avatar;
import org.moon.figura.trust.Trust;

import java.util.Collection;

public interface FiguraAPI {

    /**
     * @param avatar the avatar instance that is loading this API
     * @return your API instance
     */
    FiguraAPI build(Avatar avatar);

    /**
     * @return the global field from where your API will be accessible
     */
    String getName();

    /**
     * @return a list containing all your whitelisted classes
     */
    Collection<Class<?>> getWhitelistedClasses();

    /**
     * @return a list with all trust settings implemented from this API
     */
    default Collection<Trust> customTrust() {
        return null;
    }
}
