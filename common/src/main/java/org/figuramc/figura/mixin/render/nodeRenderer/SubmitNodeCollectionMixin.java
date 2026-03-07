package org.figuramc.figura.mixin.render.nodeRenderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.feature.ModelPartFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.apache.commons.lang3.function.TriFunction;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.FlameSubmitExtension;
import org.figuramc.figura.model.rendering.nodeRenderer.FiguraSubmission;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.figuramc.figura.ducks.NodeCollectorExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(SubmitNodeCollection.class)
public class SubmitNodeCollectionMixin implements NodeCollectorExtension {
    @Unique
    List<FiguraSubmission> figuraSubmissions = new ArrayList<>();

    public <S extends EntityRenderState> void submitFiguraModel(Avatar avatar, S renderState, TriFunction<Avatar, S, MultiBufferSource, Void> renderer) {
        figuraSubmissions.add(new FiguraSubmission(avatar, renderState, (TriFunction<Avatar, EntityRenderState, MultiBufferSource, Void>) renderer));
    }

    @Override
    public List<FiguraSubmission> getFiguraSubmissions() {
        return figuraSubmissions;
    }

    @Inject(method = "submitModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$Storage;add(Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/SubmitNodeStorage$ModelSubmit;)V"))
    private <S> void figura$onSubmitModel(Model<? super S> model, S object, PoseStack poseStack, RenderType renderType, int i, int j, int k, @Nullable TextureAtlasSprite textureAtlasSprite, int l, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, CallbackInfo ci, @Local SubmitNodeStorage.ModelSubmit<S> modelSubmit) {
        FiguraSubmitCallBackExtension modelSubmissionExtension = (FiguraSubmitCallBackExtension) (Object) modelSubmit;
        FiguraSubmitCallBackExtension modelExtension = (FiguraSubmitCallBackExtension) model;
        for (var callback : modelExtension.figura$getPreRenderingCallbacks()) {
            modelSubmissionExtension.figura$addPreRenderingCallback(callback);
        }
        for (var callback : modelExtension.figura$getPostRenderingCallbacks()) {
            modelSubmissionExtension.figura$addPostRenderingCallback(callback);
        }
        modelSubmissionExtension.figura$setPreventAnimSetup(modelExtension.figura$getPreventAnimSetup());
        modelExtension.figura$setPreventAnimSetup(false);
        modelExtension.figura$getPreRenderingCallbacks().clear();
        modelExtension.figura$getPostRenderingCallbacks().clear();
    }

    @WrapOperation(method = "submitModelPart", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/ModelPartFeatureRenderer$Storage;add(Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/SubmitNodeStorage$ModelPartSubmit;)V"))
    private void figura$onSubmitModelPart(ModelPartFeatureRenderer.Storage instance, RenderType key, SubmitNodeStorage.ModelPartSubmit modelPartSubmit, Operation<Void> original) {
        FiguraSubmitCallBackExtension modelSubmissionExtension = (FiguraSubmitCallBackExtension) (Object) modelPartSubmit;
        FiguraSubmitCallBackExtension modelExtension = (FiguraSubmitCallBackExtension) (Object) modelPartSubmit.modelPart();
        for (var callback : modelExtension.figura$getPreRenderingCallbacks()) {
            modelSubmissionExtension.figura$addPreRenderingCallback(callback);
        }
        for (var callback : modelExtension.figura$getPostRenderingCallbacks()) {
            modelSubmissionExtension.figura$addPostRenderingCallback(callback);
        }
        modelSubmissionExtension.figura$setPreventAnimSetup(modelExtension.figura$getPreventAnimSetup());
        modelExtension.figura$setPreventAnimSetup(false);
        modelExtension.figura$getPreRenderingCallbacks().clear();
        modelExtension.figura$getPostRenderingCallbacks().clear();
        original.call(instance, key, modelPartSubmit);
    }

    @WrapOperation(method = "submitItem", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private <E> boolean figura$onSubmitModelPart(List<E> instance, E e, Operation<Boolean> original) {
        SubmitNodeStorage.ItemSubmit itemSubmit = (SubmitNodeStorage.ItemSubmit) e;

        FiguraSubmitCallBackExtension itemSubmissionExtension = (FiguraSubmitCallBackExtension) (Object) itemSubmit;

        FiguraSubmitCallBackExtension displayContextExtension = (FiguraSubmitCallBackExtension) (Object) itemSubmit.displayContext();
        for (var callback : displayContextExtension.figura$getPreRenderingCallbacks()) {
            itemSubmissionExtension.figura$addPreRenderingCallback(callback);
        }
        for (var callback : displayContextExtension.figura$getPostRenderingCallbacks()) {
            itemSubmissionExtension.figura$addPostRenderingCallback(callback);
        }
        itemSubmissionExtension.figura$setPreventAnimSetup(displayContextExtension.figura$getPreventAnimSetup());
        displayContextExtension.figura$setPreventAnimSetup(false);
        displayContextExtension.figura$getPreRenderingCallbacks().clear();
        displayContextExtension.figura$getPostRenderingCallbacks().clear();
        return original.call(instance, e);
    }

    @WrapOperation(method = "submitFlame", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private <E> boolean figura$onSubmitFlame(List<E> instance, E e, Operation<Boolean> original) {
        SubmitNodeStorage.FlameSubmit flameSubmit = (SubmitNodeStorage.FlameSubmit) e;

        FlameSubmitExtension itemSubmissionExtension = (FlameSubmitExtension) (Object) flameSubmit;
        Avatar avatar = AvatarManager.getAvatar(flameSubmit.entityRenderState());
        itemSubmissionExtension.figura$setAvatar(avatar);
        return original.call(instance, e);
    }

    @Inject(method = "clear", at = @At("HEAD"))
    private void figura$clearSubmissions(CallbackInfo ci) {
        figuraSubmissions.clear();
    }
}
