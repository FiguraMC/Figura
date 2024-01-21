package org.figuramc.figura.mixin.neoforge;

import com.mojang.brigadier.StringReader;
import net.minecraft.client.gui.components.CommandSuggestions;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.ducks.SuggestionsListAccessor;
import org.figuramc.figura.font.Emojis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collection;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixinNeoForge {

    @Shadow private CommandSuggestions.SuggestionsList suggestions;

    @Shadow public abstract void showSuggestions(boolean narrateFirstSuggestion);

    @Unique private boolean emojiSuggestions;

    @Inject(method = "updateCommandInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/SharedSuggestionProvider;suggest(Ljava/lang/Iterable;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void addFiguraSuggestions(CallbackInfo ci, String string, StringReader stringReader, boolean bl2, boolean bl3, int i, String string2, int j, Collection<String> collection) {
        emojiSuggestions = false;
        if (Configs.EMOJIS.value == 0)
            return;

        String lastWord = string2.substring(j);
        Collection<String> emojis = Emojis.getMatchingEmojis(lastWord);
        emojiSuggestions = !emojis.isEmpty();
        if (emojiSuggestions)
            collection.addAll(emojis);
    }

    @Inject(method = "updateCommandInfo", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/commands/SharedSuggestionProvider;suggest(Ljava/lang/Iterable;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;", shift = At.Shift.AFTER))
    private void afterSuggesting(CallbackInfo ci) {
        if (emojiSuggestions && Configs.EMOJIS.value >= 2)
            this.showSuggestions(false);
    }

    @Inject(method = "showSuggestions", at = @At("RETURN"))
    private void showSuggestions(CallbackInfo ci) {
        if (emojiSuggestions && this.suggestions != null) {
            ((SuggestionsListAccessor) this.suggestions).figura$setFiguraList(true);
            emojiSuggestions = false;
        }
    }
}
