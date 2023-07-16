package org.moon.figura.entries.forge;

import org.moon.figura.FiguraMod;

import java.util.HashSet;
import java.util.Set;

public class EntryPointManagerImpl {
    public static <T> Set<T> load(String name, Class<T> clazz) {
        Set<T> ret = new HashSet<>();
        //TODO: Currently stubbed, need to provide a way to load an annotation or entrypoint on Forge, to be implemented in a future PR
        /*
        for (EntrypointContainer<T> entrypoint : FabricLoader.getInstance().getEntrypointContainers(name, clazz)) {
            ModMetadata metadata = entrypoint.getProvider().getMetadata();
            String modId = metadata.getId();
            try {
                ret.add(entrypoint.getEntrypoint());
            } catch (Exception e) {
                FiguraMod.LOGGER.error("Failed to load entrypoint of mod {}", modId, e);
            }
        }*/

        return ret;
    }
}
