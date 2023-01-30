package org.moon.figura.gui.widgets.permissions;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.backend2.NetworkStuff;
import org.moon.figura.gui.widgets.StatusWidget;
import org.moon.figura.permissions.Permissions;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.MathUtils;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class PlayerStatusWidget extends StatusWidget {

    private static final List<Function<Avatar, MutableComponent>> HOVER_TEXT = List.of(
            avatar -> new FiguraText("gui.permissions.size")
                    .append("\n• ").append(MathUtils.asFileSize(avatar.fileSize)),
            avatar -> new FiguraText("gui.permissions.complexity")
                    .append("\n• ").append(String.valueOf(avatar.complexity.pre)),
            avatar -> new FiguraText("gui.permissions.init")
                    .append("\n• ").append(new FiguraText("gui.permissions.init.root", avatar.init.pre))
                    .append("\n• ").append(new FiguraText("gui.permissions.init.entity", avatar.init.post)),
            avatar -> new FiguraText("gui.permissions.tick")
                    .append("\n• ").append(new FiguraText("gui.permissions.tick.world", avatar.worldTick.pre))
                    .append("\n• ").append(new FiguraText("gui.permissions.tick.entity", avatar.tick.pre)),
            avatar -> new FiguraText("gui.permissions.render")
                    .append("\n• ").append(new FiguraText("gui.permissions.render.world", avatar.worldRender.pre))
                    .append("\n• ").append(new FiguraText("gui.permissions.render.entity", avatar.render.pre))
                    .append("\n• ").append(new FiguraText("gui.permissions.render.post_entity", avatar.render.post))
                    .append("\n• ").append(new FiguraText("gui.permissions.render.post_world", avatar.worldRender.post))
    );

    private final UUID owner;
    private Avatar avatar;

    public PlayerStatusWidget(int x, int y, int width, UUID owner) {
        super(x, y, width, HOVER_TEXT.size());
        setBackground(false);

        this.owner = owner;
    }

    @Override
    public void tick() {
        avatar = AvatarManager.getAvatarForPlayer(owner);
        if (avatar == null || avatar.nbt == null) {
            status = 0;
            return;
        }

        //size
        status = avatar.fileSize > NetworkStuff.getSizeLimit() ? 1 : avatar.fileSize > NetworkStuff.getSizeLimit() * 0.75 ? 2 : 3;

        //complexity
        int complexity = avatar.renderer == null ? 0 : avatar.complexity.pre >= avatar.permissions.get(Permissions.COMPLEXITY) ? 1 : 3;
        status += complexity << 2;

        //script init
        int init = avatar.scriptError ? 1 : avatar.luaRuntime == null ? 0 : avatar.init.getTotal() >= avatar.permissions.get(Permissions.INIT_INST) * 0.75 ? 2 : 3;
        status += init << 4;

        //script tick
        int tick = avatar.scriptError ? 1 : avatar.luaRuntime == null ? 0 : avatar.tick.getTotal() >= avatar.permissions.get(Permissions.TICK_INST) * 0.75 || avatar.worldTick.getTotal() >= avatar.permissions.get(Permissions.WORLD_TICK_INST) * 0.75 ? 2 : 3;
        status += tick << 6;

        //script render
        int render = avatar.scriptError ? 1 : avatar.luaRuntime == null ? 0 : avatar.render.getTotal() >= avatar.permissions.get(Permissions.RENDER_INST) * 0.75 || avatar.worldRender.getTotal() >= avatar.permissions.get(Permissions.WORLD_RENDER_INST) * 0.75 ? 2 : 3;
        status += render << 8;
    }

    @Override
    public Component getTooltipFor(int i) {
        return avatar == null ? null : HOVER_TEXT.get(i).apply(avatar).setStyle(TEXT_COLORS.get(status >> (i * 2) & 3));
    }
}
