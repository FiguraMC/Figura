package org.moon.figura.gui.screens;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.moon.figura.FiguraMod;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.gui.widgets.Button;
import org.moon.figura.gui.widgets.Label;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.TextUtils;

public class FiguraConfirmScreen extends AbstractPanelScreen {

    private final BooleanConsumer callback;
    private final Component message;

    public FiguraConfirmScreen(BooleanConsumer callback, Object title, Object message, Screen parentScreen) {
        super(parentScreen, title instanceof Component c ? c : Component.literal(title.toString()));
        this.callback = callback;
        this.message = message instanceof Component c ? c : Component.literal(message.toString()).withStyle(FiguraMod.getAccentColor());
    }

    @Override
    protected void init() {
        super.init();
        removeWidget(panels);

        //labels
        int center = this.width / 2;
        Label title = new Label(this.getTitle(), center, 0, width - 8, true, TextUtils.Alignment.CENTER);
        Label message = new Label(this.message, center, 0, width - 8, true, TextUtils.Alignment.CENTER);

        int spacing = (minecraft.font.lineHeight + 1) * 2;
        int totalWidth = message.getHeight() + title.getHeight() + spacing * 2 + 20;
        int y = (this.height - totalWidth) / 2;

        title.y = Math.max(y, 4);
        message.y = title.y + title.getHeight() + spacing;

        addRenderableWidget(title);
        addRenderableWidget(message);

        //buttons
        addButtons(center, Math.min(message.y + message.getHeight() + spacing, this.height - 24));
    }

    protected void addButtons(int x, int y) {
        this.addRenderableWidget(new Button(x - 130, y, 128, 20, CommonComponents.GUI_YES, null, button -> run(true)));
        this.addRenderableWidget(new Button(x + 2, y, 128, 20, CommonComponents.GUI_NO, null, button -> run(false)));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            run(false);
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    protected void run(boolean bool) {
        this.callback.accept(bool);
        this.minecraft.setScreen(parentScreen);
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), this.message);
    }

    public static class FiguraConfirmLinkScreen extends FiguraConfirmScreen {

        private final String url;

        public FiguraConfirmLinkScreen(BooleanConsumer callback, String link, Screen parentScreen) {
            super(callback, Component.translatable("chat.link.confirmTrusted"), link, parentScreen);
            this.url = link;
        }

        @Override
        protected void addButtons(int x, int y) {
            this.addRenderableWidget(new Button(x - 148, y, 96, 20, Component.translatable("chat.link.open"), null, button -> run(true)));
            this.addRenderableWidget(new Button(x - 48, y, 96, 20, Component.translatable("chat.copy"), null, button -> {
                this.minecraft.keyboardHandler.setClipboard(this.url);
                FiguraToast.sendToast(FiguraText.of("toast.clipboard"));
                run(false);
            }));
            this.addRenderableWidget(new Button(x + 52, y, 96, 20, CommonComponents.GUI_CANCEL, null, button -> run(false)));
        }
    }
}
