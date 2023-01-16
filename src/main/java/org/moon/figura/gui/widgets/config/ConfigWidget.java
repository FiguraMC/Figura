package org.moon.figura.gui.widgets.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.moon.figura.config.Config;
import org.moon.figura.gui.screens.ConfigScreen;
import org.moon.figura.gui.widgets.AbstractContainerElement;
import org.moon.figura.gui.widgets.ContainerButton;
import org.moon.figura.gui.widgets.lists.ConfigList;
import org.moon.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.List;

public class ConfigWidget extends AbstractContainerElement {

    protected final List<AbstractConfigElement> entries = new ArrayList<>();
    private final ConfigList parent;
    private ContainerButton parentConfig;

    public ConfigWidget(int width, Config config, ConfigList parent) {
        super(0, 0, width, 20);
        this.parent = parent;

        this.parentConfig = new ContainerButton(parent, x, y, width, 20, config == null ? Component.empty() : config.name, config == null ? null : config.tooltip, button -> {
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

    public boolean isChanged() {
        for (AbstractConfigElement entry : entries)
            if (entry.isChanged())
                return true;
        return false;
    }

    public int getHeight() {
        return parentConfig.isToggled() ? height : 20;
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;

        this.parentConfig.setX(x);
        this.parentConfig.setY(y);

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
}
