package com.unlikepaladin.pfm.blocks.models.logStool;

import com.mojang.datafixers.util.Pair;
import com.unlikepaladin.pfm.PaladinFurnitureMod;
import com.unlikepaladin.pfm.data.materials.*;
import com.unlikepaladin.pfm.runtime.PFMBakedModelContainer;
import com.unlikepaladin.pfm.runtime.PFMRuntimeResources;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.render.model.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class UnbakedLogStoolModel implements UnbakedModel {
    public static final Identifier[] LOG_STOOL_PARTS_BASE = new Identifier[] {
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/log_stool/log_stool"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/log_stool/log_stool_tucked")
    };

    public static final Identifier STOOL_MODEL_ID = new Identifier(PaladinFurnitureMod.MOD_ID, "block/log_stool");
    public static final List<Identifier> LOG_STOOL_MODEL_IDS = new ArrayList<>() {
        {
            for(WoodVariant variant : WoodVariantRegistry.getVariants()){
                String logType = variant.isNetherWood() ? "stem" : "log";
                add(new Identifier(PaladinFurnitureMod.MOD_ID, "item/" + variant.asString() + "_" + logType + "_stool"));
                if (variant.hasStripped())
                    add(new Identifier(PaladinFurnitureMod.MOD_ID, "item/stripped_" + variant.asString() + "_" + logType + "_stool"));
            }
            for(StoneVariant variant : StoneVariantRegistry.getVariants()){
                add(new Identifier(PaladinFurnitureMod.MOD_ID, "item/" + variant.asString() + "_log_stool"));
            }
            add(STOOL_MODEL_ID);
        }
    };

    private static final Identifier PARENT = new Identifier("block/block");
    public Collection<Identifier> getModelDependencies() {
        return List.of(PARENT);
    }

    @Override
    public void setParents(Function<Identifier, UnbakedModel> modelLoader) {

    }

    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public BakedModel bake(Baker loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        if (PFMRuntimeResources.modelCacheMap.containsKey(STOOL_MODEL_ID) && PFMRuntimeResources.modelCacheMap.get(STOOL_MODEL_ID).getCachedModelParts().containsKey(rotationContainer))
            return getBakedModel(STOOL_MODEL_ID, rotationContainer, PFMRuntimeResources.modelCacheMap.get(STOOL_MODEL_ID).getCachedModelParts().get(rotationContainer));

        if (!PFMRuntimeResources.modelCacheMap.containsKey(STOOL_MODEL_ID))
            PFMRuntimeResources.modelCacheMap.put(STOOL_MODEL_ID, new PFMBakedModelContainer());

        List<BakedModel> bakedModelList = new ArrayList<>();
        for (Identifier modelPart : LOG_STOOL_PARTS_BASE) {
            bakedModelList.add(loader.bake(modelPart, rotationContainer));
        }

        PFMRuntimeResources.modelCacheMap.get(STOOL_MODEL_ID).getCachedModelParts().put(rotationContainer, bakedModelList);
        return getBakedModel(STOOL_MODEL_ID, rotationContainer, bakedModelList);
    }

    @ExpectPlatform
    public static BakedModel getBakedModel(Identifier modelId, ModelBakeSettings settings, List<BakedModel> modelParts) {
        throw new RuntimeException("Method wasn't replaced correctly");
    }
}
