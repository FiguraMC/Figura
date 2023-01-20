package org.moon.figura.mixin;

import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.moon.figura.FiguraMod;
import org.moon.figura.lang.FiguraLangManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mixin(ClientLanguage.class)
public class ClientLanguageMixin {

    @Unique
    private static ResourceLocation location;

    @Inject(method = "loadFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/language/ClientLanguage;appendFrom(Ljava/lang/String;Ljava/util/List;Ljava/util/Map;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void appendFrom(ResourceManager resourceManager, List<LanguageInfo> definitions, CallbackInfoReturnable<ClientLanguage> cir, Map<String, String> map, boolean bl, Iterator<LanguageInfo> var4, LanguageInfo languageInfo, String string, String string2, Iterator<String> var8, String string3, ResourceLocation resourceLocation) {
        location = resourceLocation;
    }

    @ModifyVariable(at = @At("HEAD"), method = "appendFrom", argsOnly = true)
    private static List<Resource> appendFrom(List<Resource> resources) {
        if (location == null || !location.getNamespace().equals(FiguraMod.MOD_ID))
            return resources;

        InputStream stream;
        try {
            stream = FiguraLangManager.LANG_PACK.getResource(PackType.CLIENT_RESOURCES, location);
        } catch (Exception ignored) {
            return resources;
        }

        int i = 0;
        for (int j = 0; j < resources.size(); j++) {
            if (resources.get(j).sourcePackId().equals(FiguraMod.MOD_NAME)) {
                i = j + 1;
                break;
            }
        }

        Resource resource = new Resource(FiguraMod.MOD_NAME + " backend lang pack", () -> stream);
        List<Resource> tempList = new ArrayList<>(resources);
        tempList.add(i, resource);

        FiguraMod.debug("Injected Lang for {}", location.getPath());
        return tempList;
    }
}
