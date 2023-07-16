package org.figuramc.figura.entries;

import org.figuramc.figura.permissions.Permissions;

import java.util.Collection;

public interface FiguraPermissions {

    /**
     * @return a string with this mod's title, used in the permissions screen
     * the string will be attempted to be translated
     */
    String getTitle();

    /**
     * @return returns a list with all permissions implemented by this mod
     * insertion order matters
     */
    Collection<Permissions> getPermissions();
}
