package org.figuramc.figura.mixin.sound;

import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.ducks.SubtitleOverlayAccessor;
import org.figuramc.figura.lua.api.sound.LuaSound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(SubtitleOverlay.class)
public class SubtitleOverlayMixin implements SubtitleOverlayAccessor {

    @Shadow @Final private List<SubtitleOverlay.Subtitle> subtitles;

    @Override
    public void figura$PlaySound(LuaSound sound) {
        Component text = sound.getSubtitleText();
        if (text == null)
            return;

        Vec3 pos = sound.getPos().asVec3();

        for (SubtitleOverlay.Subtitle subtitle : this.subtitles) {
            if (subtitle.getText().getString().equals(text.getString())) {
                subtitle.refresh(pos);
                return;
            }
        }
        this.subtitles.add(new SubtitleOverlay.Subtitle(text, sound.getAttenuation(), pos));
    }
}
