package org.figuramc.figura.gui.widgets.permissions;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.Badges;
import org.figuramc.figura.avatar.local.LocalAvatarLoader;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.gui.widgets.ContextMenu;
import org.figuramc.figura.gui.widgets.Label;
import org.figuramc.figura.gui.widgets.lists.PlayerList;
import org.figuramc.figura.lua.api.nameplate.NameplateCustomization;
import org.figuramc.figura.permissions.PermissionManager;
import org.figuramc.figura.permissions.PermissionPack;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.EntityUtils;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerPermPackElement extends AbstractPermPackElement {

    public static final ResourceLocation UNKNOWN = new FiguraIdentifier("textures/gui/unknown_portrait.png");
    private static final ResourceLocation BACKGROUND = new FiguraIdentifier("textures/gui/player_permissions.png");
    private static final Component DC_TEXT = new FiguraText("gui.permissions.disconnected").withStyle(ChatFormatting.RED);

    private final String name;
    private final ResourceLocation skin;
    private final UUID owner;
    private final ContextMenu context;
    private final Label nameLabel;
    private final PlayerStatusWidget status;

    public boolean disconnected = false;

    //drag
    public boolean dragged = false;
    public int anchorX, anchorY, initialY;
    public int index;

    public PlayerPermPackElement(int width, String name, PermissionPack pack, ResourceLocation skin, UUID owner, PlayerList parent) {
        super(width, 40, pack, parent);
        this.name = name;
        this.skin = skin;
        this.owner = owner;
        this.context = new ContextMenu(this);

        this.nameLabel = new Label(name, 0, 0, 0);
        this.status = new PlayerStatusWidget(0, 0, 70, owner);

        generateContext();
    }

    private void generateContext() {
        //name uuid
        context.addAction(new FiguraText("gui.context.copy_name"), null, button -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getName());
            FiguraToast.sendToast(new FiguraText("toast.clipboard"));
        });
        context.addAction(new FiguraText("gui.context.copy_uuid"), null, button -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getOwner().toString());
            FiguraToast.sendToast(new FiguraText("toast.clipboard"));
        });

        //reload
        context.addAction(new FiguraText("gui.context.reload"), null, button -> {
            AvatarManager.reloadAvatar(owner);
            FiguraToast.sendToast(new FiguraText("toast.reload"));
        });

        //permissions
        ContextMenu permissionsContext = new ContextMenu();
        for (Permissions.Category category : Permissions.Category.values()) {
            PermissionPack.CategoryPermissionPack categoryPack = PermissionManager.CATEGORIES.get(category);
            permissionsContext.addAction(categoryPack.getCategoryName(), null, button -> {
                pack.setCategory(categoryPack);
                if (parent.selectedEntry == this)
                    parent.parent.updatePermissions(pack);
            });
        }
        context.addTab(new FiguraText("gui.context.set_permissions"), null, permissionsContext);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (dragged)
            UIHelper.fillRounded(stack, getX() - 1, getY() - 1, getWidth() + 2, getHeight() + 2, 0x40FFFFFF);
        else
            super.render(stack, mouseX, mouseY, delta);
    }

    public void renderDragged(PoseStack stack, int mouseX, int mouseY, float delta) {
        int oX = x;
        int oY = y;
        x = mouseX - (anchorX - x);
        y = mouseY - (anchorY - y) + (initialY - oY);
        super.render(stack, mouseX, mouseY, delta);
        x = oX;
        y = oY;
    }

    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
        int width = getWidth();
        int height = getHeight();

        stack.pushPose();

        float tx = x + width / 2f;
        float ty = y + height / 2f;

        stack.translate(tx, ty, 100);
        stack.scale(scale, scale, 1f);

        animate(delta, (UIHelper.getContext() == this.context && this.context.isVisible()) || this.isMouseOver(mouseX, mouseY) || this.isFocused());

        //fix x, y, mouse
        int x = -width / 2;
        int y = -height / 2;
        mouseX = (int) ((mouseX - tx) / scale);
        mouseY = (int) ((mouseY - ty) / scale);

        //selected overlay
        if (this.parent.selectedEntry == this) {
            ArrayList<PermissionPack> list = new ArrayList<>(PermissionManager.CATEGORIES.values());
            int color = (dragged ? list.get(Math.min(index, list.size() - 1)) : pack).getColor();
            UIHelper.fillRounded(stack, x - 1, y - 1, width + 2, height + 2, color + (0xFF << 24));
        }

        //background
        UIHelper.renderHalfTexture(stack, x, y, width, height, 174, BACKGROUND);

        //head
        Component name = null;
        boolean head = false;

        Avatar avatar = AvatarManager.getAvatarForPlayer(owner);
        if (avatar != null) {
            NameplateCustomization custom = avatar.luaRuntime == null ? null : avatar.luaRuntime.nameplate.LIST;
            if (custom != null && custom.getJson() != null && avatar.permissions.get(Permissions.NAMEPLATE_EDIT) == 1)
                name = custom.getJson().copy();

            Entity e = EntityUtils.getEntityByUUID(owner);
            boolean upsideDown = e instanceof LivingEntity entity && LivingEntityRenderer.isEntityUpsideDown(entity);
            head = avatar.renderPortrait(stack, x + 4, y + 4, Math.round(32f * scale), 64, upsideDown);
        }

        if (!head) {
            if (this.skin != null) {
                //head
                UIHelper.setupTexture(this.skin);
                blit(stack, x + 4, y + 4, 32, 32, 8f, 8f, 8, 8, 64, 64);

                //hat
                RenderSystem.enableBlend();
                blit(stack, x + 4, y + 4, 32, 32, 40f, 8f, 8, 8, 64, 64);
                RenderSystem.disableBlend();
            } else {
                UIHelper.renderTexture(stack, x + 4, y + 4, 32, 32, UNKNOWN);
            }
        }

        //name
        Font font = Minecraft.getInstance().font;
        Component ogName = new TextComponent(this.name);

        if (name == null)
            name = ogName;

        name = TextUtils.replaceInText(name, "\\$\\{name\\}", ogName);
        name = TextUtils.splitText(name, "\n").get(0);
        name = TextComponent.EMPTY.copy().append(name.copy().withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(this.name + "\n" + this.owner)))));

        //badges
        name = Badges.appendBadges(name, owner, false);
        Component badges = Badges.fetchBadges(owner);
        if (!badges.getString().isEmpty())
            badges = new TextComponent(" ").append(badges);

        nameLabel.setText(TextUtils.trimToWidthEllipsis(font, name, width - 44 - font.width(badges), TextUtils.ELLIPSIS).copy().append(badges));
        nameLabel.setX(x + 40);
        nameLabel.setY(y + 4);
        //nameLabel.setOutlineColor(ColorUtils.rgbToInt(ColorUtils.rainbow(2, 1, 0.5)) + ((int) (0.5f * 0xFF) << 24));
        nameLabel.render(stack, mouseX, mouseY, delta);

        //status
        if (avatar != null && avatar.nbt != null) {
            status.tick(); //yes I know
            status.setX(x + 40);
            status.setY(y + 6 + font.lineHeight);
            status.render(stack, mouseX, mouseY, delta);
        }

        //category
        int textY = y + height - font.lineHeight - 4;
        drawString(stack, font, pack.getCategoryName().append(pack.hasChanges() ? "*" : ""), x + 40, textY, 0xFFFFFF);

        //disconnected
        if (disconnected)
            drawString(stack, font, DC_TEXT, x + width - font.width(DC_TEXT) - 4, textY, 0xFFFFFF);

        stack.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isMouseOver(mouseX, mouseY))
            return false;

        //context menu on right click
        if (button == 1) {
            context.setX((int) mouseX);
            context.setY((int) mouseY);
            context.setVisible(true);
            UIHelper.setContext(context);
            return true;
        }
        //hide old context menu
        else if (UIHelper.getContext() == context) {
            context.setVisible(false);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return !dragged && super.isMouseOver(mouseX, mouseY);
    }

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }

    @Override
    public boolean isVisible() {
        return super.isVisible() && this.pack.isVisible();
    }
}