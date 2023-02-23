package org.moon.figura.avatar;

import com.mojang.blaze3d.audio.OggAudioStream;
import com.mojang.blaze3d.audio.SoundBuffer;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.moon.figura.FiguraMod;
import org.moon.figura.animation.Animation;
import org.moon.figura.animation.AnimationPlayer;
import org.moon.figura.backend2.NetworkStuff;
import org.moon.figura.config.Config;
import org.moon.figura.lua.FiguraLuaPrinter;
import org.moon.figura.lua.FiguraLuaRuntime;
import org.moon.figura.lua.api.entity.EntityAPI;
import org.moon.figura.lua.api.event.LuaEvent;
import org.moon.figura.lua.api.particle.ParticleAPI;
import org.moon.figura.lua.api.ping.PingArg;
import org.moon.figura.lua.api.ping.PingFunction;
import org.moon.figura.lua.api.sound.SoundAPI;
import org.moon.figura.lua.api.world.BlockStateAPI;
import org.moon.figura.lua.api.world.ItemStackAPI;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.model.ParentType;
import org.moon.figura.model.PartCustomization;
import org.moon.figura.model.rendering.AvatarRenderer;
import org.moon.figura.model.rendering.EntityRenderMode;
import org.moon.figura.model.rendering.ImmediateAvatarRenderer;
import org.moon.figura.model.rendering.PartFilterScheme;
import org.moon.figura.permissions.PermissionManager;
import org.moon.figura.permissions.PermissionPack;
import org.moon.figura.permissions.Permissions;
import org.moon.figura.utils.EntityUtils;
import org.moon.figura.utils.RefilledNumber;
import org.moon.figura.utils.Version;
import org.moon.figura.utils.ui.UIHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

//the avatar class
//contains all things related to the avatar
//and also related to the owner, like its permissions
public class Avatar {

    private static CompletableFuture<Void> tasks;
    public static boolean firstPerson;

    //properties
    public final UUID owner;
    public final EntityType<?> entityType;
    public CompoundTag nbt;
    public boolean loaded = true;
    public final boolean isHost;

    //metadata
    public String name, entityName;
    public String authors;
    public Version version;
    public String id;
    public int fileSize;
    public String color;
    public boolean minify;

    //Runtime data
    private final Queue<Supplier<Varargs>> events = new ConcurrentLinkedQueue<>();

    public AvatarRenderer renderer;
    public FiguraLuaRuntime luaRuntime;
    public EntityRenderMode renderMode = EntityRenderMode.OTHER;

    public final PermissionPack.PlayerPermissionPack permissions;

    public final Map<String, SoundBuffer> customSounds = new HashMap<>();
    public final Map<Integer, Animation> animations = new HashMap<>();

    //runtime status
    public boolean hasTexture, scriptError;
    public Component errorText;
    public Set<Permissions> noPermissions = new HashSet<>();
    public Set<Permissions> permissionsToTick = new HashSet<>();
    public int versionStatus = 0;

    //limits
    public int animationComplexity;
    public final Instructions complexity;
    public final Instructions init, render, worldRender, tick, worldTick;
    public final RefilledNumber particlesRemaining, soundsRemaining;

    private Avatar(UUID owner, EntityType<?> type, String name) {
        this.owner = owner;
        this.entityType = type;
        this.isHost = type == EntityType.PLAYER && FiguraMod.isLocal(owner);
        this.permissions = type == EntityType.PLAYER ? PermissionManager.get(owner) : PermissionManager.getMobPermissions(owner);
        this.complexity = new Instructions(permissions.get(Permissions.COMPLEXITY));
        this.init = new Instructions(permissions.get(Permissions.INIT_INST));
        this.render = new Instructions(permissions.get(Permissions.RENDER_INST));
        this.worldRender = new Instructions(permissions.get(Permissions.WORLD_RENDER_INST));
        this.tick = new Instructions(permissions.get(Permissions.TICK_INST));
        this.worldTick = new Instructions(permissions.get(Permissions.WORLD_TICK_INST));
        this.particlesRemaining = new RefilledNumber(permissions.get(Permissions.PARTICLES));
        this.soundsRemaining = new RefilledNumber(permissions.get(Permissions.SOUNDS));
        this.entityName = name == null ? "" : name;
    }

