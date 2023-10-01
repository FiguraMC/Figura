package org.figuramc.figura.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4i;

import java.util.*;

public class ClickableTextHelper {
    protected TextLine[] lines;
    protected HashMap<Vector4i, String> clickUrls = new HashMap<>();
    protected HashMap<Vector4i, Component> hoverText = new HashMap<>();

    protected boolean dirty = true;
    protected Component message;

    public void setMessage(@Nullable Component message) {
        if (this.message == message) return;
        this.message = message;
        if (message == null) {
            clear();
            return;
        }
        dirty = true;
    }

    public void renderDebug(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        for (Vector4i area : hoverText.keySet()) {
            graphics.renderOutline(x + area.x, y + area.y, area.z - area.x, area.w - area.y, isPointWithinBounds(area, x, y, mouseX, mouseY) ? 0xFF00FF00 : 0xFFFF00FF);
        }
        for (Vector4i area : clickUrls.keySet()) {
            graphics.renderOutline(x + area.x, y + area.y, area.z - area.x, area.w - area.y, isPointWithinBounds(area, x, y, mouseX, mouseY) ? 0xFF00FF00 : 0xFFFF00FF);
        }
        graphics.renderOutline(mouseX-1, mouseY-1, 3, 3, 0xFF00FFFF);
    }

    public void update(Font font, int lineWidth) {
        if (!dirty || message == null) return;
        dirty = false;

        clear();

        List<TextLine> lines = new ArrayList<>();
        List<FormattedText> split = font.getSplitter().splitLines(message, lineWidth, Style.EMPTY);
        for (FormattedText curLine : split) {
            List<TextNode> nodes = new ArrayList<>();

            // Convert the Text into a list
            TextUtils.formattedTextToText(curLine).visit((style, string) -> {
                nodes.add(new TextNode(string, style));
                return Optional.empty();
            }, Style.EMPTY);

            lines.add(new TextLine(nodes.toArray(new TextNode[0])));
        }

        this.lines = lines.toArray(new TextLine[0]);

        // Finds all the hover/click events and stores them for later
        visit((a, style, x, y, w, h) -> {
            ClickEvent clickEvent = style.getClickEvent();
            HoverEvent hoverEvent = style.getHoverEvent();
            if (clickEvent == null && hoverEvent == null) return;

            // Calculates the bounding box of the text, for calculating if the mouse is hovering
            Vector4i rect = new Vector4i(x, y, x + w, y + h);

            if (clickEvent != null) {
                if (clickEvent.getAction() == ClickEvent.Action.OPEN_URL) {
                    clickUrls.put(rect, clickEvent.getValue());
                }
            }

            if (hoverEvent != null) {
                Object value = hoverEvent.getValue(hoverEvent.getAction());
                if (value instanceof Component component) {
                    hoverText.put(rect, component);
                }
            }
        });
    }

    public @Nullable Component getHoverTooltip(int cx, int cy, int mouseX, int mouseY) {
        for (Vector4i area : hoverText.keySet()) {
            if (isPointWithinBounds(area, cx, cy, mouseX, mouseY)) {
                return hoverText.get(area);
            }
        }
        return null;
    }

    public @Nullable String getClickLink(int cx, int cy, int mouseX, int mouseY) {
        for (Vector4i area : clickUrls.keySet()) {
            if (isPointWithinBounds(area, cx, cy, mouseX, mouseY)) {
                return clickUrls.get(area);
            }
        }
        return null;
    }

    public int lineCount() {
        return lines == null ? 1 : lines.length;
    }

    private static boolean isPointWithinBounds(Vector4i area, int xOffset, int yOffset, int x, int y) {
        final int x1 = area.x + xOffset;
        final int y1 = area.y + yOffset;
        final int x2 = area.z + xOffset;
        final int y2 = area.w + yOffset;

        return x > x1 && x < x2 && y > y1 && y < y2;
    }

    public void visit(MultilineTextVisitor visitor) {
        if (lines == null) return;
        Font font = Minecraft.getInstance().font;
        int y = 0;
        for (TextLine line : lines) {
            int x = 0;
            for (TextNode node : line.nodes) {
                int width = node.getWidth(font);
                visitor.visit(node.text, node.style, x, y, width, font.lineHeight);
                x += width;
            }
            y += font.lineHeight;
        }
    }

    public void clear() {
        lines = new TextLine[0];
        clickUrls.clear();
        hoverText.clear();
    }

    public void markDirty() {
        dirty = true;
    }

    @FunctionalInterface
    public interface MultilineTextVisitor {
        void visit(String text, Style style, int x, int y, int textWidth, int textHeight);
    }

    protected record TextLine(TextNode[] nodes) { }
    protected record TextNode(String text, Style style) {
        public int getWidth(Font font) {
            return font.width(asText());
        }

        public Component asText() {
            return Component.literal(text).withStyle(style);
        }
    }
}
