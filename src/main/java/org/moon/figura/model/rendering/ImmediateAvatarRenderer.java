package org.moon.figura.model.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.config.Config;
import org.moon.figura.lua.api.ClientAPI;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.FiguraVec3;
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

public class ImmediateAvatarRenderer extends AvatarRenderer {

    protected final List<FiguraImmediateBuffer> buffers = new ArrayList<>(0);
    protected final PartCustomization.Stack customizationStack = new PartCustomization.Stack();

    public static final FiguraMat4 VIEW_TO_WORLD_MATRIX = FiguraMat4.of();
    private static final PartCustomization pivotOffsetter = PartCustomization.of();
    protected static final HashMap<RenderTypes, LinkedHashMap<RenderType, FloatArrayList>> VERTICES = new HashMap<>();

    public ImmediateAvatarRenderer(Avatar avatar) {
        super(avatar);

        //Vertex data, read model parts
        List<FiguraImmediateBuffer.Builder> builders = new ArrayList<>();
        root = FiguraModelPartReader.read(avatar, avatar.nbt.getCompound("models"), builders, textureSets);

        for (int i = 0; i < textureSets.size() && i < builders.size(); i++)
            buffers.add(builders.get(i).build(textureSets.get(i), customizationStack));
    }

    @Override
    protected void clean() {
        super.clean();
        customizationStack.fullClear();
        for (FiguraImmediateBuffer buffer : buffers)
            buffer.clean();
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

        //Free customization after use
        customization.free();

        //world matrices
        VIEW_TO_WORLD_MATRIX.set(AvatarRenderer.worldToViewMatrix().invert());

        //calculate each part matrices
        calculatePartMatrices(root);

        //finish rendering
        customizationStack.pop();
        checkEmpty();
        customizationStack.fullClear();
        this.isRendering = false;
    }

    protected int commonRender(double vertOffset) {
        //flag rendering state
        this.isRendering = true;

        //setup root customizations
        PartCustomization customization = setupRootCustomization(vertOffset);

        //Push transform
        customizationStack.push(customization);

        //Free customization after use
        customization.free();

        //iris fix
        int irisConfig = UIHelper.paperdoll || !ClientAPI.hasIris() ? 0 : Config.IRIS_COMPATIBILITY_FIX.asInt();
        doIrisEmissiveFix = irisConfig >= 2 && (ClientAPI.hasIrisShader() || (avatar.renderMode != EntityRenderMode.RENDER && avatar.renderMode != EntityRenderMode.WORLD));
        offsetRenderLayers = irisConfig >= 1;

        //Iterate and setup each buffer
        for (FiguraImmediateBuffer buffer : buffers) {
            //Reset buffers
            buffer.clearBuffers();
            //Upload texture if necessary
            buffer.uploadTexIfNeeded();
        }

        //custom textures
        for (FiguraTexture texture : customTextures.values())
            texture.uploadIfDirty();

        //Set shouldRenderPivots
        int config = Config.RENDER_DEBUG_PARTS_PIVOT.asInt();
        if (!Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes() || (!avatar.isHost && config < 3))
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
        Boolean initialValue = currentFilterScheme.initialValue(root);
        if (initialValue != null)
            renderPart(root, remainingComplexity, initialValue);

        //push vertices to vertex consumer
        FiguraMod.pushProfiler("draw");

        RenderTypes[] values = RenderTypes.values();
        for (RenderTypes value : values) {
            Map<RenderType, FloatArrayList> map = VERTICES.get(value);
            if (map == null)
                continue;

            for (Map.Entry<RenderType, FloatArrayList> entry : map.entrySet()) {
                VertexConsumer consumer = bufferSource.getBuffer(entry.getKey());
                FloatArrayList vertex = entry.getValue();

                for (int i = 0; i < vertex.size(); ) {
                    consumer.vertex(
                            //pos
                            vertex.getFloat(i++),
                            vertex.getFloat(i++),
                            vertex.getFloat(i++),

                            //color
                            vertex.getFloat(i++),
                            vertex.getFloat(i++),
                            vertex.getFloat(i++),
                            vertex.getFloat(i++),

                            //uv
                            vertex.getFloat(i++),
                            vertex.getFloat(i++),

                            //overlay, light
                            (int) vertex.getFloat(i++),
                            (int) vertex.getFloat(i++),

                            //normal
                            vertex.getFloat(i++),
                            vertex.getFloat(i++),
                            vertex.getFloat(i++)
                    );
                }
            }
        }

        VERTICES.clear();
        FiguraMod.popProfiler();

        //finish rendering
        customizationStack.pop();
        checkEmpty();

        this.isRendering = false;
        if (this.dirty)
            clean();

        return prev - Math.max(remainingComplexity[0], 0);
    }

