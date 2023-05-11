package org.moon.figura.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.gui.widgets.*;
import org.moon.figura.gui.widgets.lists.PermissionsList;
import org.moon.figura.gui.widgets.lists.PlayerList;
import org.moon.figura.gui.widgets.permissions.AbstractPermPackElement;
import org.moon.figura.gui.widgets.permissions.PlayerPermPackElement;
import org.moon.figura.permissions.PermissionManager;
import org.moon.figura.permissions.PermissionPack;
import org.moon.figura.permissions.Permissions;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.MathUtils;
import org.moon.figura.utils.ui.UIHelper;

import java.util.UUID;

public class PermissionsScreen extends AbstractPanelScreen {

    // -- widgets -- //
    private PlayerList playerList;
    private EntityPreview entityWidget;

    private SliderWidget slider;

    private PermissionsList permissionsList;
    private SwitchButton expandButton;
    private Button reloadAll;
    private Button back;
    private Button resetButton;
    private SwitchButton precisePermissions;

    // -- debug -- //
    private TextField uuid;
    private Button yoink;

    // -- widget logic -- //
    private float listYPrecise;
    private float expandYPrecise;
    private float resetYPrecise;

    private boolean expanded;
    private PlayerPermPackElement dragged = null;

    public PermissionsScreen(Screen parentScreen) {
        super(parentScreen, FiguraText.of("gui.panels.title.permissions"));
    }

    @Override
    protected void init() {
        super.init();

        int middle = this.width / 2;
        int listWidth = Math.min(middle - 6, 208);
        int lineHeight =  font.lineHeight;

        int entitySize = (int) Math.min(height - 95 - lineHeight * 1.5 - (FiguraMod.DEBUG_MODE ? 24 : 0), listWidth);
        int modelSize = 11 * entitySize / 29;
        int entityX = Math.max(middle + (listWidth - entitySize) / 2 + 1, middle + 2);

        //entity widget
        entityWidget = new EntityPreview(entityX, 28, entitySize, entitySize, modelSize, -15f, 30f, Minecraft.getInstance().player, this);

        //permission slider and list
        slider = new SliderWidget(middle + 2, (int) (entityWidget.getY() + entityWidget.getHeight() + lineHeight * 1.5 + 20), listWidth, 11, 1d, 5, true) {
            @Override
            public void renderWidget(PoseStack stack, int mouseX, int mouseY, float delta) {
                super.renderWidget(stack, mouseX, mouseY, delta);

                PermissionPack selectedPack = playerList.selectedEntry.getPack();
                MutableComponent text = selectedPack.getCategoryName();

                int x = (int) (this.getX() + this.getWidth() / 2f - font.width(text) * 0.75f);
                int y = this.getY() - 4 - font.lineHeight * 2;

                stack.pushPose();
                stack.translate(x, y, 0f);
                stack.scale(1.5f, 1.5f, 1f);
                UIHelper.renderOutlineText(stack, font, text, 0, 0, 0xFFFFFF, 0x202020);
                stack.popPose();

                MutableComponent info = Component.literal("?").withStyle(Style.EMPTY.withFont(UIHelper.UI_FONT));
                int color = 0x404040;

                int width = font.width(info);
                x = Math.min((int) (x + font.width(text) * 1.5f + font.width("  ")), PermissionsScreen.this.width - width);
                y += font.lineHeight * 0.25f;

                if (UIHelper.isMouseOver(x, y, width, font.lineHeight, mouseX, mouseY)) {
                    color = 0xFFFFFF;
                    UIHelper.setTooltip(selectedPack.getCategory().info);
                }

                font.drawShadow(stack, info, x, y, color);
            }
        };
        permissionsList = new PermissionsList(middle + 2, height, listWidth, height - 54);

        // -- left -- //

        //player list
        addRenderableWidget(playerList = new PlayerList(middle - listWidth - 2, 28, listWidth, height - 32, this));

        // -- right -- //

        //add entity widget
        addRenderableWidget(entityWidget);

        // -- bottom -- //

        //add slider
        addRenderableWidget(slider);

        //reload all
        int bottomButtonsWidth = (listWidth - 24) / 2 - 2;
        addRenderableWidget(reloadAll = new Button(middle + 2, height - 24, bottomButtonsWidth, 20, FiguraText.of("gui.permissions.reload_all"), null, bx -> {
            AvatarManager.clearAllAvatars();
            FiguraToast.sendToast(FiguraText.of("toast.reload_all"));
        }));

        //back button
        addRenderableWidget(back = new Button(middle + 6 + bottomButtonsWidth, height - 24, bottomButtonsWidth, 20, FiguraText.of("gui.done"), null, bx -> onClose()));

        //debug buttons
        uuid = new TextField(middle + 2, back.getY() - 24, listWidth - 24, 20, TextField.HintType.NAME, s -> yoink.setActive(!s.isBlank()));
        yoink = new Button(middle + listWidth - 18, back.getY() - 24, 20, 20, Component.literal("yoink"), Component.literal("Set the selected player's avatar"), button -> {
            String text = uuid.getField().getValue();
            UUID id;

            try {
                id = UUID.fromString(text);
            } catch (Exception ignored) {
                id = FiguraMod.playerNameToUUID(text);
            }

            if (id == null) {
                FiguraToast.sendToast("oopsie", FiguraToast.ToastType.ERROR);
                return;
            }

            Avatar avatar = AvatarManager.getAvatarForPlayer(id);
            if (avatar == null || avatar.nbt == null)
                return;

            if (playerList.selectedEntry instanceof PlayerPermPackElement player) {
                UUID target = player.getOwner();
                if (FiguraMod.isLocal(target))
                    AvatarManager.localUploaded = false;

                AvatarManager.setAvatar(target, avatar.nbt);
                FiguraToast.sendToast("yoinked");
            }
        });
        yoink.setActive(false);

        if (FiguraMod.DEBUG_MODE) {
            addRenderableWidget(uuid);
            addRenderableWidget(yoink);
        }

        //expand button
        addRenderableWidget(expandButton = new SwitchButton( middle + listWidth - 18, height - 24, 20, 20, 0, 0, 20, new FiguraIdentifier("textures/gui/expand_v.png"), 60, 40, FiguraText.of("gui.permissions.expand_permissions.tooltip"), btn -> {
            expanded = expandButton.isToggled();

            //hide widgets
            entityWidget.setVisible(!expanded);
            slider.setVisible(!expanded);
            slider.setActive(!expanded);
            reloadAll.setVisible(!expanded);
            back.setVisible(!expanded);
            uuid.setVisible(!expanded);
            yoink.setVisible(!expanded);

            //update expand button
            expandButton.setTooltip(expanded ? FiguraText.of("gui.permissions.minimize_permissions.tooltip") : FiguraText.of("gui.permissions.expand_permissions.tooltip"));

            //set reset button activeness
            resetButton.setActive(expanded);
        }));

        //reset all button
        addRenderableWidget(resetButton = new Button(middle + 2, height, 60, 20, FiguraText.of("gui.permissions.reset"), null, btn -> {
            //clear permissions
            PermissionPack pack = playerList.selectedEntry.getPack();
            pack.clear();
            updatePermissions(pack);
        }));

        addRenderableWidget(precisePermissions = new SwitchButton(middle + 66, height, listWidth - 88, 20, FiguraText.of("gui.permissions.precise"), false) {
            @Override
            public void onPress() {
                super.onPress();
                permissionsList.precise = this.isToggled();
                permissionsList.updateList(playerList.selectedEntry.getPack());
            }

            @Override
            public void renderWidget(PoseStack stack, int mouseX, int mouseY, float delta) {
                //super.renderDefaultTexture(stack, delta);
                super.renderWidget(stack, mouseX, mouseY, delta);
            }
        });
        precisePermissions.setUnderline(false);

        //add permissions list
        addRenderableWidget(permissionsList);

        listYPrecise = permissionsList.getY();
        expandYPrecise = expandButton.getY();
        resetYPrecise = resetButton.getY();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        //set entity to render
        AbstractPermPackElement entity = playerList.selectedEntry;
        Level world = Minecraft.getInstance().level;
        if (world != null && entity instanceof PlayerPermPackElement player)
            entityWidget.setEntity(world.getPlayerByUUID(UUID.fromString(player.getPack().name)));
        else
            entityWidget.setEntity(null);

        //expand animation
        float lerpDelta = MathUtils.magicDelta(0.6f, delta);

        listYPrecise = Mth.lerp(lerpDelta, listYPrecise, expandButton.isToggled() ? 50f : height + 1);
        this.permissionsList.setY((int) listYPrecise);

        expandYPrecise = Mth.lerp(lerpDelta, expandYPrecise, expandButton.isToggled() ? listYPrecise - 22f : listYPrecise - 24f);
        this.expandButton.setY((int) expandYPrecise);

        resetYPrecise = Mth.lerp(lerpDelta, resetYPrecise, expandButton.isToggled() ? listYPrecise - 22f : height);
        this.resetButton.setY((int) resetYPrecise);
        this.precisePermissions.setY((int) resetYPrecise);

        //render
        super.render(stack, mouseX, mouseY, delta);
    }

