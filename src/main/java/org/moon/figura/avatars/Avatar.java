package org.moon.figura.avatars;

import com.mojang.blaze3d.audio.OggAudioStream;
import com.mojang.blaze3d.audio.SoundBuffer;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.moon.figura.FiguraMod;
import org.moon.figura.animation.Animation;
import org.moon.figura.animation.AnimationPlayer;
import org.moon.figura.avatars.model.ParentType;
import org.moon.figura.avatars.model.PartCustomization;
import org.moon.figura.avatars.model.rendering.AvatarRenderer;
import org.moon.figura.avatars.model.rendering.ImmediateAvatarRenderer;
import org.moon.figura.avatars.model.rendering.PartFilterScheme;
import org.moon.figura.config.Config;
import org.moon.figura.lua.FiguraLuaPrinter;
import org.moon.figura.lua.FiguraLuaRuntime;
import org.moon.figura.lua.api.event.LuaEvent;
import org.moon.figura.lua.api.ping.PingArg;
import org.moon.figura.lua.api.ping.PingFunction;
import org.moon.figura.lua.api.sound.SoundAPI;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;
import org.moon.figura.utils.EntityUtils;
import org.moon.figura.utils.RefilledNumber;
import org.moon.figura.utils.Version;
import org.moon.figura.utils.ui.UIHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

//the avatar class
//contains all things related to the avatar
//and also related to the owner, like trust settings
public class Avatar {

    private static CompletableFuture<Void> tasks;

    //properties
    public final UUID owner;
    public CompoundTag nbt;
    public boolean loaded = true;
    public final boolean isHost;

    //metadata
    public String name, entityName;
    public String authors;
    public String version;
    public int fileSize;
    public String color;

    //Runtime data
    private final Queue<Supplier<Varargs>> events = new ConcurrentLinkedQueue<>();

    public AvatarRenderer renderer;
    public FiguraLuaRuntime luaRuntime;

    public final TrustContainer trust;

    public final Map<String, SoundBuffer> customSounds = new HashMap<>();
    public final Map<Integer, Animation> animations = new ConcurrentHashMap<>();

    //runtime status
    public boolean hasTexture = false;
    public boolean scriptError = false;
    public int versionStatus = 0;

    //limits
    public int animationComplexity;
    public final Instructions complexity;
    public final Instructions init, render, worldRender, tick, worldTick;
    public final RefilledNumber particlesRemaining, soundsRemaining;

    public Avatar(UUID owner) {
        this.owner = owner;
        this.isHost = FiguraMod.isLocal(owner);
        this.trust = TrustManager.get(owner);
        this.complexity = new Instructions(trust.get(TrustContainer.Trust.COMPLEXITY));
        this.init = new Instructions(trust.get(TrustContainer.Trust.INIT_INST));
        this.render = new Instructions(trust.get(TrustContainer.Trust.RENDER_INST));
        this.worldRender = new Instructions(trust.get(TrustContainer.Trust.WORLD_RENDER_INST));
        this.tick = new Instructions(trust.get(TrustContainer.Trust.TICK_INST));
        this.worldTick = new Instructions(trust.get(TrustContainer.Trust.WORLD_TICK_INST));
        this.particlesRemaining = new RefilledNumber(trust.get(TrustContainer.Trust.PARTICLES));
        this.soundsRemaining = new RefilledNumber(trust.get(TrustContainer.Trust.SOUNDS));

        String name = EntityUtils.getNameForUUID(owner);
        this.entityName = name == null ? "" : name;
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

        tasks.thenRun(() -> { //metadata
            try {
                CompoundTag metadata = nbt.getCompound("metadata");
                name = metadata.getString("name");
                authors = metadata.getString("authors");
                version = metadata.getString("ver");
                if (metadata.contains("color"))
                    color = metadata.getString("color");
                fileSize = getFileSize();
                versionStatus = checkVersion();
                if (entityName.isBlank())
                    entityName = name;
            } catch (Exception e) {
                FiguraMod.LOGGER.error("", e);
            }
        }).thenRun(() -> { //animations and models
            try {
                loadAnimations();
                renderer = new ImmediateAvatarRenderer(this);
            } catch (Exception e) {
                FiguraMod.LOGGER.error("", e);
            }
        }).thenRun(() -> { //sounds and script
            try {
                loadCustomSounds();
                createLuaRuntime();
            } catch (Exception e) {
                FiguraMod.LOGGER.error("", e);
            }

            loaded = true;
        });
    }

    private void checkUser() {
        if (luaRuntime != null && luaRuntime.getUser() == null) {
            Entity entity = EntityUtils.getEntityByUUID(owner);
            if (entity != null) {
                luaRuntime.setUser(entity);
                run("ENTITY_INIT", init.post());
            }
        }
    }

