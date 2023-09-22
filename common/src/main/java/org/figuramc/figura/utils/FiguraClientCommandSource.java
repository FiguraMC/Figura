package org.figuramc.figura.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;


public interface FiguraClientCommandSource extends SharedSuggestionProvider {
    /**
     * Sends a feedback message to the player.
     *
     * @param message the feedback message
     */
    void figura$sendFeedback(Component message);

    /**
     * Sends an error message to the player.
     *
     * @param message the error message
     */
    void figura$sendError(Component message);

    /**
     * Gets the client instance used to run the command.
     *
     * @return the client
     */
    Minecraft figura$getClient();

    /**
     * Gets the player that used the command.
     *
     * @return the player
     */
    LocalPlayer figura$getPlayer();

    /**
     * Gets the entity that used the command.
     *
     * @return the entity
     */
    default Entity figura$getEntity() {
        return figura$getPlayer();
    }

    /**
     * Gets the position from where the command has been executed.
     *
     * @return the position
     */
    default Vec3 figura$getPosition() {
        return figura$getPlayer().position();
    }

    /**
     * Gets the rotation of the entity that used the command.
     *
     * @return the rotation
     */
    default Vec2 figura$getRotation() {
        return figura$getPlayer().getRotationVector();
    }

    /**
     * Gets the world where the player used the command.
     *
     * @return the world
     */
    ClientLevel figura$getWorld();

    /**
     * Gets the meta property under {@code key} that was assigned to this source.
     *
     * <p>This method should return the same result for every call with the same {@code key}.
     *
     * @param key the meta key
     * @return the meta
     */
    default Object figura$getMeta(String key) {
        return null;
    }
}