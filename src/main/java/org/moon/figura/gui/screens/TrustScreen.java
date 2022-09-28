package org.moon.figura.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.gui.widgets.*;
import org.moon.figura.gui.widgets.lists.PlayerList;
import org.moon.figura.gui.widgets.lists.TrustList;
import org.moon.figura.gui.widgets.trust.AbstractTrustElement;
import org.moon.figura.gui.widgets.trust.PlayerElement;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.UUID;

public class TrustScreen extends AbstractPanelScreen {

    public static final Component TITLE = FiguraText.of("gui.panels.title.trust");

    // -- widgets -- //
    private PlayerList playerList;
    private InteractableEntity entityWidget;

    private SliderWidget slider;

    private TrustList trustList;
    private SwitchButton expandButton;
    private TexturedButton reloadAll;
    private TexturedButton back;
    private TexturedButton resetButton;

    // -- debug -- //
    private TextField uuid;
    private TexturedButton yoink;

    // -- widget logic -- //
    private float listYPrecise;
    private float expandYPrecise;
    private float resetYPrecise;

    private PlayerElement dragged = null;

    public TrustScreen(Screen parentScreen) {
        super(parentScreen, TITLE, 3);
    }

    @Override
    protected void init() {
        super.init();

        int middle = this.width / 2;
        int listWidth = Math.min(middle - 6, 208);
        int lineHeight =  font.lineHeight;

        double guiScale = this.minecraft.getWindow().getGuiScale();
        double screenScale = Math.min(this.width, this.height) / 1018d;
        int modelSize = Math.min((int) ((192 / guiScale) * (screenScale * guiScale)), 96);

        int entitySize = (int) Math.min(height - 95 - lineHeight * 1.5 - (FiguraMod.DEBUG_MODE ? 24 : 0), listWidth);
        int entityX = Math.max(middle + (listWidth - entitySize) / 2 + 1, middle + 2);

        //entity widget
        entityWidget = new InteractableEntity(entityX, 28, entitySize, entitySize, modelSize, -15f, 30f, Minecraft.getInstance().player, this);

        //trust slider and list
        slider = new SliderWidget(middle + 2, (int) (entityWidget.y + entityWidget.height + lineHeight * 1.5 + 20), listWidth, 11, 1d, 5, true) {
            @Override
            public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
                super.renderButton(stack, mouseX, mouseY, delta);

                TrustContainer selectedTrust = playerList.selectedEntry.getTrust();
                MutableComponent text = selectedTrust.getGroupName();

                stack.pushPose();
                stack.translate(this.x + this.getWidth() / 2f - font.width(text) * 0.75, this.y - 4 - font.lineHeight * 2, 0f);
                stack.scale(1.5f, 1.5f, 1f);
                UIHelper.renderOutlineText(stack, font, text, 0, 0, 0xFFFFFF, 0x202020);
                stack.popPose();
            }
        };
        trustList = new TrustList(middle + 2, height, listWidth, height - 54);

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
        addRenderableWidget(reloadAll = new TexturedButton(middle + 2, height - 24, bottomButtonsWidth, 20, FiguraText.of("gui.trust.reload_all"), null, bx -> {
            AvatarManager.clearAllAvatars();
            FiguraToast.sendToast(FiguraText.of("toast.reload_all"));
        }));

        //back button
        addRenderableWidget(back = new TexturedButton(middle + 6 + bottomButtonsWidth, height - 24, bottomButtonsWidth, 20, FiguraText.of("gui.done"), null,
                bx -> this.minecraft.setScreen(parentScreen)
        ));

