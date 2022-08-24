package org.moon.figura.lua.api;

import com.mojang.brigadier.StringReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import org.luaj.vm2.LuaError;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.config.Config;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.entity.EntityAPI;
import org.moon.figura.lua.api.nameplate.Badges;
import org.moon.figura.lua.api.world.ItemStackAPI;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.mixin.gui.ChatScreenAccessor;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.LuaUtils;
import org.moon.figura.utils.TextUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "HostAPI",
        description = "host"
)
public class HostAPI {

    private final Avatar owner;
    private final boolean isHost;
    private final Minecraft minecraft;

    @LuaWhitelist
    @LuaFieldDoc(description = "host.unlock_cursor")
    public boolean unlockCursor = false;
    public Integer chatColor;

    public HostAPI(Avatar owner) {
        this.owner = owner;
        this.minecraft = Minecraft.getInstance();
        this.isHost = owner.isHost;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "host.is_host")
    public boolean isHost() {
        return isHost;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "host.get_targeted_entity")
    public EntityAPI<?> getTargetedEntity() {
        if (!isHost()) return null;

        Entity entity = this.minecraft.crosshairPickEntity;
        if (entity != null && Minecraft.getInstance().player != null && !entity.isInvisibleTo(Minecraft.getInstance().player))
            return EntityAPI.wrap(entity);

        return null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "timesData"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Integer.class, Integer.class, Integer.class},
                            argumentNames = {"fadeInTime", "stayTime", "fadeOutTime"}
                    )
            },
            description = "host.set_title_times"
    )
    public void setTitleTimes(Object x, Double y, Double z) {
        if (!isHost()) return;
        FiguraVec3 times = LuaUtils.parseVec3("setTitleTimes", x, y, z);
        this.minecraft.gui.setTimes((int) times.x, (int) times.y, (int) times.z);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "host.clear_title")
    public void clearTitle() {
        if (isHost())
            this.minecraft.gui.clear();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "text"
            ),
            description = "host.set_title"
    )
    public void setTitle(@LuaNotNil String text) {
        if (isHost())
            this.minecraft.gui.setTitle(TextUtils.tryParseJson(text));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "text"
            ),
            description = "host.set_subtitle"
    )
    public void setSubtitle(@LuaNotNil String text) {
        if (isHost())
            this.minecraft.gui.setSubtitle(TextUtils.tryParseJson(text));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = String.class,
                            argumentNames = "text"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {String.class, boolean.class},
                            argumentNames = {"text", "animated"}
                    )
            },
            description = "host.set_actionbar"
    )
    public void setActionbar(@LuaNotNil String text, boolean animated) {
        if (!isHost()) return;
        this.minecraft.gui.setOverlayMessage(TextUtils.tryParseJson(text), animated);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "message"
            ),
            description = "host.send_chat_message"
    )
    public void sendChatMessage(@LuaNotNil String message) {
        if (!isHost() || !Config.CHAT_MESSAGES.asBool()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) player.chatSigned(message, Component.literal(message));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "command"
            ),
            description = "host.send_chat_command"
    )
    public void sendChatCommand(@LuaNotNil String command) {
        if (!isHost() || !Config.CHAT_MESSAGES.asBool()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) player.commandUnsigned(command.startsWith("/") ? command.substring(1) : command);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload,
                    @LuaFunctionOverload(
                            argumentTypes = Boolean.class,
                            argumentNames = "offhand"
                    )
            },
            description = "host.swing_arm"
    )
    public void swingArm(boolean offhand) {
        if (isHost() && Minecraft.getInstance().player != null)
            Minecraft.getInstance().player.swing(offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "slot"
            ),
            description = "host.get_slot"
    )
    public ItemStackAPI getSlot(@LuaNotNil String slot) {
        if (!isHost()) return null;
        try {
            Entity e = this.owner.luaRuntime.user;
            Integer index = SlotArgument.slot().parse(new StringReader(slot));
            return ItemStackAPI.verify(e.getSlot(index).get());
        } catch (Exception e) {
            throw new LuaError("Unable to get slot \"" + slot + "\"");
        }
    }

    @LuaWhitelist
    public void setBadge(int index, boolean value, boolean pride) {
        if (!isHost()) return;
        if (!FiguraMod.DEBUG_MODE)
            throw new LuaError("Congrats, you found this debug easter egg!");
        Badges.set(owner.owner, index, value, pride);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "host.get_chat_color")
    public Integer getChatColor() {
        if (isHost())
            return this.chatColor;

        return null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "color"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b"}
                    )
            },
            description = "host.set_chat_color"
    )
    public void setChatColor(Object x, Double y, Double z) {
        if (isHost())
            this.chatColor = x == null ? null : ColorUtils.rgbToInt(LuaUtils.parseVec3("setChatColor", x, y, z));
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "host.get_chat_text")
    public String getChatText() {
        if (isHost() && Minecraft.getInstance().screen instanceof ChatScreen chat)
            return ((ChatScreenAccessor) chat).getInput().getValue();

        return null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "text"
            ),
            description = "host.set_chat_text"
    )
    public void setChatText(@LuaNotNil String text) {
        if (isHost() && Config.CHAT_MESSAGES.asBool() && Minecraft.getInstance().screen instanceof ChatScreen chat)
            ((ChatScreenAccessor) chat).getInput().setValue(text);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "host.get_screen")
    public String getScreen() {
        if (!isHost() || Minecraft.getInstance().screen == null)
            return null;
        return Minecraft.getInstance().screen.getClass().getSimpleName();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "host.is_chat_open")
    public boolean isChatOpen() {
        return isHost() && Minecraft.getInstance().screen instanceof ChatScreen;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "host.is_container_open")
    public boolean isContainerOpen() {
        return isHost() && Minecraft.getInstance().screen instanceof AbstractContainerScreen;
    }

    @LuaWhitelist
    public Object __index(String arg) {
        if ("unlockCursor".equals(arg))
            return unlockCursor;
        return null;
    }

    @LuaWhitelist
    public void __newindex(String key, Object value) {
        if ("unlockCursor".equals(key))
            unlockCursor = (Boolean) value;
    }

    @Override
    public String toString() {
        return "HostAPI";
    }
}
