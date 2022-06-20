package org.moon.figura.avatars.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.utils.TextUtils;

import java.util.List;

@LuaWhitelist
@LuaTypeDoc(
        name = "TextTask",
        description = "text_task"
)
public class TextTask extends RenderTask {

    private List<Component> text;

    @Override
    public void render(PoseStack stack, MultiBufferSource buffer, int light, int overlay) {
        if (!enabled || text == null || text.size() == 0)
            return;

        stack.pushPose();
        applyMatrices(stack);
        stack.scale(1, -1, 1);

        Font font = Minecraft.getInstance().font;

        for (int i = 0; i < text.size(); i++)
            font.drawInBatch(text.get(i), 0, i * font.lineHeight, 0xFFFFFF, false, stack.last().pose(), buffer, false, 0, emissive ? LightTexture.FULL_BRIGHT : light);

        stack.popPose();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {TextTask.class, String.class},
                            argumentNames = {"task", "text"}
                    )
            },
            description = "text_task.text"
    )
    public static RenderTask text(@LuaNotNil TextTask task, String text) {
        task.text = text == null ? null : TextUtils.splitText(TextUtils.tryParseJson(text), "\n");
        return task;
    }
}