    public Avatar(UUID owner) {
        this(owner, EntityType.PLAYER, EntityUtils.getNameForUUID(owner));
    }

    public Avatar(Entity entity) {
        this(entity.getUUID(), entity.getType(), entity.getName().getString());
    }

    public void load(CompoundTag nbt) {
        Runnable toRun = () -> {
            this.nbt = nbt;
            loaded = false;
        };

        if (tasks == null || tasks.isDone()) {
            tasks = CompletableFuture.runAsync(toRun);
        } else {
            tasks.thenRun(toRun);
        }

        tasks.join();

        if (nbt == null) {
            loaded = true;
            return;
        }

        tasks.thenRun(() -> {
            try {
                //metadata
                CompoundTag metadata = nbt.getCompound("metadata");
                name = metadata.getString("name");
                authors = metadata.getString("authors");
                version = new Version(metadata.getString("ver"));
                if (metadata.contains("id"))
                    id = metadata.getString("id");
                if (metadata.contains("color"))
                    color = metadata.getString("color");
                if (metadata.contains("minify"))
                    minify = metadata.getBoolean("minify");
                fileSize = getFileSize();
                versionStatus = getVersionStatus();
                if (entityName.isBlank())
                    entityName = name;

                //animations and models
                loadAnimations();
                renderer = new ImmediateAvatarRenderer(this);

                //sounds and script
                loadCustomSounds();
                createLuaRuntime();
            } catch (Exception e) {
                FiguraMod.LOGGER.error("", e);
                clean();
                this.nbt = null;
                this.renderer = null;
                this.luaRuntime = null;
            }

            loaded = true;
        });
    }

    public void tick() {
        if (scriptError || luaRuntime == null || !loaded)
            return;

        //fetch this avatar entity
        if (luaRuntime.getUser() == null) {
            Entity entity = EntityUtils.getEntityByUUID(owner);
            if (entity != null) {
                luaRuntime.setUser(entity);
                run("ENTITY_INIT", init.post());
            }
        }

        //tick permissions
        for (Permissions t : permissionsToTick) {
            if (permissions.get(t) > 0) {
                noPermissions.remove(t);
            } else {
                noPermissions.add(t);
            }
        }

        //sound
        particlesRemaining.set(permissions.get(Permissions.PARTICLES));
        particlesRemaining.tick();

        //particles
        soundsRemaining.set(permissions.get(Permissions.SOUNDS));
        soundsRemaining.tick();

        //call events
        FiguraMod.pushProfiler("worldTick");
        worldTick.reset(permissions.get(Permissions.WORLD_TICK_INST));
        run("WORLD_TICK", worldTick);

        FiguraMod.popPushProfiler("tick");
        tick.reset(permissions.get(Permissions.TICK_INST));
        tickEvent();

        FiguraMod.popProfiler();
    }

    public void render(float delta) {
        if (complexity.remaining <= 0) {
            noPermissions.add(Permissions.COMPLEXITY);
        } else {
            noPermissions.remove(Permissions.COMPLEXITY);
        }

        complexity.reset(permissions.get(Permissions.COMPLEXITY));

        if (scriptError || luaRuntime == null || !loaded)
            return;

        render.reset(permissions.get(Permissions.RENDER_INST));
        worldRender.reset(permissions.get(Permissions.WORLD_RENDER_INST));
        run("WORLD_RENDER", worldRender, delta);
    }

    public void runPing(int id, byte[] data) {
        events.offer(() -> {
            if (scriptError || luaRuntime == null || !loaded)
                return null;

            LuaValue[] args = PingArg.fromByteArray(data, this);
            String name = luaRuntime.ping.getName(id);
            PingFunction function = luaRuntime.ping.get(name);
            if (args == null || function == null)
                return null;

            FiguraLuaPrinter.sendPingMessage(this, name, data.length, args);
            return run(function.func, tick, (Object[]) args);
        });
    }

