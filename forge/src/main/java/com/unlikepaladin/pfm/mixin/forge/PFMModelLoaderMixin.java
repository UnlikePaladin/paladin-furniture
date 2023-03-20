package com.unlikepaladin.pfm.mixin.forge;


import com.unlikepaladin.pfm.blocks.models.mirror.UnbakedMirrorModel;
import com.unlikepaladin.pfm.blocks.models.ModelHelper;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ModelLoader.class)
public abstract class PFMModelLoaderMixin {
    @Shadow
    @Final private Map<Identifier, UnbakedModel> unbakedModels;

    @Shadow @Final private Map<Identifier, UnbakedModel> modelsToBake;

    @Inject(method = "loadModel", at = @At("HEAD"), cancellable = true)
    private void pfm$addMirrorModel(Identifier resourceId, CallbackInfo ci) {
        if (ModelHelper.containsIdentifier(UnbakedMirrorModel.MIRROR_MODEL_IDS, resourceId)){
            UnbakedModel model =  new UnbakedMirrorModel(UnbakedMirrorModel.DEFAULT_REFLECT, UnbakedMirrorModel.DEFAULT_FRAME_TEXTURE, UnbakedMirrorModel.DEFAULT_GLASS);
            this.unbakedModels.put(resourceId, model);
            this.modelsToBake.put(resourceId, model);
            ci.cancel();
        }
    }
}