package com.unlikepaladin.pfm.blocks.models.classicTable.forge;

import com.unlikepaladin.pfm.blocks.ClassicTableBlock;
import com.unlikepaladin.pfm.blocks.models.AbstractBakedModel;
import com.unlikepaladin.pfm.blocks.models.forge.ModelBitSetProperty;
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

import java.util.*;
import net.minecraft.util.math.random.Random;

public class ForgeClassicTableModel extends PFMForgeBakedModel {
    public ForgeClassicTableModel(ModelBakeSettings settings, List<BakedModel> parts) {
        super(settings, parts);
    }

    public static ModelProperty<ModelBitSetProperty> CONNECTIONS = new ModelProperty<>();

    @NotNull
    @Override
    public ModelData getModelData(@NotNull BlockRenderView world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData tileData) {
        if (state.getBlock() instanceof ClassicTableBlock) {
            ModelData.Builder builder = ModelData.builder();

            ModelData data = builder.build();
            data = super.getModelData(world, pos, state, data);

            ClassicTableBlock block = (ClassicTableBlock) state.getBlock();
            boolean north = block.canConnect(world.getBlockState(pos.north()));
            boolean east = block.canConnect(world.getBlockState(pos.east()));
            boolean west = block.canConnect(world.getBlockState(pos.west()));
            boolean south = block.canConnect(world.getBlockState(pos.south()));
            BitSet set = new BitSet();
            set.set(0, north);
            set.set(1, east);
            set.set(2, west);
            set.set(3, south);
            data = data.derive().with(CONNECTIONS, new ModelBitSetProperty(set)).build();
            return data;
        }
        return tileData;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull Random rand, @NotNull ModelData extraData, RenderLayer renderType) {
        if (state != null && state.getBlock() instanceof ClassicTableBlock && extraData.get(CONNECTIONS) != null && extraData.get(CONNECTIONS).connections != null) {
            BitSet set = extraData.get(CONNECTIONS).connections;
            List<BakedQuad> baseQuads = new ArrayList<>();
            List<BakedQuad> secondaryQuads = new ArrayList<>();
            boolean north = set.get(0);
            boolean east = set.get(1);
            boolean west = set.get(2);
            boolean south = set.get(3);
            baseQuads.addAll(getTemplateBakedModels().get(0).getQuads(state, side, rand, extraData, renderType));
            if (!north && !east) {
                secondaryQuads.addAll(getTemplateBakedModels().get(1).getQuads(state, side, rand, extraData, renderType));
            }
            if (!north && !west) {
                secondaryQuads.addAll(getTemplateBakedModels().get(2).getQuads(state, side, rand, extraData, renderType));
            }
            if (!south && !west) {
                secondaryQuads.addAll(getTemplateBakedModels().get(3).getQuads(state, side, rand, extraData, renderType));
            }
            if (!south && !east) {
                secondaryQuads.addAll(getTemplateBakedModels().get(4).getQuads(state, side, rand, extraData, renderType));
            }

            List<Sprite> spriteList = getSpriteList(state);
            List<BakedQuad> quads = getQuadsWithTexture(baseQuads, new SpriteData(spriteList.get(0)));
            quads.addAll(getQuadsWithTexture(secondaryQuads, new SpriteData(spriteList.get(1))));
            return quads;
        }
        return Collections.emptyList();
    }

    @Override
    public List<BakedQuad> getQuads(ItemStack stack, @Nullable BlockState state, @Nullable Direction face, Random random) {
        // base
        List<BakedQuad> baseQuads = new ArrayList<>(getTemplateBakedModels().get(0).getQuads(state, face, random));

        List<BakedQuad> secondaryQuads = new ArrayList<>();
        // legs
        secondaryQuads.addAll(getTemplateBakedModels().get(1).getQuads(state, face, random));
        secondaryQuads.addAll(getTemplateBakedModels().get(2).getQuads(state, face, random));
        secondaryQuads.addAll(getTemplateBakedModels().get(3).getQuads(state, face, random));
        secondaryQuads.addAll(getTemplateBakedModels().get(4).getQuads(state, face, random));

        List<Sprite> spriteList = getSpriteList(stack);
        List<BakedQuad> quads = getQuadsWithTexture(baseQuads, new SpriteData(spriteList.get(0)));
        quads.addAll(getQuadsWithTexture(secondaryQuads, new SpriteData(spriteList.get(1))));
        return quads;
    }
}