    public Varargs run(Object toRun, Instructions limit, Object... args) {
        //create event
        Supplier<Varargs> ev = () -> {
            if (scriptError || luaRuntime == null || !loaded)
                return null;

            //parse args
            LuaValue[] values = new LuaValue[args.length];
            for (int i = 0; i < values.length; i++)
                values[i] = luaRuntime.typeManager.javaToLua(args[i]).arg1();

            Varargs val = LuaValue.varargsOf(values);

            //instructions limit
            luaRuntime.setInstructionLimit(limit.remaining);

            //get and call event
            try {
                Varargs ret;
                if (toRun instanceof LuaEvent event)
                    ret = event.call(val);
                else if (toRun instanceof String event)
                    ret = luaRuntime.events.__index(event).call(val);
                else if (toRun instanceof LuaFunction func)
                    ret = func.invoke(val);
                else if (toRun instanceof Pair<?, ?> pair)
                    ret = luaRuntime.load(pair.getFirst().toString(), pair.getSecond().toString()).invoke(val);
                else
                    throw new IllegalArgumentException("Internal event error - Invalid type to run!");

                limit.use(luaRuntime.getInstructions());
                return ret;
            } catch (Exception | StackOverflowError e) {
                if (luaRuntime != null)
                    luaRuntime.error(e);
            }

            return LuaValue.NIL;
        };

        //add event to the queue
        events.offer(ev);

        Varargs val = LuaValue.NIL;

        //run all queued events
        Supplier<Varargs> e;
        while ((e = events.poll()) != null) {
            try {
                Varargs result = e.get();

                //if the event is the same the one created, set the return value to it
                if (e == ev)
                    val = result;
            } catch (Exception | StackOverflowError ex) {
                if (luaRuntime != null)
                    luaRuntime.error(ex);
            }
        }

        //return the new event result
        return val;
    }

    // -- script events -- //

    public void tickEvent() {
        if (loaded && luaRuntime != null && luaRuntime.getUser() != null)
            run("TICK", tick);
    }

    public void renderEvent(float delta) {
        if (loaded && luaRuntime != null && luaRuntime.getUser() != null)
            run("RENDER", render, delta, renderMode.name());
    }

    public void postRenderEvent(float delta) {
        if (loaded && luaRuntime != null && luaRuntime.getUser() != null)
            run("POST_RENDER", render.post(), delta, renderMode.name());
        renderMode = EntityRenderMode.OTHER;
    }

    public void postWorldRenderEvent(float delta) {
        if (!loaded)
            return;

        if (renderer != null)
            renderer.allowMatrixUpdate = false;

        run("POST_WORLD_RENDER", worldRender.post(), delta);
    }

    public boolean skullRenderEvent(float delta, BlockStateAPI block, ItemStackAPI item, EntityAPI<?> entity, String mode) {
        Varargs result = null;
        if (loaded && renderer != null && renderer.allowSkullRendering)
            result = run("SKULL_RENDER", render, delta, block, item, entity, mode);
        return result != null && result.arg(1).isboolean() && result.arg(1).checkboolean();
    }

    public boolean useItemEvent(ItemStackAPI stack, String type, int particleCount) {
        Varargs result = loaded ? run("USE_ITEM", tick, stack, type, particleCount) : null;
        return result != null && result.arg(1).isboolean() && result.arg(1).checkboolean();
    }

    // -- host only events -- //

    public String chatSendMessageEvent(String message) {
        Varargs val = loaded ? run("CHAT_SEND_MESSAGE", tick, message) : null;
        return val == null || (!val.isnil(1) && !Config.CHAT_MESSAGES.asBool()) ? message : val.isnil(1) ? "" : val.arg(1).tojstring();
    }

    public void chatReceivedMessageEvent(Component message) {
        if (loaded)
            run("CHAT_RECEIVE_MESSAGE", tick, message.getString(), Component.Serializer.toJson(message));
    }

    public boolean mouseScrollEvent(double delta) {
        Varargs result = loaded ? run("MOUSE_SCROLL", tick, delta) : null;
        return result != null && result.arg(1).isboolean() && result.arg(1).checkboolean();
    }

    public boolean mouseMoveEvent(double x, double y) {
        Varargs result = loaded ? run("MOUSE_MOVE", tick, x, y) : null;
        return result != null && result.arg(1).isboolean() && result.arg(1).checkboolean();
    }

