package com.unlikepaladin.pfm.blocks;

import com.unlikepaladin.pfm.blocks.blockentities.CounterOvenBlockEntity;
import com.unlikepaladin.pfm.data.FurnitureBlock;
import com.unlikepaladin.pfm.registry.BlockEntities;
import com.unlikepaladin.pfm.registry.Statistics;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.unlikepaladin.pfm.blocks.KitchenDrawerBlock.rotateShape;

public class KitchenCounterOvenBlock extends SmokerBlock implements DynamicRenderLayerInterface {
    private static final List<FurnitureBlock> WOOD_COUNTER_OVENS = new ArrayList<>();
    private static final List<FurnitureBlock> STONE_COUNTER_OVENS = new ArrayList<>();

    public KitchenCounterOvenBlock(Settings settings) {
        super(settings);
        if(AbstractSittableBlock.isWoodBased(this.getDefaultState()) && this.getClass().isAssignableFrom(KitchenCounterOvenBlock.class)){
            WOOD_COUNTER_OVENS.add(new FurnitureBlock(this, "kitchen_counter_oven"));
        }
        else if (this.getClass().isAssignableFrom(KitchenCounterOvenBlock.class)){
            STONE_COUNTER_OVENS.add(new FurnitureBlock(this, "kitchen_counter_oven"));
        }
        setDefaultState(this.getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(LIT, false).with(OPEN, false));
    }

    public static Stream<FurnitureBlock> streamWoodCounterOvens() {
        return WOOD_COUNTER_OVENS.stream();
    }
    public static Stream<FurnitureBlock> streamStoneCounterOvens() {
        return STONE_COUNTER_OVENS.stream();
    }

