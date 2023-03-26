package org.moon.figura.mixin.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.config.ConfigManager;
import org.moon.figura.config.Configs;
import org.moon.figura.gui.screens.WardrobeScreen;
import org.moon.figura.gui.widgets.Button;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
                x = this.width / 2 + 106;
                y = this.height / 4 + 80;
            }
        }

        if (config > 0) { //button
            if (ConfigManager.modmenuShift())
                y -= 12;

            addRenderableWidget(new net.minecraft.client.gui.components.Button(x, y, 64, 20, new FiguraText(),
                    btn -> this.minecraft.setScreen(new WardrobeScreen(this))));
        } else { //icon
            addRenderableWidget(new Button(x, y, 20, 20, 0, 0, 20, FIGURA_ICON, 60, 20, null, btn -> this.minecraft.setScreen(new WardrobeScreen(this))) {
                @Override
                public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
                    renderVanillaBackground(stack, mouseX, mouseY, delta);
                    super.renderButton(stack, mouseX, mouseY, delta);
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
