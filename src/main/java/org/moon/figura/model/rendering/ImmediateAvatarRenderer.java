package org.moon.figura.model.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.config.Configs;
import org.moon.figura.lua.api.ClientAPI;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.model.FiguraModelPart;
import org.moon.figura.model.FiguraModelPartReader;
import org.moon.figura.model.ParentType;
import org.moon.figura.model.PartCustomization;
import org.moon.figura.model.rendering.texture.FiguraTexture;
import org.moon.figura.model.rendering.texture.FiguraTextureSet;
import org.moon.figura.model.rendering.texture.RenderTypes;
import org.moon.figura.model.rendertasks.RenderTask;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.ui.UIHelper;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class ImmediateAvatarRenderer extends AvatarRenderer {

    protected final PartCustomization.PartCustomizationStack customizationStack = new PartCustomization.PartCustomizationStack();

    public static final FiguraMat4 VIEW_TO_WORLD_MATRIX = FiguraMat4.of();
    private static final PartCustomization pivotOffsetter = new PartCustomization();
    protected static final VertexBuffer VERTEX_BUFFER = new VertexBuffer();

    public ImmediateAvatarRenderer(Avatar avatar) {
        super(avatar);

        //Vertex data, read model parts
        root = FiguraModelPartReader.read(avatar, avatar.nbt.getCompound("models"), textureSets, false);

        sortParts();
    }

    public void checkEmpty() {
        if (!customizationStack.isEmpty())
            throw new IllegalStateException("Customization stack not empty!");
    }

    @Override
    public int render() {
        return commonRender(1.5d);
    }

    @Override
    public int renderSpecialParts() {
        return commonRender(0);
    }

    @Override
    public void updateMatrices() {
        //flag rendering state
        this.isRendering = true;

        //setup root customizations
        PartCustomization customization = setupRootCustomization(1.5d);

        //Push transform
        customizationStack.push(customization);

        //world matrices
        VIEW_TO_WORLD_MATRIX.set(AvatarRenderer.worldToViewMatrix().invert());

        //calculate each part matrices
        calculatePartMatrices(root);

        //finish rendering
        customizationStack.pop();
        checkEmpty();

        this.isRendering = false;
    }

    protected int commonRender(double vertOffset) {
        //flag rendering state
        this.isRendering = true;

        //iris fix
        int irisConfig = UIHelper.paperdoll || !ClientAPI.hasIris() ? 0 : Configs.IRIS_COMPATIBILITY_FIX.value;
        doIrisEmissiveFix = (irisConfig >= 2 && ClientAPI.hasIrisShader()) || (avatar.renderMode != EntityRenderMode.RENDER && avatar.renderMode != EntityRenderMode.WORLD);
        offsetRenderLayers = irisConfig >= 1;

        //custom textures
        for (FiguraTextureSet set : textureSets)
            set.uploadIfNeeded();
        for (FiguraTexture texture : customTextures.values())
            texture.uploadIfDirty();

        //Set shouldRenderPivots
        int config = Configs.RENDER_DEBUG_PARTS_PIVOT.value;
        if (!Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes() || (!avatar.isHost && config < 2))
            shouldRenderPivots = 0;
        else
            shouldRenderPivots = config;

        //world matrices
        if (allowMatrixUpdate)
            VIEW_TO_WORLD_MATRIX.set(AvatarRenderer.worldToViewMatrix().invert());

        //complexity
        int prev = avatar.complexity.remaining;
        int[] remainingComplexity = new int[] {prev};

        //render all model parts
        if (root.customization.visible == null || root.customization.visible) {
            if (currentFilterScheme.parentType.isSeparate) {
                List<FiguraModelPart> parts = separatedParts.get(currentFilterScheme.parentType);
                if (parts != null) {
                    boolean renderLayer = !currentFilterScheme.parentType.isRenderLayer;
                    if (renderLayer) {
                        PartCustomization customization = setupRootCustomization(vertOffset);
                        customizationStack.push(customization); //push root
                        customizationStack.push(root.customization); //push "models"
                    }

                    for (FiguraModelPart part : parts) {
                        if (currentFilterScheme.parentType == ParentType.Item && part != itemToRender)
                            continue;

                        boolean saved = part.savedCustomization != null;
                        if (saved) customizationStack.push(part.savedCustomization);

                        renderPart(part, remainingComplexity, currentFilterScheme.initialValue);

                        if (saved) customizationStack.pop();
                    }

                    if (renderLayer) {
                        customizationStack.pop(); //pop "models"
                        customizationStack.pop(); //pop root
                    }
                }
            } else {
                PartCustomization customization = setupRootCustomization(vertOffset);
                customizationStack.push(customization);
                renderPart(root, remainingComplexity, currentFilterScheme.initialValue);
                customizationStack.pop();
            }

            //push vertices to vertex consumer
            FiguraMod.pushProfiler("draw");
            FiguraMod.pushProfiler("primary");
            VERTEX_BUFFER.consume(true, bufferSource);
            FiguraMod.popPushProfiler("secondary");
            VERTEX_BUFFER.consume(false, bufferSource);
            FiguraMod.popProfiler(2);

            //finish rendering
            checkEmpty();
        }

        this.isRendering = false;
        if (this.dirty)
            clean();

        return prev - Math.max(remainingComplexity[0], 0);
    }

    protected PartCustomization setupRootCustomization(double vertOffset) {
        PartCustomization customization = new PartCustomization();

        customization.setPrimaryRenderType(RenderTypes.TRANSLUCENT);
        customization.setSecondaryRenderType(RenderTypes.EMISSIVE);

        double s = 1.0 / 16;
        customization.positionMatrix.scale(s, s, s);
        customization.positionMatrix.rotateZ(180);
        customization.positionMatrix.translate(0, vertOffset, 0);
        customization.normalMatrix.rotateZ(180);

        customization.positionMatrix.multiply(posMat);
        customization.normalMatrix.multiply(normalMat);

        customization.light = light;
        customization.alpha = alpha;
        customization.overlay = overlay;

        customization.primaryTexture = Pair.of(FiguraTextureSet.OverrideType.PRIMARY, null);
        customization.secondaryTexture = Pair.of(FiguraTextureSet.OverrideType.SECONDARY, null);

        return customization;
    }

    protected boolean renderPart(FiguraModelPart part, int[] remainingComplexity, boolean prevPredicate) {
        FiguraMod.pushProfiler(part.name);

        PartCustomization custom = part.customization;

        //test the current filter scheme
        FiguraMod.pushProfiler("predicate");
        Boolean thisPassedPredicate = currentFilterScheme.test(part.parentType, prevPredicate);
        if (thisPassedPredicate == null || (custom.visible != null && !custom.visible)) {
            if (part.parentType.isRenderLayer)
                part.savedCustomization = customizationStack.peek();
            FiguraMod.popProfiler(2);
            return true;
        }

        //calculate part transforms

        //calculate vanilla parent
        FiguraMod.popPushProfiler("copyVanillaPart");
        part.applyVanillaTransforms(vanillaModelData);
        part.applyExtraTransforms(customizationStack.peek());

        //visibility
        FiguraMod.popPushProfiler("checkVanillaVisible");
        if (!ignoreVanillaVisibility && custom.vanillaVisible != null && !custom.vanillaVisible) {
            FiguraMod.popProfiler(2);
            return true;
        }

        //recalculate stuff
        FiguraMod.popPushProfiler("calculatePartMatrices");
        custom.recalculate();

        //void blocked matrices
        //that's right, check only for previous predicate
        FiguraMat4 positionCopy = null;
        FiguraMat3 normalCopy = null;
        boolean voidMatrices = !allowHiddenTransforms && !prevPredicate;
        if (voidMatrices) {
            FiguraMod.popPushProfiler("clearMatrices");
            positionCopy = custom.positionMatrix.copy();
            normalCopy = custom.normalMatrix.copy();
            custom.positionMatrix.reset();
            custom.normalMatrix.reset();
        }

        //push stack
        FiguraMod.popPushProfiler("pushCustomizationStack");
        customizationStack.push(custom);

        //restore variables
        if (voidMatrices) {
            FiguraMod.popPushProfiler("restoreMatrices");
            custom.positionMatrix.set(positionCopy);
            custom.normalMatrix.set(normalCopy);
        }

        FiguraMod.popProfiler();

        if (thisPassedPredicate) {
            //recalculate world matrices
            FiguraMod.pushProfiler("worldMatrices");
            if (allowMatrixUpdate) {
                FiguraMat4 mat = partToWorldMatrices(custom);
                part.savedPartToWorldMat.set(mat);
            }

            //recalculate light
            FiguraMod.popPushProfiler("calculateLight");
            Level l;
            if (custom.light != null)
                updateLight = false;
            else if (updateLight && (l = Minecraft.getInstance().level) != null) {
                FiguraVec3 pos = part.savedPartToWorldMat.apply(0d, 0d, 0d);
                int block = l.getBrightness(LightLayer.BLOCK, pos.asBlockPos());
                int sky = l.getBrightness(LightLayer.SKY, pos.asBlockPos());
                customizationStack.peek().light = LightTexture.pack(block, sky);
            }
            FiguraMod.popProfiler();
        }

        //render this
        FiguraMod.pushProfiler("pushVertices");
        boolean breakRender = thisPassedPredicate && !part.pushVerticesImmediate(this, remainingComplexity);

        //render extras
        FiguraMod.popPushProfiler("extras");
        if (!breakRender && thisPassedPredicate) {
            boolean renderPivot = shouldRenderPivots > 0;
            boolean renderTasks = !part.renderTasks.isEmpty();
            boolean renderPivotParts = part.parentType.isPivot && allowPivotParts;

            if (renderPivot || renderTasks || renderPivotParts) {
                //fix pivots
                FiguraMod.pushProfiler("fixMatricesPivot");

                FiguraVec3 pivot = custom.getPivot();
                pivotOffsetter.setPos(pivot);
                pivotOffsetter.recalculate();
                customizationStack.push(pivotOffsetter);

                PartCustomization peek = customizationStack.peek();

                //render pivot indicators
                if (renderPivot) {
                    FiguraMod.popPushProfiler("renderPivotCube");
                    renderPivot(part, peek);
                }

                //render tasks
                if (renderTasks) {
                    FiguraMod.popPushProfiler("renderTasks");
                    int light = peek.light;
                    int overlay = peek.overlay;
                    allowSkullRendering = false;
                    for (RenderTask task : part.renderTasks.values()) {
                        if (!task.shouldRender())
                            continue;
                        int neededComplexity = task.getComplexity();
                        if (neededComplexity > remainingComplexity[0])
                            break;
                        FiguraMod.pushProfiler(task.getName());
                        task.render(customizationStack, bufferSource, light, overlay);
                        remainingComplexity[0] -= neededComplexity;
                        FiguraMod.popProfiler();
                    }
                    allowSkullRendering = true;
                }

                //render pivot parts
                if (renderPivotParts && part.parentType.isPivot) {
                    FiguraMod.popPushProfiler("savePivotParts");
                    savePivotTransform(part.parentType, peek);
                }

                customizationStack.pop();
                FiguraMod.popProfiler();
            }
        }

        //render children
        FiguraMod.popPushProfiler("children");
        for (FiguraModelPart child : part.children)
            if (!renderPart(child, remainingComplexity, thisPassedPredicate)) {
                breakRender = true;
                break;
            }

        //reset the parent
        FiguraMod.popPushProfiler("removeVanillaTransforms");
        part.resetVanillaTransforms();

        //pop
        customizationStack.pop();
        FiguraMod.popProfiler(2);

        return !breakRender;
    }

    protected void renderPivot(FiguraModelPart part, PartCustomization customization) {
        boolean group = part.customization.partType == PartCustomization.PartType.GROUP;
        FiguraVec3 color = group ? ColorUtils.Colors.MAYA_BLUE.vec : ColorUtils.Colors.FRAN_PINK.vec;
        double boxSize = group ? 1 / 16d : 1 / 32d;
        boxSize /= Math.max(Math.cbrt(part.savedPartToWorldMat.det()), 0.02);

        PoseStack stack = customization.copyIntoGlobalPoseStack();

        LevelRenderer.renderLineBox(stack, bufferSource.getBuffer(RenderType.LINES),
                -boxSize, -boxSize, -boxSize,
                boxSize, boxSize, boxSize,
                (float) color.x, (float) color.y, (float) color.z, 1f);
    }

    protected void savePivotTransform(ParentType parentType, PartCustomization customization) {
        FiguraMat4 currentPosMat = customization.getPositionMatrix();
        FiguraMat3 currentNormalMat = customization.getNormalMatrix();
        ConcurrentLinkedQueue<Pair<FiguraMat4, FiguraMat3>> queue = pivotCustomizations.computeIfAbsent(parentType, p -> new ConcurrentLinkedQueue<>());
        queue.add(new Pair<>(currentPosMat, currentNormalMat)); //These are COPIES, so ok to add
    }

    protected FiguraMat4 partToWorldMatrices(PartCustomization cust) {
        FiguraMat4 customizePeek = customizationStack.peek().positionMatrix.copy();
        customizePeek.multiply(VIEW_TO_WORLD_MATRIX);
        FiguraVec3 piv = cust.getPivot();

        FiguraMat4 translation = FiguraMat4.of();
        translation.translate(piv);
        customizePeek.rightMultiply(translation);

        return customizePeek;
    }

    protected void calculatePartMatrices(FiguraModelPart part) {
        FiguraMod.pushProfiler(part.name);

        PartCustomization custom = part.customization;

        //Store old visibility, but overwrite it in case we only want to render certain parts
        FiguraMod.pushProfiler("predicate");
        Boolean thisPassedPredicate = currentFilterScheme.test(part.parentType, true);
        if (thisPassedPredicate == null) {
            FiguraMod.popProfiler(2);
            return;
        }

        //calculate part transforms

        //calculate vanilla parent
        FiguraMod.popPushProfiler("copyVanillaPart");
        part.applyVanillaTransforms(vanillaModelData);
        part.applyExtraTransforms(customizationStack.peek());

        //push customization stack
        FiguraMod.popPushProfiler("calculatePartMatrices");
        custom.recalculate();
        FiguraMod.popPushProfiler("applyOnStack");
        customizationStack.push(custom);

        //render extras
        if (thisPassedPredicate) {
            //part to world matrices
            FiguraMod.popPushProfiler("worldMatrices");
            FiguraMat4 mat = partToWorldMatrices(custom);
            part.savedPartToWorldMat.set(mat);
        }

        //render children
        FiguraMod.popPushProfiler("children");
        for (FiguraModelPart child : part.children)
            calculatePartMatrices(child);

        //reset the parent
        part.resetVanillaTransforms();

        //pop
        customizationStack.pop();
        FiguraMod.popProfiler(2);
    }

    public void pushFaces(int faceCount, int[] remainingComplexity, FiguraTextureSet textureSet, List<Vertex> vertices) {
        //Handle cases that we can quickly
        if (faceCount == 0 || vertices.isEmpty())
            return;

        PartCustomization customization = customizationStack.peek();

        VertexData primary = getTexture(customization, textureSet, true);
        VertexData secondary = getTexture(customization, textureSet, false);

        if (primary.renderType == null && secondary.renderType == null) {
            remainingComplexity[0] += faceCount;
            return;
        }

        if (primary.renderType != null)
            pushToBuffer(faceCount, primary, customization, textureSet, vertices);
        if (secondary.renderType != null)
            pushToBuffer(faceCount, secondary, customization, textureSet, vertices);
    }

    private VertexData getTexture(PartCustomization customization, FiguraTextureSet textureSet, boolean primary) {
        RenderTypes types = primary ? customization.getPrimaryRenderType() : customization.getSecondaryRenderType();
        Pair<FiguraTextureSet.OverrideType, Object> texture = primary ? customization.primaryTexture : customization.secondaryTexture;
        VertexData ret = new VertexData();

        if (types == RenderTypes.NONE)
            return ret;

        //get texture
        ResourceLocation id = textureSet.getOverrideTexture(avatar.owner, texture);

        //color
        ret.color = primary ? customization.color : customization.color2;

        //primary
        ret.primary = primary;

        //get render type
        if (id != null) {
            if (translucent) {
                ret.renderType = RenderType.itemEntityTranslucentCull(id);
                return ret;
            }
            if (glowing) {
                ret.renderType = RenderType.outline(id);
                return ret;
            }
        }

        if (types == null)
            return ret;

        if (offsetRenderLayers && !primary && types.isOffset())
            ret.vertexOffset = -0.0005f;

        //Switch to cutout with fullbright if the iris emissive fix is enabled
        if (doIrisEmissiveFix && types == RenderTypes.EMISSIVE) {
            ret.fullBright = true;
            ret.renderType = RenderTypes.TRANSLUCENT_CULL.get(id);
        } else {
            ret.renderType = types.get(id);
        }

        return ret;
    }

    private static final FiguraVec4 pos = FiguraVec4.of();
    private static final FiguraVec3 normal = FiguraVec3.of();
    private static final FiguraVec3 uv = FiguraVec3.of(0, 0, 1);
    private void pushToBuffer(int faceCount, VertexData vertexData, PartCustomization customization, FiguraTextureSet textureSet, List<Vertex> vertices) {
        int vertCount = faceCount * 4;

        FiguraVec3 uvFixer = FiguraVec3.of();
        uvFixer.set(textureSet.getWidth(), textureSet.getHeight(), 1); //Dividing by this makes uv 0 to 1

        int overlay = customization.overlay;
        int light = vertexData.fullBright ? LightTexture.FULL_BRIGHT : customization.light;

        VERTEX_BUFFER.getBufferFor(vertexData.renderType, vertexData.primary, vertexConsumer -> {
            for (int i = 0; i < vertCount; i++) {
                Vertex vertex = vertices.get(i);

                pos.set(vertex.x, vertex.y, vertex.z, 1);
                pos.transform(customization.positionMatrix);
                pos.add(pos.normalized().scale(vertexData.vertexOffset));
                normal.set(vertex.nx, vertex.ny, vertex.nz);
                normal.transform(customization.normalMatrix);
                uv.set(vertex.u, vertex.v, 1);
                uv.divide(uvFixer);
                uv.transform(customization.uvMatrix);

                vertexConsumer.vertex(
                        (float) pos.x,
                        (float) pos.y,
                        (float) pos.z,

                        (float) vertexData.color.x,
                        (float) vertexData.color.y,
                        (float) vertexData.color.z,
                        customization.alpha,

                        (float) uv.x,
                        (float) uv.y,

                        overlay,
                        light,

                        (float) normal.x,
                        (float) normal.y,
                        (float) normal.z
                );
            }
        });
    }

    private static class VertexData {
        public RenderType renderType;
        public boolean fullBright;
        public float vertexOffset;
        public FiguraVec3 color;
        public boolean primary;
    }

    protected static class VertexBuffer {
        private final HashMap<RenderType, List<Consumer<VertexConsumer>>> primaryBuffers = new LinkedHashMap<>();
        private final HashMap<RenderType, List<Consumer<VertexConsumer>>> secondaryBuffers = new LinkedHashMap<>();

        public void getBufferFor(RenderType renderType, boolean primary, Consumer<VertexConsumer> consumer) {
            HashMap<RenderType, List<Consumer<VertexConsumer>>> buffer = primary ? primaryBuffers : secondaryBuffers;
            List<Consumer<VertexConsumer>> list = buffer.computeIfAbsent(renderType, renderType1 -> new ArrayList<>());
            list.add(consumer);
        }

        public void consume(boolean primary, MultiBufferSource bufferSource) {
            HashMap<RenderType, List<Consumer<VertexConsumer>>> map = primary ? primaryBuffers : secondaryBuffers;
            for (Map.Entry<RenderType, List<Consumer<VertexConsumer>>> entry : map.entrySet()) {
                VertexConsumer vertexConsumer = bufferSource.getBuffer(entry.getKey());
                List<Consumer<VertexConsumer>> consumers = entry.getValue();
                for (Consumer<VertexConsumer> consumer : consumers)
                    consumer.accept(vertexConsumer);
            }
            map.clear();
        }
    }
}
