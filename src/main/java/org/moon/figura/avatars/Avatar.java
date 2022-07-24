package org.moon.figura.avatars;

import com.mojang.blaze3d.audio.OggAudioStream;
import com.mojang.blaze3d.audio.SoundBuffer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.moon.figura.FiguraMod;
import org.moon.figura.animation.Animation;
import org.moon.figura.animation.AnimationPlayer;
import org.moon.figura.avatars.model.rendering.AvatarRenderer;
import org.moon.figura.avatars.model.rendering.ImmediateAvatarRenderer;
import org.moon.figura.avatars.model.rendering.PartFilterScheme;
import org.moon.figura.avatars.model.rendering.StackAvatarRenderer;
import org.moon.figura.lua.FiguraLuaPrinter;
import org.moon.figura.lua.FiguraLuaState;
import org.moon.figura.lua.api.EventsAPI;
import org.moon.figura.lua.api.SoundAPI;
import org.moon.figura.lua.api.entity.EntityWrapper;
import org.moon.figura.lua.api.nameplate.NameplateCustomization;
import org.moon.figura.lua.types.LuaFunction;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;
import org.moon.figura.utils.RefilledNumber;
import org.terasology.jnlua.LuaRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;

//the avatar class
//contains all things related to the avatar
//and also related to the owner, like trust settings
public class Avatar {

    private static CompletableFuture<Void> tasks;

    //properties
    public final UUID owner;
    public CompoundTag nbt;
    public boolean loaded = true;

    //metadata
    public String name;
    public String authors;
    public String version;
    public int fileSize;
    public String color;

    public BitSet badges = new BitSet(NameplateCustomization.badgesLen());

    //Runtime data
    public AvatarRenderer renderer;
    public FiguraLuaState luaState;

    public final Map<String, SoundBuffer> customSounds = new HashMap<>();
    public final Map<Integer, Animation> animations = Collections.synchronizedMap(new HashMap<>());

    private int entityTickLimit, entityRenderLimit;
    private int worldTickLimit, worldRenderLimit;

    //runtime status
    public boolean hasTexture = false;
    public boolean scriptError = false;

    public int complexity = 0;
    public int remainingComplexity, animationComplexity;

    public int initInstructions;
    public int entityTickInstructions, worldTickInstructions;

    public int worldRenderInstructions, entityRenderInstructions, postEntityRenderInstructions, postWorldRenderInstructions;
    public int accumulatedTickInstructions, accumulatedEntityRenderInstructions, accumulatedWorldRenderInstructions;

    public final RefilledNumber particlesRemaining, soundsRemaining;

