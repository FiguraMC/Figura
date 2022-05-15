package org.moon.figura.avatars;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.SharedConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
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
import org.moon.figura.lua.types.LuaFunction;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;
import org.terasology.jnlua.LuaRuntimeException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

//the avatar class
//contains all things related to the avatar
//and also related to the owner, like trust settings
public class Avatar {

    //metadata
    public final String name;
    public final String author;
    public final String version;
    public final float fileSize;
    public String badges = ""; //TODO fetch from backend
    public String pride;

    //Runtime data
    public final CompoundTag nbt;
    public final UUID owner;
    public final AvatarRenderer renderer;
    public FiguraLuaState luaState;

    private int tickLimit, renderLimit;

    //runtime status
    public boolean hasTexture = false;
    public boolean scriptError = false;
    public int complexity = 0;

    public int initInstructions = 0;

    public int tickInstructions = 0;

    public int renderInstructions = 0;
    public int postRenderInstructions = 0;
    public int worldRenderInstructions = 0;
    public int postWorldRenderInstructions = 0;
    public int accumulatedRenderInstructions = 0;

    public float particlesRemaining = 0f;
    public float soundsRemaining = 0f;

    public Avatar(CompoundTag nbt, UUID owner) {
        this.nbt = nbt;
        this.owner = owner;

        //read metadata
        CompoundTag metadata = nbt.getCompound("metadata");
        name = metadata.getString("name");
        author = metadata.getString("author");
        version = metadata.getString("ver");
        pride = metadata.getString("pride");
        fileSize = getFileSize();

        //read model
        renderer = new ImmediateAvatarRenderer(this);

        //read script
        luaState = createLuaState();
    }

    //Calling with maxInstructions as -1 will not set the max instructions, and instead keep them as they are.
    public void tryCall(Object toRun, int maxInstructions, Object... args) {
        try {
            if (maxInstructions != -1)
                luaState.setInstructionLimit(maxInstructions);
            if (toRun instanceof EventsAPI.LuaEvent event)
                event.call(args);
            else if (toRun instanceof LuaFunction func)
                func.call(args);
            else
                throw new LuaRuntimeException("Invalid type to run!");
        } catch (LuaRuntimeException ex) {
            FiguraLuaPrinter.sendLuaError(ex, name, owner);
            scriptError = true;
            luaState.close();
            luaState = null;
        }
    }

    public void onTick() {
        if (!scriptError && luaState != null) {
            float maxParticles = TrustManager.get(this.owner).get(TrustContainer.Trust.PARTICLES);
            this.particlesRemaining = Math.min(particlesRemaining + (maxParticles / SharedConstants.TICKS_PER_SECOND), maxParticles);

            float maxSounds = TrustManager.get(this.owner).get(TrustContainer.Trust.SOUNDS);
            this.soundsRemaining = Math.min(soundsRemaining + (maxSounds / SharedConstants.TICKS_PER_SECOND), maxSounds);

            tryCall(luaState.events.TICK, tickLimit);
            if (!Minecraft.ON_OSX)
                tickInstructions = tickLimit - luaState.getInstructions();
        }
    }

    public void onRender(Entity entity, float yaw, float delta, float alpha, PoseStack matrices, MultiBufferSource bufferSource, int light, LivingEntityRenderer<?, ?> entityRenderer, ElytraModel<?> elytraModel) {
        if (entity.isSpectator())
            renderer.currentFilterScheme = AvatarRenderer.RENDER_HEAD;
        renderer.entity = entity;
        renderer.yaw = yaw;
        renderer.tickDelta = delta;
        renderer.alpha = alpha;
        renderer.matrices = matrices;
        renderer.bufferSource = bufferSource;
        renderer.light = light;
        renderer.entityRenderer = entityRenderer;
        renderer.elytraModel = elytraModel;
        if (!scriptError && luaState != null) {
            tryCall(luaState.events.RENDER, -1, delta);
            if (!Minecraft.ON_OSX) {
                renderInstructions = renderLimit - accumulatedRenderInstructions - luaState.getInstructions();
                accumulatedRenderInstructions += renderInstructions;
            }


        }

        renderer.render();
        if (!scriptError && luaState != null) {
            tryCall(luaState.events.POST_RENDER, -1, delta);
            if (!Minecraft.ON_OSX) {
                postRenderInstructions = renderLimit - accumulatedRenderInstructions - luaState.getInstructions();
                accumulatedRenderInstructions += postRenderInstructions;
            }
        }
    }

    public void worldRenderEvent(float tickDelta) {
        renderer.tickDelta = tickDelta;
        renderer.allowMatrixUpdate = true;

        if (!scriptError && luaState != null) {
            tryCall(luaState.events.WORLD_RENDER, renderLimit, tickDelta);
            if (!Minecraft.ON_OSX) {
                worldRenderInstructions = renderLimit - luaState.getInstructions();
                accumulatedRenderInstructions = worldRenderInstructions;
            }

        }

    }

