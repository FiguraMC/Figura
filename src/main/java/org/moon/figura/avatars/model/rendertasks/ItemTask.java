package org.moon.figura.avatars.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;
import org.luaj.vm2.LuaError;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaType;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.ItemStackAPI;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.utils.LuaUtils;

@LuaType(typeName = "item_task")
@LuaTypeDoc(
        name = "Item Task",
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
                            argumentTypes = String.class,
                            argumentNames = "item"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = ItemStackAPI.class,
                            argumentNames = "item"
                    )
            },
            description = "item_task.item"
    )
    public RenderTask item(Object item) {
        this.item = LuaUtils.parseItemStack("item", item);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                        argumentTypes = String.class,
                        argumentNames = "renderType"
            ),
            description = "item_task.render_type"
    )
    public RenderTask renderType(@LuaNotNil String type) {
        try {
            this.renderType = ItemTransforms.TransformType.valueOf(type);
            this.left = this.renderType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND || this.renderType == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND;
            return this;
        } catch (Exception ignored) {
            throw new LuaError("Illegal RenderType: \"" + type + "\".");
        }
    }

    @Override
    public String toString() {
        return "Item Render Task";
    }
}
