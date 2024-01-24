package org.figuramc.figura.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.compat.wrappers.ClassWrapper;
import org.figuramc.figura.compat.wrappers.FieldWrapper;
import org.figuramc.figura.compat.wrappers.MethodWrapper;

public class SimpleVCCompat {

    private static ClassWrapper ClientManager;
    private static FieldWrapper renderEventsField;
    private static ClassWrapper RenderEvents;
    private static MethodWrapper onRenderName;
    public static void init() {
        ClientManager = new ClassWrapper("de.maxhenkel.voicechat.voice.client.ClientManager");
        renderEventsField = ClientManager.getField("renderEvents");
        RenderEvents = new ClassWrapper("de.maxhenkel.voicechat.voice.client.RenderEvents");
        onRenderName = RenderEvents.getMethod("onRenderName", Entity.class, Component.class, PoseStack.class, MultiBufferSource.class, int.class);
    }


    // Accesses SimpleVC's onRenderName method through reflection so that no dependency is actually needed
    public static void renderSimpleVCIcon(Entity entity, Component text, PoseStack stack, MultiBufferSource multiBufferSource, int light) {
        if (ClientManager.isLoaded && renderEventsField.exists() && onRenderName.exists()) {
            onRenderName.invoke(renderEventsField.getValue(ClientManager.getMethod("instance").invoke(null)), entity, text, stack, multiBufferSource, light);
        }
    }
}
