package org.figuramc.figura.ducks;

import com.mojang.blaze3d.audio.Library;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import org.figuramc.figura.lua.api.sound.LuaSound;

import java.util.Map;
import java.util.UUID;

public interface TextureAtlasAccessor {
    Map<ResourceLocation, TextureAtlasSprite> getTexturesByName();

    int getWidth();

    int getHeight();
}

