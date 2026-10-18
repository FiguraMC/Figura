package org.figuramc.figura.mixin.render.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.TextFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.ducks.CameraRenderStateExtension;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.TextUtils;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SubmitNodeCollection.class)
public class NameTagFeatureRenderer$StorageMixin {
    @Inject(method = "submitNameTag", at = @At("HEAD"), cancellable = true)
    private void figura$submitNameTag(PoseStack pose, Vec3 attachment, int yOffset, Component text,
                                     boolean seeThrough, int light, CameraRenderState camera, CallbackInfo ci) {
        CameraRenderStateExtension cameraExtension = (CameraRenderStateExtension) camera;
        var avatar = cameraExtension.figura$getAvatar();
        boolean renderingName = cameraExtension.figura$isRenderingNameTag();
        cameraExtension.figura$setAvatar(null);
        cameraExtension.figura$setRenderingNameTag(false);
        if (avatar == null || avatar.luaRuntime == null || attachment == null
                || avatar.permissions.get(Permissions.NAMEPLATE_EDIT) != 1
                || Configs.ENTITY_NAMEPLATE.value <= 0 || AvatarManager.panic)
            return;
        var custom = avatar.luaRuntime.nameplate.ENTITY;
        Minecraft client = Minecraft.getInstance();
        Font font = client.font;
        SubmitNodeCollection collection = (SubmitNodeCollection) (Object) this;
        var lines = renderingName ? TextUtils.splitText(text, "\\r?\\n") : java.util.List.of(text);
        pose.pushPose();
        try {
            var pivot = custom.getPivot();
            if (pivot == null)
                pose.translate(attachment.x, attachment.y + 0.5, attachment.z);
            else
                pose.translate(pivot.x, pivot.y, pivot.z);
            pose.rotate(camera.orientation);
            var position = custom.getPos();
            if (position != null)
                pose.translate(position.x, position.y, position.z);
            var scale = custom.getScale();
            pose.scale(0.025f * (scale == null ? 1f : (float) scale.x),
                    -0.025f * (scale == null ? 1f : (float) scale.y),
                    0.025f * (scale == null ? 1f : (float) scale.z));
            float opacity = client.gameRenderer.gameRenderState().optionsRenderState.getBackgroundOpacity(0.25f);
            int background = custom.background != null ? custom.background : ARGB.color(opacity, 0xFF000000);
            int foreground = ARGB.color(Math.max((opacity + 0.75f) * 0.5f, 0.5f), -1);
            int lightCoords = custom.light != null ? custom.light : light;
            int outline = custom.outline ? (custom.outlineColor != null ? custom.outlineColor : 0x202020) : 0;
            Matrix4f matrix = new Matrix4f(pose.last().pose());
            for (int i = 0; i < lines.size(); i++) {
                Component line = lines.get(i);
                if (line.getString().isEmpty())
                    continue;
                float x = -font.width(line) / 2f;
                float y = renderingName ? yOffset + (font.lineHeight + 1) * (i - lines.size() + 1) : yOffset;
                if (seeThrough) {
                    collection.nameTags.submit(new TextFeatureRenderer.Submit(matrix, Font.DisplayMode.NORMAL,
                            LightCoordsUtil.lightCoordsWithEmission(lightCoords, 2),
                            new TextFeatureRenderer.Content.Text(x, y, line.getVisualOrderText(), custom.shadow,
                                    -1, 0, outline)));
                    collection.seeThrough.submit(new TextFeatureRenderer.Submit(matrix, Font.DisplayMode.SEE_THROUGH,
                            lightCoords, new TextFeatureRenderer.Content.Text(x, y, line.getVisualOrderText(),
                                    custom.shadow, foreground, background, 0)));
                } else {
                    collection.nameTags.submit(new TextFeatureRenderer.Submit(matrix, Font.DisplayMode.NORMAL,
                            lightCoords, new TextFeatureRenderer.Content.Text(x, y, line.getVisualOrderText(),
                                    custom.shadow, foreground, background, outline)));
                }
            }
        } finally {
            pose.popPose();
        }
        ci.cancel();
    }
}