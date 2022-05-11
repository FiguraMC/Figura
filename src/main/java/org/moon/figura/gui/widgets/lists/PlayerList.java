package org.moon.figura.gui.widgets.lists;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.moon.figura.gui.screens.TrustScreen;
import org.moon.figura.gui.widgets.TextField;
import org.moon.figura.gui.widgets.trust.AbstractTrustElement;
import org.moon.figura.gui.widgets.trust.GroupElement;
import org.moon.figura.gui.widgets.trust.PlayerElement;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.ui.UIHelper;

import java.util.*;

public class PlayerList extends AbstractList {

    private final HashMap<UUID, PlayerElement> players = new HashMap<>();
    private final HashSet<UUID> missingPlayers = new HashSet<>();

    private final ArrayList<AbstractTrustElement> trustList = new ArrayList<>();

    public final TrustScreen parent;

    private int totalHeight = 0;
    public AbstractTrustElement selectedEntry;
    private String filter = "";

    public PlayerList(int x, int y, int width, int height, TrustScreen parent) {
        super(x, y, width, height);
        updateScissors(1, 24, -2, -27);

        this.parent = parent;

        //fix scrollbar y and height
        scrollBar.y = y + 28;
        scrollBar.setHeight(height - 32);

        //search bar
        children.add(new TextField(x + 4, y + 4, width - 8, 20, new FiguraText("gui.search"), s -> filter = s));

        //initial load
        loadGroups();
        loadPlayers();

        //select self
        PlayerElement local = Minecraft.getInstance().player != null ? players.get(Minecraft.getInstance().player.getUUID()) : null;
        if (local != null) local.onPress();
    }

    @Override
    public void tick() {
        //update players
        loadPlayers();
        super.tick();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        //background and scissors
        UIHelper.renderSliced(stack, x, y, width, height, UIHelper.OUTLINE);
        UIHelper.setupScissor(x + scissorsX, y + scissorsY, width + scissorsWidth, height + scissorsHeight);

        totalHeight = 0;
        for (AbstractTrustElement trustEntry : trustList) {
            if (trustEntry.isVisible())
                totalHeight += trustEntry.getHeight() + 8;
        }

        //scrollbar visible
        scrollBar.visible = totalHeight > height - 32;
        scrollBar.setScrollRatio(trustList.isEmpty() ? 0f : (float) totalHeight / trustList.size(), totalHeight - (height - 32));

        //render stuff
        int xOffset = width / 2 - 87 - (scrollBar.visible ? 7 : 0);
        int playerY = scrollBar.visible ? (int) -(Mth.lerp(scrollBar.getScrollProgress(), -32, totalHeight - height)) : 32;
        boolean hidden = false;

        for (AbstractTrustElement trust : trustList) {
            if (hidden || !trust.isVisible()) {
                trust.visible = false;
                continue;
            }

            trust.visible = true;
            trust.x = x + Math.max(4, xOffset);
            trust.y = y + playerY;

            if (trust.y + trust.getHeight() > y + scissorsY)
                trust.render(stack, mouseX, mouseY, delta);

            playerY += trust.getHeight() + 8;
            if (playerY > height)
                hidden = true;
        }

        //reset scissor
        RenderSystem.disableScissor();

        //render children
        super.render(stack, mouseX, mouseY, delta);
    }

    @Override
    public List<? extends GuiEventListener> contents() {
        return trustList;
    }

    private void loadGroups() {
        for (TrustContainer container : TrustManager.GROUPS.values()) {
            GroupElement group = new GroupElement(container, this);
            trustList.add(group);
            children.add(group);
        }
    }

    private void loadPlayers() {
        //reset missing players
        missingPlayers.clear();
        missingPlayers.addAll(players.keySet());

        //for all players
        for (UUID uuid : new ArrayList<>(Minecraft.getInstance().player == null ? Collections.emptyList() : Minecraft.getInstance().player.connection.getOnlinePlayerIds())) {
            //get player
            PlayerInfo player = Minecraft.getInstance().player.connection.getPlayerInfo(uuid);
            if (player == null)
                continue;

            //get player data
            String name = player.getProfile().getName();
            UUID id = player.getProfile().getId();
            ResourceLocation skin = player.getSkinLocation();

            //filter check
            if (!name.toLowerCase().contains(filter.toLowerCase()))
                continue;

            //player is not missing
            missingPlayers.remove(id);

            players.computeIfAbsent(id, uuid1 -> {
                PlayerElement entry = new PlayerElement(name, TrustManager.get(id), skin, id, this);

                trustList.add(entry);
                children.add(entry);

                return entry;
            });
        }

        //remove missing players
        for (UUID missingID : missingPlayers) {
            PlayerElement entry = players.remove(missingID);
            trustList.remove(entry);
            children.remove(entry);
        }

        sortList();
    }

    private void sortList() {
        trustList.sort(AbstractTrustElement::compareTo);
        children.sort((element1, element2) -> {
            if (element1 instanceof AbstractTrustElement container1 && element2 instanceof AbstractTrustElement container2)
                return container1.compareTo(container2);
            return 0;
        });
    }

    public void updateScroll() {
        //store old scroll pos
        float pastScroll = (totalHeight - height) * scrollBar.getScrollProgress();

        //get new height
        totalHeight = 0;
        for (AbstractTrustElement trustEntry : trustList) {
            if (trustEntry.isVisible())
                totalHeight += trustEntry.getHeight() + 8;
        }

        //set new scroll percentage
        scrollBar.setScrollProgress(pastScroll / (totalHeight - height));
    }
}