        //debug buttons
        uuid = new TextField(middle + 2, back.y - 24, listWidth - 24, 20, Component.literal("Name/UUID"), s -> yoink.active = !s.isBlank());
        yoink = new TexturedButton(middle + listWidth - 18, back.y - 24, 20, 20, Component.literal("yoink"), Component.literal("Set the selected player's avatar"), button -> {
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

            if (playerList.selectedEntry instanceof PlayerElement player) {
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
        addRenderableWidget(expandButton = new SwitchButton( middle + listWidth - 18, height - 24, 20, 20, 0, 0, 20, new FiguraIdentifier("textures/gui/expand_v.png"), 60, 40, FiguraText.of("gui.trust.expand_trust.tooltip"), btn -> {
            boolean expanded = expandButton.isToggled();

            //hide widgets
            entityWidget.setVisible(!expanded);
            slider.visible = !expanded;
            reloadAll.visible = !expanded;
            back.visible = !expanded;
            uuid.setVisible(!expanded);
            yoink.visible = !expanded;

            //update expand button
            expandButton.setTooltip(expanded ? FiguraText.of("gui.trust.minimize_trust.tooltip") : FiguraText.of("gui.trust.expand_trust.tooltip"));

            //set reset button activeness
            resetButton.active = expanded;
        }));

        //reset all button
        addRenderableWidget(resetButton = new TexturedButton(middle + 2, height, 60, 20, FiguraText.of("gui.trust.reset"), null, btn -> {
            //clear trust
            TrustContainer trust = playerList.selectedEntry.getTrust();
            trust.getSettings().clear();
            updateTrustData(trust);
        }));

        //add trust list
        addRenderableWidget(trustList);

        listYPrecise = trustList.y;
        expandYPrecise = expandButton.y;
        resetYPrecise = resetButton.y;
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        //set entity to render
        AbstractTrustElement entity = playerList.selectedEntry;
        Level world = Minecraft.getInstance().level;
        if (world != null && entity instanceof PlayerElement player)
            entityWidget.setEntity(world.getPlayerByUUID(UUID.fromString(player.getTrust().name)));
        else
            entityWidget.setEntity(null);

        //expand animation
        float lerpDelta = (float) (1f - Math.pow(0.6f, delta));

        listYPrecise = Mth.lerp(lerpDelta, listYPrecise, expandButton.isToggled() ? 50f : height + 1);
        this.trustList.y = (int) listYPrecise;

        expandYPrecise = Mth.lerp(lerpDelta, expandYPrecise, listYPrecise - 24f);
        this.expandButton.y = (int) expandYPrecise;

        resetYPrecise = Mth.lerp(lerpDelta, resetYPrecise, expandButton.isToggled() ? listYPrecise - 22f : height);
        this.resetButton.y = (int) resetYPrecise;

        //render
        super.render(stack, mouseX, mouseY, delta);
    }

    @Override
    public void renderOverlays(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (dragged != null)
            dragged.renderDragged(stack, mouseX, mouseY, delta);

        super.renderOverlays(stack, mouseX, mouseY, delta);
    }

    @Override
    public void removed() {
        TrustManager.saveToDisk();
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
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        boolean bool = false;

        if (playerList.selectedEntry instanceof PlayerElement element && element.isMouseOver(mouseX, mouseY)) {
            dragged = element;
            dragged.dragged = true;
            dragged.index = playerList.getTrustAt(mouseY);
            bool = true;
        }

        if (dragged != null) {
            dragged.index = playerList.getTrustAt(mouseY);
            bool = true;
        }

        return bool || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragged == null)
            return super.mouseReleased(mouseX, mouseY, button);

        TrustContainer trust = dragged.getTrust();
        ArrayList<ResourceLocation> list = new ArrayList<>(TrustManager.GROUPS.keySet());
        ResourceLocation id = list.get(Math.min(dragged.index, list.size() - (TrustManager.isLocal(trust) ? 1 : 2)));

        trust.setParent(id);
        updateTrustData(trust);

        dragged.dragged = false;
        dragged = null;
        return true;
    }

    public void updateTrustData(TrustContainer trust) {
        //reset run action
        slider.setAction(null);

        //set slider active only for players
        boolean group = TrustManager.GROUPS.containsValue(trust);
        slider.active = !group;

        ArrayList<ResourceLocation> groupList = new ArrayList<>(TrustManager.GROUPS.keySet());

        //set step sizes
        slider.setMax(TrustManager.isLocal(trust) ? groupList.size() : groupList.size() - 1);

        //set slider progress
        slider.setScrollProgress(groupList.indexOf(group ? new ResourceLocation("group", trust.name) : trust.getParentID()) / (slider.getMax() - 1d));

        //set new slider action
        slider.setAction(scroll -> {
            //set new trust parent
            ResourceLocation newTrust = groupList.get(((SliderWidget) scroll).getIntValue());
            trust.setParent(newTrust);

            //and update the advanced trust
            trustList.updateList(trust);
        });

        //update advanced trust list
        trustList.updateList(trust);
    }
}
