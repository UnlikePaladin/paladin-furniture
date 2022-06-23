package com.unlikepaladin.pfm.blocks;

import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SimpleLight extends PowerableBlock{
    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;
    private static final List<SimpleLight> SIMPLE_LIGHTS = new ArrayList<>();
    public SimpleLight(Settings settings) {
        super(settings);
        setDefaultState(this.getStateManager().getDefaultState().with(LIT,  false).with(POWERLOCKED, false));
        SIMPLE_LIGHTS.add(this);
    }
    @Override
    public void setPowered(World world, BlockPos lightPos, boolean powered) {
        BlockState state = world.getBlockState(lightPos);
        world.setBlockState(lightPos, state.with(LIT, powered).with(POWERLOCKED, powered));
    }

    public static Stream<SimpleLight> streamSimpleLights() {
        return SIMPLE_LIGHTS.stream();
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        boolean powered = ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos());
        return this.getDefaultState().with(LIT, powered);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(LIT) && !world.isReceivingRedstonePower(pos) && !state.get(POWERLOCKED)) {
            world.setBlockState(pos, state.cycle(LIT), Block.NOTIFY_ALL);
        }
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT);
        builder.add(POWERLOCKED);
    }
    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction direction = Direction.UP;
        return Block.sideCoversSmallSquare(world, pos.offset(direction), direction.getOpposite());
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (!state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return state;
    }


    private static final VoxelShape SIMPLE_LIGHT = VoxelShapes.union(createCuboidShape(4.5, 13.5, 4.5,11.5, 14.5, 11.5),createCuboidShape(3, 14.5, 3,13, 16, 13));
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SIMPLE_LIGHT;
    }
}