    public void tick() {
        if (scriptError || luaRuntime == null)
            return;

        checkUser();

        //sound
        particlesRemaining.set(trust.get(TrustContainer.Trust.PARTICLES));
        particlesRemaining.tick();

        //particles
        soundsRemaining.set(trust.get(TrustContainer.Trust.SOUNDS));
        soundsRemaining.tick();

        //call events
        worldTick.reset(trust.get(TrustContainer.Trust.WORLD_TICK_INST));
        run("WORLD_TICK", worldTick);

        tick.reset(trust.get(TrustContainer.Trust.TICK_INST));
        tickEvent();
    }

    public void render(float delta) {
        complexity.reset(trust.get(TrustContainer.Trust.COMPLEXITY));

        if (scriptError || luaRuntime == null)
            return;

        render.reset(trust.get(TrustContainer.Trust.RENDER_INST));
        worldRender.reset(trust.get(TrustContainer.Trust.WORLD_RENDER_INST));
        run("WORLD_RENDER", worldRender, delta);
    }

    public void runPing(int id, byte[] data) {
        events.offer(() -> {
            if (scriptError || luaRuntime == null)
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
            if (scriptError || luaRuntime == null)
                return null;

            //parse args
            LuaValue[] values = new LuaValue[args.length];
            for (int i = 0; i < values.length; i++)
                values[i] = luaRuntime.typeManager.javaToLua(args[i]);

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
                    ret = luaRuntime.run(pair.getFirst().toString(), pair.getSecond().toString());
                else
                    throw new IllegalArgumentException("Invalid type to run!");

                limit.use(luaRuntime.getInstructions());
                return ret;
            } catch (Exception e) {
                if (luaRuntime != null)
                    luaRuntime.error(e);
            }

            return LuaValue.NIL;
        };

        //add event to the queue
        events.offer(ev);

        Varargs val = null;

        //run all queued events
        while (!events.isEmpty()) {
            Supplier<Varargs> e = events.poll();
            Varargs result = e.get();

            //if the event is the same the one created, set the return value to it
            if (e == ev)
                val = result;
        }

        //return the new event result
        return val;
    }

    // -- script events -- //

    public void tickEvent() {
        if (luaRuntime != null && luaRuntime.getUser() != null)
            run("TICK", tick);
    }

    public void renderEvent(float delta, String context) {
        if (luaRuntime != null && luaRuntime.getUser() != null)
            run("RENDER", render, delta, context);
    }

    public void postRenderEvent(float delta, String context) {
        if (luaRuntime != null && luaRuntime.getUser() != null)
            run("POST_RENDER", render.post(), delta, context);
    }

    public void postWorldRenderEvent(float delta) {
        if (renderer != null)
            renderer.allowMatrixUpdate = false;

        run("POST_WORLD_RENDER", worldRender.post(), delta);
    }

    public void skullRenderEvent(float delta, FiguraVec3 pos) {
        if (renderer != null && renderer.allowSkullRendering)
            run("SKULL_RENDER", render, delta, pos);
    }

    // -- host only events -- //

    public String chatSendMessageEvent(String message) {
        Varargs val = run("CHAT_SEND_MESSAGE", tick, message);
        return val == null || (!val.isnil(1) && !Config.CHAT_MESSAGES.asBool()) ? message : val.isnil(1) ? null : val.arg(1).tojstring();
    }

    public void chatReceivedMessageEvent(String message) {
        run("CHAT_RECEIVE_MESSAGE", tick, message);
    }

    public void mouseScrollEvent(double delta) {
        run("MOUSE_SCROLL", tick, delta);
    }

    // -- rendering events -- //

    public void render(Entity entity, float yaw, float delta, float alpha, PoseStack matrices, MultiBufferSource bufferSource, int light, int overlay, LivingEntityRenderer<?, ?> entityRenderer, PartFilterScheme filter, boolean translucent, boolean glowing) {
        if (renderer == null)
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

        if (UIHelper.paperdoll) {
            int prev = complexity.remaining;
            complexity.remaining = trust.get(TrustContainer.Trust.COMPLEXITY);
            renderer.render();
            complexity.remaining = prev;
        } else {
            complexity.use(renderer.render());
        }
    }

    public synchronized void worldRender(Entity entity, double camX, double camY, double camZ, PoseStack matrices, MultiBufferSource bufferSource, int light, float tickDelta) {
        if (renderer == null)
            return;

        for (Queue<Pair<FiguraMat4, FiguraMat3>> queue : renderer.pivotCustomizations.values()) {
            while (!queue.isEmpty()) {
                Pair<FiguraMat4, FiguraMat3> pair = queue.poll();
                pair.getFirst().free();
                pair.getSecond().free();
            }
        }

        renderer.allowMatrixUpdate = true;
        renderer.entity = entity;
        renderer.currentFilterScheme = PartFilterScheme.WORLD;
        renderer.bufferSource = bufferSource;
        renderer.matrices = matrices;
        renderer.tickDelta = tickDelta;
        renderer.light = light;
        renderer.alpha = 1f;
        renderer.overlay = OverlayTexture.NO_OVERLAY;
        renderer.translucent = false;
        renderer.glowing = false;

        matrices.pushPose();
        matrices.translate(-camX, -camY, -camZ);
        matrices.scale(-1, -1, 1);
        complexity.use(renderer.renderSpecialParts());
        matrices.popPose();
    }

