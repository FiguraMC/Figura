package org.figuramc.figura.mixin.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.figuramc.figura.gui.FiguraFunctionClickEvent;
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

    @Shadow @Final @Mutable
    public static Codec<ClickEvent.Action> UNSAFE_CODEC;
    @Shadow @Final @Mutable
    public static Codec<ClickEvent.Action> CODEC;

    static {
        figura$addVariant("FIGURA_FUNCTION", "figura_function", false, FiguraFunctionClickEvent.CODEC);
    }

    @Invoker("<init>")
    public static ClickEvent.Action figura$invokeInit(String internalName, int internalId, String name, boolean user, MapCodec<? extends ClickEvent> codec) {
        throw new AssertionError();
    }

    @SuppressWarnings({"SameParameterValue"}) // technically right, but it's ugly to hardcode values here
    private static ClickEvent.Action figura$addVariant(String internalName, String name, boolean user, MapCodec<? extends ClickEvent> codec) {
        ArrayList<ClickEvent.Action> variants = new ArrayList<>(Arrays.asList($VALUES));
        ClickEvent.Action action = figura$invokeInit(internalName, variants.getLast().ordinal() + 1, name, user, codec);
        variants.add(action);
        $VALUES = variants.toArray(new ClickEvent.Action[0]);
        UNSAFE_CODEC = StringRepresentable.fromEnum(ClickEvent.Action::values);
        CODEC = UNSAFE_CODEC.validate(ClickEvent.Action::filterForSerialization);
        return action;
    }

    @Inject(at = @At("HEAD"), method = "isAllowedFromServer", cancellable = true)
    private void isAllowedFromServer(CallbackInfoReturnable<Boolean> cir) {
        if (TextUtils.allowScriptEvents)
            cir.setReturnValue(true);
    }

    @Inject(at = @At("HEAD"), method = "filterForSerialization", cancellable = true)
    private static void filterForSerialization(ClickEvent.Action action, CallbackInfoReturnable<DataResult<ClickEvent.Action>> cir) {
        if (TextUtils.allowScriptEvents)
            cir.setReturnValue(DataResult.success(action, Lifecycle.stable()));
    }
}
