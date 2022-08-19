package org.moon.figura.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.FiguraMod;
import org.moon.figura.config.Config;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.ui.UIHelper;

public class FiguraToast implements Toast {

    private final Component title;
    private final Component message;
    private final ToastType type;

    private long startTime;
    private boolean justUpdated;

    public FiguraToast(Component title, Component message, ToastType type) {
        this.title = TextComponent.EMPTY.copy().setStyle(type.style).append(title);
        this.message = message;
        this.type = type;
    }

    @Override
    public Visibility render(PoseStack stack, ToastComponent component, long startTime) {
        long timeDiff = startTime - this.startTime;

        if (this.justUpdated) {
            this.startTime = startTime;
            this.justUpdated = false;
        }

        UIHelper.setupTexture(type.texture);
        UIHelper.blit(stack, 0, 0, 0f, (int) ((FiguraMod.ticks / 5f) % type.frames + 1) * 32f, width(), height(), 160, 32 * type.frames);

        Font font = component.getMinecraft().font;
        if (this.message == null) {
            font.draw(stack, this.title, 31, 12, 0xFFFFFF);
        } else {
            font.draw(stack, this.title, 31, 7, 0xFFFFFF);
            font.draw(stack, this.message, 31, 18, 0xFFFFFF);
        }

        return timeDiff < 5000 ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
    }

    //new toast
    public static void sendToast(Object title) {
        sendToast(title, TextComponent.EMPTY.copy());
    }

    public static void sendToast(Object title, ToastType type) {
        sendToast(title, TextComponent.EMPTY.copy(), type);
    }

    public static void sendToast(Object title, Object message) {
        sendToast(title, message, ToastType.DEFAULT);
    }

    public static void sendToast(Object title, Object message, ToastType type) {
        Component text = title instanceof Component t ? t : new TranslatableComponent(title.toString());
        Component text2 = message instanceof Component m ? m : new TranslatableComponent(message.toString());

        if (type == ToastType.DEFAULT && Config.EASTER_EGGS.asBool()) {
            if (FiguraMod.CHEESE_DAY || Math.random() < 0.0001)
                type = ToastType.CHEESE;
            else if (FiguraMod.DATE.getDayOfMonth() == 21 && FiguraMod.DATE.getMonthValue() == 9)
                type = ToastType.FRAN;
        }

        ToastComponent toasts = Minecraft.getInstance().getToasts();
        toasts.clear();
        toasts.addToast(new FiguraToast(text, text2, type));
    }

    public enum ToastType {
        DEFAULT(new FiguraIdentifier("textures/gui/toast/default.png"), 4, 0x55FFFF),
        WARNING(new FiguraIdentifier("textures/gui/toast/warning.png"), 4, 0xFFFF00),
        ERROR(new FiguraIdentifier("textures/gui/toast/error.png"), 4, 0xFF0000),
        CHEESE(new FiguraIdentifier("textures/gui/toast/cheese.png"), 1, ColorUtils.Colors.CHEESE.hex),
        FRAN(new FiguraIdentifier("textures/gui/toast/fran.png"), 4, ColorUtils.Colors.FRAN_PINK.hex);

        private final ResourceLocation texture;
        private final int frames;
        private final Style style;

        ToastType(ResourceLocation texture, int frames, int color) {
            this.texture = texture;
            this.frames = frames;
            this.style = Style.EMPTY.withColor(color);
        }
    }
}