    public void firstPersonWorldRender(Entity watcher, MultiBufferSource bufferSource, PoseStack matrices, Camera camera, float tickDelta) {
        if (renderer == null)
            return;

        int light = Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(watcher, tickDelta);
        Vec3 camPos = camera.getPosition();
        worldRender(watcher, camPos.x, camPos.y, camPos.z, matrices, bufferSource, light, tickDelta);
    }

    public void firstPersonRender(PoseStack stack, MultiBufferSource bufferSource, Player player, PlayerRenderer playerRenderer, ModelPart arm, int light, int overlay, float tickDelta) {
        if (renderer == null)
            return;

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
    }

    public void hudRender(PoseStack stack, MultiBufferSource bufferSource, Entity entity, float tickDelta) {
        if (renderer == null)
            return;

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
    }

    public boolean skullRender(PoseStack stack, MultiBufferSource bufferSource, int light, Direction direction, float yaw) {
        if (renderer == null || !renderer.allowSkullRendering)
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
        if (renderer == null)
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
        return comp > 0 && luaRuntime != null && !luaRuntime.vanilla_model.HEAD.getVisible();
    }

    public boolean renderHeadOnHud(PoseStack stack, int x, int y, int screenSize, float modelScale, boolean scissors) {
        if (!Config.AVATAR_HEADS.asBool())
            return false;

        //matrices
        stack.pushPose();
        stack.translate(x, y, 0d);
        stack.scale(modelScale, -modelScale, modelScale);
        stack.mulPose(Vector3f.XP.rotationDegrees(180f));

        //scissors
        FiguraVec4 oldScissors = UIHelper.scissors.copy();
        FiguraVec3 pos = FiguraMat4.fromMatrix4f(stack.last().pose()).apply(0d, 0d, 0d);

        int x1 = (int) pos.x;
        int y1 = (int) pos.y;
        int x2 = (int) pos.x + screenSize;
        int y2 = (int) pos.y + screenSize;

        if (scissors) {
            x1 = (int) Math.round(Math.max(x1, oldScissors.x));
            y1 = (int) Math.round(Math.max(y1, oldScissors.y));
            x2 = (int) Math.round(Math.min(x2, oldScissors.x + oldScissors.z));
            y2 = (int) Math.round(Math.min(y2, oldScissors.y + oldScissors.w));
        }

        UIHelper.setupScissor(x1, y1, x2 - x1, y2 - y1);
        UIHelper.paperdoll = true;
        UIHelper.dollScale = 16f;

        //render
        Lighting.setupForFlatItems();
        stack.translate(4d / 16d, 8d / 16d, 0d);
        //boolean ret = skullRender(stack, getBufferSource(), LightTexture.FULL_BRIGHT, null, 0);
        boolean ret = headRender(stack, getBufferSource(), LightTexture.FULL_BRIGHT);

        //return
        if (scissors)
            UIHelper.setupScissor((int) oldScissors.x, (int) oldScissors.y, (int) oldScissors.z, (int) oldScissors.w);
        else
            RenderSystem.disableScissor();

        UIHelper.paperdoll = false;
        stack.popPose();
        return ret;
    }

    private static final PartCustomization PIVOT_PART_RENDERING_CUSTOMIZATION = PartCustomization.of();
    public synchronized boolean pivotPartRender(ParentType parent, Consumer<PoseStack> consumer) {
        if (renderer == null || !parent.isPivot)
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
            matrixPair.getFirst().free();
            matrixPair.getSecond().free();
        }

        queue.clear();
        return true;
    }

    // -- animations -- //

    public void applyAnimations() {
        int animationsLimit = trust.get(TrustContainer.Trust.BB_ANIMATIONS);
        int limit = animationsLimit;
        for (Animation animation : animations.values())
            limit = AnimationPlayer.tick(animation, limit);
        animationComplexity = animationsLimit - limit;
    }

    public void clearAnimations() {
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

    private int checkVersion() {
        try {
            return Version.of(version).compareTo(Version.VERSION);
        } catch (Exception ignored) {
            return 0;
        }
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

        init.reset(trust.get(TrustContainer.Trust.INIT_INST));
        runtime.setInstructionLimit(init.remaining);

        events.offer(() -> {
            if (runtime.init(autoScripts)) {
                init.use(runtime.getInstructions());
                this.luaRuntime = runtime;
            }
            return null;
        });
    }

    private void loadAnimations() {
        if (!nbt.contains("animations"))
            return;

        ListTag root = nbt.getList("animations", Tag.TAG_COMPOUND);
        for (int i = 0; i < root.size(); i++) {
            try {
                CompoundTag animNbt = root.getCompound(i);

                if (!animNbt.contains("mdl") || !animNbt.contains("name"))
                    continue;

                Animation animation = new Animation(this,
                        animNbt.getString("mdl"), animNbt.getString("name"),
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
                        animation.addCode(compound.getFloat("time"), compound.getString("src"));
                    }
                }

                animations.put(i, animation);
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
