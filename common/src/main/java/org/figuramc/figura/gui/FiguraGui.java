package org.figuramc.figura.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class FiguraGui {

    public static void onRender(GuiGraphics guiGraphics, float tickDelta, CallbackInfo ci) {
        if (AvatarManager.panic)
            return;

        FiguraMod.pushProfiler(FiguraMod.MOD_ID);

        // render popup menu below everything, as if it were in the world
        FiguraMod.pushProfiler("popupMenu");
        PopupMenu.render(guiGraphics);
        FiguraMod.popProfiler();

        // get avatar
        Entity entity = Minecraft.getInstance().getCameraEntity();
        Avatar avatar = entity == null ? null : AvatarManager.getAvatar(entity);

        if (avatar != null) {
            // hud parent type
            avatar.hudRender(guiGraphics.pose(), Minecraft.getInstance().renderBuffers().bufferSource(), entity, tickDelta);

            // hud hidden by script
            if (avatar.luaRuntime != null && !avatar.luaRuntime.renderer.renderHUD) {
                // render figura overlays
                renderOverlays(guiGraphics);
                // cancel this method
                ci.cancel();
            }
        }

        FiguraMod.popProfiler();
    }

    public static void renderOverlays(GuiGraphics guiGraphics) {
        FiguraMod.pushProfiler(FiguraMod.MOD_ID);

        // render paperdoll
        FiguraMod.pushProfiler("paperdoll");
        PaperDoll.render(guiGraphics, false);

        // render wheel
        FiguraMod.popPushProfiler("actionWheel");
        ActionWheel.render(guiGraphics);

        FiguraMod.popProfiler(2);
    }
}
