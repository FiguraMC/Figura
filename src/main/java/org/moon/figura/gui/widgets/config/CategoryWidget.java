package org.moon.figura.gui.widgets.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.moon.figura.config.ConfigType;
import org.moon.figura.config.Configs;
import org.moon.figura.gui.screens.ConfigScreen;
import org.moon.figura.gui.widgets.AbstractContainerElement;
import org.moon.figura.gui.widgets.ContainerButton;
import org.moon.figura.gui.widgets.lists.ConfigList;
import org.moon.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.List;

public class CategoryWidget extends AbstractContainerElement {

    protected final List<AbstractConfigElement> entries = new ArrayList<>();
    private final ConfigType.Category config;
    private final ConfigList parent;
    private ContainerButton parentConfig;

    public CategoryWidget(int width, ConfigType.Category config, ConfigList parent) {
        super(0, 0, width, 20);
        this.config = config;
        this.parent = parent;

        this.parentConfig = new ContainerButton(parent, 0, 0, width, 20, config == null ? Component.empty() : config.name, config == null ? null : config.tooltip, button -> {
            boolean toggled = this.parentConfig.isToggled();
            setShowChildren(toggled);
            ConfigScreen.CATEGORY_DATA.put(config, toggled);
            parent.updateScroll();
        });

        Boolean expanded = ConfigScreen.CATEGORY_DATA.get(config);
        this.parentConfig.setToggled(expanded == null || expanded);
        this.parentConfig.shouldHaveBackground(false);
        children.add(this.parentConfig);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        //children background
        if (parentConfig.isToggled() && entries.size() > 0)
            UIHelper.fill(stack, getX(), getY() + 21, getX() + getWidth(), getY() + getHeight(), 0x11FFFFFF);

        if (config == Configs.PAPERDOLL)
            parent.parentScreen.renderPaperdoll = parentConfig.isToggled() && parent.isMouseOver(mouseX, mouseY) && isMouseOver(mouseX, mouseY);

        //children
        super.render(stack, mouseX, mouseY, delta);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return UIHelper.isMouseOver(getX(), getY(), getWidth(), getHeight(), mouseX, mouseY);
    }

    public void addConfig(ConfigType<?> config) {
        int width = getWidth();
        AbstractConfigElement element;
        if (config instanceof ConfigType.BoolConfig boolConfig) {
            element = new BooleanElement(width, boolConfig, parent);
        } else if (config instanceof ConfigType.EnumConfig enumConfig) {
            element = new EnumElement(width, enumConfig, parent);
        } else if (config instanceof ConfigType.InputConfig<?> inputConfig) {
            element = new InputElement(width, inputConfig, parent);
        } else if (config instanceof ConfigType.KeybindConfig keybindConfig) {
            element = new KeybindElement(width, keybindConfig, parent);
        } else if (config instanceof ConfigType.ButtonConfig buttonConfig) {
            element = new ButtonElement(width, buttonConfig, parent);
        } else {
            return;
        }

        this.setHeight(super.getHeight() + 22);
        this.children.add(element);
        this.entries.add(element);
    }

    public boolean isChanged() {
        for (AbstractConfigElement entry : entries)
            if (entry.isChanged())
                return true;
        return false;
    }

    public int getHeight() {
        return parentConfig.isToggled() ? super.getHeight() : 20;
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        this.parentConfig.setX(x);
        for (AbstractConfigElement entry : entries)
            entry.setX(x);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        this.parentConfig.setY(y);
        for (int i = 0; i < entries.size(); i++)
            entries.get(i).setY(y + 22 * (i + 1));
    }

    public void setShowChildren(boolean bool) {
        this.parentConfig.setToggled(bool);
        for (AbstractConfigElement element : entries)
            element.setVisible(bool);
    }

    public boolean isShowingChildren() {
        return parentConfig.isToggled();
    }

    public void updateKeybinds() {
        for (AbstractConfigElement element : entries) {
            if (element instanceof KeybindElement keybind)
                keybind.updateText();
        }
    }
}
