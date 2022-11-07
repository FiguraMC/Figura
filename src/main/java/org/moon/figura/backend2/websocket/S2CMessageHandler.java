package org.moon.figura.backend2.websocket;

import net.minecraft.network.chat.TextComponent;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.backend2.NetworkStuff;
import org.moon.figura.config.Config;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraText;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class S2CMessageHandler {

    public static final byte
        AUTH = 0,
        PING = 1,
        EVENT = 2,
        TOAST = 3,
        CHAT = 4,
        NOTICE = 5;

    public static void handle(ByteBuffer bytes) {
        if (!bytes.hasRemaining())
            return;

        byte b = bytes.get();
        FiguraMod.debug("Got ws message of type: " + b);

        switch (b) {
            case AUTH -> auth();
            case PING -> ping(bytes);
            case EVENT -> event(bytes);
            case TOAST -> toast(bytes);
            case CHAT -> chat(bytes);
            case NOTICE -> notice(bytes);
        }
    }

    private static void auth() {
        FiguraMod.LOGGER.info("Connected to " + FiguraMod.MOD_NAME + " ws backend");
        NetworkStuff.backendStatus = 3;
        if (Config.CONNECTION_TOASTS.asBool())
            FiguraToast.sendToast(new FiguraText("backend.connected"));
    }

    private static void ping(ByteBuffer bytes) {
        UUID uuid = new UUID(bytes.getLong(), bytes.getLong());

        Avatar avatar = AvatarManager.getLoadedAvatar(uuid);
        if (avatar == null)
            return;

        int id = bytes.getInt();
        bytes.get(); //sync value is ignored

        byte[] data = new byte[bytes.remaining()];
        bytes.get(data);

        avatar.runPing(id, data);
        NetworkStuff.pingsReceived++;
        if (NetworkStuff.lastPing == 0) NetworkStuff.lastPing = FiguraMod.ticks;
    }

    private static void event(ByteBuffer bytes) {
        UUID uuid = new UUID(bytes.getLong(), bytes.getLong());
        AvatarManager.reloadAvatar(uuid);
    }

    private static void toast(ByteBuffer bytes) {
        byte type = bytes.get();
        String[] str = StandardCharsets.UTF_8.decode(bytes).toString().split("\0", 2);
        FiguraToast.sendToast(str[0], str.length > 1 ? str[1] : "", FiguraToast.ToastType.values()[type]);
    }

    private static void chat(ByteBuffer bytes) {
        String message = StandardCharsets.UTF_8.decode(bytes).toString();
        FiguraMod.sendChatMessage(TextComponent.EMPTY.copy().append(new TextComponent("-- " + FiguraMod.MOD_NAME + " backend message --\n\n").withStyle(ColorUtils.Colors.SKYE_BLUE.style)).append(message));
    }

    private static void notice(ByteBuffer bytes) {
        if (!Config.CONNECTION_TOASTS.asBool())
            return;

        byte type = bytes.get();
        switch (type) {
            case 0 -> FiguraToast.sendToast(new FiguraText("backend.warning"), new FiguraText("backend.ping_size"), FiguraToast.ToastType.ERROR);
            case 1 -> FiguraToast.sendToast(new FiguraText("backend.warning"), new FiguraText("backend.ping_rate"), FiguraToast.ToastType.ERROR);
        }
    }
}
