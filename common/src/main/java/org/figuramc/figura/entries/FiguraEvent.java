package org.figuramc.figura.entries;

import com.mojang.datafixers.util.Pair;
import org.figuramc.figura.lua.api.event.LuaEvent;

import java.util.Collection;

public interface FiguraEvent {

    /**
     * @return a string of this mod's ID, case-insensitive
     * the ID will be prefixed in the event's name with a ., to avoid
     * conflicts with events added by others
     */
    String getID();

    /**
     * @return returns a collection of a pair of the event's name and the event itself
     * Figura will just register these events and make them available to users for you,
     * but it will not call them, that is up to you as a developer.
     */
    Collection<Pair<String, LuaEvent>> getEvents();
}
