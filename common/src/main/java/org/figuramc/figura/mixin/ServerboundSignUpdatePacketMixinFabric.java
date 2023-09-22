package org.figuramc.figura.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.lua.api.event.EventsAPI;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundSignUpdatePacket.class)
public class ServerboundSignUpdatePacketMixinFabric {
    @Shadow @Final private String[] lines;

    @Inject(at = @At(value = "TAIL"), method = "Lnet/minecraft/network/protocol/game/ServerboundSignUpdatePacket;<init>(Lnet/minecraft/core/BlockPos;ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V")
    void extextura$runSignUpdateListener(BlockPos pos, boolean front, String line1, String line2, String line3, String line4, CallbackInfo ci) {
        if (AvatarManager.panic || !Configs.CHAT_MESSAGES.value) return;
        var avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar == null) return;
        avatar.signUpdateEvent(pos, this.lines, front);
    }
}
