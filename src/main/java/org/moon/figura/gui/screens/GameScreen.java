package org.moon.figura.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.moon.figura.FiguraMod;
import org.moon.figura.gui.widgets.Label;
import org.moon.figura.gui.widgets.Button;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.ui.UIHelper;

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

    private static final String EGG = "FRAN";
    private String egg = EGG;

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

        //back button
        addRenderableWidget(new Button(this.width - 28, 4, 24, 24, 0, 0, 24, new FiguraIdentifier("textures/gui/back.png"), 72, 24, FiguraText.of("gui.done"),
            bx -> this.minecraft.setScreen(parentScreen)
        ));

        //text
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
        egg += (char) keyCode;
        egg = egg.substring(1);
        if (EGG.equals(egg)) {
            grid.yes = true;
        } else {
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
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        scale = (int) Math.max(1, scale + Math.signum(amount));
        grid.init();
        return true;
    }

    private static class Grid implements Renderable {

        private Cell[][] grid;
        private final int width, height;
        private long gen = 0;
        private boolean yes = false;

        private Grid(int width, int height) {
            this.width = width;
            this.height = height;
            init();
        }

        private void init() {
            gen = 0;
            int width = this.width / scale;
            int height = this.height / scale;

            //create grid
            grid = new Cell[width][height];

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    //create cell :D
                    Cell cell = new Cell(i, j, (int) Math.round(Math.random()));

                    //neighbours
                    if (i > 0) {
                        if (j > 0) cell.addNeighbor(grid[i - 1][j - 1]); //top left
                        cell.addNeighbor(grid[i - 1][j]); //top middle
                        if (j < height - 1) cell.addNeighbor(grid[i - 1][j + 1]); //top right
                    }
                    if (j > 0) cell.addNeighbor(grid[i][j - 1]); //left

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
        public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
            stack.pushPose();
            stack.scale(scale, scale, scale);

            for (Cell[] cells : grid) {
                for (Cell cell : cells) {
                    if (yes) cell.color = 0xFF000000 + ColorUtils.rgbToInt(ColorUtils.hsvToRGB(FiguraVec3.of((FiguraMod.ticks % 360) / 360f, 1f, 1f)));
                    cell.render(stack);
                }
            }

            stack.popPose();
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

        private void render(PoseStack stack) {
            this.alive = this.future;
            if (this.alive == 1)
                UIHelper.fill(stack, this.x, this.y, this.x + 1, this.y + 1, color);
        }
    }
}
