package org.moon.figura.gui.widgets.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.config.Config;
import org.moon.figura.gui.widgets.AbstractContainerElement;
import org.moon.figura.gui.widgets.SwitchButton;
import org.moon.figura.gui.widgets.lists.ConfigList;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.List;

public class ConfigWidget extends AbstractContainerElement {

    protected final List<AbstractConfigElement> entries = new ArrayList<>();
    private final ConfigList parent;
    private ContainerButton parentConfig;

    public ConfigWidget(int width, Component name, Component tooltip, ConfigList parent) {
        super(0, 0, width, 20);
        this.parent = parent;

        this.parentConfig = new ContainerButton(parent, x, y, width, 20, name, tooltip, button -> {
            setShowChildren(this.parentConfig.isToggled());
            parent.updateScroll();
        });

        this.parentConfig.setToggled(true);
        this.parentConfig.shouldHaveBackground(false);
        children.add(this.parentConfig);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        //children background
        if (parentConfig.isToggled() && entries.size() > 0)
            UIHelper.fill(stack, x, y + 21, x + width, y + height, 0x11FFFFFF);

        //children
        super.render(stack, mouseX, mouseY, delta);
    }

    public void addConfig(Config config) {
        AbstractConfigElement element = switch (config.type) {
            case BOOLEAN -> new BooleanElement(width, config, parent);
            case ENUM -> new EnumElement(width, config, parent);
            case INPUT -> new InputElement(width, config, parent);
            case KEYBIND -> new KeybindElement(width, config, parent);
            default -> null;
        };

        if (element == null)
            return;

        this.height += 22;
        this.children.add(element);
        this.entries.add(element);
    }

    public int getHeight() {
        return parentConfig.isToggled() ? height : 20;
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;

        this.parentConfig.x = x;
        this.parentConfig.y = y;

        for (int i = 0; i < entries.size(); i++)
            entries.get(i).setPos(x, y + 22 * (i + 1));
    }

    public void setShowChildren(boolean bool) {
        this.parentConfig.setToggled(bool);
        for (AbstractConfigElement element : entries)
            element.setVisible(bool);
    }

    public boolean isShowingChildren() {
        return parentConfig.isToggled();
    }

    public static class ContainerButton extends SwitchButton {

        private final ConfigList parent;

        public ContainerButton(ConfigList parent, int x, int y, int width, int height, Component text, Component tooltip, OnPress pressAction) {
            super(x, y, width, height, text, tooltip, pressAction);
            this.parent = parent;
        }

        @Override
        protected void renderText(PoseStack stack) {
            //get text color
            int color = (!this.active || !this.isToggled() ? ChatFormatting.DARK_GRAY : ChatFormatting.WHITE).getColor();

            //draw text
            Font font = Minecraft.getInstance().font;
            font.drawShadow(
                    stack, getMessage(),
                    this.x + 3, this.y + this.height / 2f - font.lineHeight / 2f,
                    color
            );

            //draw arrow
            Component arrow = new TextComponent(this.toggled ? "V" : "^").setStyle(Style.EMPTY.withFont(TextUtils.FIGURA_FONT));
            font.drawShadow(
                    stack, arrow,
                    this.x + this.width - font.width(arrow) - 3, this.y + this.height / 2f - font.lineHeight / 2f,
                    color
            );
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
        }
    }
}