    public boolean mousePressEvent(int button, int action, int modifiers) {
        Varargs result = loaded ? run("MOUSE_PRESS", tick, button, action, modifiers) : null;
        return result != null && result.arg(1).isboolean() && result.arg(1).checkboolean();
    }

    public boolean keyPressEvent(int key, int action, int modifiers) {
        Varargs result = loaded ? run("KEY_PRESS", tick, key, action, modifiers) : null;
        return result != null && result.arg(1).isboolean() && result.arg(1).checkboolean();
    }

    // -- rendering events -- //

    private void render() {
        if (renderMode == EntityRenderMode.RENDER || renderMode == EntityRenderMode.FIRST_PERSON) {
            complexity.use(renderer.render());
            return;
        }

        int prev = complexity.remaining;
        complexity.remaining = permissions.get(Permissions.COMPLEXITY);
        renderer.render();
        complexity.remaining = prev;
    }

    public void render(Entity entity, float yaw, float delta, float alpha, PoseStack matrices, MultiBufferSource bufferSource, int light, int overlay, LivingEntityRenderer<?, ?> entityRenderer, PartFilterScheme filter, boolean translucent, boolean glowing) {
        if (renderer == null || !loaded)
            return;

        renderer.vanillaModelData.update(entityRenderer);
        renderer.currentFilterScheme = filter;
        renderer.entity = entity;
        renderer.yaw = yaw;
        renderer.tickDelta = delta;
        renderer.alpha = alpha;
        renderer.matrices = matrices;
        renderer.bufferSource = bufferSource;
        renderer.light = light;
        renderer.overlay = overlay;
        renderer.translucent = translucent;
        renderer.glowing = glowing;

        render();
    }

    public synchronized void worldRender(Entity entity, double camX, double camY, double camZ, PoseStack matrices, MultiBufferSource bufferSource, int lightFallback, float tickDelta) {
        if (renderer == null || !loaded)
            return;

        renderer.pivotCustomizations.values().clear();
        renderer.allowMatrixUpdate = true;
        renderer.updateLight = renderMode != EntityRenderMode.OTHER;
        renderer.entity = entity;
        renderer.currentFilterScheme = PartFilterScheme.WORLD;
        renderer.bufferSource = bufferSource;
        renderer.matrices = matrices;
        renderer.tickDelta = tickDelta;
        renderer.light = lightFallback;
        renderer.alpha = 1f;
        renderer.overlay = OverlayTexture.NO_OVERLAY;
        renderer.translucent = false;
        renderer.glowing = false;

        matrices.pushPose();
        matrices.translate(-camX, -camY, -camZ);
        matrices.scale(-1, -1, 1);
        complexity.use(renderer.renderSpecialParts());
        matrices.popPose();

        renderer.updateLight = false;
    }

    public void capeRender(Entity entity, MultiBufferSource bufferSource, PoseStack stack, int light, float tickDelta, ModelPart cloak) {
        if (renderer == null || !loaded)
            return;

        FiguraMod.pushProfiler(FiguraMod.MOD_ID);
        FiguraMod.pushProfiler(this);
        FiguraMod.pushProfiler("capeRender");

        renderer.vanillaModelData.update(ParentType.Cape, cloak);
        renderer.entity = entity;
        renderer.currentFilterScheme = PartFilterScheme.CAPE;
        renderer.bufferSource = bufferSource;
        renderer.matrices = stack;
        renderer.tickDelta = tickDelta;
        renderer.light = light;
        renderer.alpha = 1f;
        renderer.overlay = OverlayTexture.NO_OVERLAY;

        render();

        FiguraMod.popProfiler(3);
    }

