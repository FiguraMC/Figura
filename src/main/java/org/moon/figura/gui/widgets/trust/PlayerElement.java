package org.moon.figura.gui.widgets.trust;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.avatars.Badges;
import org.moon.figura.avatars.providers.LocalAvatarLoader;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.gui.widgets.ContextMenu;
import org.moon.figura.gui.widgets.Label;
import org.moon.figura.gui.widgets.lists.PlayerList;
import org.moon.figura.lua.api.nameplate.NameplateCustomization;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerElement extends AbstractTrustElement {

    public static final ResourceLocation UNKNOWN = new FiguraIdentifier("textures/gui/unknown_portrait.png");
    private static final ResourceLocation BACKGROUND = new FiguraIdentifier("textures/gui/player_trust.png");
    private static final Component DC_TEXT = new FiguraText("gui.trust.disconnected").withStyle(ChatFormatting.RED);

    private final String name;
    private final ResourceLocation skin;
    private final UUID owner;
    private final ContextMenu context;
    private final Label nameLabel;
    private final PlayerStatusWidget status;

    public boolean disconnected = false;

    public PlayerElement(String name, TrustContainer trust, ResourceLocation skin, UUID owner, PlayerList parent) {
        super(40, trust, parent);
        this.name = name;
        this.skin = skin;
        this.owner = owner;
        this.context = new ContextMenu(this);

        this.nameLabel = new Label(name, 0, 0, false, 0);
        this.status = new PlayerStatusWidget(0, 0, 70, owner);

        generateContext();
    }

    private void generateContext() {
        //name uuid
        context.addAction(new FiguraText("gui.context.copy_name"), button -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getName());
            FiguraToast.sendToast(new FiguraText("toast.clipboard"));
        });
        context.addAction(new FiguraText("gui.context.copy_uuid"), button -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getOwner().toString());
            FiguraToast.sendToast(new FiguraText("toast.clipboard"));
        });

        //reload
        context.addAction(new FiguraText("gui.context.reload"), button -> {
            AvatarManager.reloadAvatar(owner);
            FiguraToast.sendToast(new FiguraText("toast.reload"));
        });

        //trust
        ContextMenu trustContext = new ContextMenu();
        ArrayList<ResourceLocation> groupList = new ArrayList<>(TrustManager.GROUPS.keySet());
        for (int i = 0; i < (TrustManager.isLocal(trust) ? groupList.size() : groupList.size() - 1); i++) {
            ResourceLocation parentID = groupList.get(i);
            TrustContainer container = TrustManager.get(parentID);
            trustContext.addAction(container.getGroupName(), button -> {
                trust.setParent(parentID);
                if (parent.selectedEntry == this)
                    parent.parent.updateTrustData(trust);
            });
        }
        context.addTab(new FiguraText("gui.context.set_trust"), trustContext);

        if (FiguraMod.DEBUG_MODE) {
            context.addAction(new TextComponent("yoink to cache"), button -> {
                Avatar a = AvatarManager.getAvatarForPlayer(owner);
                if (a != null) {
                    if (a.nbt != null) {
                        LocalAvatarLoader.saveNbt(a.nbt);
                        FiguraToast.sendToast("yoinked");
                    } else {
                        FiguraToast.sendToast("no avatar :(", FiguraToast.ToastType.ERROR);
                    }
                }
            });
        }
    }

    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
        stack.pushPose();

        float tx = x + width / 2f;
        float ty = y + height / 2f;

        stack.translate(tx, ty, 100);
        stack.scale(scale, scale, scale);

        animate(mouseX, mouseY, delta);

        //fix x, y, mouse
        int x = -width / 2;
        int y = -height / 2;
        mouseX = (int) ((mouseX - tx) / scale);
        mouseY = (int) ((mouseY - ty) / scale);

        //selected overlay
        if (this.parent.selectedEntry == this) {
            UIHelper.fillRounded(stack, x - 1, y - 1, width + 2, height + 2, 0xFFFFFFFF);
        }

        //background
        UIHelper.renderTexture(stack, x, y, width, height, BACKGROUND);

        //head
        Component name = null;

        boolean head = false;
        Avatar avatar = AvatarManager.getAvatarForPlayer(owner);
        if (avatar != null) {
            NameplateCustomization custom = avatar.luaRuntime == null ? null : avatar.luaRuntime.nameplate.LIST;
            if (custom != null && custom.getText() != null && avatar.trust.get(TrustContainer.Trust.NAMEPLATE_EDIT) == 1)
                name = NameplateCustomization.applyCustomization(custom.getText());

            stack.pushPose();
            float s = 16 * 4;
            stack.translate(x + 20, y + 36, 0);
            stack.scale(s, -s, s);
            stack.mulPose(Vector3f.XP.rotationDegrees(180f));

            FiguraVec4 oldScissors = UIHelper.scissors.copy();

            int xx = Math.round(tx + x * scale + 4);
            int yy = Math.round(ty + y * scale + 4);
            int endX = Math.round(xx + 32 * scale);
            int endY = Math.round(yy + 32 * scale);
            xx = (int) Math.max(xx, oldScissors.x);
            yy = (int) Math.max(yy, oldScissors.y);

            UIHelper.setupScissor(xx, yy, endX - xx, endY - yy);

            Lighting.setupForFlatItems();
            head = avatar.headRender(stack, avatar.getBufferSource(), LightTexture.FULL_BRIGHT);

            UIHelper.setupScissor((int) oldScissors.x, (int) oldScissors.y, (int) oldScissors.z, (int) oldScissors.w);
            stack.popPose();
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

        if (name == null)
            name = new TextComponent(this.name);

        name = TextComponent.EMPTY.copy()
                .append(name.copy().withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(this.name + "\n" + this.owner)))))
                .append(Badges.fetchBadges(avatar));

        nameLabel.setText(TextUtils.trimToWidthEllipsis(font, name, width - 40, TextUtils.ELLIPSIS));
        nameLabel.x = x + 40;
        nameLabel.y = y + 4;
        //nameLabel.setOutlineColor(ColorUtils.rgbToInt(ColorUtils.rainbow(2, 1, 0.5)) + ((int) (0.5f * 0xFF) << 24));
        nameLabel.render(stack, mouseX, mouseY, delta);

        //status
        if (avatar != null && avatar.nbt != null) {
            status.tick(); //yes I know
            status.x = x + 40;
            status.y = y + 6 + font.lineHeight;
            status.render(stack, mouseX, mouseY, delta);
        }

        //trust
        int textY = y + height - font.lineHeight - 4;
        drawString(stack, font, trust.getGroupName(), x + 40, textY, 0xFFFFFF);

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
            context.setPos((int) mouseX, (int) mouseY);
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

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }
}
