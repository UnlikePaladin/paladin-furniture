package com.unlikepaladin.pfm.blocks.models.kitchenCounter;

import com.unlikepaladin.pfm.PaladinFurnitureMod;
import com.unlikepaladin.pfm.data.materials.*;
import com.unlikepaladin.pfm.runtime.PFMBakedModelContainer;
import com.unlikepaladin.pfm.runtime.PFMRuntimeResources;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class UnbakedKitchenCounterModel implements UnbakedModel {
    public static final Identifier[] COUNTER_MODEL_PARTS_BASE = new Identifier[] {
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/kitchen_counter/kitchen_counter"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/kitchen_counter/kitchen_counter_edge_left"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/kitchen_counter/kitchen_counter_edge_right"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/kitchen_counter/kitchen_counter_inner_corner_left"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/kitchen_counter/kitchen_counter_inner_corner_right"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/kitchen_counter/kitchen_counter_outer_corner_left"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/kitchen_counter/kitchen_counter_outer_corner_right")
    };


    private static final Identifier PARENT = new Identifier("block/block");
    public static final Identifier COUNTER_MODEL_ID = new Identifier(PaladinFurnitureMod.MOD_ID, "block/kitchen_counter");
    public static final List<Identifier> COUNTER_MODEL_IDS = new ArrayList<>() {
        {
            for(WoodVariant variant : WoodVariantRegistry.getVariants()){
                add(new Identifier(PaladinFurnitureMod.MOD_ID, "item/" + variant.asString() + "_kitchen_counter"));
                if (variant.hasStripped())
                    add(new Identifier(PaladinFurnitureMod.MOD_ID, "item/stripped_" + variant.asString() + "_kitchen_counter"));
            }
            for(StoneVariant variant : StoneVariantRegistry.getVariants()){
                if (variant.identifier.getPath().equals("quartz"))
                    continue;
                add(new Identifier(PaladinFurnitureMod.MOD_ID, "item/" + variant.asString() + "_kitchen_counter"));
            }
            for(ExtraCounterVariant variant : ExtraCounterVariant.values()){
                add(new Identifier(PaladinFurnitureMod.MOD_ID, "item/" + variant.asString() + "_kitchen_counter"));
            }
            add(COUNTER_MODEL_ID);
        }
    };

    @Override
    public Collection<Identifier> getModelDependencies() {
        return List.of(PARENT);
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<com.mojang.datafixers.util.Pair<String, String>> unresolvedTextureReferences) {
        return Collections.emptyList();
    }

    public static final Map<ModelBakeSettings, List<BakedModel>> CACHED_MODELS = new ConcurrentHashMap<>();
    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        if (PFMRuntimeResources.modelCacheMap.containsKey(COUNTER_MODEL_ID) && PFMRuntimeResources.modelCacheMap.get(COUNTER_MODEL_ID).getCachedModelParts().containsKey(rotationContainer))
            return getBakedModel(COUNTER_MODEL_ID, rotationContainer, PFMRuntimeResources.modelCacheMap.get(COUNTER_MODEL_ID).getCachedModelParts().get(rotationContainer));

        if (!PFMRuntimeResources.modelCacheMap.containsKey(COUNTER_MODEL_ID))
            PFMRuntimeResources.modelCacheMap.put(COUNTER_MODEL_ID, new PFMBakedModelContainer());

        List<BakedModel> bakedModelList = new ArrayList<>();
        for (Identifier modelPart : COUNTER_MODEL_PARTS_BASE) {
            bakedModelList.add(loader.bake(modelPart, rotationContainer));
        }

        PFMRuntimeResources.modelCacheMap.get(COUNTER_MODEL_ID).getCachedModelParts().put(rotationContainer, bakedModelList);
        return getBakedModel(COUNTER_MODEL_ID, rotationContainer, bakedModelList);
    }

    @ExpectPlatform
    public static BakedModel getBakedModel(Identifier modelId, ModelBakeSettings settings, List<BakedModel> modelParts) {
        throw new RuntimeException("Method wasn't replaced correctly");
    }
}