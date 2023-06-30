package com.unlikepaladin.pfm.blocks.models.fridge;

import com.mojang.datafixers.util.Pair;
import com.unlikepaladin.pfm.PaladinFurnitureMod;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class UnbakedIronFridgeModel implements UnbakedModel {
    public static final List<String> FRIDGE_MODEL_PARTS_BASE = new ArrayList<>() {
        {
            add("block/iron_fridge/iron_fridge_single");
            add("block/iron_fridge/iron_fridge_top");
            add("block/iron_fridge/iron_fridge_middle");
            add("block/iron_fridge/iron_fridge_bottom");
            add("block/iron_fridge/iron_fridge");
            add("block/iron_fridge/iron_fridge_single_open");
            add("block/iron_fridge/iron_fridge_top_open");
            add("block/iron_fridge/iron_fridge_middle_open");
            add("block/iron_fridge/iron_fridge_bottom_open");
            add("block/iron_fridge/iron_fridge_open");
        }
    };

    public static final List<Identifier> ALL_MODEL_IDS = new ArrayList<>() {
        {
            for (String part : FRIDGE_MODEL_PARTS_BASE) {
                add(new Identifier(PaladinFurnitureMod.MOD_ID, part));
            }
        }
    };


    private static final Identifier PARENT = new Identifier("block/block");
    private final SpriteIdentifier frameTex;

    @Override
    public Collection<Identifier> getModelDependencies() {
        return List.of(PARENT);
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return List.of(frameTex);
    }

    public static final List<Identifier> IRON_FRIDGE_MODEL_IDS = new ArrayList<>() { {
        add(new Identifier(PaladinFurnitureMod.MOD_ID, "block/iron_fridge"));
    }};

    public UnbakedIronFridgeModel() {
        this.frameTex = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("minecraft", "block/iron_block"));
    }
    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        Map<String,BakedModel> bakedModels = new LinkedHashMap<>();
        for (String modelPart : FRIDGE_MODEL_PARTS_BASE) {
            bakedModels.put(modelPart, loader.bake(new Identifier(PaladinFurnitureMod.MOD_ID, modelPart), rotationContainer));
        }
        return getBakedModel(textureGetter.apply(frameTex), rotationContainer, bakedModels, FRIDGE_MODEL_PARTS_BASE);
    }

    @ExpectPlatform
    public static BakedModel getBakedModel(Sprite frame, ModelBakeSettings settings, Map<String,BakedModel> bakedModels, List<String> MODEL_PARTS) {
        throw new RuntimeException("Method wasn't replaced correctly");
    }
}