    public Avatar(UUID owner) {
        this.owner = owner;
        this.particlesRemaining = new RefilledNumber(TrustManager.get(this.owner).get(TrustContainer.Trust.PARTICLES));
        this.soundsRemaining = new RefilledNumber(TrustManager.get(this.owner).get(TrustContainer.Trust.SOUNDS));
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
                color = metadata.getString("color");
                fileSize = getFileSize();
            } catch (Exception e) {
                FiguraMod.LOGGER.error("", e);
            }
        }).thenRun(() -> { //animations and models
            try {
                loadAnimations();
                renderer = new StackAvatarRenderer(this);
            } catch (Exception e) {
                FiguraMod.LOGGER.error("", e);
            }
        }).thenRun(() -> { //sounds and script
            try {
                loadCustomSounds();
                createLuaState();
            } catch (Exception e) {
                FiguraMod.LOGGER.error("", e);
            }

            loaded = true;
        });
    }

    public void tick() {
        if (scriptError || luaState == null)
            return;

        //trust
        TrustContainer container = TrustManager.get(owner);
        entityTickLimit = container.get(TrustContainer.Trust.TICK_INST);
        worldTickLimit = container.get(TrustContainer.Trust.WORLD_TICK_INST);
        entityRenderLimit = container.get(TrustContainer.Trust.RENDER_INST);
        worldRenderLimit = container.get(TrustContainer.Trust.WORLD_RENDER_INST);

        //sound
        particlesRemaining.set(container.get(TrustContainer.Trust.PARTICLES));
        particlesRemaining.tick();

        //particles
        soundsRemaining.set(container.get(TrustContainer.Trust.SOUNDS));
        soundsRemaining.tick();

        //call events
        worldTickEvent();
        tickEvent();
    }

    // -- script events -- //

    //Calling with maxInstructions as -1 will not set the max instructions, and instead keep them as they are.
    //returns whatever if it succeeded or not calling the function
    public void tryCall(Object toRun, int maxInstructions, Object... args) {
        if (scriptError || luaState == null)
            return;

        try {
            if (maxInstructions != -1)
                luaState.setInstructionLimit(maxInstructions);
            if (toRun instanceof EventsAPI.LuaEvent event)
                event.call(args);
            else if (toRun instanceof LuaFunction func)
                func.call(args);
            else
                throw new LuaRuntimeException("Invalid type to run!");
        } catch (Exception ex) {
            FiguraLuaPrinter.sendLuaError(ex, name, owner);
            scriptError = true;
            luaState.close();
            luaState = null;
        }
    }

    public void tickEvent() {
        if (scriptError || luaState == null || !EntityWrapper.exists(luaState.entity))
            return;

        tryCall(luaState.events.TICK, entityTickLimit);
        if (FiguraMod.DO_OUR_NATIVES_WORK && luaState != null) {
            entityTickInstructions = entityTickLimit - luaState.getInstructions();
            accumulatedTickInstructions += entityTickInstructions;
        }
    }

    public void worldTickEvent() {
        if (scriptError || luaState == null)
            return;

        tryCall(luaState.events.WORLD_TICK, worldTickLimit);
        if (FiguraMod.DO_OUR_NATIVES_WORK && luaState != null) {
            worldTickInstructions = worldTickLimit - luaState.getInstructions();
            accumulatedTickInstructions = worldTickInstructions;
        }
    }

    public void renderEvent(float delta) {
        if (scriptError || luaState == null)
            return;

        tryCall(luaState.events.RENDER, entityRenderLimit, delta);
        if (FiguraMod.DO_OUR_NATIVES_WORK && luaState != null) {
            entityRenderInstructions = entityRenderLimit - luaState.getInstructions();
            accumulatedEntityRenderInstructions = entityRenderInstructions;
        }
    }

    public void postRenderEvent(float delta) {
        if (scriptError || luaState == null)
            return;

        tryCall(luaState.events.POST_RENDER, -1, delta);
        if (FiguraMod.DO_OUR_NATIVES_WORK && luaState != null) {
            postEntityRenderInstructions = entityRenderLimit - accumulatedEntityRenderInstructions - luaState.getInstructions();
            accumulatedEntityRenderInstructions += postEntityRenderInstructions;
        }
    }

    public void worldRenderEvent(float delta) {
        if (scriptError || luaState == null)
            return;

        tryCall(luaState.events.WORLD_RENDER, worldRenderLimit, delta);
        if (FiguraMod.DO_OUR_NATIVES_WORK && luaState != null) {
            worldRenderInstructions = worldRenderLimit - luaState.getInstructions();
            accumulatedWorldRenderInstructions = worldRenderInstructions;
        }
    }

    public void postWorldRenderEvent(float delta) {
        if (scriptError || luaState == null)
            return;

        tryCall(luaState.events.POST_WORLD_RENDER, -1, delta);
        if (FiguraMod.DO_OUR_NATIVES_WORK && luaState != null) {
            postWorldRenderInstructions = worldRenderLimit - accumulatedEntityRenderInstructions - luaState.getInstructions();
            accumulatedWorldRenderInstructions += postWorldRenderInstructions;
        }
        renderer.allowMatrixUpdate = false;
    }

    public String chatSendMessageEvent(String message) {
        if (!scriptError && luaState != null)
            tryCall(luaState.events.CHAT_SEND_MESSAGE, -1, message);
        return message;
    }

    public void chatReceivedMessageEvent(String message) {
        if (!scriptError && luaState != null)
            tryCall(luaState.events.CHAT_RECEIVE_MESSAGE, -1, message);
    }

    // -- rendering events -- //

    public void render(Entity entity, float yaw, float delta, float alpha, PoseStack matrices, MultiBufferSource bufferSource, int light, int overlay, LivingEntityRenderer<?, ?> entityRenderer, PartFilterScheme filter) {
        if (renderer == null)
            return;

        renderer.currentFilterScheme = filter;
        renderer.entity = entity;
        renderer.yaw = yaw;
        renderer.tickDelta = delta;
        renderer.alpha = alpha;
        renderer.matrices = matrices;
        renderer.bufferSource = bufferSource;
        renderer.light = light;
        renderer.overlay = overlay;
        renderer.entityRenderer = entityRenderer;

        renderer.render();
    }

    public void worldRender(Entity entity, double camX, double camY, double camZ, PoseStack matrices, MultiBufferSource bufferSource, int light, float tickDelta) {
        if (renderer == null)
            return;

        complexity = 0;
        remainingComplexity = TrustManager.get(owner).get(TrustContainer.Trust.COMPLEXITY);

        renderer.allowMatrixUpdate = true;
        renderer.entity = entity;
        renderer.currentFilterScheme = PartFilterScheme.WORLD;
        renderer.bufferSource = bufferSource;
        renderer.matrices = matrices;
        renderer.tickDelta = tickDelta;
        renderer.light = light;
        renderer.alpha = 1f;
        renderer.overlay = OverlayTexture.NO_OVERLAY;

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

        arm.xRot = 0;
        //renderer.allowMatrixUpdate = true;
        PartFilterScheme filter = arm == playerRenderer.getModel().leftArm ? PartFilterScheme.LEFT_ARM : PartFilterScheme.RIGHT_ARM;
        render(player, 0f, tickDelta, 1f, stack, bufferSource, light, overlay, playerRenderer, filter);
        //renderer.allowMatrixUpdate = false;
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

        stack.pushPose();
        stack.scale(16, 16, -16);
        renderer.renderSpecialParts();
        stack.popPose();

        //renderer.allowMatrixUpdate = false;
    }

    // -- animations -- //

    public void applyAnimations() {
        int animationsLimit = TrustManager.get(owner).get(TrustContainer.Trust.BB_ANIMATIONS);
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
            renderer.clean();

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

    public int getScriptMemory() {
        if (luaState == null)
            return 0;

        return luaState.getTotalMemory() - luaState.getFreeMemory();
    }

    // -- loading -- //

    private void createLuaState() {
        if (!nbt.contains("scripts"))
            return;

        Map<String, String> scripts = parseScripts(nbt.getCompound("scripts"));
        CompoundTag metadata = nbt.getCompound("metadata");
        ListTag autoScripts = null;

        if (metadata.contains("autoScripts"))
            autoScripts = metadata.getList("autoScripts", Tag.TAG_STRING);

        FiguraLuaState luaState = new FiguraLuaState(this, Math.min(TrustManager.get(owner).get(TrustContainer.Trust.MAX_MEM), 2048));

        if (renderer != null && renderer.root != null)
            luaState.loadGlobal(renderer.root, "models");

        TrustContainer trust = TrustManager.get(owner);
        int initLimit = trust.get(TrustContainer.Trust.INIT_INST);
        entityTickLimit = trust.get(TrustContainer.Trust.TICK_INST);
        worldTickLimit = trust.get(TrustContainer.Trust.WORLD_TICK_INST);
        entityRenderLimit = trust.get(TrustContainer.Trust.RENDER_INST);
        worldRenderLimit = trust.get(TrustContainer.Trust.WORLD_RENDER_INST);

        luaState.setInstructionLimit(initLimit);
        this.luaState = luaState;

        if (!luaState.init(scripts, autoScripts)) {
            luaState.close();
            this.luaState = null;
        } else if (FiguraMod.DO_OUR_NATIVES_WORK) {
            initInstructions = initLimit - luaState.getInstructions();
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
                        Animation.addCode(animation, compound.getFloat("time"), compound.getString("src"));
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
                byte[] source = root.getByteArray(key);

                try (ByteArrayInputStream inputStream = new ByteArrayInputStream(source); OggAudioStream oggAudioStream = new OggAudioStream(inputStream)) {
                    SoundBuffer sound = new SoundBuffer(oggAudioStream.readAll(), oggAudioStream.getFormat());
                    this.customSounds.put(key, sound);
                }
            } catch (Exception e) {
                FiguraMod.LOGGER.warn("Failed to load custom sound \"" + key + "\"", e);
            }
        }
    }

    private static Map<String, String> parseScripts(CompoundTag scripts) {
        Map<String, String> result = new HashMap<>();
        for (String s : scripts.getAllKeys()) {
            StringBuilder builder = new StringBuilder();
            ListTag list = scripts.getList(s, Tag.TAG_STRING);
            for (Tag tag : list)
                builder.append(tag.getAsString());
            result.put(s, builder.toString());
        }
        return result;
    }
}