    @Override
    public void renderOverlays(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (dragged != null && dragged.dragged)
            dragged.renderDragged(stack, mouseX, mouseY, delta);

        super.renderOverlays(stack, mouseX, mouseY, delta);
    }

    @Override
    public void removed() {
        PermissionManager.saveToDisk();
        super.removed();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        //yeet ESC key press for collapsing the card list
        if (keyCode == 256 && expandButton.isToggled()) {
            expandButton.onPress();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean bool = super.mouseClicked(mouseX, mouseY, button);
        dragged = null;

        if (button == 0 && playerList.selectedEntry instanceof PlayerPermPackElement element && element.isMouseOver(mouseX, mouseY)) {
            dragged = element;
            element.anchorX = (int) mouseX;
            element.anchorY = (int) mouseY;
            element.initialY = element.getY();
        }

        return bool;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragged != null) {
            dragged.index = playerList.getCategoryAt(mouseY);
            dragged.dragged = true;
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean bool = super.mouseReleased(mouseX, mouseY, button);

        if (dragged == null || !dragged.dragged)
            return bool;

        PermissionPack pack = dragged.getPack();
        Permissions.Category category = Permissions.Category.indexOf(Math.min(dragged.index, Permissions.Category.values().length - 1));

        pack.setCategory(PermissionManager.CATEGORIES.get(category));
        updatePermissions(pack);

        dragged.dragged = false;
        dragged = null;
        return bool;
    }

    public void updatePermissions(PermissionPack pack) {
        //reset run action
        slider.setAction(null);

        //set slider active only for players
        slider.setActive(pack instanceof PermissionPack.PlayerPermissionPack && !expanded);

        //set step sizes
        slider.setMax(Permissions.Category.values().length);

        //set slider progress
        slider.setScrollProgress(pack.getCategory().index / (slider.getMax() - 1d));

        //set new slider action
        slider.setAction(scroll -> {
            //set new permissions category
            Permissions.Category category = Permissions.Category.indexOf(((SliderWidget) scroll).getIntValue());
            pack.setCategory(PermissionManager.CATEGORIES.get(category));

            //and update the advanced permissions
            permissionsList.updateList(pack);
        });

        //update advanced permissions list
        permissionsList.updateList(pack);
    }
}
