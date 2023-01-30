package org.moon.figura.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.gui.widgets.*;
import org.moon.figura.gui.widgets.lists.PlayerList;
import org.moon.figura.gui.widgets.lists.PermissionsList;
import org.moon.figura.gui.widgets.permissions.AbstractPermPackElement;
import org.moon.figura.gui.widgets.permissions.PlayerPermPackElement;
import org.moon.figura.permissions.Permissions;
import org.moon.figura.permissions.PermissionPack;
import org.moon.figura.permissions.PermissionManager;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.ui.UIHelper;

import java.util.UUID;

public class PermissionsScreen extends AbstractPanelScreen {

    public static final Component TITLE = new FiguraText("gui.panels.title.permissions");

    // -- widgets -- //
    private PlayerList playerList;
    private InteractableEntity entityWidget;

    private SliderWidget slider;

    private PermissionsList permissionsList;
    private SwitchButton expandButton;
    private TexturedButton reloadAll;
    private TexturedButton back;
    private TexturedButton resetButton;
    private SwitchButton precisePermissions;

    // -- debug -- //
    private TextField uuid;
    private TexturedButton yoink;

    // -- widget logic -- //
    private float listYPrecise;
    private float expandYPrecise;
    private float resetYPrecise;

    private boolean expanded;
    private PlayerPermPackElement dragged = null;

    public PermissionsScreen(Screen parentScreen) {
        super(parentScreen, TITLE, PermissionsScreen.class);
    }

    @Override
    public Component getTitle() {
        return TITLE;
    }

