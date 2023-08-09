package org.figuramc.figura.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.figuramc.figura.font.Emojis;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Display.TextDisplay.class)
public abstract class TextDisplayMixin extends Display {

    @Shadow @Final private static EntityDataAccessor<Component> DATA_TEXT_ID;

    public TextDisplayMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "getText", at = @At("HEAD"), cancellable = true)
    public void applyEmojis(CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(Emojis.applyEmojis(this.entityData.get(DATA_TEXT_ID)));
    }

}
