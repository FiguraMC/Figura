package org.figuramc.figura.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.gui.widgets.*;
import org.figuramc.figura.lua.api.ClientAPI;
import org.figuramc.figura.mixin.gui.ScreenAccessor;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.List;

public abstract class AbstractPanelScreen extends Screen {

    public static final List<ResourceLocation> BACKGROUNDS = List.of(
            new FiguraIdentifier("textures/gui/background/background_0.png"),
            new FiguraIdentifier("textures/gui/background/background_1.png"),
            new FiguraIdentifier("textures/gui/background/background_2.png")
    );

    //variables
    protected final Screen parentScreen;
    public PanelSelectorWidget panels;

    //overlays
    public ContextMenu contextMenu;
    public Component tooltip;

    //stuff :3
    private static final String EGG = "ĉĉĈĈćĆćĆBAā";
    private String egg = EGG;

    protected AbstractPanelScreen(Screen parentScreen, Component title) {
        super(title);
        this.parentScreen = parentScreen;
    }

    public Class<? extends Screen> getSelectedPanel() {
        return this.getClass();
    };

    @Override
    protected void init() {
        super.init();

        //add panel selector
        this.addRenderableWidget(panels = new PanelSelectorWidget(parentScreen, 0, 0, width, getSelectedPanel()));

        //clear overlays
        contextMenu = null;
        tooltip = null;
    }

    @Override
    public void tick() {
        for (Widget renderable : this.renderables()) {
            if (renderable instanceof FiguraTickable tickable)
                tickable.tick();
        }

        renderables().removeIf(r -> r instanceof FiguraRemovable removable && removable.isRemoved());

        super.tick();
    }

    public List<Widget> renderables() {
        return ((ScreenAccessor) this).getRenderables();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        //setup figura framebuffer
        //UIHelper.useFiguraGuiFramebuffer();

        //render background
        this.renderBackground(stack, delta);

        //render contents
        super.render(stack, mouseX, mouseY, delta);

        //render overlays
        this.renderOverlays(stack, mouseX, mouseY, delta);

        //restore vanilla framebuffer
        //UIHelper.useVanillaFramebuffer();
    }

    public void renderBackground(PoseStack stack, float delta) {
        //render
        float speed = Configs.BACKGROUND_SCROLL_SPEED.tempValue * 0.125f;
        for (ResourceLocation background : BACKGROUNDS) {
            UIHelper.renderAnimatedBackground(stack, background, 0, 0, this.width, this.height, 64, 64, speed, delta);
            speed /= 0.5;
        }
    }

    public void renderOverlays(PoseStack stack, int mouseX, int mouseY, float delta) {
        //fps
        if (Configs.GUI_FPS.value)
            font.draw(stack, ClientAPI.getFPS() + " fps", 1, 1, 0xFFFFFF);


        //render context
        if (contextMenu != null && contextMenu.isVisible()) {
            //translate the stack here because of nested contexts
            stack.pushPose();
            stack.translate(0f, 0f, 500f);
            contextMenu.render(stack, mouseX, mouseY, delta);
            stack.popPose();
        }

        //render tooltip
        if (tooltip != null)
            UIHelper.renderTooltip(stack, tooltip, mouseX, mouseY, true);

        tooltip = null;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parentScreen);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        //fix mojang focusing for text fields
        for (GuiEventListener listener : this.children()) {
            if (listener instanceof TextField field)
                field.getField().setFocus(field.isEnabled() && field.isMouseOver(mouseX, mouseY));
        }
        return this.contextMenuClick(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean contextMenuClick(double mouseX, double mouseY, int button) {
        //attempt to run context first
        if (contextMenu != null && contextMenu.isVisible()) {
            //attempt to click on the context menu
            boolean clicked = contextMenu.mouseClicked(mouseX, mouseY, button);

            //then try to click on the category container and suppress it
            //let the category handle the context menu visibility
            if (!clicked && contextMenu.parent != null && contextMenu.parent.mouseClicked(mouseX, mouseY, button))
                return true;

            //otherwise, remove visibility and suppress the click only if we clicked on the context
            contextMenu.setVisible(false);
            return clicked;
        }

        //no interaction was made
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        //yeet mouse 0 and isDragging check
        return this.getFocused() != null && this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        //better check for mouse released when outside element boundaries
        return this.getFocused() != null && this.getFocused().mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        //hide previous context
        if (contextMenu != null)
            contextMenu.setVisible(false);

        //fix scrolling targeting only one child
        boolean ret = false;
        for (GuiEventListener child : this.children()) {
            if (child.isMouseOver(mouseX, mouseY))
                ret = ret || child.mouseScrolled(mouseX, mouseY, amount);
        }
        return ret;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        egg += (char) keyCode;
        egg = egg.substring(1);
        if (EGG.equals(egg)) {
            Minecraft.getInstance().setScreen(new GameScreen(this));
            return true;
        }

        if (children().contains(panels) && panels.cycleTab(keyCode))
            return true;

        if (keyCode == 256 && contextMenu != null && contextMenu.isVisible()) {
            contextMenu.setVisible(false);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
