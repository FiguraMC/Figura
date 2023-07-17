package org.figuramc.figura.gui.screens;

import net.minecraft.client.gui.screens.Screen;
import org.figuramc.figura.config.ConfigType;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.utils.FiguraText;

public class NetworkFilterScreen extends AbstractPanelScreen {
    private final ConfigType.NetworkFilterConfig config = Configs.NETWORK_FILTER;
    public NetworkFilterScreen(Screen parentScreen) {
        super(parentScreen, FiguraText.of("gui.config.network_filter_list"));
    }

    @Override
    protected void init() {
        super.init();

        removeWidget(panels);
    }
}
