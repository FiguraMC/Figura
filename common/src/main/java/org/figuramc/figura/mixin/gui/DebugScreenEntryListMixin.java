package org.figuramc.figura.mixin.gui;

import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.FiguraMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(DebugScreenEntryList.class)
public class DebugScreenEntryListMixin {
    @Shadow
    private Map<Identifier, DebugScreenEntryStatus> allStatuses;

    private void setFullDebugStatuses() {
        this.allStatuses.put(FiguraMod.FIGURA_DEBUG_KEY, DebugScreenEntryStatus.IN_OVERLAY);
    }

    @Inject(method = "loadDefaultProfile", at = @At(value = "FIELD", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/gui/components/debug/DebugScreenEntryList;allStatuses:Ljava/util/Map;"))
    private void injectLoadDefaultProfile(CallbackInfo ci) {
        this.setFullDebugStatuses();
    }

    @Inject(method = "loadProfile", at = @At(value = "FIELD", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/gui/components/debug/DebugScreenEntryList;allStatuses:Ljava/util/Map;"))
    private void injectLoadProfile(DebugScreenProfile debugScreenProfile, CallbackInfo ci) {
        this.setFullDebugStatuses();
    }

    @Inject(method = "rebuildCurrentList", at = @At("HEAD"))
    private void injectSodiumSettings(CallbackInfo ci) {
        if (!this.allStatuses.containsKey(FiguraMod.FIGURA_DEBUG_KEY)) {
            this.allStatuses.put(FiguraMod.FIGURA_DEBUG_KEY, DebugScreenEntryStatus.IN_OVERLAY);
        }
    }
}
