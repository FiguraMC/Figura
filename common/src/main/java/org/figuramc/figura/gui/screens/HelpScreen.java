package org.moon.figura.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.moon.figura.FiguraMod;
import org.moon.figura.commands.FiguraLinkCommand;
import org.moon.figura.config.Configs;
import org.moon.figura.gui.widgets.Button;
import org.moon.figura.gui.widgets.IconButton;
import org.moon.figura.gui.widgets.Label;
import org.moon.figura.gui.widgets.ParticleWidget;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.ui.UIHelper;

public class HelpScreen extends AbstractPanelScreen {

    public static final ResourceLocation ICONS = new FiguraIdentifier("textures/gui/help_icons.png");
    public static final String LUA_MANUAL = "https://www.lua.org/manual/5.2/manual.html";
    public static final String LUA_VERSION = "5.2 - Figura";

    private IconButton kofi;

    public HelpScreen(Screen parentScreen) {
        super(parentScreen, new FiguraText("gui.panels.title.help"));
    }

    @Override
    protected void init() {
        super.init();

        int lineHeight = this.minecraft.font.lineHeight;
        int middle = width / 2;
        int labelWidth = Math.min(width - 8, 420);
        int y = 28;
        Style color = FiguraMod.getAccentColor();

        //in-game docs
        this.addRenderableWidget(new Title(new FiguraText("gui.help.docs").withStyle(color), middle, y, labelWidth));

        IconButton docs;
        this.addRenderableWidget(docs = new IconButton(middle - 60, y += lineHeight + 4, 120, 24, 20, 0, 20, ICONS, 60, 40, new FiguraText("gui.help.ingame_docs"), null, button -> this.minecraft.setScreen(new DocsScreen(this))));
        docs.setActive(false);
        this.addRenderableWidget(new IconButton(middle - 60, y += 28, 120, 24, 0, 0, 20, ICONS, 60, 40, new FiguraText("gui.help.lua_manual"), null, bx -> UIHelper.openURL(LUA_MANUAL).run()));
        this.addRenderableWidget(new IconButton(middle - 60, y += 28, 120, 24, 40, 0, 20, ICONS, 60, 40, new FiguraText("gui.help.external_wiki"), null, bx -> UIHelper.openURL(FiguraLinkCommand.LINK.WIKI.url).run()));

        //links
        this.addRenderableWidget(new Title(new FiguraText("gui.help.links").withStyle(color), middle, y += 28, labelWidth));

        this.addRenderableWidget(new IconButton(middle - 124, y += lineHeight + 4, 80, 24, 0, 20, 20, ICONS, 60, 40, new TextComponent("Discord"), null, bx -> UIHelper.openURL(FiguraLinkCommand.LINK.DISCORD.url).run()));
        this.addRenderableWidget(new IconButton(middle - 40, y, 80, 24, 20, 20, 20, ICONS, 60, 40, new TextComponent("GitHub"), null, bx -> UIHelper.openURL(FiguraLinkCommand.LINK.GITHUB.url).run()) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (Configs.EASTER_EGGS.value && this.isHoveredOrFocused() && this.isMouseOver(mouseX, mouseY) && button == 1) {
                    int dim = getTextureSize();
                    int x = (int) (Math.random() * dim) + getX() + 2;
                    int y = (int) (Math.random() * dim) + getY() + 2;
                    addRenderableOnly(new ParticleWidget(x, y, ParticleTypes.HEART));

                    boolean purr = Math.random() < 0.95;
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(purr ? SoundEvents.CAT_PURR : SoundEvents.CAT_AMBIENT, 1f));
                    return false;
                }

                return super.mouseClicked(mouseX, mouseY, button);
            }
        });
        this.addRenderableWidget(kofi = new IconButton(middle + 44, y, 80, 24, 40, 20, 20, ICONS, 60, 40, new TextComponent("Ko-fi"), null, b -> UIHelper.openURL(FiguraLinkCommand.LINK.KOFI.url).run()));

        //texts
        this.addRenderableWidget(new Title(new FiguraText("gui.help.about").withStyle(color), middle, y += 28, labelWidth));

        this.addRenderableWidget(new Label(new FiguraText("gui.help.lua_version", new TextComponent(LUA_VERSION).withStyle(color)), middle, y += lineHeight + 4, TextUtils.Alignment.CENTER));
        this.addRenderableWidget(new Label(new FiguraText("gui.help.figura_version", new TextComponent(FiguraMod.VERSION.toString()).withStyle(color)), middle, y += lineHeight + 4, TextUtils.Alignment.CENTER));

        Label fran = new Label(new FiguraText("gui.help.credits", new TextComponent("Fran").withStyle(Style.EMPTY.withClickEvent(new TextUtils.FiguraClickEvent(UIHelper.openURL("https://github.com/Francy-chan"))))).withStyle(ColorUtils.Colors.FRAN_PINK.style), middle, y + lineHeight + 4, TextUtils.Alignment.CENTER);
        fran.setAlpha(0x40);
        this.addRenderableWidget(fran);

        //back
        addRenderableWidget(new Button(middle - 60, height - 24, 120, 20, new FiguraText("gui.done"), null, bx -> onClose()));
    }

    @Override
    public void tick() {
        super.tick();

        if (FiguraMod.ticks % 5 == 0 && kofi.isHoveredOrFocused()) {
            int x = (int) (Math.random() * kofi.getWidth()) + kofi.getX();
            int y = (int) (Math.random() * kofi.getHeight()) + kofi.getY();
            addRenderableOnly(new ParticleWidget(x, y, ParticleTypes.HAPPY_VILLAGER));
        }
    }

    private static class Title extends Label {

        private final int width;

        public Title(Object text, int x, int y, int width) {
            super(text, x, y, TextUtils.Alignment.CENTER);
            this.width = width;
        }

        @Override
        public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
            int x = getRawX();
            int y = getRawY();

            //lines
            int y0 = y + getHeight() / 2;
            int y1 = y0 + 1;

            int x0 = x - width / 2;
            int x1 = x - getWidth() / 2 - 4;
            UIHelper.fill(stack, x0, y0, x1, y1, 0xFFFFFFFF);

            x0 = x + getWidth() / 2 + 4;
            x1 = x + width / 2;
            UIHelper.fill(stack, x0, y0, x1, y1, 0xFFFFFFFF);

            //text
            super.render(stack, mouseX, mouseY, delta);
        }
    }
}
