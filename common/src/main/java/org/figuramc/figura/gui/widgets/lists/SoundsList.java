package org.moon.figura.gui.widgets.lists;

import com.mojang.blaze3d.audio.SoundBuffer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.gui.widgets.AbstractContainerElement;
import org.moon.figura.gui.widgets.Label;
import org.moon.figura.gui.widgets.ParentedButton;
import org.moon.figura.lua.api.sound.LuaSound;
import org.moon.figura.lua.api.sound.SoundAPI;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.MathUtils;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SoundsList extends AbstractList {

    private final List<SoundElement> sounds = new ArrayList<>();

    private final Avatar owner;
    private SoundElement selected;

    public SoundsList(int x, int y, int width, int height, Avatar owner) {
        super(x, y, width, height);
        this.owner = owner;
        updateList();

        Label noOwner, noSounds;
        this.children.add(noOwner = new Label(new FiguraText("gui.error.no_avatar").withStyle(ChatFormatting.YELLOW), x + width / 2, y + height / 2, TextUtils.Alignment.CENTER, 0));
        this.children.add(noSounds = new Label(new FiguraText("gui.error.no_sounds").withStyle(ChatFormatting.YELLOW), x + width / 2, y + height / 2, TextUtils.Alignment.CENTER, 0));
        noOwner.centerVertically = noSounds.centerVertically = true;

        noOwner.setVisible(owner == null);
        noSounds.setVisible(!noOwner.isVisible() && sounds.isEmpty());
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        //background and scissors
        UIHelper.renderSliced(stack, getX(), getY(), getWidth(), getHeight(), UIHelper.OUTLINE_FILL);
        UIHelper.setupScissor(getX() + scissorsX, getY() + scissorsY, getWidth() + scissorsWidth, getHeight() + scissorsHeight);

        if (!sounds.isEmpty())
            updateEntries();

        //children
        super.render(stack, mouseX, mouseY, delta);

        //reset scissor
        UIHelper.disableScissor();
    }

    private void updateEntries() {
        //scrollbar
        int totalHeight = -4;
        for (SoundElement sound : sounds)
            totalHeight += sound.getHeight() + 8;
        int entryHeight = sounds.isEmpty() ? 0 : totalHeight / sounds.size();

        scrollBar.setVisible(totalHeight > getHeight());
        scrollBar.setScrollRatio(entryHeight, totalHeight - getHeight());

        //render list
        int xOffset = scrollBar.isVisible() ? 4 : 11;
        int yOffset = scrollBar.isVisible() ? (int) -(Mth.lerp(scrollBar.getScrollProgress(), -4, totalHeight - getHeight())) : 4;
        for (SoundElement sound : sounds) {
            sound.setX(getX() + xOffset);
            sound.setY(getY() + yOffset);
            yOffset += sound.getHeight() + 8;
        }
    }

    private void updateList() {
        //clear old widgets
        sounds.forEach(children::remove);

        //add new sounds
        if (owner == null)
            return;

        for (Map.Entry<String, SoundBuffer> entry : owner.customSounds.entrySet()) {
            SoundElement sound = new SoundElement(getWidth() - 22, entry.getKey(), entry.getValue(), this, owner);
            sounds.add(sound);
            children.add(sound);
        }

        sounds.sort(SoundElement::compareTo);

        if (!sounds.isEmpty())
            selected = sounds.get(0);
    }

    public LuaSound getSound() {
        return selected != null ? selected.getSound() : null;
    }

    private static class SoundElement extends AbstractContainerElement implements Comparable<SoundElement> {

        private final Component size;
        private final String name;
        private final SoundBuffer sound;
        private final Avatar owner;
        private final SoundsList parent;

        private final ParentedButton play, stop;

        public SoundElement(int width, String name, SoundBuffer sound, SoundsList parent, Avatar owner) {
            super(0, 0, width, 20);
            this.name = name;
            this.sound = sound;
            this.owner = owner;
            this.parent = parent;

            int len = owner.nbt.getCompound("sounds").getByteArray(name).length;
            this.size = new TextComponent("(" + MathUtils.asFileSize(len) + ")").withStyle(ChatFormatting.GRAY);

            //play button
            children.add(0, play = new ParentedButton(0, 0, 20, 20, 0, 0, 20, new FiguraIdentifier("textures/gui/play.png"), 60, 20, new FiguraText("gui.sound.play"), this, button -> {}) {
                @Override
                public void playDownSound(SoundManager soundManager) {
                    Vec3 vec =  Minecraft.getInstance().player == null ? new Vec3(0, 0, 0) : Minecraft.getInstance().player.position();
                    getSound().pos(vec.x, vec.y, vec.z).play();
                }
            });

            //stop button
            children.add(stop = new ParentedButton(0, 0, 20, 20, 0, 0, 20, new FiguraIdentifier("textures/gui/stop.png"), 60, 20, new FiguraText("gui.sound.stop"), this,
                    button -> SoundAPI.getSoundEngine().figura$stopSound(owner.owner, name))
            );
        }

        @Override
        public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
            if (!this.isVisible()) return;

            int x = getX();
            int y = getY();
            int width = getWidth();
            int height = getHeight();

            //selected outline
            if (parent.selected == this)
                UIHelper.fillOutline(stack, x - 1, y - 1, width + 2, height + 2, 0xFFFFFFFF);

            //vars
            Font font = Minecraft.getInstance().font;
            int textY = y + height / 2 - font.lineHeight / 2;

            //hovered arrow
            setHovered(isMouseOver(mouseX, mouseY));
            if (isHovered()) font.draw(stack, HOVERED_ARROW, x + 4, textY, 0xFFFFFF);

            //render name
            font.draw(stack, this.name, x + 16, textY, 0xFFFFFF);

            //render size
            font.draw(stack, size, x + width - 96 - font.width(size), textY, 0xFFFFFF);

            //render children
            super.render(stack, mouseX, mouseY, delta);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            boolean clicked = super.mouseClicked(mouseX, mouseY, button);
            if (!clicked) {
                if (isMouseOver(mouseX, mouseY)) {
                    parent.selected = this;
                    return true;
                }
            }
            return clicked;
        }

        @Override
        public void setX(int x) {
            super.setX(x);
            play.setX(x + getWidth() - 64);
            stop.setX(x + getWidth() - 40);
        }

        @Override
        public void setY(int y) {
            super.setY(y);
            play.setY(y);
            stop.setY(y);
        }

        public LuaSound getSound() {
            return new LuaSound(sound, name, owner);
        }

        @Override
        public int compareTo(SoundElement o) {
            return this.name.compareTo(o.name);
        }
    }
}
