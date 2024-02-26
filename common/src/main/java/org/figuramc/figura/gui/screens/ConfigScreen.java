package org.figuramc.figura.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.figuramc.figura.config.ConfigManager;
import org.figuramc.figura.config.ConfigType;
import org.figuramc.figura.gui.PaperDoll;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.gui.widgets.Label;
import org.figuramc.figura.gui.widgets.SearchBar;
import org.figuramc.figura.gui.widgets.lists.ConfigList;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.IOUtils;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ConfigScreen extends AbstractPanelScreen {

    public static final Map<ConfigType.Category, Boolean> CATEGORY_DATA = new HashMap<>();

    private ConfigList list;
    private Button cancel;
    private final boolean hasPanels;
    public boolean renderPaperdoll;

    public ConfigScreen(Screen parentScreen) {
        this(parentScreen, true);
    }

    public ConfigScreen(Screen parentScreen, boolean enablePanels) {
        super(parentScreen, FiguraText.of("gui.panels.title.settings"));
        this.hasPanels = enablePanels;
    }

    @Override
    protected void init() {
        super.init();
        loadNbt();

        if (!hasPanels) {
            this.removeWidget(panels);
            Label l;
            this.addRenderableWidget(l = new Label(getTitle(), this.width / 2, 14, TextUtils.Alignment.CENTER));
            l.centerVertically = true;
        }

        // -- middle -- // 

        int width = Math.min(this.width - 8, 420);
        list = new ConfigList((this.width - width) / 2, 52, width, height - 80, this);

        this.addRenderableWidget(new SearchBar(this.width / 2 - 122, 28, 244, 20, query -> list.updateSearch(query.toLowerCase(Locale.US))));
        this.addRenderableWidget(list);

        // -- bottom buttons -- // 

        // cancel
        this.addRenderableWidget(cancel = new Button(this.width / 2 - 122, height - 24, 120, 20, FiguraText.of("gui.cancel"), null, button -> {
            ConfigManager.discardConfig();
            list.updateList();
        }));
        cancel.setActive(false);

        // done
        addRenderableWidget(new Button(this.width / 2 + 2, height - 24, 120, 20, FiguraText.of("gui.done"), null, button -> onClose()));
    }

    @Override
    public void tick() {
        super.tick();
        this.cancel.setActive(list.hasChanges());
    }

    @Override
    public void removed() {
        ConfigManager.applyConfig();
        ConfigManager.saveConfig();
        saveNbt();
        super.removed();
    }

    @Override
    public void renderBackground(GuiGraphics gui, float delta) {
        super.renderBackground(gui, delta);
        if (renderPaperdoll)
            UIHelper.renderWithoutScissors(gui, g -> PaperDoll.render(g, true));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return list.updateKey(InputConstants.Type.MOUSE.getOrCreate(button)) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return list.updateKey(keyCode == 256 ? InputConstants.UNKNOWN : InputConstants.getKey(keyCode, scanCode)) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    private static void loadNbt() {
        IOUtils.readCacheFile("settings", nbt -> {
            ListTag groupList = nbt.getList("settings", Tag.TAG_COMPOUND);
            for (Tag tag : groupList) {
                CompoundTag compound = (CompoundTag) tag;

                String config = compound.getString("config");
                boolean expanded = compound.getBoolean("expanded");
                CATEGORY_DATA.put(ConfigManager.CATEGORIES_REGISTRY.get(config), expanded);
            }
        });
    }

    private static void saveNbt() {
        IOUtils.saveCacheFile("settings", nbt -> {
            ListTag list = new ListTag();

            for (Map.Entry<ConfigType.Category, Boolean> entry : CATEGORY_DATA.entrySet()) {
                CompoundTag compound = new CompoundTag();
                compound.putString("config", entry.getKey().id);
                compound.putBoolean("expanded", entry.getValue());
                list.add(compound);
            }

            nbt.put("settings", list);
        });
    }

    public static void clearCache() {
        IOUtils.deleteCacheFile("settings");
    }
}