    public void elytraRender(Entity entity, MultiBufferSource bufferSource, PoseStack stack, int light, float tickDelta, EntityModel<?> model) {
        if (renderer == null || !loaded)
            return;

        FiguraMod.pushProfiler(FiguraMod.MOD_ID);
        FiguraMod.pushProfiler(this);
        FiguraMod.pushProfiler("elytraRender");

        renderer.entity = entity;
        renderer.bufferSource = bufferSource;
        renderer.matrices = stack;
        renderer.tickDelta = tickDelta;
        renderer.light = light;
        renderer.alpha = 1f;
        renderer.overlay = OverlayTexture.NO_OVERLAY;

        //left
        FiguraMod.pushProfiler("leftWing");
        renderer.vanillaModelData.update(ParentType.LeftElytra, model);
        renderer.currentFilterScheme = PartFilterScheme.LEFT_ELYTRA;
        render();

        //right
        FiguraMod.popPushProfiler("rightWing");
        renderer.vanillaModelData.update(ParentType.RightElytra, model);
        renderer.currentFilterScheme = PartFilterScheme.RIGHT_ELYTRA;
        render();

        FiguraMod.popProfiler(4);
    }

    public void firstPersonWorldRender(Entity watcher, MultiBufferSource bufferSource, PoseStack matrices, Camera camera, float tickDelta) {
        if (renderer == null || !loaded)
            return;

        FiguraMod.pushProfiler(FiguraMod.MOD_ID);
        FiguraMod.pushProfiler(this);
        FiguraMod.pushProfiler("firstPersonWorldRender");

        int light = Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(watcher, tickDelta);
        Vec3 camPos = camera.getPosition();

        EntityRenderMode oldMode = renderMode;
        renderMode = EntityRenderMode.FIRST_PERSON_WORLD;

        worldRender(watcher, camPos.x, camPos.y, camPos.z, matrices, bufferSource, light, tickDelta);

        renderMode = oldMode;

        FiguraMod.popProfiler(3);
    }

    public void firstPersonRender(PoseStack stack, MultiBufferSource bufferSource, Player player, PlayerRenderer playerRenderer, ModelPart arm, int light, int overlay, float tickDelta) {
        if (renderer == null || !loaded)
            return;

        FiguraMod.pushProfiler(FiguraMod.MOD_ID);
        FiguraMod.pushProfiler(this);
        FiguraMod.pushProfiler("firstPersonRender");

        PartFilterScheme filter = arm == playerRenderer.getModel().leftArm ? PartFilterScheme.LEFT_ARM : PartFilterScheme.RIGHT_ARM;
        boolean config = Config.ALLOW_FP_HANDS.asBool();
        renderer.allowHiddenTransforms = config;

        stack.pushPose();
        if (!config) {
            stack.mulPose(Vector3f.ZP.rotation(arm.zRot));
            stack.mulPose(Vector3f.YP.rotation(arm.yRot));
            stack.mulPose(Vector3f.XP.rotation(arm.xRot));
        }
        render(player, 0f, tickDelta, 1f, stack, bufferSource, light, overlay, playerRenderer, filter, false, false);
        stack.popPose();

        renderer.allowHiddenTransforms = true;

        FiguraMod.popProfiler(3);
    }

    public void hudRender(PoseStack stack, MultiBufferSource bufferSource, Entity entity, float tickDelta) {
        if (renderer == null || !loaded)
            return;

        FiguraMod.pushProfiler(this);
        FiguraMod.pushProfiler("hudRender");

        renderer.currentFilterScheme = PartFilterScheme.HUD;
        renderer.entity = entity;
        renderer.tickDelta = tickDelta;
        renderer.overlay = OverlayTexture.NO_OVERLAY;
        renderer.light = LightTexture.FULL_BRIGHT;
        renderer.alpha = 1f;
        renderer.matrices = stack;
        renderer.bufferSource = bufferSource;
        renderer.translucent = false;
        renderer.glowing = false;

        Lighting.setupForFlatItems();

        stack.pushPose();
        stack.scale(16, 16, -16);
        RenderSystem.disableDepthTest();
        complexity.use(renderer.renderSpecialParts());
        ((MultiBufferSource.BufferSource) renderer.bufferSource).endBatch();
        RenderSystem.enableDepthTest();
        stack.popPose();

        Lighting.setupFor3DItems();

        FiguraMod.popProfiler(2);
    }

