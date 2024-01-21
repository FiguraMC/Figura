package org.figuramc.figura.mixin.gui;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.util.StringRepresentable;
import org.figuramc.figura.utils.TextUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

@Mixin(ClickEvent.Action.class)
public class ClickEventActionMixin {

    // courtesy of https://github.com/SpongePowered/Mixin/issues/387
    @Shadow @Final @Mutable
    private static ClickEvent.Action[] $VALUES;
    @Shadow @Final private String name;

    static {
        figura$addVariant("FIGURA_FUNCTION", "figura_function", false);
    }

    @Invoker("<init>")
    public static ClickEvent.Action figura$invokeInit(String internalName, int internalId, String name, boolean user) {
        throw new AssertionError();
    }

    @SuppressWarnings({"SameParameterValue"}) // technically right, but it's ugly to hardcode values here
    private static ClickEvent.Action figura$addVariant(String internalName, String name, boolean user) {
        ArrayList<ClickEvent.Action> variants = new ArrayList<>(Arrays.asList($VALUES));
        ClickEvent.Action action = figura$invokeInit(internalName, variants.get(variants.size() - 1).ordinal() + 1, name, user);
        variants.add(action);
        $VALUES = variants.toArray(new ClickEvent.Action[0]);
        return action;
    }

    @Inject(at = @At("HEAD"), method = "isAllowedFromServer", cancellable = true)
    private void isAllowedFromServer(CallbackInfoReturnable<Boolean> cir) {
        if (TextUtils.allowScriptEvents)
            cir.setReturnValue(true);
    }
}
