package com.unlikepaladin.pfm.blocks.models.modernDinnerTable.forge;

import com.unlikepaladin.pfm.blocks.ModernDinnerTableBlock;
import com.unlikepaladin.pfm.blocks.models.AbstractBakedModel;
import com.unlikepaladin.pfm.blocks.models.forge.ModelBitSetProperty;
import com.unlikepaladin.pfm.blocks.models.forge.PFMForgeBakedModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ForgeModernDinnerTableModel extends PFMForgeBakedModel {
    public ForgeModernDinnerTableModel(ModelBakeSettings settings, List<BakedModel> modelList) {
        super(settings, modelList);
    }

    public static ModelProperty<ModelBitSetProperty> CONNECTIONS = new ModelProperty<>();
    @Override
    public void appendProperties(ModelDataMap.Builder builder) {
        super.appendProperties(builder);
        builder.withProperty(CONNECTIONS);
    }

    @NotNull
    @Override
    public IModelData getModelData(@NotNull BlockRenderView world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull IModelData tileData) {
        if (state.getBlock() instanceof ModernDinnerTableBlock) {
            ModelDataMap.Builder builder = new ModelDataMap.Builder();
            appendProperties(builder);

            IModelData data = builder.build();
            super.getModelData(world, pos, state, data);

            ModernDinnerTableBlock block = (ModernDinnerTableBlock) state.getBlock();
            Direction.Axis dir = state.get(ModernDinnerTableBlock.AXIS);
            boolean left = block.isTable(world, pos, dir, -1);
            boolean right = block.isTable(world, pos, dir, 1);
            BitSet set = new BitSet();
            set.set(0, left);
            set.set(1, right);
            data.setData(CONNECTIONS, new ModelBitSetProperty(set));
            return data;
        }
        return tileData;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull Random rand, @NotNull IModelData extraData) {
        if (state != null && state.getBlock() instanceof ModernDinnerTableBlock && extraData.getData(CONNECTIONS) != null && extraData.getData(CONNECTIONS).connections != null) {
            List<BakedQuad> baseQuads = new ArrayList<>();
            List<BakedQuad> secondaryQuads = new ArrayList<>();

            BitSet set = extraData.getData(CONNECTIONS).connections;
            boolean left = set.get(0);
            boolean right = set.get(1);
            baseQuads.addAll(getTemplateBakedModels().get(0).getQuads(state, side, rand, extraData));
            if (left && right) {
                baseQuads.addAll(getTemplateBakedModels().get(4).getQuads(state, side, rand, extraData));
            }
            if (!left && right) {
                secondaryQuads.addAll(getTemplateBakedModels().get(1).getQuads(state, side, rand, extraData));
            }
            if (!right && left) {
                secondaryQuads.addAll(getTemplateBakedModels().get(2).getQuads(state, side, rand, extraData));
            }
            if (!right && !left) {
                secondaryQuads.addAll(getTemplateBakedModels().get(3).getQuads(state, side, rand, extraData));
            }
            List<Sprite> spriteList = getSpriteList(state);
            List<BakedQuad> quads = getQuadsWithTexture(baseQuads, spriteList.get(0));
            quads.addAll(getQuadsWithTexture(secondaryQuads, spriteList.get(1)));
            return quads;
        }
        return Collections.emptyList();
    }

    @Override
    public List<BakedQuad> getQuads(ItemStack stack, @Nullable BlockState state, @Nullable Direction face, Random random) {
        // base
        List<BakedQuad> baseQuads = new ArrayList<>(getTemplateBakedModels().get(0).getQuads(state, face, random));
        // legs
        List<BakedQuad> secondaryQuads = new ArrayList<>(getTemplateBakedModels().get(3).getQuads(state, face, random));

        List<Sprite> spriteList = getSpriteList(stack);
        List<BakedQuad> quads = getQuadsWithTexture(baseQuads, spriteList.get(0));
        quads.addAll(getQuadsWithTexture(secondaryQuads, spriteList.get(1)));
        return quads;
    }
}