    public boolean skullRender(PoseStack stack, MultiBufferSource bufferSource, int light, Direction direction, float yaw) {
        if (renderer == null || !loaded || !renderer.allowSkullRendering)
            return false;

        renderer.allowPivotParts = false;
        renderer.allowRenderTasks = false;
        renderer.currentFilterScheme = PartFilterScheme.SKULL;
        renderer.tickDelta = 1f;
        renderer.overlay = OverlayTexture.NO_OVERLAY;
        renderer.light = light;
        renderer.alpha = 1f;
        renderer.matrices = stack;
        renderer.bufferSource = bufferSource;
        renderer.translucent = false;
        renderer.glowing = false;

        stack.pushPose();

        if (direction == null)
            stack.translate(0.5d, 0d, 0.5d);
        else
            stack.translate((0.5d - direction.getStepX() * 0.25d), 0.25d, (0.5d - direction.getStepZ() * 0.25d));

        stack.scale(-1f, -1f, 1f);
        stack.mulPose(Vector3f.YP.rotationDegrees(yaw));

        int comp = renderer.renderSpecialParts();
        complexity.use(comp);

        if (comp > 0) {
            renderer.allowPivotParts = true;
            renderer.allowRenderTasks = true;
            stack.popPose();
            return true;
        }

        //head
        boolean bool = headRender(stack, bufferSource, light);

        stack.popPose();
        return bool;
    }

    public boolean headRender(PoseStack stack, MultiBufferSource bufferSource, int light) {
        if (renderer == null || !loaded)
            return false;

        stack.pushPose();
        stack.translate(0d, 24d / 16d, 0d);
        boolean oldMat = renderer.allowMatrixUpdate;

        //pre render
        renderer.allowPivotParts = false;
        renderer.allowRenderTasks = false;
        renderer.currentFilterScheme = PartFilterScheme.HEAD;
        renderer.tickDelta = 1f;
        renderer.overlay = OverlayTexture.NO_OVERLAY;
        renderer.light = light;
        renderer.alpha = 1f;
        renderer.matrices = stack;
        renderer.bufferSource = bufferSource;
        renderer.translucent = false;
        renderer.glowing = false;
        renderer.allowHiddenTransforms = false;
        renderer.allowMatrixUpdate = false;

        //render
        int comp = renderer.renderSpecialParts();

        //pos render
        renderer.allowMatrixUpdate = oldMat;
        renderer.allowHiddenTransforms = true;
        renderer.allowRenderTasks = true;
        renderer.allowPivotParts = true;

        stack.popPose();
        return comp > 0 && luaRuntime != null && !luaRuntime.vanilla_model.HEAD.checkVisible();
    }

    public boolean renderPortrait(PoseStack stack, int x, int y, int size, float modelScale) {
        if (!Config.AVATAR_PORTRAITS.asBool() || renderer == null || !loaded)
            return false;

        //matrices
        stack.pushPose();
        stack.translate(x, y, 0d);
        stack.scale(modelScale, -modelScale, modelScale);
        stack.mulPose(Vector3f.XP.rotationDegrees(180f));

        //scissors
        FiguraVec3 pos = FiguraMat4.fromMatrix4f(stack.last().pose()).apply(0d, 0d, 0d);

        int x1 = (int) pos.x;
        int y1 = (int) pos.y;
        int x2 = (int) pos.x + size;
        int y2 = (int) pos.y + size;

        UIHelper.setupScissor(x1, y1, x2 - x1, y2 - y1);
        UIHelper.paperdoll = true;
        UIHelper.dollScale = 16f;

        //setup render
        Lighting.setupForFlatItems();

        renderer.allowPivotParts = false;
        renderer.allowRenderTasks = false;
        renderer.currentFilterScheme = PartFilterScheme.PORTRAIT;
        renderer.tickDelta = 1f;
        renderer.overlay = OverlayTexture.NO_OVERLAY;
        int light = renderer.light = LightTexture.FULL_BRIGHT;
        renderer.alpha = 1f;
        renderer.matrices = stack;
        MultiBufferSource buffer = renderer.bufferSource = getBufferSource();
        renderer.translucent = false;
        renderer.glowing = false;

        stack.translate(4d / 16d, 8d / 16d, 0d);

        //render
        int comp = renderer.renderSpecialParts();
        complexity.use(comp);
        boolean ret = comp > 0 || headRender(stack, buffer, light);

        //return
        UIHelper.disableScissor();
        UIHelper.paperdoll = false;
        renderer.allowPivotParts = true;
        renderer.allowRenderTasks = true;
        stack.popPose();
        return ret;
    }

