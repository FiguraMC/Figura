package org.figuramc.figura.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SimpleVCCompat {
    private static Method onRenderName;
    private static Object renderEvents;
    private static boolean simpleVCPresent = true;

    // Accesses SimpleVC's onRenderName method through reflection so that no dependency is actually needed
    public static void renderSimpleVCIcon(Entity entity, Component text, PoseStack stack, MultiBufferSource multiBufferSource, int light) {
        if (simpleVCPresent && renderEvents != null && onRenderName != null) {
            try {
                onRenderName.invoke(renderEvents, entity, text, stack, multiBufferSource, light);
            } catch (InvocationTargetException | IllegalAccessException e) {
                simpleVCPresent = false;
            }
            return;
        }
        if (onRenderName == null && renderEvents == null && simpleVCPresent) {
            try {
                Class<?> clientManager = Class.forName("de.maxhenkel.voicechat.voice.client.ClientManager");
                Field renderEventsField = clientManager.getDeclaredField("renderEvents");
                renderEventsField.setAccessible(true);
                renderEvents = renderEventsField.get(clientManager.getDeclaredMethod("instance").invoke(null));
                Class<?> renderEventsClass = Class.forName("de.maxhenkel.voicechat.voice.client.RenderEvents");
                Method method = renderEventsClass.getDeclaredMethod("onRenderName", Entity.class, Component.class, PoseStack.class, MultiBufferSource.class, int.class);
                method.setAccessible(true);
                onRenderName = method;
            } catch (Exception ignored) {
                simpleVCPresent = false;
            }
        }
    }
}
