package org.figuramc.figura.mixin.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.gui.screens.WardrobeScreen;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
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

    @Inject(method = "createPauseMenu", at = @At("RETURN"))
    private void createPauseMenuButton(CallbackInfo ci) {
        int x, y;

        int config = Configs.BUTTON_LOCATION.value;
        switch (config) {
            case 1 -> { // top left
                x = 4;
                y = 4;
            }
            case 2 -> {// top right
                x = this.width - 68;
                y = 4;
            }
            case 3 -> { // bottom left
                x = 4;
                y = this.height - 24;
            }
            case 4 -> { // bottom right
                x = this.width - 68;
                y = this.height - 24;
            }
            default -> { // icon
                x = this.width / 2 + 106;
                y = this.height / 4 + 80;
            }
        }

        if (config > 0) { // button
            addRenderableWidget(new Button(x, y, 64, 20, FiguraText.of(), null, btn -> this.minecraft.setScreen(new WardrobeScreen(this))) {
                @Override
                public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
                    ChatFormatting color;
                    if (this.isHoveredOrFocused()) {
                        color = ChatFormatting.AQUA;
                    } else if (AvatarManager.panic) {
                        color = ChatFormatting.GRAY;
                    } else {
                        color = ChatFormatting.WHITE;
                    }
                    setMessage(getMessage().copy().withStyle(color));

                    renderVanillaBackground(stack, mouseX, mouseY, delta);
                    super.renderButton(stack, mouseX, mouseY, delta);
                }

                @Override
                protected void renderDefaultTexture(PoseStack stack, float delta) {}
            });
        } else { // icon
            addRenderableWidget(new Button(x, y, 20, 20, 0, 0, 20, FIGURA_ICON, 60, 20, null, btn -> this.minecraft.setScreen(new WardrobeScreen(this))) {
                @Override
                public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
                    renderVanillaBackground(stack, mouseX, mouseY, delta);
                    super.renderButton(stack, mouseX, mouseY, delta);
                }

                @Override
                protected int getU() {
                    int u = super.getU();
                    if (u == 1 && AvatarManager.panic)
                        return 0;
                    return u;
                }
            });
        }
    }
}