    private static final PartCustomization PIVOT_PART_RENDERING_CUSTOMIZATION = PartCustomization.of();
    public synchronized boolean pivotPartRender(ParentType parent, Consumer<PoseStack> consumer) {
        if (renderer == null || !loaded || !parent.isPivot)
            return false;

        Queue<Pair<FiguraMat4, FiguraMat3>> queue = renderer.pivotCustomizations.computeIfAbsent(parent, p -> new ConcurrentLinkedQueue<>());

        if (queue.isEmpty())
            return false;

        int i = 0;
        while (!queue.isEmpty() && i++ < 1000) { // limit of 1000 pivot part renders, just in case something goes infinitely somehow
            Pair<FiguraMat4, FiguraMat3> matrixPair = queue.poll();
            PIVOT_PART_RENDERING_CUSTOMIZATION.setPositionMatrix(matrixPair.getFirst());
            PIVOT_PART_RENDERING_CUSTOMIZATION.setNormalMatrix(matrixPair.getSecond());
            PIVOT_PART_RENDERING_CUSTOMIZATION.needsMatrixRecalculation = false;
            PoseStack stack = PIVOT_PART_RENDERING_CUSTOMIZATION.copyIntoGlobalPoseStack();
            consumer.accept(stack);
        }

        queue.clear();
        return true;
    }

    public void updateMatrices(LivingEntityRenderer<?, ?> entityRenderer, PoseStack stack) {
        if (renderer == null || !loaded)
            return;

        FiguraMod.pushProfiler(FiguraMod.MOD_ID);
        FiguraMod.pushProfiler(this);
        FiguraMod.pushProfiler("updateMatrices");

        renderer.currentFilterScheme = PartFilterScheme.MODEL;
        renderer.matrices = stack;
        renderer.vanillaModelData.update(entityRenderer);
        renderer.updateMatrices();

        FiguraMod.popProfiler(3);
    }

    // -- animations -- //

    public void applyAnimations() {
        if (!loaded)
            return;

        int animationsLimit = permissions.get(Permissions.BB_ANIMATIONS);
        int limit = animationsLimit;
        for (Animation animation : animations.values())
            limit = AnimationPlayer.tick(animation, limit);
        animationComplexity = animationsLimit - limit;

        if (limit <= 0) {
            noPermissions.add(Permissions.BB_ANIMATIONS);
        } else {
            noPermissions.remove(Permissions.BB_ANIMATIONS);
        }
    }

    public void clearAnimations() {
        if (!loaded)
            return;

        for (Animation animation : animations.values())
            AnimationPlayer.clear(animation);
    }

    // -- functions -- //

    /**
     * We should call this whenever an avatar is no longer reachable!
     * It free()s all the CachedType used inside of the avatar, and also
     * closes the native texture resources.
     * also closes and stops this avatar sounds
     */
    public void clean() {
        if (renderer != null)
            renderer.invalidate();

        SoundAPI.getSoundEngine().figura$stopSound(owner, null);
        for (SoundBuffer value : customSounds.values())
            value.releaseAlBuffer();

        ParticleAPI.getParticleEngine().figura$clearParticles(owner);

        events.clear();
    }

    public MultiBufferSource getBufferSource() {
        return renderer != null && renderer.bufferSource != null ? renderer.bufferSource : Minecraft.getInstance().renderBuffers().bufferSource();
    }

    private int getFileSize() {
        try {
            //get size
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            NbtIo.writeCompressed(nbt, baos);
            return baos.size();
        } catch (Exception e) {
            FiguraMod.LOGGER.warn("Failed to generate file size for model " + this.name, e);
            return 0;
        }
    }

    private int getVersionStatus() {
        if (version == null || (NetworkStuff.latestVersion != null && version.compareTo(NetworkStuff.latestVersion) > 0))
            return 0;
        return version.compareTo(FiguraMod.VERSION);
    }

    // -- loading -- //

