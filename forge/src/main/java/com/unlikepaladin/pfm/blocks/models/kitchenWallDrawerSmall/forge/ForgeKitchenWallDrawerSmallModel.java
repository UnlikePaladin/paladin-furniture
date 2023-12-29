package com.unlikepaladin.pfm.blocks.models.kitchenWallDrawerSmall.forge;

import com.unlikepaladin.pfm.blocks.KitchenSinkBlock;
import com.unlikepaladin.pfm.blocks.KitchenWallDrawerSmallBlock;
import com.unlikepaladin.pfm.blocks.models.ModelHelper;
import com.unlikepaladin.pfm.blocks.models.forge.PFMForgeBakedModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.math.random.Random;

public class ForgeKitchenWallDrawerSmallModel extends PFMForgeBakedModel {
    public ForgeKitchenWallDrawerSmallModel(ModelBakeSettings settings, List<BakedModel> modelParts) {
        super(settings, modelParts);
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull Random rand, @NotNull ModelData extraData, RenderLayer layer) {
        if (state != null && extraData.get(OPEN) != null) {
            int openOffset = extraData.get(OPEN) ? 1 : 0;
            List<BakedQuad> originalQuads = getTemplateBakedModels().get(openOffset).getQuads(state, side, rand, extraData, layer);
            List<Sprite> spriteList = getSpriteList(state);
            return getQuadsWithTexture(originalQuads, ModelHelper.getOakPlankLogSprites(), spriteList);
        }
        return Collections.emptyList();
    }

    public static ModelProperty<Boolean> OPEN = new ModelProperty<>();

    @NotNull
    @Override
    public ModelData getModelData(@NotNull BlockRenderView world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData tileData) {
        if (state.getBlock() instanceof KitchenWallDrawerSmallBlock) {
            ModelData.Builder builder = ModelData.builder();
            ModelData data = builder.build();
            data = super.getModelData(world, pos, state, data);
            data = data.derive().with(OPEN, state.get(KitchenWallDrawerSmallBlock.OPEN)).build();
            return data;
        }
        return tileData;
    }

    @Override
    public List<BakedQuad> getQuads(ItemStack stack, @Nullable BlockState state, @Nullable Direction face, Random random) {
        List<Sprite> spriteList = getSpriteList(stack);
        List<BakedQuad> originalQuads = getTemplateBakedModels().get(0).getQuads(state, face, random);
        return getQuadsWithTexture(originalQuads, ModelHelper.getOakPlankLogSprites(), spriteList);
    }
}
