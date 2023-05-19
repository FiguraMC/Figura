package org.moon.figura.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.luaj.vm2.LuaError;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.ItemStackAPI;
import org.moon.figura.lua.api.world.WorldAPI;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.model.PartCustomization;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "ItemTask",
        value = "item_task"
)
public class ItemTask extends RenderTask {

    private ItemStack item;
    private ItemDisplayContext displayMode = ItemDisplayContext.NONE;
    private boolean left = false;
    private int cachedComplexity;

    public ItemTask(String name, Avatar owner) {
        super(name, owner);
    }

    @Override
    public void render(PartCustomization.PartCustomizationStack stack, MultiBufferSource buffer, int light, int overlay) {
        this.pushOntoStack(stack);
        PoseStack poseStack = stack.peek().copyIntoGlobalPoseStack();
        poseStack.scale(-16, 16, -16);

        LivingEntity entity = owner.renderer.entity instanceof LivingEntity living ? living : null;
        int newLight = this.customization.light != null ? this.customization.light : light;
        int newOverlay = this.customization.overlay != null ? this.customization.overlay : overlay;
        int seed = entity != null ? entity.getId() + displayMode.ordinal() : 0;

        Minecraft.getInstance().getItemRenderer().renderStatic(
                entity, item, displayMode, left,
                poseStack, buffer, WorldAPI.getCurrentWorld(),
                newLight, newOverlay, seed
        );

        stack.pop();
    }

    @Override
    public int getComplexity() {
        return cachedComplexity;
    }

    @Override
    public boolean shouldRender() {
        return super.shouldRender() && item != null && !item.isEmpty();
    }

    // -- lua -- //


    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "item"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = ItemStackAPI.class,
                            argumentNames = "item"
                    )
            },
            aliases = "item",
            value = "item_task.set_item"
    )
    public ItemTask setItem(Object item) {
        this.item = LuaUtils.parseItemStack("item", item);
        Minecraft client = Minecraft.getInstance();
        RandomSource random = client.level != null ? client.level.random : RandomSource.create();
        cachedComplexity = client.getItemRenderer().getModel(this.item, null, null, 0).getQuads(null, null, random).size();
        return this;
    }

    @LuaWhitelist
    public ItemTask item(Object item) {
        return setItem(item);
    }

    @LuaWhitelist
    @LuaMethodDoc("item_task.get_display_mode")
    public String getDisplayMode() {
        return this.displayMode.name();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                        argumentTypes = String.class,
                        argumentNames = "displayMode"
            ),
            aliases = "displayMode",
            value = "item_task.set_display_mode"
    )
    public ItemTask setDisplayMode(@LuaNotNil String mode) {
        try {
            this.displayMode = ItemDisplayContext.valueOf(mode.toUpperCase());
            this.left = this.displayMode == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || this.displayMode == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
            return this;
        } catch (Exception ignored) {
            throw new LuaError("Illegal display mode: \"" + mode + "\".");
        }
    }

    @LuaWhitelist
    public ItemTask displayMode(@LuaNotNil String mode) {
        return setDisplayMode(mode);
    }

    @Override
    public String toString() {
        return name + " (Item Render Task)";
    }
}
