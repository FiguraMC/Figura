package org.figuramc.figura.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.Badges;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.permissions.PermissionManager;
import org.figuramc.figura.permissions.PermissionPack;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.MathUtils;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class PopupMenu {

    private static final FiguraIdentifier BACKGROUND = new FiguraIdentifier("textures/gui/popup.png");
    private static final FiguraIdentifier ICONS = new FiguraIdentifier("textures/gui/popup_icons.png");

    private static final MutableComponent VERSION_WARN = Component.empty()
            .append(Badges.System.WARNING.badge.copy().withStyle(Style.EMPTY.withFont(Badges.FONT)))
            .append(" ")
            .append(Badges.System.WARNING.desc.copy().withStyle(ChatFormatting.YELLOW));
    private static final MutableComponent ERROR_WARN = Component.empty()
            .append(Badges.System.ERROR.badge.copy().withStyle(Style.EMPTY.withFont(Badges.FONT)))
            .append(" ")
            .append(Badges.System.ERROR.desc.copy().withStyle(ChatFormatting.RED));
    private static final MutableComponent PERMISSION_WARN = Component.empty()
            .append(Badges.System.PERMISSIONS.badge.copy().withStyle(Style.EMPTY.withFont(Badges.FONT)))
            .append(" ")
            .append(Badges.System.PERMISSIONS.desc.copy().withStyle(ChatFormatting.BLUE));

    private static final List<Pair<Component, Consumer<UUID>>> BUTTONS = List.of(
            Pair.of(FiguraText.of("popup_menu.cancel"), id -> {}),
            Pair.of(FiguraText.of("popup_menu.reload"), id -> {
                AvatarManager.reloadAvatar(id);
                FiguraToast.sendToast(FiguraText.of("toast.reload"));
            }),
            Pair.of(FiguraText.of("popup_menu.increase_permissions"), id -> {
                PermissionPack pack = PermissionManager.get(id);
                if (PermissionManager.increaseCategory(pack))
                    FiguraToast.sendToast(FiguraText.of("toast.permission_change"), pack.getCategoryName());
            }),
            Pair.of(FiguraText.of("popup_menu.decrease_permissions"), id -> {
                PermissionPack pack = PermissionManager.get(id);
                if (PermissionManager.decreaseCategory(pack))
                    FiguraToast.sendToast(FiguraText.of("toast.permission_change"), pack.getCategoryName());
            })
    );
    private static final int LENGTH = BUTTONS.size();

    // runtime data
    private static int index = 0;
    private static boolean enabled = false;
    private static Entity entity;
    private static UUID id;

    public static void render(GuiGraphics gui) {
        if (!isEnabled()) return;

        if (entity == null) {
            id = null;
            return;
        }

        id = entity.getUUID();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || (entity.isInvisibleTo(minecraft.player) && entity != minecraft.player)) {
            entity = null;
            id = null;
            return;
        }

        RenderSystem.disableDepthTest();
        PoseStack pose = gui.pose();
        pose.pushPose();

        // world to screen space
        FiguraVec3 worldPos = FiguraVec3.fromVec3(entity.getPosition(minecraft.getFrameTime()));
        worldPos.add(0f, entity.getBbHeight() + 0.1f, 0f);

        FiguraVec4 vec = MathUtils.worldToScreenSpace(worldPos);
        if (vec.z < 1) return; // too close

        Window window = minecraft.getWindow();
        double w = window.getGuiScaledWidth();
        double h = window.getGuiScaledHeight();
        double s = Configs.POPUP_SCALE.value * Math.max(Math.min(window.getHeight() * 0.035 / vec.w * (1 / window.getGuiScale()), Configs.POPUP_MAX_SIZE.value), Configs.POPUP_MIN_SIZE.value);

        pose.translate((vec.x + 1) / 2 * w, (vec.y + 1) / 2 * h, -100);
        pose.scale((float) (s * 0.5), (float) (s * 0.5), 1);

        // background
        int width = LENGTH * 18;

        UIHelper.enableBlend();
        int frame = Configs.REDUCED_MOTION.value ? 0 : (int) ((FiguraMod.ticks / 5f) % 4);
        gui.blit(BACKGROUND, width / -2, -24, width, 26, 0, frame * 26, width, 26, width, 104);

        // icons
        pose.translate(0f, 0f, -2f);
        UIHelper.enableBlend();
        for (int i = 0; i < LENGTH; i++)
            gui.blit(ICONS, width / -2 + (18 * i), -24, 18, 18, 18 * i, i == index ? 18 : 0, 18, 18, width, 36);

        // texts
        Font font = minecraft.font;

        Component title = BUTTONS.get(index).getFirst();

        PermissionPack tc = PermissionManager.get(id);
        MutableComponent permissionName = tc.getCategoryName().append(tc.hasChanges() ? "*" : "");

        MutableComponent name = entity.getName().copy();

        boolean error = false;
        boolean version = false;
        boolean noPermissions = false;

        Component badges = Badges.fetchBadges(id);
        if (!badges.getString().isEmpty())
            name.append(" ").append(badges);

        Avatar avatar = AvatarManager.getAvatarForPlayer(id);
        if (avatar != null) {
            error = avatar.scriptError;
            version = avatar.versionStatus > 0;
            noPermissions = !avatar.noPermissions.isEmpty();
        }

        // render texts
        UIHelper.renderOutlineText(gui, font, name, -font.width(name) / 2, -36, 0xFFFFFF, 0x202020);

        pose.scale(0.5f, 0.5f, 0.5f);
        pose.translate(0f, 0f, -1f);

        UIHelper.renderOutlineText(gui, font, permissionName, -font.width(permissionName) / 2, -54, 0xFFFFFF, 0x202020);
        gui.drawString(font, title, -width + 4, -12, 0xFFFFFF);

        if (error)
            UIHelper.renderOutlineText(gui, font, ERROR_WARN, -font.width(ERROR_WARN) / 2, 0, 0xFFFFFF, 0x202020);
        if (version)
            UIHelper.renderOutlineText(gui, font, VERSION_WARN, -font.width(VERSION_WARN) / 2, error ? font.lineHeight : 0, 0xFFFFFF, 0x202020);
        if (noPermissions)
            UIHelper.renderOutlineText(gui, font, PERMISSION_WARN, -font.width(PERMISSION_WARN) / 2, (error ? font.lineHeight : 0) + (version ? font.lineHeight : 0), 0xFFFFFF, 0x202020);

        // finish rendering
        pose.popPose();
    }

    public static void scroll(double d) {
        index = (int) (index - d + LENGTH) % LENGTH;
    }

    public static void hotbarKeyPressed(int i) {
        if (i < LENGTH && i >= 0)
            index = i;
    }

    public static void run() {
        if (id != null)
            BUTTONS.get(index).getSecond().accept(id);

        enabled = false;
        entity = null;
        id = null;
        index = 0;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        PopupMenu.enabled = enabled;
    }

    public static boolean hasEntity() {
        return entity != null;
    }

    public static void setEntity(Entity entity) {
        PopupMenu.entity = entity;
    }

    public static UUID getEntityId() {
        return id;
    }
}
