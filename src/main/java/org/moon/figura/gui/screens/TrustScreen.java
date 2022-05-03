package org.moon.figura.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.moon.figura.gui.widgets.InteractableEntity;
import org.moon.figura.gui.widgets.SliderWidget;
import org.moon.figura.gui.widgets.SwitchButton;
import org.moon.figura.gui.widgets.TexturedButton;
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

    public static final Component TITLE = new FiguraText("gui.panels.title.trust");

    // -- widgets -- //
    private PlayerList playerList;
    private InteractableEntity entityWidget;

    private SliderWidget slider;

    private TrustList trustList;
    private SwitchButton expandButton;

    private TexturedButton resetButton;

    // -- widget logic -- //
    private float listYPrecise;
    private float expandYPrecise;
    private float resetYPrecise;

    public TrustScreen(Screen parentScreen) {
        super(parentScreen, TITLE, 3);
    }

    @Override
    protected void init() {
        super.init();

        int listWidth = Math.min(this.width / 2 - 6, 208);
        int lineHeight =  Minecraft.getInstance().font.lineHeight;

        //entity widget
        entityWidget = new InteractableEntity(width / 2 + 2, 28, listWidth, (int) Math.min(height - 95 - lineHeight * 1.5, listWidth), (int) (height * 0.25f), -15f, 30f, Minecraft.getInstance().player);

        //trust slider and list
        slider = new SliderWidget(width / 2 + 2, (int) (entityWidget.y + entityWidget.getHeight() + lineHeight * 1.5 + 20), listWidth, 11, 1f, 5) {
            @Override
            public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
                super.renderButton(stack, mouseX, mouseY, delta);

                TrustContainer selectedTrust = playerList.getSelectedEntry().getTrust();
                Font font = Minecraft.getInstance().font;
                MutableComponent text = selectedTrust.getGroupName();

                stack.pushPose();
                stack.translate(this.x + this.getWidth() / 2f - font.width(text) * 0.75, this.y - 4 - font.lineHeight * 2, 0f);
                stack.scale(1.5f, 1.5f, 1f);
                UIHelper.renderOutlineText(stack, font, text, 0, 0, selectedTrust.getGroupColor(), 0x202020);
                stack.popPose();
            }
        };
        trustList = new TrustList(width / 2 + 2, height, listWidth, height - 64);

        // -- left -- //

        //player list
        playerList = new PlayerList(width / 2 - listWidth - 2, 28, listWidth, height - 32, this);
        addRenderableWidget(playerList);

        // -- right -- //

        //add entity widget
        addRenderableWidget(entityWidget);

        // -- bottom -- //

        //add slider
        addRenderableWidget(slider);

        //expand button
        expandButton = new SwitchButton(slider.x + slider.getWidth() / 2 - 10, height - 32, 20, 20, 0, 0, 20, new FiguraIdentifier("textures/gui/expand.png"), 40, 40, new FiguraText("gui.trust.expand_trust.tooltip"), btn -> {
            boolean expanded = expandButton.isToggled();

            //hide widgets
            entityWidget.visible = !expanded;
            slider.visible = !expanded;

            //update expand button
            expandButton.setUV(expanded ? 20 : 0, 0);
            expandButton.setTooltip(expanded ? new FiguraText("gui.trust.minimize_trust.tooltip") : new FiguraText("gui.trust.expand_trust.tooltip"));

            //set reset button activeness
            resetButton.active = expanded;
        });
        addRenderableWidget(expandButton);

        //reset all button
        resetButton = new TexturedButton(width / 2 + 2, height, 60, 20, new FiguraText("gui.trust.reset"), null, btn -> {
            //clear trust
            TrustContainer trust = playerList.getSelectedEntry().getTrust();
            trust.getSettings().clear();
            updateTrustData(trust);
        }) {
            @Override
            public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
                UIHelper.renderSliced(stack, x, y, width, height, UIHelper.OUTLINE);
                super.renderButton(stack, mouseX, mouseY, delta);
            }
        };
        addRenderableWidget(resetButton);

        //add trust list
        addRenderableWidget(trustList);

        listYPrecise = trustList.y;
        expandYPrecise = expandButton.y;
        resetYPrecise = resetButton.y;
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        //set entity to render
        AbstractTrustElement entity = playerList.getSelectedEntry();
        Level world = Minecraft.getInstance().level;
        if (world != null && entity instanceof PlayerElement player)
            entityWidget.setEntity(world.getPlayerByUUID(UUID.fromString(player.getTrust().name)));
        else
            entityWidget.setEntity(null);

        //expand animation
        float lerpDelta = (float) (1f - Math.pow(0.6f, delta));

        listYPrecise = Mth.lerp(lerpDelta, listYPrecise, expandButton.isToggled() ? 60f : height);
        this.trustList.y = (int) listYPrecise;

        expandYPrecise = Mth.lerp(lerpDelta, expandYPrecise, listYPrecise - 28f);
        this.expandButton.y = (int) expandYPrecise;

        resetYPrecise = Mth.lerp(lerpDelta, resetYPrecise, expandButton.isToggled() ? 38f : height);
        this.resetButton.y = (int) resetYPrecise;

        //render
        super.render(stack, mouseX, mouseY, delta);
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
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return super.mouseScrolled(mouseX, mouseY, amount) || (slider.visible && slider.mouseScrolled(mouseX, mouseY, amount));
    }

    public void updateTrustData(TrustContainer trust) {
        //reset run action
        slider.setAction(null);

        //set slider active only for players
        boolean group = TrustManager.GROUPS.containsValue(trust);
        slider.active = !group;

        ArrayList<ResourceLocation> groupList = new ArrayList<>(TrustManager.GROUPS.keySet());

        //set step sizes
        slider.setSteps(TrustManager.isLocal(trust) ? groupList.size() : groupList.size() - 1);

        //set slider progress
        slider.setScrollProgress(groupList.indexOf(group ? new ResourceLocation("group", trust.name) : trust.getParentID()) / (slider.getSteps() - 1f));

        //set new slider action
        slider.setAction(scroll -> {
            //set new trust parent
            ResourceLocation newTrust = groupList.get(((SliderWidget) scroll).getStepValue());
            trust.setParent(newTrust);

            //and update the advanced trust
            trustList.updateList(trust);
        });

        //update advanced trust list
        trustList.updateList(trust);
    }
}
