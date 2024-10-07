package com.unlikepaladin.pfm.blocks.models.bed;

import com.mojang.datafixers.util.Pair;
import com.unlikepaladin.pfm.PaladinFurnitureMod;
import com.unlikepaladin.pfm.blocks.ShowerTowelBlock;
import com.unlikepaladin.pfm.data.materials.WoodVariant;
import com.unlikepaladin.pfm.data.materials.WoodVariantRegistry;
import com.unlikepaladin.pfm.runtime.PFMBakedModelContainer;
import com.unlikepaladin.pfm.runtime.PFMRuntimeResources;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Material;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;


@Environment(EnvType.CLIENT)
public class UnbakedBedModel implements UnbakedModel {
    public static final Identifier[] BED_MODEL_PARTS_BASE = new Identifier[] {
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/simple_bed/mattresses/red_foot_mattress"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/simple_bed/mattresses/red_head_mattress"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/simple_bed/template/foot/simple_bed_foot"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/simple_bed/template/head/simple_bed_head"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/simple_bed/template/foot/simple_bed_foot_right"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/simple_bed/template/foot/simple_bed_foot_left"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/simple_bed/template/head/simple_bed_head_right"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/simple_bed/template/head/simple_bed_head_left"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/simple_bed/template/bunk/foot/simple_bed_foot_right"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/simple_bed/template/bunk/foot/simple_bed_foot_left"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/simple_bed/template/bunk/head/simple_bed_head"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/simple_bed/template/full/simple_bed"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/classic_bed/mattresses/red_foot_mattress"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/classic_bed/mattresses/red_head_mattress"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/classic_bed/template/foot/classic_bed_foot"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/classic_bed/template/head/classic_bed_head"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/classic_bed/template/foot/classic_bed_foot_right"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/classic_bed/template/foot/classic_bed_foot_left"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/classic_bed/template/head/classic_bed_head_right"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/classic_bed/template/head/classic_bed_head_left"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/classic_bed/template/bunk/foot/classic_bed_foot_right"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/classic_bed/template/bunk/foot/classic_bed_foot_left"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/simple_bed/template/bunk/head/simple_bed_head"),
            new Identifier(PaladinFurnitureMod.MOD_ID, "block/classic_bed/template/full/classic_bed"),
    };

    private static final Identifier PARENT = new Identifier("block/block");
    public static final Identifier BED_MODEL_ID = new Identifier(PaladinFurnitureMod.MOD_ID, "block/simple_bed");
    public static final List<Identifier> BED_MODEL_IDS = new ArrayList<>() {
        {
            for(WoodVariant variant : WoodVariantRegistry.getVariants()){
                int i = 0;
                for (DyeColor dyeColor : DyeColor.values()) {
                    if (i > 15)
                        break;
                    add(new Identifier(PaladinFurnitureMod.MOD_ID, "item/" + variant.asString() + "_" + dyeColor.getName() + "_simple_bed"));
                    i++;
                }
            }
            for(WoodVariant variant : WoodVariantRegistry.getVariants()){
                int i = 0;
                for (DyeColor dyeColor : DyeColor.values()) {
                    if (i > 15)
                        break;
                    add(new Identifier(PaladinFurnitureMod.MOD_ID, "item/" + variant.asString() + "_" + dyeColor.getName() + "_classic_bed"));
                    i++;
                }
            }
            add(BED_MODEL_ID);
        }
    };

    @Override
    public Collection<Identifier> getModelDependencies() {
        return List.of(PARENT);
    }
    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        if (PFMRuntimeResources.modelCacheMap.containsKey(BED_MODEL_ID) && PFMRuntimeResources.modelCacheMap.get(BED_MODEL_ID).getCachedModelParts().containsKey(rotationContainer))
            return getBakedModel(BED_MODEL_ID, rotationContainer, PFMRuntimeResources.modelCacheMap.get(BED_MODEL_ID).getCachedModelParts().get(rotationContainer));

        if (!PFMRuntimeResources.modelCacheMap.containsKey(BED_MODEL_ID))
            PFMRuntimeResources.modelCacheMap.put(BED_MODEL_ID, new PFMBakedModelContainer());

        List<BakedModel> bakedModelList = new ArrayList<>();
        for (Identifier modelPart : BED_MODEL_PARTS_BASE) {
            bakedModelList.add(loader.bake(modelPart, rotationContainer));
        }

        PFMRuntimeResources.modelCacheMap.get(BED_MODEL_ID).getCachedModelParts().put(rotationContainer, bakedModelList);
        return getBakedModel(BED_MODEL_ID, rotationContainer, bakedModelList);
    }

    @ExpectPlatform
    public static BakedModel getBakedModel(Identifier model, ModelBakeSettings settings, List<BakedModel> modelParts) {
        throw new RuntimeException("Method wasn't replaced correctly");
    }
}