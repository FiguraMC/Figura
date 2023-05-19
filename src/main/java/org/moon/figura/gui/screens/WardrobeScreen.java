package org.moon.figura.gui.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.avatar.local.LocalAvatarFetcher;
import org.moon.figura.avatar.local.LocalAvatarLoader;
import org.moon.figura.backend2.NetworkStuff;
import org.moon.figura.commands.FiguraLinkCommand;
import org.moon.figura.config.Configs;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.gui.widgets.*;
import org.moon.figura.gui.widgets.lists.AvatarList;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.ui.UIHelper;

import java.nio.file.Path;
import java.util.List;

public class WardrobeScreen extends AbstractPanelScreen {

    private LoadingErrorWidget loadingErrorWidget;
    private StatusWidget statusWidget;
    private AvatarInfoWidget avatarInfo;
    private Label panic;

    private Button upload, delete;

    public WardrobeScreen(Screen parentScreen) {
        super(parentScreen, new FiguraText("gui.panels.title.wardrobe"));
    }

    @Override
    protected void init() {
        super.init();

        //screen
        Minecraft minecraft = Minecraft.getInstance();
        int middle = width / 2;
        int panels = Math.min(width / 3, 256) - 8;

        int modelBgSize = Math.min(width - panels * 2 - 16, height - 96);
        panels = Math.max((width - modelBgSize) / 2 - 8, panels);

        // -- left -- //

        AvatarList avatarList = new AvatarList(4, 28, panels, height - 32, this);
        addRenderableWidget(avatarList);

        // -- middle -- //

        //model
        int entitySize = 11 * modelBgSize / 29;
        int entityX = middle - modelBgSize / 2;
        int entityY = this.height / 2 - modelBgSize / 2;

        EntityPreview entity = new EntityPreview(entityX, entityY, modelBgSize, modelBgSize, entitySize, -15f, 30f, minecraft.player, this);
        addRenderableWidget(entity);

        int buttX = entity.getX() + entity.getWidth() / 2;
        int buttY = entity.getY() + entity.getHeight() + 4;

        //upload
        addRenderableWidget(upload = new Button(buttX - 48, buttY, 24, 24, 0, 0, 24, new FiguraIdentifier("textures/gui/upload.png"), 72, 24, new FiguraText("gui.wardrobe.upload.tooltip"), button -> {
            Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
            try {
                LocalAvatarLoader.loadAvatar(null, null);
            } catch (Exception ignored) {}
            NetworkStuff.uploadAvatar(avatar);
            AvatarList.selectedEntry = null;
        }));
        upload.setActive(false);

        //reload
        addRenderableWidget(new Button(buttX - 12, buttY, 24, 24, 0, 0, 24, new FiguraIdentifier("textures/gui/reload.png"), 72, 24, new FiguraText("gui.wardrobe.reload.tooltip"), button -> {
            AvatarManager.clearAvatars(FiguraMod.getLocalPlayerUUID());
            try {
                LocalAvatarLoader.loadAvatar(null, null);
            } catch (Exception ignored) {}
            AvatarManager.localUploaded = true;
            NetworkStuff.auth();
            AvatarList.selectedEntry = null;
        }));

        //delete
        addRenderableWidget(delete = new Button(buttX + 24, buttY, 24, 24, 0, 0, 24, new FiguraIdentifier("textures/gui/delete.png"), 72, 24, new FiguraText("gui.wardrobe.delete.tooltip"), button ->
                NetworkStuff.deleteAvatar(null))
        );

        statusWidget = new StatusWidget(entity.getX() + entity.getWidth() - 64, 0, 64);
        statusWidget.setY(entity.getY() - statusWidget.getHeight() - 4);
        addRenderableOnly(statusWidget);

        addRenderableOnly(loadingErrorWidget = new LoadingErrorWidget(statusWidget.getX() - 18, statusWidget.getY(), 14));

        // -- bottom -- //

        //version
        MutableComponent versionText = new FiguraText().append(" " + FiguraMod.VERSION.noBuildString()).withStyle(ChatFormatting.ITALIC);
        boolean oldVersion = NetworkStuff.latestVersion != null && NetworkStuff.latestVersion.compareTo(FiguraMod.VERSION) > 0;
        if (oldVersion) {
            versionText
                    .append(" ")
                    .append(new TextComponent("=")
                            .withStyle(Style.EMPTY
                                    .withFont(UIHelper.UI_FONT)
                                    .withItalic(false)
                                    .applyLegacyFormat(ChatFormatting.WHITE)
                            ))
                    .withStyle(Style.EMPTY
                            .applyFormat(ChatFormatting.AQUA)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    new FiguraText("gui.new_version.tooltip", new TextComponent(NetworkStuff.latestVersion.toString()).withStyle(ChatFormatting.GREEN))
                                            .append("\n")
                                            .append(new FiguraText("gui.new_version_download.tooltip"))
                            ))
                            .withClickEvent(new TextUtils.FiguraClickEvent(UIHelper.openURL(FiguraLinkCommand.LINK.MODRINTH.url + "/versions")))
                    );
        }

        Label version = new Label(versionText, middle, this.height - 4, TextUtils.Alignment.CENTER);
        addRenderableWidget(version);
        if (!oldVersion) version.setAlpha(0x33);
        version.setY(version.getRawY() - version.getHeight());

        int rightSide = Math.min(panels, 134);

        //back
        Button back = new Button(width - rightSide - 4, height - 24, rightSide, 20, new FiguraText("gui.done"), null, bx -> onClose());
        addRenderableWidget(back);

        // -- right side -- //

        rightSide = panels / 2 + 52;

        //avatar settings
        Button avatarSettings;
        addRenderableWidget(avatarSettings = new Button(
                this.width - rightSide, 28, 24, 24,
                0, 0, 24,
                new FiguraIdentifier("textures/gui/avatar_settings.png"),
                72, 24,
                new FiguraText("gui.avatar_settings.tooltip"),
                bx -> {}
        ));
        avatarSettings.setActive(false);

        //sounds
        Button sounds = new Button(this.width - rightSide + 36, 28, 24, 24, 0, 0, 24, new FiguraIdentifier("textures/gui/sound.png"), 72, 24, new FiguraText("gui.wardrobe.sound.tooltip"),
                button -> Minecraft.getInstance().setScreen(new SoundScreen(this))
        );
        addRenderableWidget(sounds);

        //keybinds
        Button keybinds = new Button(this.width - rightSide + 72, 28, 24, 24, 0, 0, 24, new FiguraIdentifier("textures/gui/keybind.png"), 72, 24, new FiguraText("gui.wardrobe.keybind.tooltip"),
                button -> Minecraft.getInstance().setScreen(new KeybindScreen(this))
        );
        addRenderableWidget(keybinds);

        //avatar metadata
        addRenderableOnly(avatarInfo = new AvatarInfoWidget(this.width - panels - 4, 56, panels, back.y - 60));

        //panic warning - always added last, on top
        addRenderableWidget(panic = new Label(
                new FiguraText("gui.panic", Configs.PANIC_BUTTON.keyBind.getTranslatedKeyMessage()).withStyle(ChatFormatting.YELLOW),
                middle, version.getRawY(), TextUtils.Alignment.CENTER, 0)
        );
        panic.setY(panic.getRawY() - panic.getHeight());
        panic.setVisible(false);
    }

    @Override
    public void tick() {
        //children tick
        super.tick();
        loadingErrorWidget.tick();
        statusWidget.tick();
        avatarInfo.tick();

        //panic visible
        panic.setVisible(AvatarManager.panic);

        //backend buttons
        Avatar avatar;
        boolean backend = NetworkStuff.backendStatus == 3;
        upload.setActive(NetworkStuff.canUpload() && !AvatarManager.localUploaded && (avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID())) != null && avatar.nbt != null);
        delete.setActive(backend);
    }

    @Override
    public void removed() {
        super.removed();
        LocalAvatarFetcher.save();
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        super.onFilesDrop(paths);

        StringBuilder packs = new StringBuilder();
        for (int i = 0; i < paths.size(); i++) {
            if (i > 0)
                packs.append("\n");
            packs.append(paths.get(i).getFileName());
        }

        this.minecraft.setScreen(new FiguraConfirmScreen(confirmed -> {
            if (confirmed) {
                try {
                    LocalAvatarFetcher.loadExternal(paths);
                    FiguraToast.sendToast(new FiguraText("toast.wardrobe_copy.success", paths.size()));
                } catch (Exception e) {
                    FiguraToast.sendToast(new FiguraText("toast.wardrobe_copy.error"), FiguraToast.ToastType.ERROR);
                    FiguraMod.LOGGER.error("Failed to copy files", e);
                }
            }
            this.minecraft.setScreen(this);
        }, new FiguraText("gui.wardrobe.drop_files"), packs.toString(), this));
    }
}
