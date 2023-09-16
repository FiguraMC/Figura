package org.figuramc.figura.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.gui.widgets.Label;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class GameScreen extends AbstractPanelScreen {

    private static final int[][] RULES = {
            {0, 0, 0, 1, 0, 0, 0, 0, 0}, // dead
            {0, 0, 1, 1, 0, 0, 0, 0, 0} // alive
    };

    private Label keys, stats;
    private Grid grid;
    private boolean paused = false;
    private static int scale = 5;

    protected GameScreen(Screen parentScreen) {
        super(parentScreen, Component.empty());
    }

    @Override
    public Class<? extends Screen> getSelectedPanel() {
        return parentScreen.getClass();
    }

    protected void init() {
        super.init();
        this.removeWidget(panels);

        addRenderableOnly(grid = new Grid(width, height));

        // back button
        addRenderableWidget(new Button(this.width - 20, 4, 16, 16, 0, 0, 16, new FiguraIdentifier("textures/gui/search_clear.png"), 48, 16, FiguraText.of("gui.done"), bx -> onClose()));

        // text
        addRenderableWidget(keys = new Label(
                Component.empty()
                        .append(Component.literal("[R]").withStyle(FiguraMod.getAccentColor()))
                        .append(" restart, ")
                        .append(Component.literal("[P]").withStyle(FiguraMod.getAccentColor()))
                        .append(" pause, ")
                        .append(Component.literal("[SPACE]").withStyle(FiguraMod.getAccentColor()))
                        .append(" step")
                        .append("\n")
                        .append(Component.literal("[F1]").withStyle(FiguraMod.getAccentColor()))
                        .append(" hide text, ")
                        .append(Component.literal("[Scroll]").withStyle(FiguraMod.getAccentColor()))
                        .append(" scale (restarts)"),
                4, 4, 0)
        );
        addRenderableWidget(stats = new Label("", 4, keys.getRawY() + keys.getHeight(), 0));
    }

    @Override
    public void tick() {
        super.tick();
        if (!paused) grid.tick();
        stats.setText(
                Component.literal("Generation")
                        .append(Component.literal(" " + grid.gen).withStyle(FiguraMod.getAccentColor()))
                        .append(", Scale")
                        .append(Component.literal(" " + scale).withStyle(FiguraMod.getAccentColor()))
        );
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_R -> grid.init();
            case GLFW.GLFW_KEY_P -> paused = !paused;
            case GLFW.GLFW_KEY_SPACE -> grid.tick();
            case GLFW.GLFW_KEY_F1 -> {
                keys.setVisible(!keys.isVisible());
                stats.setVisible(!stats.isVisible());
            }
            default -> {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount, double d) {
        scale = (int) Math.max(1, scale + Math.signum(amount));
        grid.init();
        return true;
    }

    private static class Grid implements Renderable {

        private Cell[][] grid;
        private final int width, height;
        private long gen = 0;

        private Grid(int width, int height) {
            this.width = width;
            this.height = height;
            init();
        }

        private void init() {
            gen = 0;
            int width = this.width / scale;
            int height = this.height / scale;

            // create grid
            grid = new Cell[width][height];

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    // create cell :D
                    Cell cell = new Cell(i, j, (int) Math.round(Math.random()));

                    // neighbours
                    if (i > 0) {
                        if (j > 0) cell.addNeighbor(grid[i - 1][j - 1]); // top left
                        cell.addNeighbor(grid[i - 1][j]); // top middle
                        if (j < height - 1) cell.addNeighbor(grid[i - 1][j + 1]); // top right
                    }
                    if (j > 0) cell.addNeighbor(grid[i][j - 1]); // left

                    grid[i][j] = cell;
                }
            }
        }

        private void tick() {
            gen++;
            for (Cell[] cells : grid)
                for (Cell cell : cells)
                    cell.update();
        }

        @Override
        public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
            PoseStack pose = gui.pose();
            pose.pushPose();
            pose.scale(scale, scale, scale);

            for (Cell[] cells : grid) {
                for (Cell cell : cells) {
                    cell.render(gui);
                }
            }

            pose.popPose();
        }
    }

    private static class Cell {
        private final int x, y;
        private int alive, future;
        private final ArrayList<Cell> neighbors = new ArrayList<>();
        private int color = 0xFFFFFFFF;

        private Cell(int x, int y, int alive) {
            this.x = x;
            this.y = y;
            this.alive = alive;
            this.future = alive;
        }

        private void addNeighbor(Cell cell) {
            this.neighbors.add(cell);
            cell.neighbors.add(this);
        }

        private void update() {
            int neigh = 0;
            for (Cell cell : neighbors)
                neigh += cell.alive;
            this.future = RULES[this.alive][neigh];
        }

        private void render(GuiGraphics gui) {
            this.alive = this.future;
            if (this.alive == 1)
                gui.fill(this.x, this.y, this.x + 1, this.y + 1, color);
        }
    }
}
