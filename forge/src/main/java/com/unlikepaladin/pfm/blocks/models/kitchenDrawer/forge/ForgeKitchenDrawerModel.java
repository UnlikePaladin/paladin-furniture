package com.unlikepaladin.pfm.blocks.models.kitchenDrawer.forge;

import com.unlikepaladin.pfm.blocks.KitchenDrawerBlock;
import com.unlikepaladin.pfm.blocks.models.AbstractBakedModel;
import com.unlikepaladin.pfm.blocks.models.forge.ModelBitSetProperty;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.texture.Sprite;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import net.minecraft.util.math.random.Random;

public class ForgeKitchenDrawerModel extends AbstractBakedModel {
    public ForgeKitchenDrawerModel(Sprite frame, ModelBakeSettings settings, Map<String, BakedModel> bakedModels, List<String> MODEL_PARTS) {
        super(frame, settings, bakedModels);
        this.modelParts = MODEL_PARTS;
    }
    private final List<String> modelParts;
    public static ModelProperty<ModelBitSetProperty> CONNECTIONS = new ModelProperty<>();
    public static ModelProperty<BlockState> NEIGHBOR_FACING = new ModelProperty<>();
    public static ModelProperty<BlockState> NEIGHBOR_OPPOSITE = new ModelProperty<>();

    @NotNull
    @Override
    public ModelData getModelData(@NotNull BlockRenderView world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData tileData) {
        ModelData.Builder builder = ModelData.builder();
        if (state.getBlock() instanceof KitchenDrawerBlock) {
            KitchenDrawerBlock block = (KitchenDrawerBlock) state.getBlock();
            Direction direction = state.get(KitchenDrawerBlock.FACING);
            boolean right = block.canConnect(world, pos, direction.rotateYCounterclockwise());
            boolean left = block.canConnect(world, pos, direction.rotateYClockwise());
            BlockState neighborStateFacing = world.getBlockState(pos.offset(direction));
            BlockState neighborStateOpposite = world.getBlockState(pos.offset(direction.getOpposite()));
            boolean isNeighborStateOppositeFacingDifferentDirection;
            if (neighborStateOpposite.contains(Properties.HORIZONTAL_FACING)) {
                Direction direction3;
                if (neighborStateOpposite.getBlock() instanceof AbstractFurnaceBlock) {
                    direction3 = neighborStateOpposite.get(Properties.HORIZONTAL_FACING).getOpposite();
                }
                else {
                    direction3 = neighborStateOpposite.get(Properties.HORIZONTAL_FACING);
                }
                isNeighborStateOppositeFacingDifferentDirection = block.isDifferentOrientation(state, world, pos, direction3);
            } else {
                isNeighborStateOppositeFacingDifferentDirection = false;
            }

            boolean isNeighborStateFacingDifferentDirection;
            if (neighborStateFacing.contains(Properties.HORIZONTAL_FACING)) {
                Direction direction2 = neighborStateFacing.get(Properties.HORIZONTAL_FACING);
                isNeighborStateFacingDifferentDirection = block.isDifferentOrientation(state, world, pos, direction2.getOpposite());
            } else {
                isNeighborStateFacingDifferentDirection = false;
            }
            BitSet set = new BitSet();
            set.set(0, left);
            set.set(1, right);
            set.set(2, isNeighborStateOppositeFacingDifferentDirection);
            set.set(3, isNeighborStateFacingDifferentDirection);
            builder.with(CONNECTIONS, new ModelBitSetProperty(set)).with(NEIGHBOR_FACING, neighborStateFacing).with(NEIGHBOR_OPPOSITE, neighborStateOpposite);
        }
        return builder.build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull Random rand, @NotNull ModelData extraData, RenderLayer renderLayer) {
        if (state != null && state.getBlock() instanceof KitchenDrawerBlock && extraData.get(CONNECTIONS) != null && extraData.get(CONNECTIONS).connections != null) {
            BitSet set = extraData.get(CONNECTIONS).connections;
            Direction direction = state.get(KitchenDrawerBlock.FACING);
            KitchenDrawerBlock block = (KitchenDrawerBlock) state.getBlock();
            boolean left = set.get(0);
            boolean right = set.get(1);
            boolean isNeighborStateOppositeFacingDifferentDirection =  set.get(2);
            boolean isNeighborStateFacingDifferentDirection = set.get(3);
            BlockState neighborStateFacing = extraData.get(NEIGHBOR_FACING);
            BlockState neighborStateOpposite = extraData.get(NEIGHBOR_OPPOSITE);
            int openOffset = state.get(KitchenDrawerBlock.OPEN) ? 7 : 0;
            if (block.canConnectToCounter(neighborStateFacing) && neighborStateFacing.contains(Properties.HORIZONTAL_FACING)) {
                Direction direction2 = neighborStateFacing.get(Properties.HORIZONTAL_FACING);
                if (direction2.getAxis() != state.get(Properties.HORIZONTAL_FACING).getAxis() && isNeighborStateFacingDifferentDirection) {
                    if (direction2 == direction.rotateYCounterclockwise()) {
                        return getBakedModels().get(modelParts.get(5 + openOffset)).getQuads(state, side, rand, extraData, renderLayer);
                    }
                    else {
                        return getBakedModels().get(modelParts.get(6 + openOffset)).getQuads(state, side, rand, extraData, renderLayer);
                    }
                } else {
                    return getMiddleQuads(state, side, rand, extraData, renderLayer, left, right, openOffset);
                }
            }
            else if (block.canConnectToCounter(neighborStateOpposite) && neighborStateOpposite.contains(Properties.HORIZONTAL_FACING)) {
                Direction direction3;
                if (neighborStateOpposite.getBlock() instanceof AbstractFurnaceBlock) {
                    direction3 = neighborStateOpposite.get(Properties.HORIZONTAL_FACING).getOpposite();
                }
                else {
                    direction3 = neighborStateOpposite.get(Properties.HORIZONTAL_FACING);
                }
                if (direction3.getAxis() != state.get(Properties.HORIZONTAL_FACING).getAxis() && isNeighborStateOppositeFacingDifferentDirection) {
                    if (direction3 == direction.rotateYCounterclockwise()) {
                        return getBakedModels().get(modelParts.get(4 + openOffset)).getQuads(state, side, rand, extraData, renderLayer);
                    } else {
                        return getBakedModels().get(modelParts.get(3 + openOffset)).getQuads(state, side, rand, extraData, renderLayer);
                    }
                } else {
                    return getMiddleQuads(state, side, rand, extraData, renderLayer, left, right, openOffset);
                }
            }
            else
                return getMiddleQuads(state, side, rand, extraData, renderLayer, left, right, openOffset);
        }
        return Collections.emptyList();
    }

    private List<BakedQuad> getMiddleQuads(BlockState state, Direction side, Random rand, ModelData extraData, RenderLayer renderLayer, boolean left, boolean right, int openOffset) {
        if (left && right) {
            return getBakedModels().get(modelParts.get(openOffset)).getQuads(state, side, rand, extraData, renderLayer);
        } else if (left) {
            return getBakedModels().get(modelParts.get(1 + openOffset)).getQuads(state, side, rand, extraData, renderLayer);
        } else if (right) {
            return getBakedModels().get(modelParts.get(2 + openOffset)).getQuads(state, side, rand, extraData, renderLayer);
        } else {
            return getBakedModels().get(modelParts.get(openOffset)).getQuads(state, side, rand, extraData, renderLayer);
        }
    }
}