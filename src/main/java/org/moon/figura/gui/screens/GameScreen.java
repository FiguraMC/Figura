package org.moon.figura.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;
import org.moon.figura.gui.widgets.Label;
import org.moon.figura.gui.widgets.TexturedButton;
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

    private static final String EGG = "fran";
    private String egg = "";

    protected GameScreen(Screen parentScreen) {
        super(parentScreen, TextComponent.EMPTY.copy(), 2);
    }

    protected void init() {
        super.init();
        this.removeWidget(panels);

        addRenderableOnly(grid = new Grid(width, height));

        //back button
        addRenderableWidget(new TexturedButton(this.width - 28, 4, 24, 24, 0, 0, 24, new FiguraIdentifier("textures/gui/back.png"), 72, 24, new FiguraText("gui.back"),
            bx -> this.minecraft.setScreen(parentScreen)
        ));

        //text
        addRenderableOnly(keys = new Label("[R] restart, [P] pause, [SPACE] single step\n[F1] hide text, [Scroll] change scale (restarts)", 4, 4, false, 0));
        addRenderableOnly(stats = new Label("Iterations " + grid.iterations + ", Scale " + scale, 4, keys.y + keys.height, false, 0));
    }

    @Override
    public void tick() {
        super.tick();
        if (!paused) grid.tick();
        stats.setText("Iterations " + grid.iterations + ", Scale " + scale);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R)
            grid.init();
        else if (keyCode == GLFW.GLFW_KEY_P)
            paused = !paused;
        else if (keyCode == GLFW.GLFW_KEY_SPACE)
            grid.tick();
        else if (keyCode == GLFW.GLFW_KEY_F1) {
            keys.setVisible(!keys.isVisible());
            stats.setVisible(!stats.isVisible());
        } else
            return super.keyPressed(keyCode, scanCode, modifiers);

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        scale = (int) Math.max(1, scale + Math.signum(amount));
        grid.init();
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        egg += String.valueOf(chr).toLowerCase();
        if (egg.equals(EGG.substring(0, egg.length()))) {
            if (egg.length() == EGG.length()) {
                egg = "";
                grid.yes = true;
            }
            return true;
        } else {
            egg = "";
            return false;
        }
    }

    private static class Grid implements Widget {

        private Cell[][] grid;
        private final int width, height;
        private long iterations = 0;
        private boolean yes = false;

        private Grid(int width, int height) {
            this.width = width;
            this.height = height;
            init();
        }

        private void init() {
            iterations = 0;
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
            iterations++;
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
                    if (yes) cell.color = 0xFF000000 + ColorUtils.rgbToInt(ColorUtils.rainbow());
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
