package org.figuramc.figura.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import org.figuramc.figura.ducks.ParticleEngineAccessor;
import org.figuramc.figura.utils.ui.UIHelper;

public class ParticleWidget implements FiguraWidget, FiguraTickable, FiguraRemovable {

    private final SpriteSet sprite;

    private final int x;
    private float lastY, y;
    private float lastSize, size;
    private final float initialSize;
    private boolean visible = true;
    private boolean removed;

    public ParticleWidget(int x, int y, ParticleType<?> particle) {
        this.x = x;
        this.lastY = this.y = y;
        this.sprite = getParticle(particle);

        this.initialSize = this.lastSize = this.size = (int) (8 * (Math.random() + 1));
        removed = sprite == null;
    }

    @Override
    public void tick() {
        if (!visible || removed)
            return;

        lastY = y;
        lastSize = size;
        y -= 1f;
        size -= 1f;
        removed = size <= 0;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        if (!visible || removed)
            return;

        float size = Mth.lerp(delta, lastSize, this.size);
        float y = Mth.lerp(delta, lastY, this.y);
        UIHelper.renderSprite(gui, (int) (x - size / 2f), (int) (y - size / 2f), 0, (int) size, (int) size, sprite.get((int) (initialSize - size), (int) initialSize));
    }

    private static SpriteSet getParticle(ParticleType<?> particleType) {
        return ((ParticleEngineAccessor) Minecraft.getInstance().particleEngine).figura$getParticleSprite(BuiltInRegistries.PARTICLE_TYPE.getKey(particleType));
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isRemoved() {
        return removed;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public void setX(int x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getY() {
        return (int) y;
    }

    @Override
    public void setY(int y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getWidth() {
        return (int) size;
    }

    @Override
    public void setWidth(int width) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getHeight() {
        return (int) size;
    }

    @Override
    public void setHeight(int height) {
        throw new UnsupportedOperationException();
    }
}