    private void createLuaRuntime() {
        if (!nbt.contains("scripts"))
            return;

        Map<String, String> scripts = new HashMap<>();
        CompoundTag scriptsNbt = nbt.getCompound("scripts");
        for (String s : scriptsNbt.getAllKeys())
            scripts.put(s, new String(scriptsNbt.getByteArray(s), StandardCharsets.UTF_8));

        CompoundTag metadata = nbt.getCompound("metadata");

        ListTag autoScripts;
        if (metadata.contains("autoScripts"))
            autoScripts = metadata.getList("autoScripts", Tag.TAG_STRING);
        else
            autoScripts = null;

        FiguraLuaRuntime runtime = new FiguraLuaRuntime(this, scripts);
        if (renderer != null && renderer.root != null)
            runtime.setGlobal("models", renderer.root);

        init.reset(permissions.get(Permissions.INIT_INST));
        runtime.setInstructionLimit(init.remaining);

        events.offer(() -> {
            if (runtime.init(autoScripts))
                init.use(runtime.getInstructions());
            return null;
        });
    }

    private void loadAnimations() {
        if (!nbt.contains("animations"))
            return;

        ArrayList<String> autoAnims = new ArrayList<>();
        CompoundTag metadata = nbt.getCompound("metadata");
        if (metadata.contains("autoAnims")) {
            for (Tag name : metadata.getList("autoAnims", Tag.TAG_STRING))
                autoAnims.add(name.getAsString());
        }

        ListTag root = nbt.getList("animations", Tag.TAG_COMPOUND);
        for (int i = 0; i < root.size(); i++) {
            try {
                CompoundTag animNbt = root.getCompound(i);

                if (!animNbt.contains("mdl") || !animNbt.contains("name"))
                    continue;

                String mdl = animNbt.getString("mdl");
                String name = animNbt.getString("name");

                Animation animation = new Animation(this,
                        mdl, name,
                        animNbt.contains("loop") ? Animation.LoopMode.valueOf(animNbt.getString("loop").toUpperCase()) : Animation.LoopMode.ONCE,
                        animNbt.contains("ovr") && animNbt.getBoolean("ovr"),
                        animNbt.contains("len") ? animNbt.getFloat("len") : 0f,
                        animNbt.contains("off") ? animNbt.getFloat("off") : 0f,
                        animNbt.contains("bld") ? animNbt.getFloat("bld") : 1f,
                        animNbt.contains("sdel") ? animNbt.getFloat("sdel") : 0f,
                        animNbt.contains("ldel") ? animNbt.getFloat("ldel") : 0f
                );

                if (animNbt.contains("code")) {
                    for (Tag code : animNbt.getList("code", Tag.TAG_COMPOUND)) {
                        CompoundTag compound = (CompoundTag) code;
                        animation.newCode(compound.getFloat("time"), compound.getString("src"));
                    }
                }

                animations.put(i, animation);

                if (autoAnims.contains(mdl + "." + name))
                    animation.play();
            } catch (Exception ignored) {}
        }
    }

    private void loadCustomSounds() {
        if (!nbt.contains("sounds"))
            return;

        CompoundTag root = nbt.getCompound("sounds");
        for (String key : root.getAllKeys()) {
            try {
                loadSound(key, root.getByteArray(key));
            } catch (Exception e) {
                FiguraMod.LOGGER.warn("Failed to load custom sound \"" + key + "\"", e);
            }
        }
    }

    public void loadSound(String name, byte[] data) throws Exception {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data); OggAudioStream oggAudioStream = new OggAudioStream(inputStream)) {
            SoundBuffer sound = new SoundBuffer(oggAudioStream.readAll(), oggAudioStream.getFormat());
            this.customSounds.put(name, sound);
        }
    }

    public static class Instructions {

        public int max, remaining;
        private int currPre, currPost;
        public int pre, post;
        private boolean inverted;

        public Instructions(int remaining) {
            reset(remaining);
        }

        public Instructions post() {
            inverted = true;
            return this;
        }

        public int getTotal() {
            return pre + post;
        }

        public void reset(int remaining) {
            this.max = this.remaining = remaining;
            currPre = currPost = 0;
        }

        public void use(int amount) {
            remaining -= amount;

            if (!inverted) {
                currPre += amount;
                pre = currPre;
            } else {
                currPost += amount;
                post = currPost;
                inverted = false;
            }
        }
    }
}
