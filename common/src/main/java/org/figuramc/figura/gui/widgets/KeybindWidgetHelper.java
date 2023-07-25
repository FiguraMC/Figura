package org.figuramc.figura.gui.widgets;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.lua.api.keybind.FiguraKeybind;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.List;

public class KeybindWidgetHelper {

    private Component tooltip;
    private boolean vanillaConflict, avatarConflict;

    public void renderConflictBars(GuiGraphics gui, int x, int y, int width, int height) {
        // conflict bars
        if (vanillaConflict || avatarConflict) {
            if (avatarConflict) {
                gui.fill(x, y, x + width, y + height, ChatFormatting.YELLOW.getColor() | 0xFF000000);
                x -= width + 4;
            }
            if (vanillaConflict) {
                gui.fill(x, y, x + width, y + height, ChatFormatting.RED.getColor() | 0xFF000000);
            }
        }
    }

    public void renderTooltip() {
        if (tooltip != null)
            UIHelper.setTooltip(tooltip);
    }


    // -- texts -- // 



    // must be called before getText()
    public void setTooltip(FiguraKeybind keybind, List<FiguraKeybind> keyBindings) {
        MutableComponent text = Component.empty();

        // avatar conflicts
        Component avatar = checkForAvatarConflicts(keybind, keyBindings);
        boolean hasAvatarConflict = avatar != null && !avatar.getString().isBlank();
        if (hasAvatarConflict)
            text.append(avatar);

        // vanilla conflicts
        Component vanilla = checkForVanillaConflicts(keybind);
        if (vanilla != null && !vanilla.getString().isBlank()) {
            if (hasAvatarConflict)
                text.append("\n");
            text.append(vanilla);
        }

        // set tooltip
        setTooltipTail(text);
    }

    public void setTooltip(KeyMapping keybind) {
        MutableComponent text = Component.empty();

        // vanilla conflicts
        Component vanilla = checkForVanillaConflicts(keybind);
        if (vanilla != null && !vanilla.getString().isBlank())
            text.append(vanilla);

        // set tooltip
        setTooltipTail(text);
    }

    private void setTooltipTail(Component text) {
        if (vanillaConflict || avatarConflict) {
            this.tooltip = FiguraText.of("gui.duplicate_keybind", text);
        } else {
            this.tooltip = null;
        }
    }

    public Component getText(boolean isDefault, boolean isSelected, Component initialMessage) {
        // button message
        MutableComponent message = initialMessage.copy();
        if (isDefault || isSelected) message.withStyle(ChatFormatting.WHITE);
        else message.withStyle(FiguraMod.getAccentColor());

        if (isSelected) message.withStyle(ChatFormatting.UNDERLINE);

        if (this.avatarConflict || this.vanillaConflict) {
            MutableComponent left = Component.literal("[ ").withStyle(this.vanillaConflict ? ChatFormatting.RED : ChatFormatting.YELLOW);
            MutableComponent right = Component.literal(" ]").withStyle(this.avatarConflict ? ChatFormatting.YELLOW : ChatFormatting.RED);
            message = left.append(message).append(right);
        }

        // selected
        if (isSelected)
            message = Component.literal("> ").append(message).append(" <").withStyle(FiguraMod.getAccentColor());

        return message;
    }


    // -- avatar conflict -- // 


    public Component checkForAvatarConflicts(FiguraKeybind keybind, List<FiguraKeybind> keyBindings) {
        this.avatarConflict = false;

        int id = keybind.getID();
        if (id == -1)
            return null;

        MutableComponent message = Component.empty();
        for (FiguraKeybind keyBinding : keyBindings) {
            if (keyBinding != keybind && keyBinding.getID() == id) {
                this.avatarConflict = true;
                message.append(Component.literal("\n• ").withStyle(ChatFormatting.YELLOW).append(keyBinding.getName()));
            }
        }

        return message;
    }


    // -- vanilla conflict -- // 


    public Component checkForVanillaConflicts(FiguraKeybind keybind) {
        this.vanillaConflict = false;
        if (keybind.getID() == -1)
            return null;

        String keyName = keybind.getKey();
        MutableComponent message = Component.empty();
        for (KeyMapping key : Minecraft.getInstance().options.keyMappings) {
            if (key.saveString().equals(keyName)) {
                this.vanillaConflict = true;
                message.append(Component.literal("\n• ").withStyle(ChatFormatting.RED)
                        .append(Component.translatable(key.getCategory()))
                        .append(": ")
                        .append(Component.translatable(key.getName()))
                );
            }
        }

        return message;
    }

    public Component checkForVanillaConflicts(KeyMapping keybind) {
        this.vanillaConflict = false;
        if (keybind.isUnbound())
            return null;

        String keyName = keybind.saveString();
        MutableComponent message = Component.empty();
        for (KeyMapping key : Minecraft.getInstance().options.keyMappings) {
            if (key != keybind && key.saveString().equals(keyName)) {
                this.vanillaConflict = true;
                message.append(Component.literal("\n• ").withStyle(ChatFormatting.RED)
                        .append(Component.translatable(key.getCategory()))
                        .append(": ")
                        .append(Component.translatable(key.getName()))
                );
            }
        }

        return message;
    }
}