    public void endWorldRenderEvent() {
        renderer.allowMatrixUpdate = false;
        if (!scriptError && luaState != null) {
            tryCall(luaState.events.POST_WORLD_RENDER, -1, renderer.tickDelta);
            if (!Minecraft.ON_OSX) {
                postWorldRenderInstructions = renderLimit - accumulatedRenderInstructions - luaState.getInstructions();
                accumulatedRenderInstructions += postWorldRenderInstructions;
            }
        }

    }

    public void chatSendMessageEvent(String message) {
        if (!scriptError && luaState != null)
            tryCall(luaState.events.CHAT_SEND_MESSAGE, -1, message);
    }

    public void chatReceivedMessageEvent(String message) {
        if (!scriptError && luaState != null)
            tryCall(luaState.events.CHAT_RECEIVED_MESSAGE, -1, message);
    }

    public void onWorldRender(Entity entity, double camX, double camY, double camZ, PoseStack matrices, MultiBufferSource bufferSource, int light, float tickDelta) {
        renderer.entity = entity;
        renderer.currentFilterScheme = AvatarRenderer.RENDER_WORLD;
        renderer.bufferSource = bufferSource;
        renderer.matrices = matrices;
        renderer.tickDelta = tickDelta;
        renderer.light = light;
        renderer.alpha = 1f;
        matrices.pushPose();
        matrices.translate(-camX, -camY, -camZ);
        matrices.scale(-1, -1, 1);

        renderer.renderWorldParts();
        matrices.popPose();
    }

    public void onFirstPersonWorldRender(Entity watcher, MultiBufferSource bufferSource, PoseStack matrices, Camera camera, float tickDelta) {
        int light = Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(watcher, tickDelta);
        Vec3 camPos = camera.getPosition();
        onWorldRender(watcher, camPos.x, camPos.y, camPos.z, matrices, bufferSource, light, tickDelta);
    }

    public void onFirstPersonRender(PoseStack stack, MultiBufferSource bufferSource, Player player, PlayerRenderer playerRenderer, ElytraModel<?> elytraModel, ModelPart arm, int light, float tickDelta) {
        arm.xRot = 0;
        renderer.currentFilterScheme = arm == playerRenderer.getModel().leftArm ? AvatarRenderer.RENDER_LEFT_ARM : AvatarRenderer.RENDER_RIGHT_ARM;
        onRender(player, 0f, tickDelta, 1f, stack, bufferSource, light, playerRenderer, elytraModel);
    }

    /**
     * We should call this whenever an avatar is no longer reachable!
     * It free()s all the CachedType used inside of the avatar, and also
     * closes the native texture resources.
     */
    public void clean() {
        renderer.clean();
    }

    private float getFileSize() {
        try {
            //get size
            DataOutputStream dos = new DataOutputStream(new ByteArrayOutputStream());
            NbtIo.writeCompressed(nbt, dos);
            long size = dos.size();

            //format size to kb
            DecimalFormat df = new DecimalFormat("#0.00", new DecimalFormatSymbols(Locale.US));
            df.setRoundingMode(RoundingMode.HALF_UP);
            return Float.parseFloat(df.format(size / 1000f));
        } catch (Exception e) {
            FiguraMod.LOGGER.warn("Failed to generate file size for model " + this.name, e);
            return 0f;
        }
    }

    private FiguraLuaState createLuaState() {
        if (!nbt.contains("scripts"))
            return null;
        Map<String, String> scripts = parseScripts(nbt.getCompound("scripts"));

        CompoundTag metadata = nbt.getCompound("metadata");
        ListTag autoScripts = null;
        if (metadata.contains("autoScripts"))
            autoScripts = metadata.getList("autoScripts", Tag.TAG_STRING);

        FiguraLuaState luaState = new FiguraLuaState(this, TrustManager.get(owner).get(TrustContainer.Trust.MAX_MEM));

        if (renderer != null && renderer.root != null)
            luaState.loadGlobal(renderer.root, "models");

        int initLimit = TrustManager.get(owner).get(TrustContainer.Trust.INIT_INST);
        tickLimit = TrustManager.get(owner).get(TrustContainer.Trust.TICK_INST);
        renderLimit = TrustManager.get(owner).get(TrustContainer.Trust.RENDER_INST);

        luaState.setInstructionLimit(initLimit);
        if (luaState.init(scripts, autoScripts)) {
            initInstructions = initLimit - luaState.getInstructions();
            return luaState;
        }
        else
            luaState.close();

        return null;
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
