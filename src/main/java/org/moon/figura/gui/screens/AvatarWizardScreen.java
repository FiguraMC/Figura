package org.moon.figura.gui.screens;

import net.minecraft.client.gui.screens.Screen;
import org.moon.figura.FiguraMod;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.gui.widgets.Button;
import org.moon.figura.gui.widgets.lists.AvatarWizardList;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.wizards.AvatarWizard;

public class AvatarWizardScreen extends AbstractPanelScreen {

    private final Screen sourcePanel;

    private final AvatarWizard wizard = new AvatarWizard();
    private Button build;

    public AvatarWizardScreen(AbstractPanelScreen parentScreen) {
        super(parentScreen.parentScreen, FiguraText.of("gui.panels.title.avatar_wizard"));
        sourcePanel = parentScreen;
    }

    @Override
    public Class<? extends Screen> getSelectedPanel() {
        return sourcePanel.getClass();
    }

    @Override
    protected void init() {
        super.init();

        // -- bottom buttons -- //

        //cancel
        this.addRenderableWidget(new Button(width / 2 - 122, height - 24, 120, 20, FiguraText.of("gui.cancel"), null,
                button -> this.minecraft.setScreen(sourcePanel)
        ));

        //done
        addRenderableWidget(build = new Button(width / 2 + 4, height - 24, 120, 20, FiguraText.of("gui.create"), null, button -> {
            try {
                wizard.build();
                FiguraToast.sendToast(FiguraText.of("toast.avatar_wizard.success"));
            } catch (Exception e) {
                FiguraToast.sendToast(FiguraText.of("toast.avatar_wizard.error"), FiguraToast.ToastType.ERROR);
                FiguraMod.LOGGER.error("", e);
            }

            this.minecraft.setScreen(sourcePanel);
        }));

        // -- wizard -- //

        int width = Math.min(this.width - 8, 420) / 2;
        this.addRenderableWidget(new AvatarWizardList((this.width - width) / 2, 28, width, height - 56, wizard));
    }

    @Override
    public void tick() {
        super.tick();
        build.setActive(wizard.canBuild());
    }
}
