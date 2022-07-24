package org.moon.figura.avatars.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.ItemStackWrapper;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.utils.LuaUtils;
import org.terasology.jnlua.LuaRuntimeException;

@LuaWhitelist
@LuaTypeDoc(
        name = "ItemTask",
        description = "item_task"
)
public class ItemTask extends RenderTask {

    private ItemStack item;
    private ItemTransforms.TransformType renderType = ItemTransforms.TransformType.NONE;
    private boolean left = false;

    @Override
    public void render(PoseStack stack, MultiBufferSource buffer, int light, int overlay) {
        if (!enabled || item == null || item.isEmpty())
            return;

        stack.pushPose();
        this.apply(stack);
        stack.scale(-16, 16, -16);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                null, item, renderType, left,
                stack, buffer, null,
                emissive ? LightTexture.FULL_BRIGHT : light, overlay, 0);

        stack.popPose();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {ItemTask.class, String.class},
                            argumentNames = {"task", "item"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {ItemTask.class, ItemStackWrapper.class},
                            argumentNames = {"task", "item"}
                    )
            },
            description = "item_task.item"
    )
    public static RenderTask item(@LuaNotNil ItemTask task, Object item) {
        task.item = LuaUtils.parseItemStack("item", item);
        return task;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                        argumentTypes = {ItemTask.class, String.class},
                        argumentNames = {"task", "renderType"}
            ),
            description = "item_task.render_type"
    )
    public static RenderTask renderType(@LuaNotNil ItemTask task, @LuaNotNil String type) {
        try {
            task.renderType = ItemTransforms.TransformType.valueOf(type);
            task.left = task.renderType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND || task.renderType == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND;
            return task;
        } catch (Exception ignored) {
            throw new LuaRuntimeException("Illegal RenderType: \"" + type + "\".");
        }
    }
}
