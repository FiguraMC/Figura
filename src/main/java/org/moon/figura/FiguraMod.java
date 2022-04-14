package org.moon.figura;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.moon.figura.testing.LuaTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

public class FiguraMod implements ClientModInitializer {

    public static final String MOD_ID = "figura";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final boolean CHEESE_DAY = LocalDate.now().getDayOfMonth() == 1 && LocalDate.now().getMonthValue() == 4;
    public static final Path GAME_DIR = FabricLoader.getInstance().getGameDir();

    public static int ticks = 0;

    @Override
    public void onInitializeClient() {
        //register fabric events
        ClientTickEvents.END_CLIENT_TICK.register(FiguraMod::tick);

        //TODO - test
        LuaTest.vectorTest();

        /*
        try {
            File f = new File("C:/Users/Fran/Desktop/haha cu-be.bbmodel");
            FileInputStream fs = new FileInputStream(f);
            String json = new String(fs.readAllBytes());
            NbtCompound nbt = BlockbenchModelParser.parseModel(json);

            System.out.println(nbt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }

    public static void tick(MinecraftClient client) {
        ticks++;
    }

    // -- Helper Functions -- //

    //directories
    public static Path getFiguraDirectory() {
        Path p = GAME_DIR.normalize().resolve(MOD_ID);
        try {
            Files.createDirectories(p);
        } catch (Exception e) {
            LOGGER.error("Failed to create the main Figura directory");
            LOGGER.error(e);
        }

        return p;
    }

    public static Path getCacheDirectory() {
        Path p = getFiguraDirectory().resolve("cache");
        try {
            Files.createDirectories(p);
        } catch (IOException e) {
            LOGGER.error("Failed to create cache directory");
            LOGGER.error(e);
        }

        return p;
    }

    public static Path getLocalAvatarDirectory() {
        Path p = getFiguraDirectory().resolve("avatars");
        try {
            Files.createDirectories(p);
        } catch (IOException e) {
            LOGGER.error("Failed to create avatar directory");
            LOGGER.error(e);
        }

        return p;
    }
}
