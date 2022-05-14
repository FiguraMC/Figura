package org.moon.figura.gui.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.config.Config;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.gui.widgets.*;
import org.moon.figura.gui.widgets.lists.AvatarList;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraText;

public class WardrobeScreen extends AbstractPanelScreen {

    public static final Component TITLE = new FiguraText("gui.panels.title.wardrobe");

    private StatusWidget statusWidget;
    private AvatarInfoWidget avatarInfo;
    private TextWidget panic1, panic2;

    private TexturedButton upload, reload, delete;

    public WardrobeScreen(Screen parentScreen) {
        super(parentScreen, TITLE, 2);
    }

    @Override
    protected void init() {
        super.init();

        //screen
        int middle = width / 2;
        int third = this.width / 3 - 8;
        double guiScale = this.minecraft.getWindow().getGuiScale();
        double screenScale = Math.min(this.width, this.height) / 1018d;

        //model
        int modelBgSize = Math.min((int) ((512 / guiScale) * (screenScale * guiScale)), third);
        int entitySize = (int) ((192 / guiScale) * (screenScale * guiScale));

        // -- left -- //

        AvatarList avatarList = new AvatarList(4, 28, third, height - 36);
        addRenderableWidget(avatarList);

        // -- middle -- //

        int entityX = middle - modelBgSize / 2;
        int entityY = this.height / 2 - modelBgSize / 2;

        InteractableEntity entity = new InteractableEntity(entityX, entityY, modelBgSize, modelBgSize, entitySize, -15f, 30f, Minecraft.getInstance().player, this);
        addRenderableWidget(entity);

        int buttX = entity.x + entity.width / 2;
        int buttY = entity.y + entity.height + 4;

        //upload
        addRenderableWidget(upload = new TexturedButton(buttX - 48, buttY, 24, 24, 24, 0, 24, new FiguraIdentifier("textures/gui/upload.png"), 48, 48, new FiguraText("gui.wardrobe.upload.tooltip"), button -> {
            FiguraToast.sendToast(new TextComponent("lol nope").setStyle(Style.EMPTY.withColor(0xFFADAD)), FiguraToast.ToastType.DEFAULT);
        }));
        upload.active = false;

        //reload
        addRenderableWidget(reload = new TexturedButton(buttX - 12, buttY, 24, 24, 24, 0, 24, new FiguraIdentifier("textures/gui/reload.png"), 48, 48, new FiguraText("gui.wardrobe.reload.tooltip"), button -> {
            FiguraToast.sendToast(new TextComponent("lol nope").setStyle(Style.EMPTY.withColor(0xFFADAD)), FiguraToast.ToastType.DEFAULT);
        }));
        reload.active = false;

        //delete
        addRenderableWidget(delete = new TexturedButton(buttX + 24, buttY, 24, 24, 24, 0, 24, new FiguraIdentifier("textures/gui/delete.png"), 48, 48, new FiguraText("gui.wardrobe.delete.tooltip"), button -> {
            FiguraToast.sendToast(new TextComponent("lol nope").setStyle(Style.EMPTY.withColor(0xFFADAD)), FiguraToast.ToastType.DEFAULT);
        }));
        delete.active = false;

        statusWidget = new StatusWidget(entity.x + entity.width - 64, 0, 64);
        statusWidget.y = entity.y - statusWidget.height - 4;
        addRenderableOnly(statusWidget);

        // -- bottom -- //

        //version
        TextWidget version = new TextWidget(new FiguraText().append(" " + FiguraMod.VERSION).withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY), middle, this.height - 5, true);
        addRenderableOnly(version);

        int rightSide = Math.min(third, 134);

        //back
        addRenderableWidget(new TexturedButton(width - rightSide - 4, height - 24, rightSide, 20, new FiguraText("gui.back"), null,
            bx -> this.minecraft.setScreen(parentScreen)
        ));

        // -- right side -- //

        //hellp
        addRenderableWidget(new TexturedButton(
                this.width - rightSide - 4, 32, 24, 24,
                24, 0, 24,
                new FiguraIdentifier("textures/gui/help.png"),
                48, 48,
                new FiguraText("gui.help.tooltip"),
                bx -> this.minecraft.setScreen(new ConfirmLinkScreen((bl) -> {
                    if (bl) Util.getPlatform().openUri(FiguraMod.WIKI);
                    this.minecraft.setScreen(this);
                }, FiguraMod.WIKI, true))
        ));

        //sounds
        TexturedButton sounds = new TexturedButton(this.width - rightSide / 2 - 16, 32, 24, 24, 24, 0, 24, new FiguraIdentifier("textures/gui/sound.png"), 48, 48, new FiguraText("gui.wardrobe.sound.tooltip"), button -> {
            Minecraft.getInstance().setScreen(new SoundScreen(this));
        });
        sounds.active = false; //TODO
        addRenderableWidget(sounds);

        //keybinds
        TexturedButton keybinds = new TexturedButton(this.width - 28, 32, 24, 24, 24, 0, 24, new FiguraIdentifier("textures/gui/keybind.png"), 48, 48, new FiguraText("gui.wardrobe.keybind.tooltip"), button -> {
            Minecraft.getInstance().setScreen(new KeybindScreen(this));
        });
        addRenderableWidget(keybinds);

        //avatar metadata
        addRenderableOnly(avatarInfo = new AvatarInfoWidget(this.width - rightSide - 4, 64, rightSide));

        //panic warning - always added last, on top
        addRenderableOnly(panic1 = new TextWidget(new FiguraText("gui.panic.1").withStyle(ChatFormatting.YELLOW), middle, this.height - 23, true, 0));
        addRenderableOnly(panic2 = new TextWidget(new FiguraText("gui.panic.2", Config.PANIC_BUTTON.keyBind.getTranslatedKeyMessage()).withStyle(ChatFormatting.YELLOW), middle, this.height - 14, true, 0));
        panic1.setVisible(false);
        panic2.setVisible(false);
    }

    @Override
    public void tick() {
        //children tick
        super.tick();
        statusWidget.tick();
        avatarInfo.tick();

        //panic visible
        panic1.setVisible(AvatarManager.panic);
        panic2.setVisible(AvatarManager.panic);

        //backend buttons
        boolean backend = false; //TODO - fetch backend status
        upload.active = backend;
        reload.active = backend;
        delete.active = backend;
    }
}
