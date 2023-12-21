package com.unlikepaladin.pfm.blocks.models.kitchenCabinet.forge;

import com.unlikepaladin.pfm.blocks.KitchenCabinetBlock;
import com.unlikepaladin.pfm.blocks.KitchenWallDrawerBlock;
import com.unlikepaladin.pfm.blocks.models.AbstractBakedModel;
import com.unlikepaladin.pfm.blocks.models.forge.ModelBitSetProperty;
import com.unlikepaladin.pfm.blocks.models.forge.PFMForgeBakedModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.texture.Sprite;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ForgeKitchenCabinetModel extends PFMForgeBakedModel {
    public ForgeKitchenCabinetModel(Sprite frame, ModelBakeSettings settings, Map<String, BakedModel> bakedModels, List<String> MODEL_PARTS) {
        super(settings, bakedModels.values().stream().toList());
        this.modelParts = MODEL_PARTS;
    }
    private final List<String> modelParts;
    public static ModelProperty<ModelBitSetProperty> CONNECTIONS = new ModelProperty<>();
    public static ModelProperty<BlockState> NEIGHBOR_OPPOSITE = new ModelProperty<>();
    public static ModelProperty<BlockState> NEIGHBOR_FACING = new ModelProperty<>();

    @NotNull
    @Override
    public IModelData getModelData(@NotNull BlockRenderView world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull IModelData tileData) {
        ModelDataMap.Builder builder = new ModelDataMap.Builder();
        if (state.getBlock() instanceof KitchenCabinetBlock) {
            KitchenCabinetBlock block = (KitchenCabinetBlock) state.getBlock();
            Direction direction = state.get(KitchenCabinetBlock.FACING);
            BlockState neighborStateOpposite = world.getBlockState(pos.offset(direction.getOpposite()));
            Direction direction3;
            Direction direction2;
            boolean innerCorner = block.isCabinet(neighborStateOpposite) && (direction3 = neighborStateOpposite.get(KitchenCabinetBlock.FACING)).getAxis() != state.get(KitchenCabinetBlock.FACING).getAxis() && block.isDifferentOrientation(state, world, pos, direction3);
            BlockState blockState = world.getBlockState(pos.offset(direction));
            boolean isNeighborStateOppositeFacingDifferentDirection;
            if (blockState.contains(Properties.HORIZONTAL_FACING)) {
                direction2 = blockState.get(Properties.HORIZONTAL_FACING);
                isNeighborStateOppositeFacingDifferentDirection = block.isDifferentOrientation(state, world, pos, direction2.getOpposite());
            } else {
                isNeighborStateOppositeFacingDifferentDirection = false;
            }
            BitSet set = new BitSet();
            set.set(0, innerCorner);
            set.set(1, isNeighborStateOppositeFacingDifferentDirection);
            builder.withInitial(CONNECTIONS, new ModelBitSetProperty(set)).withInitial(NEIGHBOR_OPPOSITE, neighborStateOpposite).withInitial(NEIGHBOR_FACING, blockState);
        }
        return builder.build();
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull Random rand, @NotNull IModelData extraData) {
        if (state != null && state.getBlock() instanceof KitchenCabinetBlock && extraData.getData(CONNECTIONS) != null && extraData.getData(CONNECTIONS).connections != null) {
            BitSet set = extraData.getData(CONNECTIONS).connections;
            KitchenCabinetBlock block = (KitchenCabinetBlock) state.getBlock();
            Direction direction = state.get(KitchenCabinetBlock.FACING);
            BlockState neighborStateOpposite = extraData.getData(NEIGHBOR_OPPOSITE);

            Direction direction3 = null;
            if (neighborStateOpposite.contains(Properties.HORIZONTAL_FACING)) {
                direction3 = neighborStateOpposite.get(Properties.HORIZONTAL_FACING);
            }
            Direction direction2;
            BlockState blockState = extraData.getData(NEIGHBOR_FACING);
            int openOffset = state.get(KitchenWallDrawerBlock.OPEN) ? 5 : 0;
            boolean innerCorner = set.get(0);
            boolean isNeighborStateOppositeFacingDifferentDirection = set.get(1);
            if (block.isCabinet(blockState) && (direction2 = blockState.get(KitchenCabinetBlock.FACING)).getAxis() != state.get(KitchenCabinetBlock.FACING).getAxis() && isNeighborStateOppositeFacingDifferentDirection) {
                if (direction2 == direction.rotateYCounterclockwise()) {
                    return getTemplateBakedModels().get(3 + openOffset).getQuads(state, side, rand, extraData);
                }
                else {
                    return getTemplateBakedModels().get(4 + openOffset).getQuads(state, side, rand, extraData);
                }
            }
            else if (innerCorner) {
                if (direction3 == direction.rotateYCounterclockwise()) {
                    return getTemplateBakedModels().get(2 + openOffset).getQuads(state, side, rand, extraData);
                } else {
                    return getTemplateBakedModels().get(1 + openOffset).getQuads(state, side, rand, extraData);
                }
            } else {
                return getTemplateBakedModels().get(openOffset).getQuads(state, side, rand, extraData);
            }
        }
        return Collections.emptyList();
    }
}