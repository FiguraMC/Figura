package org.moon.figura.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.moon.figura.FiguraMod;
import org.moon.figura.commands.FiguraLinkCommand;
import org.moon.figura.ducks.ParticleEngineAccessor;
import org.moon.figura.gui.widgets.Button;
import org.moon.figura.gui.widgets.IconButton;
import org.moon.figura.gui.widgets.Label;
import org.moon.figura.mixin.gui.ScreenAccessor;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.ui.UIHelper;

import java.util.ArrayList;

public class HelpScreen extends AbstractPanelScreen {

    public static final ResourceLocation ICONS = new FiguraIdentifier("textures/gui/help_icons.png");
    public static final String LUA_MANUAL = "https://www.lua.org/manual/5.2/manual.html";
    public static final String LUA_VERSION = "5.2 - Figura";

    private final ArrayList<Label> titles = new ArrayList<>();
    private final ArrayList<Pet> pets = new ArrayList<>();
    private IconButton kofi;
    private Label fran;

    public HelpScreen(Screen parentScreen) {
        super(parentScreen, FiguraText.of("gui.panels.title.help"));
    }

    @Override
    protected void init() {
        super.init();

        titles.clear();

        int lineHeight = this.minecraft.font.lineHeight;
        int middle = width / 2;
        int y = 28;
        Style color = FiguraMod.getAccentColor();

        //in-game docs
        Label l;
        this.addRenderableWidget(l = new Label(FiguraText.of("gui.help.docs").withStyle(color), middle, y, TextUtils.Alignment.CENTER));
        titles.add(l);

        IconButton docs;
        this.addRenderableWidget(docs = new IconButton(middle - 60, y += lineHeight + 4, 120, 24, 20, 0, 20, ICONS, 60, 40, FiguraText.of("gui.help.ingame_docs"), null, button -> this.minecraft.setScreen(new DocsScreen(this))));
        docs.active = false;
        this.addRenderableWidget(new IconButton(middle - 60, y += 28, 120, 24, 0, 0, 20, ICONS, 60, 40, FiguraText.of("gui.help.lua_manual"), null, openLink(LUA_MANUAL)));
        this.addRenderableWidget(new IconButton(middle - 60, y += 28, 120, 24, 40, 0, 20, ICONS, 60, 40, FiguraText.of("gui.help.external_wiki"), null, openLink(FiguraLinkCommand.LINK.WIKI.url)));

        //links
        this.addRenderableWidget(l = new Label(FiguraText.of("gui.help.links").withStyle(color), middle, y += 28, TextUtils.Alignment.CENTER));
        titles.add(l);

        this.addRenderableWidget(new IconButton(middle - 124, y += lineHeight + 4, 80, 24, 0, 20, 20, ICONS, 60, 40, Component.literal("Discord"), null, openLink(FiguraLinkCommand.LINK.DISCORD.url)));
        this.addRenderableWidget(new IconButton(middle - 40, y, 80, 24, 20, 20, 20, ICONS, 60, 40, Component.literal("GitHub"), null, openLink(FiguraLinkCommand.LINK.GITHUB.url)) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (this.isHoveredOrFocused() && this.isMouseOver(mouseX, mouseY) && button == 1) {
                    int dim = getTextureSize();
                    int x = (int) (Math.random() * dim) + getX() + 2;
                    int y = (int) (Math.random() * dim) + getY() + 2;
                    SpriteSet sprite = getParticle(ParticleTypes.HEART);
                    if (sprite != null)
                        addRenderableOnly(new Pet(x, y, sprite, HelpScreen.this));

                    boolean purr = Math.random() < 0.95;
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(purr ? SoundEvents.CAT_PURR : SoundEvents.CAT_AMBIENT, 1f));
                    return false;
                }

                return super.mouseClicked(mouseX, mouseY, button);
            }
        });
        this.addRenderableWidget(kofi = new IconButton(middle + 44, y, 80, 24, 40, 20, 20, ICONS, 60, 40, Component.literal("Ko-fi"), null, openLink(FiguraLinkCommand.LINK.KOFI.url)));

        //texts
        this.addRenderableWidget(l = new Label(FiguraText.of("gui.help.about").withStyle(color), middle, y += 28, TextUtils.Alignment.CENTER));
        titles.add(l);

        this.addRenderableWidget(new Label(FiguraText.of("gui.help.lua_version", Component.literal(LUA_VERSION).withStyle(color)), middle, y += lineHeight + 4, TextUtils.Alignment.CENTER));
        this.addRenderableWidget(new Label(FiguraText.of("gui.help.figura_version", Component.literal(FiguraMod.VERSION.toString()).withStyle(color)), middle, y += lineHeight + 4, TextUtils.Alignment.CENTER));

        this.addRenderableWidget(fran = new Label(FiguraText.of("gui.help.credits", Component.literal("Fran").withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Francy-chan")))).withStyle(ColorUtils.Colors.FRAN_PINK.style), middle, y + lineHeight + 4, TextUtils.Alignment.CENTER));
        fran.alpha = 64;

        //back
        addRenderableWidget(new Button(middle - 60, height - 24, 120, 20, FiguraText.of("gui.done"), null, bx -> this.minecraft.setScreen(parentScreen)));
    }

    @Override
    public void tick() {
        super.tick();

        if (FiguraMod.ticks % 10 == 0 && kofi.isHoveredOrFocused()) {
            int x = (int) (Math.random() * kofi.getWidth()) + kofi.getX();
            int y = (int) (Math.random() * kofi.getHeight()) + kofi.getY();
            SpriteSet sprite = getParticle(ParticleTypes.HAPPY_VILLAGER);
            if (sprite != null) addRenderableOnly(new Pet(x, y, sprite, HelpScreen.this));
        }
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        super.render(stack, mouseX, mouseY, delta);

        //lines
        int width = Math.min(this.width - 8, 420);
        int minX = this.width / 2 - width / 2;
        int maxX = minX + width;

        for (Label label : titles)
            renderLine(stack, label, minX, maxX);

        //fran
        float lerpDelta = (float) (1f - Math.pow(0.6f, delta));
        fran.alpha = (int) Mth.lerp(lerpDelta, fran.alpha, fran.isMouseOver(mouseX, mouseY) ? 255 : 64);

        //pets
        if (!pets.isEmpty()) {
            ((ScreenAccessor) this).getRenderables().removeAll(pets);
            pets.clear();
        }
    }

    private static void renderLine(PoseStack stack, Label label, int minX, int maxX) {
        //render line
        int labelWidth = label.getWidth() / 2;
        int y = label.y + label.getHeight() / 2;
        int x = label.x - labelWidth;
        UIHelper.fill(stack, minX, y, x - 4, y + 1, 0xFFFFFFFF);
        UIHelper.fill(stack, label.x + labelWidth + 4, y, maxX, y + 1, 0xFFFFFFFF);
    }

    private Button.OnPress openLink(String url) {
        return bx -> this.minecraft.setScreen(new ConfirmLinkScreen((bl) -> {
            if (bl) Util.getPlatform().openUri(url);
            this.minecraft.setScreen(this);
        }, url, true));
    }

    private SpriteSet getParticle(ParticleType<?> particleType) {
        return ((ParticleEngineAccessor) this.minecraft.particleEngine).figura$getParticleSprite(BuiltInRegistries.PARTICLE_TYPE.getKey(particleType));
    }

    private static class Pet implements Renderable {

        private final HelpScreen parent;
        private final SpriteSet sprite;

        private final int x;
        private int y;
        private float size;
        private final float initialSize;

        public Pet(int x, int y, SpriteSet sprite, HelpScreen parent) {
            this.x = x;
            this.y = y;
            this.sprite = sprite;
            this.parent = parent;

            this.initialSize = this.size = 8 + (int) (Math.random() * 4);
        }

        @Override
        public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
            UIHelper.renderSprite(stack, (int) (x - size / 2f), (int) (y - size / 2f), (int) size, (int) size, sprite.get((int) (initialSize - size), (int) initialSize));
            y -= 0.5 * delta;
            size -= 0.5 * delta;
            if (size <= 0) parent.pets.add(this);
        }
    }
}
