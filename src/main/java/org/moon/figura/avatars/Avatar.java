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
import org.moon.figura.avatars.model.rendering.AvatarRenderer;
import org.moon.figura.avatars.model.rendering.ImmediateAvatarRenderer;
import org.moon.figura.lua.FiguraLuaPrinter;
import org.moon.figura.lua.FiguraLuaState;
import org.moon.figura.lua.api.EventsAPI;
import org.moon.figura.lua.api.entity.EntityWrapper;
import org.moon.figura.lua.api.nameplate.NameplateCustomization;
import org.moon.figura.lua.api.sound.FiguraChannel;
import org.moon.figura.lua.types.LuaFunction;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;
import org.moon.figura.utils.RefilledNumber;
import org.terasology.jnlua.LuaRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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

    public final HashMap<String, SoundBuffer> customSounds = new HashMap<>();

    private int tickLimit, renderLimit;
    private int worldTickLimit, worldRenderLimit;

    //runtime status
    public boolean hasTexture = false;
    public boolean scriptError = false;

    public int complexity = 0;
    public int initInstructions = 0;
    public int tickInstructions = 0;
    public int worldTickInstructions = 0;
    public int renderInstructions = 0;
    public int worldRenderInstructions = 0;

    public int postRenderInstructions = 0;
    public int postWorldRenderInstructions = 0;
    public int accumulatedRenderInstructions = 0;
    public int accumulatedTickInstructions = 0;

    public final RefilledNumber particlesRemaining = new RefilledNumber();
    public final RefilledNumber soundsRemaining = new RefilledNumber();

    public Avatar(UUID owner) {
        this.owner = owner;
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
        if (nbt == null)
            return;

        loaded = false;

        //sounds
        run(() -> { //metadata
            this.nbt = nbt;
            CompoundTag metadata = nbt.getCompound("metadata");
            name = metadata.getString("name");
            authors = metadata.getString("authors");
            version = metadata.getString("ver");
            color = metadata.getString("color");
            fileSize = getFileSize();
        }).thenRun(() -> { //models
            renderer = new ImmediateAvatarRenderer(this);
        }).thenRun(this::loadCustomSounds).thenRun(() -> { //script
            createLuaState();
            loaded = true;
        });
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

        //sound
        particlesRemaining.set(TrustManager.get(this.owner).get(TrustContainer.Trust.PARTICLES));
        particlesRemaining.tick();

        //particles
        soundsRemaining.set(TrustManager.get(this.owner).get(TrustContainer.Trust.SOUNDS));
        soundsRemaining.tick();

        tryCall(luaState.events.TICK, tickLimit);
        if (FiguraMod.DO_OUR_NATIVES_WORK && luaState != null) {
            tickInstructions = tickLimit - luaState.getInstructions();
            accumulatedTickInstructions += tickInstructions;
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

        tryCall(luaState.events.RENDER, renderLimit, delta);
        if (FiguraMod.DO_OUR_NATIVES_WORK && luaState != null) {
            renderInstructions = renderLimit - accumulatedRenderInstructions - luaState.getInstructions();
            accumulatedRenderInstructions += renderInstructions;
        }
    }

    public void postRenderEvent(float delta) {
        if (scriptError || luaState == null)
            return;

        tryCall(luaState.events.POST_RENDER, -1, delta);
        if (FiguraMod.DO_OUR_NATIVES_WORK && luaState != null) {
            postRenderInstructions = renderLimit - accumulatedRenderInstructions - luaState.getInstructions();
            accumulatedRenderInstructions += postRenderInstructions;
        }
    }

    public void worldRenderEvent(float delta) {
        if (scriptError || luaState == null)
            return;

        tryCall(luaState.events.WORLD_RENDER, worldRenderLimit, delta);
        if (FiguraMod.DO_OUR_NATIVES_WORK && luaState != null) {
            worldRenderInstructions = worldRenderLimit - luaState.getInstructions();
            accumulatedRenderInstructions = worldRenderInstructions;
        }
    }

    public void postWorldRenderEvent() {
        if (scriptError || luaState == null)
            return;

        tryCall(luaState.events.POST_WORLD_RENDER, -1, renderer.tickDelta);
        if (FiguraMod.DO_OUR_NATIVES_WORK && luaState != null) {
            postWorldRenderInstructions = worldRenderLimit - accumulatedRenderInstructions - luaState.getInstructions();
            accumulatedRenderInstructions += postWorldRenderInstructions;
        }
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

    public void render(Entity entity, float yaw, float delta, float alpha, PoseStack matrices, MultiBufferSource bufferSource, int light, int overlay, LivingEntityRenderer<?, ?> entityRenderer, AvatarRenderer.PartFilterScheme filter) {
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

        renderer.allowMatrixUpdate = true;
        renderer.entity = entity;
        renderer.currentFilterScheme = AvatarRenderer.PartFilterScheme.WORLD;
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

        renderer.allowMatrixUpdate = false;
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
        renderer.allowMatrixUpdate = true;
        AvatarRenderer.PartFilterScheme filter = arm == playerRenderer.getModel().leftArm ? AvatarRenderer.PartFilterScheme.LEFT_ARM : AvatarRenderer.PartFilterScheme.RIGHT_ARM;
        render(player, 0f, tickDelta, 1f, stack, bufferSource, light, overlay, playerRenderer, filter);
        renderer.allowMatrixUpdate = false;
    }

    public void hudRender(PoseStack stack, MultiBufferSource bufferSource, Entity entity, float tickDelta) {
        if (renderer == null)
            return;

        renderer.allowMatrixUpdate = true;
        renderer.currentFilterScheme = AvatarRenderer.PartFilterScheme.HUD;
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

        renderer.allowMatrixUpdate = false;
    }

    // -- extra stuff -- //

    /**
     * We should call this whenever an avatar is no longer reachable!
     * It free()s all the CachedType used inside of the avatar, and also
     * closes the native texture resources.
     * also closes and stops this avatar sounds
     */
    public void clean() {
        if (renderer != null)
            renderer.clean();

        FiguraChannel.getInstance().stopSound(owner, null);
        for (SoundBuffer value : customSounds.values())
            value.discardAlBuffer();
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

        int initLimit = TrustManager.get(owner).get(TrustContainer.Trust.INIT_INST);
        tickLimit = TrustManager.get(owner).get(TrustContainer.Trust.TICK_INST);
        worldTickLimit = TrustManager.get(owner).get(TrustContainer.Trust.WORLD_TICK_INST);
        renderLimit = TrustManager.get(owner).get(TrustContainer.Trust.RENDER_INST);
        worldRenderLimit = TrustManager.get(owner).get(TrustContainer.Trust.WORLD_RENDER_INST);

        luaState.setInstructionLimit(initLimit);
        this.luaState = luaState;

        if (!luaState.init(scripts, autoScripts)) {
            luaState.close();
            this.luaState = null;
        } else {
            if (FiguraMod.DO_OUR_NATIVES_WORK)
                initInstructions = initLimit - luaState.getInstructions();
        }
    }

    private void loadCustomSounds() {
        if (!nbt.contains("sounds"))
            return;

        CompoundTag root = nbt.getCompound("sounds");
        for (String key : root.getAllKeys()) {
            try {
                byte[] source = root.getByteArray(key);
                OggAudioStream oggAudioStream = new OggAudioStream(new ByteArrayInputStream(source));
                SoundBuffer sound = new SoundBuffer(oggAudioStream.readAll(), oggAudioStream.getFormat());

                this.customSounds.put(key, sound);
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
