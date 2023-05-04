package org.moon.figura.mixin.gui;

import com.mojang.brigadier.StringReader;
import net.minecraft.client.gui.components.CommandSuggestions;
import org.moon.figura.config.Configs;
import org.moon.figura.ducks.SuggestionsListAccessor;
import org.moon.figura.gui.Emojis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collection;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {

    @Shadow private CommandSuggestions.SuggestionsList suggestions;

    @Unique private boolean figuraList;

    @Inject(method = "updateCommandInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/SharedSuggestionProvider;suggest(Ljava/lang/Iterable;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void addFiguraSuggestions(CallbackInfo ci, String string, StringReader stringReader, boolean bl2, int i, String string2, int j, Collection<String> collection) {
        if (!Configs.CHAT_EMOJIS.value)
            return;

        String lastWord = string2.substring(j);
        Collection<String> emojis = Emojis.getMatchingEmojis(lastWord);
        if (!emojis.isEmpty()) {
            collection.clear();
            collection.addAll(emojis);
            figuraList = true;
        }
    }

    @Inject(method = "showSuggestions", at = @At("RETURN"))
    private void afterShowSuggestions(CallbackInfo ci) {
        if (figuraList) {
            if (this.suggestions != null)
                ((SuggestionsListAccessor) this.suggestions).figura$setFiguraList(true);
            figuraList = false;
        }
    }
}
