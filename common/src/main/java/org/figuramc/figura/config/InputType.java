package org.figuramc.figura.config;

import com.google.common.net.HostAndPort;
import org.figuramc.figura.gui.widgets.TextField;
import org.figuramc.figura.utils.ColorUtils;

import java.net.IDN;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Predicate;

public enum InputType {
    ANY(s -> true),
    INT(s -> {
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }),
    POSITIVE_INT(s -> {
        try {
            Integer i = Integer.parseInt(s);
            return i >= 0;
        } catch (Exception ignored) {
            return false;
        }
    }),
    FLOAT(s -> {
        try {
            Float f = Float.parseFloat(s);
            return !f.isInfinite() && !f.isNaN();
        } catch (Exception ignored) {
            return false;
        }
    }),
    POSITIVE_FLOAT(s -> {
        try {
            Float f = Float.parseFloat(s);
            return !f.isInfinite() && !f.isNaN() && f >= 0f;
        } catch (Exception ignored) {
            return false;
        }
    }),
    HEX_COLOR(s -> ColorUtils.userInputHex(s, null) != null),
    FOLDER_PATH(s -> {
        try {
            return s.trim().isEmpty() || Files.isDirectory(Paths.get(s.trim()));
        } catch (Exception ignored) {
            return false;
        }
    }),
    IP(InputType::isValidAddress);

    public final Predicate<String> validator;
    public final TextField.HintType hint;

    InputType(Predicate<String> predicate) {
        this.validator = predicate;
        this.hint = TextField.HintType.valueOf(this.name());
    }

    public static boolean isValidAddress(String address) {
        try {
            HostAndPort hostAndPort = HostAndPort.fromString(address);
            String string = hostAndPort.getHost();
            if (!string.isEmpty()) {
                IDN.toASCII(string);
                return true;
            }
        } catch (IllegalArgumentException ignored) {
        }
        return false;
    }

}
