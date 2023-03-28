package org.moon.figura.mixin.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.config.Configs;
import org.moon.figura.gui.screens.WardrobeScreen;
import org.moon.figura.gui.widgets.Button;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PauseScreen.class)
public class PauseScreenMixin extends Screen {

    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Unique
    private static final ResourceLocation FIGURA_ICON = new FiguraIdentifier("textures/gui/icon.png");

    @Unique
    private LayoutElement lanButton;

    @Inject(method = "createPauseMenu",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isLocalServer()Z"),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;hasSingleplayerServer()Z"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isLocalServer()Z")
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void saveLanButton(CallbackInfo ci, GridLayout gridLayout, GridLayout.RowHelper rowHelper) {
        gridLayout.visitChildren(element -> lanButton = element);
    }

    @Inject(method = "createPauseMenu", at = @At("RETURN"))
    private void createPauseMenuButton(CallbackInfo ci) {
        int x, y;

        int config = Configs.BUTTON_LOCATION.value;
        switch (config) {
            case 1 -> { //top left
                x = 4;
                y = 4;
            }
            case 2 -> {//top right
                x = this.width - 68;
                y = 4;
            }
            case 3 -> { //bottom left
                x = 4;
                y = this.height - 24;
            }
            case 4 -> { //bottom right
                x = this.width - 68;
                y = this.height - 24;
            }
            default -> { //icon
                x = lanButton == null ? this.width / 2 + 106 : lanButton.getX() + lanButton.getWidth() + 4;
                y = lanButton == null ? this.height / 4 + 80 : lanButton.getY();
            }
        }

        if (config > 0) { //button
            addRenderableWidget(Button.builder(FiguraText.of(), btn -> this.minecraft.setScreen(new WardrobeScreen(this))).bounds(x, y, 64, 20).build());
        } else { //icon
            addRenderableWidget(new Button(x, y, 20, 20, 0, 0, 20, FIGURA_ICON, 60, 20, null, btn -> this.minecraft.setScreen(new WardrobeScreen(this))) {
                @Override
                public void renderWidget(PoseStack stack, int mouseX, int mouseY, float delta) {
                    renderVanillaBackground(stack, mouseX, mouseY, delta);
                    super.renderWidget(stack, mouseX, mouseY, delta);
                }

                @Override
                protected int getUVStatus() {
                    int uv = super.getUVStatus();
                    if (uv == 1 && AvatarManager.panic)
                        return 0;
                    return uv;
                }
            });
        }
    }
}