    public static final BooleanProperty OPEN = Properties.OPEN;

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return getFactory().create(pos, state);
    }

    @ExpectPlatform
    public static BlockEntityType.BlockEntityFactory<? extends CounterOvenBlockEntity> getFactory() {
        throw new AssertionError();
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(OPEN);
        super.appendProperties(builder);
    }
    public static boolean connectsVertical(Block block) {
        return block instanceof KitchenCounterBlock || block instanceof KitchenCounterOvenBlock;
    }

    public int getFlammability(BlockState state, BlockView world, BlockPos pos, Direction face) {
        if (AbstractSittableBlock.isWoodBased(state)) {
            return 20;
        }
        return 0;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(world, type, BlockEntities.KITCHEN_COUNTER_OVEN_BLOCK_ENTITY);
    }

    @Override
    public void openScreen(World world, BlockPos pos, PlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof CounterOvenBlockEntity) {
            player.openHandledScreen((NamedScreenHandlerFactory)blockEntity);
            player.incrementStat(Statistics.STOVE_OPENED);
        }
    }

    protected static final VoxelShape COUNTER_OVEN = VoxelShapes.union(createCuboidShape(0, 1, 0, 16, 14, 14),createCuboidShape(0, 0, 0, 16, 1, 12),createCuboidShape(0, 14, 0, 16, 16, 16),createCuboidShape(1.8, 11.2, 14.54, 14.3, 11.8, 15.14),createCuboidShape(2.5, 11.2, 13.07, 3.1, 11.8, 14.57),createCuboidShape(12.6, 11.2, 13.07, 13.2, 11.8, 14.57),createCuboidShape(1.8, 1.9, 14.44, 14.3, 2.5, 15.04),createCuboidShape(2.5, 1.9, 12.47, 3.1, 2.5, 14.47),createCuboidShape(12.6, 1.9, 12.47, 13.2, 2.5, 14.47));
    protected static final VoxelShape COUNTER_OVEN_SOUTH = rotateShape(Direction.NORTH, Direction.SOUTH, COUNTER_OVEN);
    protected static final VoxelShape COUNTER_OVEN_EAST = rotateShape(Direction.NORTH, Direction.EAST, COUNTER_OVEN);
    protected static final VoxelShape COUNTER_OVEN_WEST = rotateShape(Direction.NORTH, Direction.WEST, COUNTER_OVEN);
    protected static final VoxelShape COUNTER_OVEN_BOTTOM = VoxelShapes.union(createCuboidShape(0, 1, 0,16, 16, 13),createCuboidShape(0, 0, 0,16, 1, 12),createCuboidShape(0, 1, 13,16, 15, 14),createCuboidShape(1.8, 11.2, 14.54,14.3, 11.8, 15.14),createCuboidShape(2.5, 11.2, 13.07,3.1, 11.8, 14.57),createCuboidShape(12.6, 11.2, 13.07,13.2, 11.8, 14.57),createCuboidShape(1.8, 1.9, 14.44,14.3, 2.5, 15.04),createCuboidShape(2.5, 1.9, 12.47,3.1, 2.5, 14.47),createCuboidShape(12.6, 1.9, 12.47,13.2, 2.5, 14.47));
    protected static final VoxelShape COUNTER_OVEN_MIDDLE = VoxelShapes.union(createCuboidShape(0, 0, 0,16, 16, 13),createCuboidShape(0, 1, 13,16, 15, 14),createCuboidShape(1.8, 11.2, 14.54,14.3, 11.8, 15.14),createCuboidShape(2.5, 11.2, 13.07,3.1, 11.8, 14.57),createCuboidShape(12.6, 11.2, 13.07,13.2, 11.8, 14.57),createCuboidShape(1.8, 1.9, 14.44,14.3, 2.5, 15.04),createCuboidShape(2.5, 1.9, 12.47,3.1, 2.5, 14.47),createCuboidShape(12.6, 1.9, 12.47,13.2, 2.5, 14.47));
    protected static final VoxelShape COUNTER_OVEN_BOTTOM_SOUTH = rotateShape(Direction.NORTH, Direction.SOUTH, COUNTER_OVEN_BOTTOM);
    protected static final VoxelShape COUNTER_OVEN_BOTTOM_EAST = rotateShape(Direction.NORTH, Direction.EAST, COUNTER_OVEN_BOTTOM);
    protected static final VoxelShape COUNTER_OVEN_BOTTOM_WEST = rotateShape(Direction.NORTH, Direction.WEST, COUNTER_OVEN_BOTTOM);
    protected static final VoxelShape COUNTER_OVEN_MIDDLE_SOUTH = rotateShape(Direction.NORTH, Direction.SOUTH, COUNTER_OVEN_MIDDLE);
    protected static final VoxelShape COUNTER_OVEN_MIDDLE_EAST = rotateShape(Direction.NORTH, Direction.EAST, COUNTER_OVEN_MIDDLE);
    protected static final VoxelShape COUNTER_OVEN_MIDDLE_WEST = rotateShape(Direction.NORTH, Direction.WEST, COUNTER_OVEN_MIDDLE);
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        Direction dir = state.get(FACING);
        boolean up = connectsVertical(view.getBlockState(pos.up()).getBlock());
        boolean down = connectsVertical(view.getBlockState(pos.down()).getBlock());
        if(up){
            return switch (dir){
                case NORTH -> COUNTER_OVEN_BOTTOM_SOUTH;
                case SOUTH -> COUNTER_OVEN_BOTTOM;
                case EAST -> COUNTER_OVEN_BOTTOM_WEST;
                default -> COUNTER_OVEN_BOTTOM_EAST;
            };
        }
        else if(down) {
            return switch (dir){
                case NORTH -> COUNTER_OVEN_MIDDLE_SOUTH;
                case SOUTH -> COUNTER_OVEN_MIDDLE;
                case EAST -> COUNTER_OVEN_MIDDLE_WEST;
                default -> COUNTER_OVEN_MIDDLE_EAST;
            };
        }
        else {
        return switch (dir) {
            case WEST -> COUNTER_OVEN_EAST;
            case NORTH -> COUNTER_OVEN_SOUTH;
            case SOUTH -> COUNTER_OVEN;
            default -> COUNTER_OVEN_WEST;
            };
        }
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public RenderLayer getCustomRenderLayer() {
        return RenderLayer.getTranslucent();
    }
}