    protected PartCustomization setupRootCustomization(double vertOffset) {
        PartCustomization customization = PartCustomization.of();

        customization.setPrimaryRenderType(RenderTypes.TRANSLUCENT);
        customization.setSecondaryRenderType(RenderTypes.EMISSIVE);

        double s = 1.0 / 16;
        customization.positionMatrix.scale(s, s, s);
        customization.positionMatrix.rotateZ(180);
        customization.positionMatrix.translate(0, vertOffset, 0);
        customization.normalMatrix.rotateZ(180);

        FiguraMat4 posMat = FiguraMat4.fromMatrix4f(matrices.last().pose());
        FiguraMat3 normalMat = FiguraMat3.fromMatrix3f(matrices.last().normal());

        customization.positionMatrix.multiply(posMat);
        customization.normalMatrix.multiply(normalMat);

        posMat.free();
        normalMat.free();

        customization.visible = true;
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
        if (thisPassedPredicate == null) {
            part.advanceVerticesImmediate(this); //stinky
            FiguraMod.popProfiler(2);
            return true;
        }

        //calculate part transforms

        //calculate vanilla parent
        FiguraMod.popPushProfiler("copyVanillaPart");
        part.applyVanillaTransforms(vanillaModelData);
        part.applyExtraTransforms(customizationStack.peek().positionMatrix);

        //recalculate stuff
        FiguraMod.popPushProfiler("checkVisibility");

        Boolean storedVisibility = custom.visible;
        custom.visible = part.getVisible() && (currentFilterScheme.ignoreVanillaVisible || part.getVanillaVisible()) && thisPassedPredicate;

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
        custom.visible = storedVisibility;

        if (voidMatrices) {
            FiguraMod.popPushProfiler("restoreMatrices");
            custom.positionMatrix.set(positionCopy);
            custom.normalMatrix.set(normalCopy);
            positionCopy.free();
            normalCopy.free();
        }

        FiguraMod.popProfiler();

        if (thisPassedPredicate) {
            //recalculate world matrices
            FiguraMod.pushProfiler("worldMatrices");
            if (allowMatrixUpdate) {
                FiguraMat4 mat = partToWorldMatrices(custom);
                part.savedPartToWorldMat.set(mat);
                mat.free();
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
                pos.free();
            }
            FiguraMod.popProfiler();
        }

        //render this
        FiguraMod.pushProfiler("pushVertices");
        if (!part.pushVerticesImmediate(this, remainingComplexity)) {
            customizationStack.pop();
            FiguraMod.popProfiler(2);
            return false;
        }

        //render extras
        FiguraMod.popPushProfiler("extras");
        if (thisPassedPredicate) {
            PartCustomization peek = customizationStack.peek();

            boolean renderPivot = shouldRenderPivots > 0 && (shouldRenderPivots % 2 == 0 || peek.visible);
            boolean renderTasks = peek.visible && allowRenderTasks && !part.renderTasks.isEmpty();
            boolean renderPivotParts = peek.visible && part.parentType.isPivot && allowPivotParts;

            if (renderPivot || renderTasks || renderPivotParts) {
                //fix pivots
                FiguraMod.pushProfiler("fixMatricesPivot");

                FiguraVec3 pivot = custom.getPivot();
                pivotOffsetter.setPos(pivot);
                pivotOffsetter.recalculate();
                customizationStack.push(pivotOffsetter);
                pivot.free();

                //render pivot indicators
                if (renderPivot) {
                    FiguraMod.popPushProfiler("renderPivotCube");
                    renderPivot(part);
                }

                //render tasks
                if (renderTasks) {
                    FiguraMod.popPushProfiler("renderTasks");
                    int light = peek.light;
                    int overlay = peek.overlay;
                    allowSkullRendering = false;
                    for (RenderTask task : part.renderTasks.values()) {
                        int neededComplexity = task.getComplexity();
                        if (neededComplexity > remainingComplexity[0])
                            continue;
                        FiguraMod.pushProfiler(task.getName());
                        if (task.render(customizationStack, bufferSource, light, overlay))
                            remainingComplexity[0] -= neededComplexity;
                        FiguraMod.popProfiler();
                    }
                    allowSkullRendering = true;
                }

                //render pivot parts
                if (renderPivotParts) {
                    FiguraMod.popPushProfiler("savePivotParts");
                    if (part.parentType.isPivot && allowPivotParts)
                        savePivotTransform(part.parentType);
                }

                customizationStack.pop();
                FiguraMod.popProfiler();
            }
        }

        //render children
        FiguraMod.popPushProfiler("children");
        for (FiguraModelPart child : part.children)
            if (!renderPart(child, remainingComplexity, thisPassedPredicate)) {
                customizationStack.pop();
                FiguraMod.popProfiler(2);
                return false;
            }

        //reset the parent
        FiguraMod.popPushProfiler("removeVanillaTransforms");
        part.resetVanillaTransforms();

        //pop
        customizationStack.pop();
        FiguraMod.popProfiler(2);

        return true;
    }

    protected void renderPivot(FiguraModelPart part) {
        boolean group = part.customization.partType == PartCustomization.PartType.GROUP;
        FiguraVec3 color = group ? ColorUtils.Colors.MAYA_BLUE.vec : ColorUtils.Colors.FRAN_PINK.vec;
        double boxSize = group ? 1 / 16d : 1 / 32d;
        boxSize /= Math.max(Math.cbrt(part.savedPartToWorldMat.det()), 0.02);

        PoseStack stack = customizationStack.peek().copyIntoGlobalPoseStack();

        LevelRenderer.renderLineBox(stack, bufferSource.getBuffer(RenderType.LINES),
                -boxSize, -boxSize, -boxSize,
                boxSize, boxSize, boxSize,
                (float) color.x, (float) color.y, (float) color.z, 1f);
    }

    protected void savePivotTransform(ParentType parentType) {
        FiguraMat4 currentPosMat = customizationStack.peek().getPositionMatrix();
        FiguraMat3 currentNormalMat = customizationStack.peek().getNormalMatrix();
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

        piv.free();
        translation.free();

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
        part.applyExtraTransforms(customizationStack.peek().positionMatrix);

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
            mat.free();
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

    public void pushFaces(int texIndex, int faceCount, int[] remainingComplexity) {
        buffers.get(texIndex).pushVertices(this, faceCount, remainingComplexity);
    }

    public void advanceFaces(int texIndex, int faceCount) {
        buffers.get(texIndex).advanceBuffers(faceCount);
    }
}
