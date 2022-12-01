package org.moon.figura.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import org.moon.figura.config.Config;
import org.moon.figura.config.ConfigManager;
import org.moon.figura.gui.widgets.Label;
import org.moon.figura.gui.widgets.TexturedButton;
import org.moon.figura.gui.widgets.lists.ConfigList;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.IOUtils;

import java.util.HashMap;
import java.util.Map;

public class ConfigScreen extends AbstractPanelScreen {

    public static final Component TITLE = new FiguraText("gui.panels.title.settings");

    public static final Map<Config, Boolean> CATEGORY_DATA = new HashMap<>();

    private ConfigList list;
    private TexturedButton cancel;
    private final boolean hasPanels;

    public ConfigScreen(Screen parentScreen) {
        this(parentScreen, true);
    }

    public ConfigScreen(Screen parentScreen, boolean enablePanels) {
        super(parentScreen, TITLE, ConfigScreen.class);
        this.hasPanels = enablePanels;
    }

    @Override
    public Component getTitle() {
        return TITLE;
    }

    @Override
    protected void init() {
        super.init();
        loadNbt();

        if (!hasPanels) {
            this.removeWidget(panels);
            this.addRenderableOnly(new Label(TITLE, this.width / 2, 14, true));
        }

        // -- bottom buttons -- //

        //cancel
        this.addRenderableWidget(cancel = new TexturedButton(width / 2 - 122, height - 24, 120, 20, new FiguraText("gui.cancel"), null, button -> {
            ConfigManager.discardConfig();
            list.updateList();
        }));

        //done
        addRenderableWidget(new TexturedButton(width / 2 + 4, height - 24, 120, 20, new FiguraText("gui.done"), null,
                button -> this.minecraft.setScreen(parentScreen)
        ));

        // -- config list -- //

        int width = Math.min(this.width - 8, 420);
        this.addRenderableWidget(list = new ConfigList((this.width - width) / 2, 28, width, height - 56));
    }

    @Override
    public void tick() {
        super.tick();
        this.cancel.active = list.hasChanges();
    }

    @Override
    public void removed() {
        ConfigManager.applyConfig();
        ConfigManager.saveConfig();
        saveNbt();
        super.removed();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        KeyMapping bind = list.focusedBinding;
        //attempt to set keybind
        if (bind != null) {
            bind.setKey(InputConstants.Type.MOUSE.getOrCreate(button));
            list.focusedBinding = null;
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        KeyMapping bind = list.focusedBinding;
        //attempt to set keybind
        if (bind != null) {
            bind.setKey(keyCode == 256 ? InputConstants.UNKNOWN: InputConstants.getKey(keyCode, scanCode));
            list.focusedBinding = null;
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    private void loadNbt() {
        IOUtils.readCacheFile("settings", nbt -> {
            ListTag groupList = nbt.getList("settings", Tag.TAG_COMPOUND);
            for (Tag tag : groupList) {
                CompoundTag compound = (CompoundTag) tag;

                String config = compound.getString("config");
                boolean expanded = compound.getBoolean("expanded");
                CATEGORY_DATA.put(Config.valueOf(config), expanded);
            }
        });
    }

    private void saveNbt() {
        IOUtils.saveCacheFile("settings", nbt -> {
            ListTag list = new ListTag();

            for (Map.Entry<Config, Boolean> entry : CATEGORY_DATA.entrySet()) {
                CompoundTag compound = new CompoundTag();
                compound.putString("config", entry.getKey().name());
                compound.putBoolean("expanded", entry.getValue());
                list.add(compound);
            }

            nbt.put("settings", list);
        });
    }
}
