package org.moon.figura.lua.api;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.brigadier.StringReader;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import org.luaj.vm2.LuaError;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.config.Config;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.ItemStackAPI;
import org.moon.figura.lua.docs.*;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.mixin.gui.ChatScreenAccessor;
import org.moon.figura.model.rendering.texture.FiguraTexture;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.LuaUtils;
import org.moon.figura.utils.TextUtils;

import java.util.*;

@LuaWhitelist
@LuaTypeDoc(
        name = "HostAPI",
        value = "host"
)
public class HostAPI {

    private final Avatar owner;
    private final boolean isHost;
    private final Minecraft minecraft;

    @LuaWhitelist
    @LuaFieldDoc("host.unlock_cursor")
    public boolean unlockCursor = false;
    public Integer chatColor;

    public HostAPI(Avatar owner) {
        this.owner = owner;
        this.minecraft = Minecraft.getInstance();
        this.isHost = owner.isHost;
    }

    @LuaWhitelist
    @LuaMethodDoc("host.is_host")
    public boolean isHost() {
        return isHost;
    }

    @LuaWhitelist
    @LuaMethodDoc(value = "host.is_cursor_unlocked")
    public boolean isCursorUnlocked() {
        return unlockCursor;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "boolean"
            ),
            value = "host.set_unlock_cursor")
    public void setUnlockCursor(boolean bool) {
        unlockCursor = bool;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "timesData"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Integer.class, Integer.class},
                            argumentNames = {"fadeInTime", "stayTime", "fadeOutTime"}
                    )
            },
            value = "host.set_title_times"
    )
    public void setTitleTimes(Object x, Double y, Double z) {
        if (!isHost()) return;
        FiguraVec3 times = LuaUtils.parseVec3("setTitleTimes", x, y, z);
        this.minecraft.gui.setTimes((int) times.x, (int) times.y, (int) times.z);
    }

    @LuaWhitelist
    @LuaMethodShadow("setTitleTimes")
    public HostAPI titleTimes(Object x, Double y, Double z) {
        setTitleTimes(x, y, z);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("host.clear_title")
    public void clearTitle() {
        if (isHost())
            this.minecraft.gui.clear();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "text"
            ),
            value = "host.set_title"
    )
    public void setTitle(@LuaNotNil String text) {
        if (isHost())
            this.minecraft.gui.setTitle(TextUtils.tryParseJson(text));
    }

    @LuaWhitelist
    @LuaMethodShadow("setTitle")
    public HostAPI title(@LuaNotNil String text) {
        setTitle(text);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "text"
            ),
            value = "host.set_subtitle"
    )
    public void setSubtitle(@LuaNotNil String text) {
        if (isHost())
            this.minecraft.gui.setSubtitle(TextUtils.tryParseJson(text));
    }

    @LuaWhitelist
    @LuaMethodShadow("setSubtitle")
    public HostAPI subtitle(@LuaNotNil String text) {
        setSubtitle(text);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "text"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, boolean.class},
                            argumentNames = {"text", "animated"}
                    )
            },
            value = "host.set_actionbar"
    )
    public void setActionbar(@LuaNotNil String text, boolean animated) {
        if (isHost())
            this.minecraft.gui.setOverlayMessage(TextUtils.tryParseJson(text), animated);
    }

    @LuaWhitelist
    @LuaMethodShadow("setActionbar")
    public HostAPI actionbar(@LuaNotNil String text, boolean animated) {
        setActionbar(text, animated);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "message"
            ),
            value = "host.send_chat_message"
    )
    public void sendChatMessage(@LuaNotNil String message) {
        if (!isHost() || !Config.CHAT_MESSAGES.asBool()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) player.chat(message.startsWith("/") ? message.substring(1) : message);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "command"
            ),
            value = "host.send_chat_command"
    )
    public void sendChatCommand(@LuaNotNil String command) {
        if (!isHost() || !Config.CHAT_MESSAGES.asBool()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) player.chat(command.startsWith("/") ? command : "/" + command);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "message"
            ),
            value = "host.append_chat_history"
    )
    public void appendChatHistory(@LuaNotNil String message) {
        if (isHost())
            this.minecraft.gui.getChat().addRecentChat(message);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Boolean.class,
                            argumentNames = "offhand"
                    )
            },
            value = "host.swing_arm"
    )
    public void swingArm(boolean offhand) {
        if (isHost() && Minecraft.getInstance().player != null)
            Minecraft.getInstance().player.swing(offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "slot"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = Integer.class,
                            argumentNames = "slot"
                    )
            },
            value = "host.get_slot"
    )
    public ItemStackAPI getSlot(@LuaNotNil Object slot) {
        if (!isHost()) return null;

        Integer index;
        if (slot instanceof String s) {
            try {
                index = SlotArgument.slot().parse(new StringReader(s));
            } catch (Exception e) {
                throw new LuaError("Unable to get slot \"" + slot + "\"");
            }
        } else if (slot instanceof Integer i)
            index = i;
        else {
            throw new LuaError("Invalid type for getSlot: " + slot.getClass().getSimpleName());
        }

        Entity e = this.owner.luaRuntime.getUser();
        return ItemStackAPI.verify(e.getSlot(index).get());
    }

    @LuaWhitelist
    public void setBadge(int index, boolean value, boolean pride) {
        if (!isHost()) return;
        if (!FiguraMod.DEBUG_MODE)
            throw new LuaError("Congrats, you found this debug easter egg!");

        Pair<BitSet, BitSet> badges = AvatarManager.getBadges(owner.owner);
        if (badges == null)
            return;

        BitSet set = pride ? badges.getFirst() : badges.getSecond();
        set.set(index, value);
    }

    @LuaWhitelist
    public HostAPI badge(int index, boolean value, boolean pride) {
        setBadge(index, value, pride);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("host.get_chat_color")
    public Integer getChatColor() {
        if (isHost())
            return this.chatColor;

        return null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "color"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b"}
                    )
            },
            value = "host.set_chat_color"
    )
    public void setChatColor(Object x, Double y, Double z) {
        if (isHost())
            this.chatColor = x == null ? null : ColorUtils.rgbToInt(LuaUtils.parseVec3("setChatColor", x, y, z));
    }

    @LuaWhitelist
    @LuaMethodShadow("setChatColor")
    public HostAPI chatColor(Object x, Double y, Double z) {
        setChatColor(x, y, z);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("host.get_chat_text")
    public String getChatText() {
        if (isHost() && Minecraft.getInstance().screen instanceof ChatScreen chat)
            return ((ChatScreenAccessor) chat).getInput().getValue();

        return null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "text"
            ),
            value = "host.set_chat_text"
    )
    public void setChatText(@LuaNotNil String text) {
        if (isHost() && Config.CHAT_MESSAGES.asBool() && Minecraft.getInstance().screen instanceof ChatScreen chat)
            ((ChatScreenAccessor) chat).getInput().setValue(text);
    }

    @LuaWhitelist
    @LuaMethodShadow("setChatText")
    public HostAPI chatText(@LuaNotNil String text) {
        setChatText(text);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("host.get_screen")
    public String getScreen() {
        if (!isHost() || Minecraft.getInstance().screen == null)
            return null;
        return Minecraft.getInstance().screen.getClass().getName();
    }

    @LuaWhitelist
    @LuaMethodDoc("host.is_chat_open")
    public boolean isChatOpen() {
        return isHost() && Minecraft.getInstance().screen instanceof ChatScreen;
    }

    @LuaWhitelist
    @LuaMethodDoc("host.is_container_open")
    public boolean isContainerOpen() {
        return isHost() && Minecraft.getInstance().screen instanceof AbstractContainerScreen;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraTexture.class,
                    argumentNames = "texture"
            ),
            value = "host.save_texture")
    public void saveTexture(@LuaNotNil FiguraTexture texture) {
        if (isHost()) {
            try {
                texture.saveCache();
            } catch (Exception e) {
                throw new LuaError(e.getMessage());
            }
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "name"
            ),
            value = "host.screenshot")
    public FiguraTexture screenshot(@LuaNotNil String name) {
        if (!isHost())
            return null;

        NativeImage img = Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget());
        return owner.luaRuntime.texture.register(name, img, true);
    }

    @LuaWhitelist
    @LuaMethodDoc("host.is_avatar_uploaded")
    public boolean isAvatarUploaded() {
        return isHost() && AvatarManager.localUploaded;
    }

    @LuaWhitelist
    @LuaMethodDoc("host.get_status_effects")
    public List<Map<String, Object>> getStatusEffects() {
        List<Map<String, Object>> list = new ArrayList<>();

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return list;

        for (MobEffectInstance effect : player.getActiveEffects()) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", effect.getEffect().getDescriptionId());
            map.put("amplifier", effect.getAmplifier());
            map.put("duration", effect.getDuration());
            map.put("visible", effect.isVisible());

            list.add(map);
        }

        return list;
    }

    @LuaWhitelist
    @LuaMethodDoc("host.get_clipboard")
    public String getClipboard() {
        return Minecraft.getInstance().keyboardHandler.getClipboard();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "text"
            ),
            value = "host.set_clipboard")
    public void setClipboard(@LuaNotNil String text) {
        Minecraft.getInstance().keyboardHandler.setClipboard(text);
    }

    @LuaWhitelist
    @LuaMethodShadow("setClipboard")
    public HostAPI clipboard(@LuaNotNil String text) {
        setClipboard(text);
        return this;
    }

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
