package org.figuramc.figura.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ClickEvent;
import org.jetbrains.annotations.NotNull;

public record FiguraFunctionClickEvent(String chunk) implements ClickEvent{
    public static final MapCodec<FiguraFunctionClickEvent> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(Codec.STRING.fieldOf("value").forGetter(FiguraFunctionClickEvent::chunk)).apply(instance, FiguraFunctionClickEvent::new)
    );

    @Override
    public @NotNull Action action() {
        return Action.valueOf("FIGURA_FUNCTION");
    }
}
