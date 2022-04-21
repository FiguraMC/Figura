package org.moon.figura;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.avatars.providers.LocalAvatarFetcher;
import org.moon.figura.avatars.providers.LocalAvatarLoader;
import org.moon.figura.testing.LuaTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

public class FiguraMod implements ClientModInitializer {

    public static final String MOD_ID = "figura";
    public static final String VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).get().getMetadata().getVersion().getFriendlyString();
    public static final boolean CHEESE_DAY = LocalDate.now().getDayOfMonth() == 1 && LocalDate.now().getMonthValue() == 4;
    public static final Path GAME_DIR = FabricLoader.getInstance().getGameDir();
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID.substring(0, 1).toUpperCase() + MOD_ID.substring(1));

    public static int ticks = 0;

    @Override
    public void onInitializeClient() {
        //register fabric events
        ClientTickEvents.END_CLIENT_TICK.register(FiguraMod::tick);

        //TODO - test
        LuaTest.test();

        try {
            LocalAvatarFetcher.load();
            if (!LocalAvatarFetcher.ALL_AVATARS.isEmpty()) {
                AvatarManager.loadLocalAvatar(LocalAvatarFetcher.ALL_AVATARS.get(0).getPath());
                //LocalAvatarLoader.saveNbt();
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    public static void tick(Minecraft client) {
        LocalAvatarLoader.tickWatchedKey();
        ticks++;
    }

    // -- Helper Functions -- //

    //mod root directory
    public static Path getFiguraDirectory() {
        Path p = GAME_DIR.normalize().resolve(MOD_ID);
        try {
            Files.createDirectories(p);
        } catch (Exception e) {
            LOGGER.error("Failed to create the main Figura directory", e);
        }

        return p;
    }

    //get local player uuid
    public static UUID getLocalPlayerUUID() {
        return Minecraft.getInstance().getUser().getGameProfile().getId();
    }
}
