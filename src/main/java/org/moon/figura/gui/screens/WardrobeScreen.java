package org.moon.figura.gui.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.avatars.providers.LocalAvatarFetcher;
import org.moon.figura.avatars.providers.LocalAvatarLoader;
import org.moon.figura.backend.NetworkManager;
import org.moon.figura.commands.FiguraLinkCommand;
import org.moon.figura.config.Config;
import org.moon.figura.gui.widgets.*;
import org.moon.figura.gui.widgets.lists.AvatarList;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraText;

public class WardrobeScreen extends AbstractPanelScreen {

    public static final Component TITLE = FiguraText.of("gui.panels.title.wardrobe");

    private StatusWidget statusWidget;
    private AvatarInfoWidget avatarInfo;
    private Label panic;

    private TexturedButton upload, delete;

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
        addRenderableWidget(upload = new TexturedButton(buttX - 48, buttY, 24, 24, 0, 0, 24, new FiguraIdentifier("textures/gui/upload.png"), 72, 24, FiguraText.of("gui.wardrobe.upload.tooltip"), button -> {
            Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
            try {
                LocalAvatarLoader.loadAvatar(null);
            } catch (Exception ignored) {}
            NetworkManager.uploadAvatar(avatar, null);
            AvatarList.selectedEntry = null;
        }));
        upload.active = false;

        //reload
        addRenderableWidget(new TexturedButton(buttX - 12, buttY, 24, 24, 0, 0, 24, new FiguraIdentifier("textures/gui/reload.png"), 72, 24, FiguraText.of("gui.wardrobe.reload.tooltip"), button -> {
            AvatarManager.clearAvatar(FiguraMod.getLocalPlayerUUID());
            AvatarManager.localUploaded = true;
            NetworkManager.assertBackend();
            AvatarList.selectedEntry = null;
        }));

        //delete
        addRenderableWidget(delete = new TexturedButton(buttX + 24, buttY, 24, 24, 0, 0, 24, new FiguraIdentifier("textures/gui/delete.png"), 72, 24, FiguraText.of("gui.wardrobe.delete.tooltip"), button ->
                NetworkManager.deleteAvatar(null))
        );

        statusWidget = new StatusWidget(entity.x + entity.width - 64, 0, 64);
        statusWidget.y = entity.y - statusWidget.height - 4;
        addRenderableOnly(statusWidget);

        // -- bottom -- //

        //version
        Label version = new Label(FiguraText.of().append(" " + FiguraMod.VERSION).withStyle(ChatFormatting.ITALIC), middle, this.height - 5, true);
        addRenderableOnly(version);
        version.setColor(0x33FFFFFF);

        int rightSide = Math.min(third, 134);

        //back
        TexturedButton back = new TexturedButton(width - rightSide - 4, height - 24, rightSide, 20, FiguraText.of("gui.done"), null,
                bx -> this.minecraft.setScreen(parentScreen)
        );
        addRenderableWidget(back);

        // -- right side -- //

        //hellp
        addRenderableWidget(new TexturedButton(
                this.width - rightSide - 4, 32, 24, 24,
                0, 0, 24,
                new FiguraIdentifier("textures/gui/help.png"),
                72, 24,
                FiguraText.of("gui.help.tooltip"),
                bx -> this.minecraft.setScreen(new ConfirmLinkScreen((bl) -> {
                    if (bl) Util.getPlatform().openUri(FiguraLinkCommand.LINK.WIKI.url);
                    this.minecraft.setScreen(this);
                }, FiguraLinkCommand.LINK.WIKI.url, true))
        ));

        //sounds
        TexturedButton sounds = new TexturedButton(this.width - rightSide / 2 - 16, 32, 24, 24, 0, 0, 24, new FiguraIdentifier("textures/gui/sound.png"), 72, 24, FiguraText.of("gui.wardrobe.sound.tooltip"),
                button -> Minecraft.getInstance().setScreen(new SoundScreen(this))
        );
        addRenderableWidget(sounds);

        //keybinds
        TexturedButton keybinds = new TexturedButton(this.width - 28, 32, 24, 24, 0, 0, 24, new FiguraIdentifier("textures/gui/keybind.png"), 72, 24, FiguraText.of("gui.wardrobe.keybind.tooltip"),
                button -> Minecraft.getInstance().setScreen(new KeybindScreen(this))
        );
        addRenderableWidget(keybinds);

        //avatar metadata
        addRenderableOnly(avatarInfo = new AvatarInfoWidget(this.width - rightSide - 4, 64, rightSide, back.y - 68));

        //panic warning - always added last, on top
        addRenderableOnly(panic = new Label(FiguraText.of("gui.panic.1").withStyle(ChatFormatting.YELLOW).append("\n").append(FiguraText.of("gui.panic.2", Config.PANIC_BUTTON.keyBind.getTranslatedKeyMessage())),
                middle, this.height - 23, true, 0)
        );
        panic.setVisible(false);
    }

    @Override
    public void tick() {
        //children tick
        super.tick();
        statusWidget.tick();
        avatarInfo.tick();

        //panic visible
        panic.setVisible(AvatarManager.panic);

        //backend buttons
        Avatar avatar;
        boolean backend = NetworkManager.backendStatus == 3;
        upload.active = backend && NetworkManager.canUpload() && !AvatarManager.localUploaded && (avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID())) != null && avatar.nbt != null;
        delete.active = backend;
    }

    @Override
    public void removed() {
        LocalAvatarFetcher.save();
        super.removed();
    }
}