    @Override
    protected void init() {
        super.init();

        int middle = this.width / 2;
        int listWidth = Math.min(middle - 6, 208);
        int lineHeight =  font.lineHeight;

        double guiScale = this.minecraft == null ? 1 : this.minecraft.getWindow().getGuiScale();
        double screenScale = Math.min(this.width, this.height) / 1018d;
        int modelSize = Math.min((int) ((192 / guiScale) * (screenScale * guiScale)), 96);

        int entitySize = (int) Math.min(height - 95 - lineHeight * 1.5 - (FiguraMod.DEBUG_MODE ? 24 : 0), listWidth);
        int entityX = Math.max(middle + (listWidth - entitySize) / 2 + 1, middle + 2);

        //entity widget
        entityWidget = new InteractableEntity(entityX, 28, entitySize, entitySize, modelSize, -15f, 30f, Minecraft.getInstance().player, this);

        //permission slider and list
        slider = new SliderWidget(middle + 2, (int) (entityWidget.y + entityWidget.height + lineHeight * 1.5 + 20), listWidth, 11, 1d, 5, true) {
            @Override
            public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
                super.renderButton(stack, mouseX, mouseY, delta);

                PermissionPack selectedPack = playerList.selectedEntry.getPack();
                MutableComponent text = selectedPack.getCategoryName();

                stack.pushPose();
                stack.translate(this.x + this.getWidth() / 2f - font.width(text) * 0.75, this.y - 4 - font.lineHeight * 2, 0f);
                stack.scale(1.5f, 1.5f, 1f);
                UIHelper.renderOutlineText(stack, font, text, 0, 0, 0xFFFFFF, 0x202020);
                stack.popPose();
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
        addRenderableWidget(reloadAll = new TexturedButton(middle + 2, height - 24, bottomButtonsWidth, 20, new FiguraText("gui.permissions.reload_all"), null, bx -> {
            AvatarManager.clearAllAvatars();
            FiguraToast.sendToast(new FiguraText("toast.reload_all"));
        }));

        //back button
        addRenderableWidget(back = new TexturedButton(middle + 6 + bottomButtonsWidth, height - 24, bottomButtonsWidth, 20, new FiguraText("gui.done"), null,
                bx -> this.minecraft.setScreen(parentScreen)
        ));

        //debug buttons
        uuid = new TextField(middle + 2, back.y - 24, listWidth - 24, 20, TextField.HintType.NAME, s -> yoink.active = !s.isBlank());
        yoink = new TexturedButton(middle + listWidth - 18, back.y - 24, 20, 20, new TextComponent("yoink"), new TextComponent("Set the selected player's avatar"), button -> {
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
        yoink.active = false;

        if (FiguraMod.DEBUG_MODE) {
            addRenderableWidget(uuid);
            addRenderableWidget(yoink);
        }

        //expand button
        addRenderableWidget(expandButton = new SwitchButton( middle + listWidth - 18, height - 24, 20, 20, 0, 0, 20, new FiguraIdentifier("textures/gui/expand_v.png"), 60, 40, new FiguraText("gui.permissions.expand_permissions.tooltip"), btn -> {
            expanded = expandButton.isToggled();

            //hide widgets
            entityWidget.setVisible(!expanded);
            slider.visible = !expanded;
            slider.active = !expanded;
            reloadAll.visible = !expanded;
            back.visible = !expanded;
            uuid.setVisible(!expanded);
            yoink.visible = !expanded;

            //update expand button
            expandButton.setTooltip(expanded ? new FiguraText("gui.permissions.minimize_permissions.tooltip") : new FiguraText("gui.permissions.expand_permissions.tooltip"));

            //set reset button activeness
            resetButton.active = expanded;
        }));

        //reset all button
        addRenderableWidget(resetButton = new TexturedButton(middle + 2, height, 60, 20, new FiguraText("gui.permissions.reset"), null, btn -> {
            //clear permissions
            PermissionPack pack = playerList.selectedEntry.getPack();
            pack.clear();
            updatePermissions(pack);
        }));

        addRenderableWidget(precisePermissions = new SwitchButton(middle + 72, height, 30, 20, false) {
            @Override
            public void onPress() {
                super.onPress();
                permissionsList.precise = this.isToggled();
                permissionsList.updateList(playerList.selectedEntry.getPack());
            }

            @Override
            protected void renderText(PoseStack stack) {
                Font font = Minecraft.getInstance().font;
                drawString(stack, font, this.getMessage(), x + width + 4, y + height / 2 - font.lineHeight / 2, 0xFFFFFF);
            }
        });
        precisePermissions.setMessage(new FiguraText("gui.permissions.precise"));

        //add permissions list
        addRenderableWidget(permissionsList);

        listYPrecise = permissionsList.y;
        expandYPrecise = expandButton.y;
        resetYPrecise = resetButton.y;
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
        float lerpDelta = (float) (1f - Math.pow(0.6f, delta));

        listYPrecise = Mth.lerp(lerpDelta, listYPrecise, expandButton.isToggled() ? 50f : height + 1);
        this.permissionsList.y = (int) listYPrecise;

        expandYPrecise = Mth.lerp(lerpDelta, expandYPrecise, expandButton.isToggled() ? listYPrecise - 22f : listYPrecise - 24f);
        this.expandButton.y = (int) expandYPrecise;

        resetYPrecise = Mth.lerp(lerpDelta, resetYPrecise, expandButton.isToggled() ? listYPrecise - 22f : height);
        this.resetButton.y = (int) resetYPrecise;
        this.precisePermissions.y = (int) resetYPrecise;

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
            element.initialY = element.y;
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
        if (dragged == null || !dragged.dragged)
            return super.mouseReleased(mouseX, mouseY, button);

        PermissionPack pack = dragged.getPack();
        Permissions.Category category = Permissions.Category.indexOf(Math.min(dragged.index, Permissions.Category.values().length - 1));

        pack.setCategory(PermissionManager.CATEGORIES.get(category));
        updatePermissions(pack);

        dragged.dragged = false;
        dragged = null;
        return true;
    }

    public void updatePermissions(PermissionPack pack) {
        //reset run action
        slider.setAction(null);

        //set slider active only for players
        slider.active = pack instanceof PermissionPack.PlayerPermissionPack && !expanded;

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
