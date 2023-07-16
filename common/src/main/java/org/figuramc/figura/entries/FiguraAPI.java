package org.figuramc.figura.entries;

import org.figuramc.figura.avatar.Avatar;

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
     * @return a list containing all your classes that are documented
     */
    Collection<Class<?>> getDocsClasses();
}
