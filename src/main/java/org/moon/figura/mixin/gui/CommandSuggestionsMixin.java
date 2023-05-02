package org.moon.figura.mixin.gui;

import com.mojang.brigadier.StringReader;
import net.minecraft.client.gui.components.CommandSuggestions;
import org.moon.figura.gui.Emojis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collection;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {

    @Inject(method = "updateCommandInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/SharedSuggestionProvider;suggest(Ljava/lang/Iterable;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void addFiguraSuggestions(CallbackInfo ci, String string, StringReader stringReader, boolean bl2, int i, String string2, int j, Collection<String> collection) {
        String lastWord = string2.substring(j);
        Collection<String> emojis = Emojis.getMatchingEmojis(lastWord);
        if (!emojis.isEmpty()) {
            collection.clear();
            collection.addAll(emojis);
        }
    }
}
