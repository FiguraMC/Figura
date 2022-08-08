package org.moon.figura.gui.widgets.trust;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.avatars.providers.LocalAvatarLoader;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.gui.widgets.ContextMenu;
import org.moon.figura.gui.widgets.lists.PlayerList;
import org.moon.figura.lua.api.nameplate.Badges;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.MathUtils;
import org.moon.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerElement extends AbstractTrustElement {

    private final String name;
    private final ResourceLocation skin;
    private final UUID owner;
    private final ContextMenu context;

    private static final ResourceLocation BACKGROUND = new FiguraIdentifier("textures/gui/player_trust.png");

    public PlayerElement(String name, TrustContainer trust, ResourceLocation skin, UUID owner, PlayerList parent) {
        super(40, trust, parent);
        this.name = name;
        this.skin = skin;
        this.owner = owner;
        this.context = new ContextMenu(this);

        generateContext();
    }

    private void generateContext() {
        //name uuid
        context.addAction(FiguraText.of("gui.context.copy_name"), button -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getName());
            FiguraToast.sendToast(FiguraText.of("toast.clipboard"));
        });
        context.addAction(FiguraText.of("gui.context.copy_uuid"), button -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getOwner().toString());
            FiguraToast.sendToast(FiguraText.of("toast.clipboard"));
        });

        //reload
        context.addAction(FiguraText.of("gui.context.reload"), button -> {
            AvatarManager.reloadAvatar(owner);
            FiguraToast.sendToast(FiguraText.of("toast.reload"));
        });

        //trust
        ContextMenu trustContext = new ContextMenu();
        ArrayList<ResourceLocation> groupList = new ArrayList<>(TrustManager.GROUPS.keySet());
        for (int i = 0; i < (TrustManager.isLocal(trust) ? groupList.size() : groupList.size() - 1); i++) {
            ResourceLocation parentID = groupList.get(i);
            TrustContainer container = TrustManager.get(parentID);
            trustContext.addAction(container.getGroupName().copy().setStyle(Style.EMPTY.withColor(container.getGroupColor())), button -> {
                trust.setParent(parentID);
                if (parent.selectedEntry == this)
                    parent.parent.updateTrustData(trust);
            });
        }
        context.addTab(FiguraText.of("gui.context.set_trust"), trustContext);

        if (FiguraMod.DEBUG_MODE) {
            context.addAction(Component.literal("yoink to cache"), button -> {
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
        stack.translate(x + width / 2f, y + height / 2f, 100);
        stack.scale(scale, scale, scale);

        animate(mouseX, mouseY, delta);

        //fix x, y
        int x = -width / 2;
        int y = -height / 2;

        //selected overlay
        if (this.parent.selectedEntry == this) {
            UIHelper.fillRounded(stack, x - 1, y - 1, width + 2, height + 2, 0xFFFFFFFF);
        }

        //background
        UIHelper.renderTexture(stack, x, y, width, height, BACKGROUND);

        //head
        UIHelper.setupTexture(this.skin);
        blit(stack, x + 4, y + 4, 32, 32, 8f, 8f, 8, 8, 64, 64);

        //hat
        RenderSystem.enableBlend();
        blit(stack, x + 4, y + 4, 32, 32, 40f, 8f, 8, 8, 64, 64);
        RenderSystem.disableBlend();

        //name
        Font font = Minecraft.getInstance().font;
        Avatar avatar = AvatarManager.getAvatarForPlayer(owner);
        UIHelper.renderOutlineText(stack, font, Component.literal(this.name).append(Badges.fetchBadges(avatar)), x + 40, y + 4, 0xFFFFFF, 0);

        //size
        if (avatar != null && avatar.nbt != null)
            drawString(stack, font, FiguraText.of("gui.trust.avatar_size", MathUtils.asFileSize(avatar.fileSize)), x + 40, y + 6 + font.lineHeight, 0x888888);

        //trust
        drawString(stack, font, trust.getGroupName(), x + 40, y + height - font.lineHeight - 4, trust.getGroupColor());

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
