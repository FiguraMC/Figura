package org.moon.figura.backend;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.utils.FiguraText;

public enum Response {
    UPLOAD_SUCCESS("upload_success"),
    UPLOAD_TOO_BIG(FiguraToast.ToastType.ERROR, "upload_error", "upload_too_big"),
    UPLOAD_TOO_MANY(FiguraToast.ToastType.ERROR, "upload_error", "upload_too_many"),

    DELETE_SUCCESS("delete_success"),
    DELETE_NOT_FOUND(FiguraToast.ToastType.ERROR,"delete_error", "delete_not_found");

    public final FiguraToast.ToastType TYPE;
    public final MutableComponent TITLE, SUBTITLE;

    Response(String title) {
        this(FiguraToast.ToastType.DEFAULT, title, null);
    }

    Response(FiguraToast.ToastType type, String title, String sub) {
        TYPE = type;
        TITLE = FiguraText.of("backend." + title);
        SUBTITLE = sub == null ? Component.empty() : FiguraText.of("backend." + sub);
    }
}

