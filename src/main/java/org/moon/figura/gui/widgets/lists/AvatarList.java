package org.moon.figura.gui.widgets.lists;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.providers.LocalAvatarFetcher;
import org.moon.figura.gui.widgets.TextField;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.ui.UIHelper;

import java.util.List;

public class AvatarList extends AbstractList {

    // -- Variables -- //

    // Search bar
    private final TextField textField;
    private String filter = "";

    // -- Constructors -- //

    public AvatarList(int x, int y, int width, int height) {
        super(x, y, width, height);

        children.add(textField = new TextField(x + 4, y + 4, width - 8, 22, new FiguraText("gui.search"), s -> filter = s));

        //scrollbar
        this.scrollBar.y = y + 30;
        this.scrollBar.setHeight(height - 34);

        //scissors
        this.updateScissors(1, 26, -2, -27);
    }

    // -- Functions -- //
    @Override
    public void tick() {
        loadContents();
        super.tick();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        //background and scissors
        UIHelper.renderSliced(stack, x, y, width, height, UIHelper.OUTLINE);
        UIHelper.setupScissor(x + scissorsX, y + scissorsY, width + scissorsWidth, height + scissorsHeight);



        //reset scissor
        RenderSystem.disableScissor();

        //render children
        super.render(stack, mouseX, mouseY, delta);
    }

    private void loadContents() {
        //update list
        if (FiguraMod.ticks % 20 == 0) {
            LocalAvatarFetcher.load();
        }

        // Load avatars //
        List<LocalAvatarFetcher.AvatarFolder> foundAvatars = LocalAvatarFetcher.ALL_AVATARS;
    }

    public void updateWidth(int width) {
        this.width = width;
        textField.width = width - 8;
        scrollBar.x = this.x + this.width - 14;
    }
}
