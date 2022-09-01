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
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.luaj.vm2.*;
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
    public AvatarRenderer renderer;
    public FiguraLuaRuntime luaRuntime;

    public final TrustContainer trust;

    public final Map<String, SoundBuffer> customSounds = new HashMap<>();
    public final Map<Integer, Animation> animations = new ConcurrentHashMap<>();

    private int initLimit, worldRenderLimit;

    //runtime status
    public boolean hasTexture = false;
    public boolean scriptError = false;
    public int versionStatus = 0;

    public int complexity = 0;
    public int animationComplexity;

    public int initInstructions, entityInitInstructions;
    public int entityTickInstructions, worldTickInstructions;

    public int worldRenderInstructions, entityRenderInstructions, postEntityRenderInstructions, postWorldRenderInstructions;
    public int accumulatedInitInstructions, accumulatedTickInstructions, accumulatedEntityRenderInstructions, accumulatedWorldRenderInstructions;

    public final RefilledNumber particlesRemaining, soundsRemaining;

    public Avatar(UUID owner) {
        this.owner = owner;
        this.isHost = FiguraMod.isLocal(owner);
        this.trust = TrustManager.get(owner);
        this.particlesRemaining = new RefilledNumber(trust.get(TrustContainer.Trust.PARTICLES));
        this.soundsRemaining = new RefilledNumber(trust.get(TrustContainer.Trust.SOUNDS));
        entityName = EntityUtils.getNameForUUID(owner);
    }

    private static CompletableFuture<Void> run(Runnable toRun) {
        if (tasks == null || tasks.isDone()) {
            tasks = CompletableFuture.runAsync(toRun);
        } else {
            tasks.thenRun(toRun);
        }
        return tasks;
    }

    public void load(CompoundTag nbt) {
        CompletableFuture<Void> future = run(() -> {
            this.nbt = nbt;
            loaded = false;
        });

        future.join();

        if (nbt == null) {
            loaded = true;
            return;
        }

        future.thenRun(() -> { //metadata
            try {
                CompoundTag metadata = nbt.getCompound("metadata");
                name = metadata.getString("name");
                authors = metadata.getString("authors");
                version = metadata.getString("ver");
                if (metadata.contains("color"))
                    color = metadata.getString("color");
                fileSize = getFileSize();
                versionStatus = checkVersion();
                if (entityName == null)
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
        if (luaRuntime != null && luaRuntime.user == null) {
            Entity entity = EntityUtils.getEntityByUUID(owner);
            if (entity != null) {
                luaRuntime.setUser(entity);
                entityInitEvent();
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
        worldTickEvent();
        tickEvent();
    }

    public void runPing(int id, byte[] data) {
        if (scriptError || luaRuntime == null)
            return;

        Varargs args = PingArg.fromByteArray(data, this);
        String name = luaRuntime.ping.getName(id);
        PingFunction function = luaRuntime.ping.get(name);
        if (args == null || function == null)
            return;

        FiguraLuaPrinter.sendPingMessage(this, name, data.length, args);
        function.func.invoke(args);
    }

    // -- script events -- //

    //Calling with maxInstructions as -1 will not set the max instructions, and instead keep them as they are.
    //returns whatever if it succeeded or not calling the function
    public void tryCall(Object toRun, int maxInstructions, Object... args) {
        if (scriptError || luaRuntime == null || UIHelper.paperdoll)
            return;

        try {
            Varargs val = switch (args.length) {
                case 0 -> LuaValue.NONE;
                case 1 -> luaRuntime.typeManager.javaToLua(args[0]);
                case 2 -> LuaValue.varargsOf(
                        luaRuntime.typeManager.javaToLua(args[0]),
                        luaRuntime.typeManager.javaToLua(args[1])
                );
                default -> {
                    LuaValue[] values = new LuaValue[args.length];
                    for (int i = 0; i < values.length; i++)
                        values[i] = luaRuntime.typeManager.javaToLua(args[i]);
                    yield LuaValue.varargsOf(values);
                }
            };

            if (maxInstructions != -1)
                luaRuntime.setInstructionLimit(maxInstructions);
            if (toRun instanceof LuaEvent event)
                event.call(val);
            else if (toRun instanceof LuaFunction func)
                func.invoke(val);
            else
                throw new LuaError("Invalid type to run!");
        } catch (LuaError ex) {
            FiguraLuaPrinter.sendLuaError(ex, entityName, owner);
            scriptError = true;
            luaRuntime = null;
        }
    }

    public void entityInitEvent() {
        if (scriptError || luaRuntime == null)
            return;

        tryCall(luaRuntime.events.ENTITY_INIT, Math.max(initLimit - initInstructions, 1));
        if (luaRuntime != null) {
            entityInitInstructions = luaRuntime.getInstructions();
            accumulatedInitInstructions += entityInitInstructions;
        }
    }

    public void tickEvent() {
        if (scriptError || luaRuntime == null || luaRuntime.user == null)
            return;

        int entityTickLimit = trust.get(TrustContainer.Trust.TICK_INST);
        tryCall(luaRuntime.events.TICK, entityTickLimit);
        if (luaRuntime != null) {
            entityTickInstructions = luaRuntime.getInstructions();
            accumulatedTickInstructions += entityTickInstructions;
        }
    }

    public void worldTickEvent() {
        if (scriptError || luaRuntime == null)
            return;

        int worldTickLimit = trust.get(TrustContainer.Trust.WORLD_TICK_INST);
        tryCall(luaRuntime.events.WORLD_TICK, worldTickLimit);
        if (luaRuntime != null) {
            worldTickInstructions = luaRuntime.getInstructions();
            accumulatedTickInstructions = worldTickInstructions;
        }
    }

    public void renderEvent(float delta) {
        if (scriptError || luaRuntime == null || luaRuntime.user == null)
            return;

        int entityRenderLimit = trust.get(TrustContainer.Trust.RENDER_INST);
        tryCall(luaRuntime.events.RENDER, entityRenderLimit, LuaDouble.valueOf(delta));
        if (luaRuntime != null) {
            entityRenderInstructions = luaRuntime.getInstructions();
            accumulatedEntityRenderInstructions = entityRenderInstructions;
        }
    }

    public void postRenderEvent(float delta) {
        if (scriptError || luaRuntime == null)
            return;

        tryCall(luaRuntime.events.POST_RENDER, -1, LuaDouble.valueOf(delta));
        if (luaRuntime != null) {
            postEntityRenderInstructions = luaRuntime.getInstructions() - entityRenderInstructions;
            accumulatedEntityRenderInstructions += postEntityRenderInstructions;
        }
    }

    public void worldRenderEvent(float delta) {
        if (scriptError || luaRuntime == null)
            return;

        worldRenderLimit = trust.get(TrustContainer.Trust.WORLD_RENDER_INST);
        tryCall(luaRuntime.events.WORLD_RENDER, worldRenderLimit, LuaDouble.valueOf(delta));
        if (luaRuntime != null) {
            worldRenderInstructions = luaRuntime.getInstructions();
            accumulatedWorldRenderInstructions = worldRenderInstructions;
        }
    }

    public void postWorldRenderEvent(float delta) {
        if (renderer != null)
            renderer.allowMatrixUpdate = false;

        if (scriptError || luaRuntime == null || worldRenderLimit == 0)
            return;

        tryCall(luaRuntime.events.POST_WORLD_RENDER, Math.max(worldRenderLimit - worldRenderInstructions, 1), LuaDouble.valueOf(delta));
        if (luaRuntime != null) {
            postWorldRenderInstructions = luaRuntime.getInstructions();
            accumulatedWorldRenderInstructions += postWorldRenderInstructions;
        }
    }

    public String chatSendMessageEvent(String message) {
        if (!scriptError && luaRuntime != null) {
            try {
                Varargs result = luaRuntime.events.CHAT_SEND_MESSAGE.pipedCall(LuaValue.valueOf(message));
                LuaValue value = result.arg(1);
                return value.isnil() ? null : Config.CHAT_MESSAGES.asBool() ? value.tojstring() : message;
            } catch (LuaError ex) {
                FiguraLuaPrinter.sendLuaError(ex, entityName, owner);
                scriptError = true;
                luaRuntime = null;
            }
        }
        return message;
    }

    public void chatReceivedMessageEvent(String message) {
        if (!scriptError && luaRuntime != null)
            tryCall(luaRuntime.events.CHAT_RECEIVE_MESSAGE, -1, LuaValue.valueOf(message));
    }

    public void skullRenderEvent(SkullBlockEntity skullBlockEntity, float delta) {
        if (scriptError || luaRuntime == null || renderer == null || !renderer.allowSkullRendering)
            return;
        tryCall(luaRuntime.events.SKULL_RENDER, -1, delta, FiguraVec3.fromBlockPos(skullBlockEntity.getBlockPos()));
        if (luaRuntime != null) {
            postEntityRenderInstructions = luaRuntime.getInstructions();
            accumulatedEntityRenderInstructions = postEntityRenderInstructions + entityRenderInstructions;
        }
    }

    public void mouseScrollEvent(double delta) {
        if (!scriptError && luaRuntime != null)
            tryCall(luaRuntime.events.MOUSE_SCROLL, -1, LuaValue.valueOf(delta));
    }

    public void previewRenderEvent() {
        if (!scriptError && luaRuntime != null)
            tryCall(luaRuntime.events.PREVIEW_RENDER, trust.get(TrustContainer.Trust.RENDER_INST));
    }

    public void postPreviewRenderEvent() {
        if (!scriptError && luaRuntime != null)
            tryCall(luaRuntime.events.POST_PREVIEW_RENDER, -1);
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

        renderer.render();
    }

    public synchronized void worldRender(Entity entity, double camX, double camY, double camZ, PoseStack matrices, MultiBufferSource bufferSource, int light, float tickDelta) {
        if (renderer == null)
            return;

        complexity = 0;

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
        renderer.renderSpecialParts();
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

        int oldComplexity = complexity;

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
        complexity = oldComplexity;
    }

    public void hudRender(PoseStack stack, MultiBufferSource bufferSource, Entity entity, float tickDelta) {
        if (renderer == null)
            return;

        //renderer.allowMatrixUpdate = true;
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
        renderer.renderSpecialParts();
        ((MultiBufferSource.BufferSource) renderer.bufferSource).endBatch();
        RenderSystem.enableDepthTest();
        stack.popPose();

        Lighting.setupFor3DItems();

        //renderer.allowMatrixUpdate = false;
    }

    public boolean skullRender(PoseStack stack, MultiBufferSource bufferSource, int light, Direction direction, float yaw) {
        if (renderer == null || !renderer.allowSkullRendering)
            return false;

        int oldComplexity = complexity;

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

        renderer.renderSpecialParts();

        //hacky
        if (complexity > oldComplexity) {
            renderer.allowPivotParts = true;
            renderer.allowRenderTasks = true;
            stack.popPose();
            return true;
        }

        //otherwise render head parts
        stack.translate(0d, 24d / 16d, 0d);
        boolean oldMat = renderer.allowMatrixUpdate;
        renderer.allowMatrixUpdate = false;
        renderer.allowHiddenTransforms = false;
        renderer.allowPivotParts = false;
        renderer.allowRenderTasks = false;
        renderer.currentFilterScheme = PartFilterScheme.HEAD;
        renderer.renderSpecialParts();

        renderer.allowMatrixUpdate = oldMat;
        renderer.allowHiddenTransforms = true;
        renderer.allowRenderTasks = true;
        renderer.allowPivotParts = true;

        //hacky 2
        stack.popPose();
        return complexity > oldComplexity && luaRuntime != null && !luaRuntime.vanilla_model.HEAD.getVisible();
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

    public void pauseAnimations() {
        for (Animation animation : animations.values())
            animation.gamePause();
    }

    public void resumeAnimations() {
        for (Animation animation : animations.values())
            animation.gameResume();
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
        ListTag autoScripts = null;

        if (metadata.contains("autoScripts"))
            autoScripts = metadata.getList("autoScripts", Tag.TAG_STRING);

        FiguraLuaRuntime luaRuntime = new FiguraLuaRuntime(this);
        if (renderer != null && renderer.root != null)
            luaRuntime.setGlobal("models", renderer.root);

        initLimit = trust.get(TrustContainer.Trust.INIT_INST);
        luaRuntime.setInstructionLimit(initLimit);
        this.luaRuntime = luaRuntime;

        boolean error = !luaRuntime.init(scripts, autoScripts);
        if (error) {
            this.luaRuntime = null;
        } else {
            initInstructions = luaRuntime.getInstructions();
            accumulatedInitInstructions = initInstructions;
        }
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
}
