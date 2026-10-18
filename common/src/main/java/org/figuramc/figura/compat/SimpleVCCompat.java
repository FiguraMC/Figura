package org.figuramc.figura.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
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
        onRenderName = RenderEvents.getMethod("onRenderName", EntityRenderState.class, CameraRenderState.class, PoseStack.class, SubmitNodeCollector.class);
    }


    // Accesses SimpleVC's onRenderName method through reflection so that no dependency is actually needed
    public static void renderSimpleVCIcon(EntityRenderState state, CameraRenderState cameraRenderState, PoseStack stack, SubmitNodeCollector collector) {
        if (ClientManager.isLoaded && renderEventsField.exists() && onRenderName.exists()) {
            onRenderName.invoke(renderEventsField.getValue(ClientManager.getMethod("instance").invoke(null)), state, cameraRenderState, stack, collector);
        }
    }
}
