package org.figuramc.figura.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.ducks.LivingEntityRendererAccessor;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.entity.EntityAPI;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.mixin.LivingEntityAccessor;
import org.figuramc.figura.model.FiguraModelPart;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaError;

import java.util.OptionalInt;
import java.util.function.Function;

@LuaWhitelist
@LuaTypeDoc(
        name = "EntityTask",
        value = "entity_task"
)
public class EntityTask extends RenderTask {

    @Nullable Entity entity;
    long ticksSinceEntity;

    public EntityTask(String name, Avatar owner, FiguraModelPart parent) {
        super(name, owner, parent);
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource buffer, int light, int overlay) {
        stack.scale(16, 16, 16);

        if (entity != null) {
            assert Minecraft.getInstance().level != null;
            entity.tickCount = (int) (Minecraft.getInstance().level.getGameTime() - ticksSinceEntity);
            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            boolean h = dispatcher.shouldRenderHitBoxes();
            dispatcher.setRenderHitBoxes(false);
            OptionalInt prev = LivingEntityRendererAccessor.overrideOverlay;
            LivingEntityRendererAccessor.overrideOverlay = OptionalInt.of(this.customization.overlay != null ? this.customization.overlay : overlay);
            try {
                Minecraft.getInstance().getEntityRenderDispatcher()
                        .render(
                                entity, 0.0, 0.0, 0.0, 0.0F, Minecraft.getInstance().getFrameTime(), stack, buffer,
                                this.customization.light != null ? this.customization.light : light
                        );
            }
            finally {
                LivingEntityRendererAccessor.overrideOverlay = prev;
                dispatcher.setRenderHitBoxes(h);
            }
        }
    }


    @Override
    public int getComplexity() {
        return 20;  // good enough
    }

    @Override
    public boolean shouldRender() {
        return super.shouldRender() && entity != null;
    }

    @LuaWhitelist
    @LuaMethodDoc("entity_task.as_entity")
    public EntityAPI<?> asEntity() {
        return entity == null ? null : new EntityAPI<>(entity);
    }

    @LuaWhitelist
    @Contract("_,_->this")
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "nbt"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, String.class},
                            argumentNames = {"id", "nbt"}
                    )
            },

            value = "entity_task.set_nbt"
    )
    public EntityTask setNbt(String nbtOrId, String nullOrNbt) {
        try {
            CompoundTag finalNbt;
            if(nullOrNbt == null) {
                finalNbt = (new TagParser(new StringReader(nbtOrId))).readStruct();

                if (!finalNbt.contains("id", CompoundTag.TAG_STRING)) {
                    throw new LuaError("Nbt must contain id");
                }
            }
            else {
                finalNbt = (new TagParser(new StringReader(nullOrNbt))).readStruct();
                finalNbt.put("id", StringTag.valueOf(nbtOrId));
            }

            assert Minecraft.getInstance().level != null;
            entity = EntityType.loadEntityRecursive(finalNbt, Minecraft.getInstance().level, Function.identity());
            if (entity == null) {
                throw new LuaError("Could not create entity");
            }
            ticksSinceEntity = Minecraft.getInstance().level.getGameTime() - entity.tickCount;
        } catch (CommandSyntaxException e) {
            throw new LuaError(e.getMessage());
        }
        return this;
    }

    @LuaWhitelist
    @Contract("_->this")
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "distance"
            ),
            value = "entity_task.update_walking_distance"
    )
    public EntityTask updateWalkingDistance(float distance) {
        if(entity != null && entity instanceof LivingEntity living) {
            ((LivingEntityAccessor) living).invokeUpdateWalkAnimation(distance);
        }
        return this;
    }

    @LuaWhitelist
    @Contract("_->this")
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraVec2.class,
                    argumentNames = "rotation"
            ),
            value = "entity_task.set_head_rotation"
    )
    public EntityTask setHeadRotation(FiguraVec2 vec2) {
        if(entity != null && entity instanceof LivingEntity living) {
            living.yHeadRot = (float) vec2.y;
            living.yHeadRotO = (float) vec2.y;
            living.setXRot((float) vec2.x);
            living.xRotO = (float) vec2.x;
        }
        return this;
    }
}