package com.unlikepaladin.pfm.blocks;

import com.unlikepaladin.pfm.blocks.behavior.SinkBehavior;
import com.unlikepaladin.pfm.blocks.blockentities.SinkBlockEntity;
import com.unlikepaladin.pfm.data.FurnitureBlock;
import com.unlikepaladin.pfm.registry.BlockEntities;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static net.minecraft.block.Block.createCuboidShape;

public class KitchenSink extends CauldronBlock implements BlockEntityProvider {
    private final BlockState baseBlockState;
    private final Block baseBlock;
    public static final IntProperty LEVEL_4 = IntProperty.of("level", 0, 3);
    private final Map<Item, SinkBehavior> behaviorMap;
    private static final List<FurnitureBlock> WOOD_SINKS = new ArrayList<>();
    private static final List<FurnitureBlock> STONE_SINKS = new ArrayList<>();
    public KitchenSink(Settings settings, Map<Item, SinkBehavior> map) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH).with(LEVEL_4, 0));
        this.baseBlockState = this.getDefaultState();
        this.behaviorMap = map;
        this.baseBlock = baseBlockState.getBlock();
        if((material.equals(Material.WOOD) || material.equals(Material.NETHER_WOOD)) && this.getClass().isAssignableFrom(KitchenSink.class)){
            WOOD_SINKS.add(new FurnitureBlock(this, "kitchen_sink"));
        }
        else if (this.getClass().isAssignableFrom(KitchenSink.class)){
            STONE_SINKS.add(new FurnitureBlock(this, "kitchen_sink"));
        }
    }

    public static Stream<FurnitureBlock> streamWoodSinks() {
        return WOOD_SINKS.stream();
    }
    public static Stream<FurnitureBlock> streamStoneSinks() {
        return STONE_SINKS.stream();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.HORIZONTAL_FACING);
        stateManager.add(LEVEL_4);
    }
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing());
    }


    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockPos sourcePos = pos.down().down();
        ItemStack itemStack = player.getStackInHand(hand);
        SinkBehavior sinkBehavior = this.behaviorMap.get(itemStack.getItem());
        if (state.get(LEVEL_4) > 0 && sinkBehavior != null) {
            return sinkBehavior.interact(state, world, pos, player, hand, itemStack);
        }
        if (state.get(LEVEL_4) < 3) {
            BlockState sourceState = world.getBlockState(sourcePos);
            if (sourceState.getFluidState().getFluid() == Fluids.WATER && !sourceState.getFluidState().isEmpty()) {
                if (sourceState.getProperties().contains(Properties.WATERLOGGED)) {
                    world.setBlockState(sourcePos, sourceState.with(Properties.WATERLOGGED, false));
                }
                else {
                    world.setBlockState(sourcePos, Blocks.AIR.getDefaultState());
                }
                SinkBlockEntity blockEntity = (SinkBlockEntity) world.getBlockEntity(pos);
                if (blockEntity != null) {
                    blockEntity.setFilling(true);
                }
                world.setBlockState(pos, state.with(LEVEL_4, 3));
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    public static void spawnParticles(Direction facing, World world, BlockPos pos) {
        if (world.isClient) {
            int x = pos.getX(), y = pos.getY(), z = pos.getZ();
            if (facing == Direction.EAST) {
                world.addParticle(ParticleTypes.FALLING_WATER, x + 0.76, y + 1.14, z + 0.5, 0.0, 0.0, 0.0);
                world.addParticle(ParticleTypes.FALLING_WATER, x + 0.76, y + 1.14, z + 0.5, 0.0, 0.0, 0.0);
                world.addParticle(ParticleTypes.FALLING_WATER, x + 0.76, y + 1.14, z + 0.5, 0.0, 0.0, 0.0);
            }
            else if (facing == Direction.SOUTH){
                world.addParticle(ParticleTypes.FALLING_WATER, x + 0.5, y + 1.14, z + 0.76, 0.0, 0.0, 0.0);
                world.addParticle(ParticleTypes.FALLING_WATER, x + 0.5, y + 1.14, z + 0.76, 0.0, 0.0, 0.0);
                world.addParticle(ParticleTypes.FALLING_WATER, x + 0.5, y + 1.14, z + 0.76, 0.0, 0.0, 0.0);
            }
            else if (facing == Direction.NORTH){
                world.addParticle(ParticleTypes.FALLING_WATER, x + 0.5, y + 1.14, z + 0.24, 0.0, 0.0, 0.0);
                world.addParticle(ParticleTypes.FALLING_WATER, x + 0.5, y + 1.14, z + 0.24, 0.0, 0.0, 0.0);
                world.addParticle(ParticleTypes.FALLING_WATER, x + 0.5, y + 1.14, z + 0.24, 0.0, 0.0, 0.0);
            }
            else {
                world.addParticle(ParticleTypes.FALLING_WATER, x + 0.24, y + 1.14, z + 0.5, 0.0, 0.0, 0.0);
                world.addParticle(ParticleTypes.FALLING_WATER, x + 0.24, y + 1.14, z + 0.5, 0.0, 0.0, 0.0);
                world.addParticle(ParticleTypes.FALLING_WATER, x + 0.24, y + 1.14, z + 0.5, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    private static final VoxelShape FACING_NORTH = VoxelShapes.combineAndSimplify(VoxelShapes.union(VoxelShapes.fullCube(), createCuboidShape(1.0625, 11.3, 0.296,15.0625, 16.3, 12.296)),VoxelShapes.union(createCuboidShape(2, 11, 2.3,14, 16.3, 11.3),createCuboidShape(0, 0, 13,16, 14, 16),createCuboidShape(0, 0, 12,16, 1, 13)), BooleanBiFunction.ONLY_FIRST);
    private static final VoxelShape FACING_EAST = VoxelShapes.combineAndSimplify(VoxelShapes.union(VoxelShapes.fullCube(), createCuboidShape(3.704, 11.3, 1.0625,15.704, 16.3, 15.0625)), VoxelShapes.union(createCuboidShape(4.7, 11, 2,13.7, 16.3, 14),createCuboidShape(0,0,0,3, 14, 16),createCuboidShape(3, 0, 0,4, 1, 16)), BooleanBiFunction.ONLY_FIRST);
    private static final VoxelShape FACING_SOUTH = VoxelShapes.combineAndSimplify(VoxelShapes.union(VoxelShapes.fullCube(), createCuboidShape(0.9375, 11.3, 3.704,14.9375, 16.3, 15.704)), VoxelShapes.union(createCuboidShape(2, 11, 4.7,14, 16.3, 13.7),createCuboidShape(0, 0, 0,16, 14, 3),createCuboidShape(0, 0, 3,16, 1, 4)), BooleanBiFunction.ONLY_FIRST);
    private static final VoxelShape FACING_WEST = VoxelShapes.combineAndSimplify(VoxelShapes.union(VoxelShapes.fullCube(), createCuboidShape(0.296, 11.3, 0.9375,12.296, 16.3, 14.9375)), VoxelShapes.union(createCuboidShape(2.3, 11, 2,11.3, 16.3, 14),createCuboidShape(13, 0, 0,16, 14, 16),createCuboidShape(12, 0, 0,13, 1, 16)), BooleanBiFunction.ONLY_FIRST);
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction dir = state.get(Properties.HORIZONTAL_FACING);
        switch (dir) {
            case NORTH: return FACING_NORTH;
            case SOUTH: return FACING_SOUTH;
            case EAST: return FACING_EAST;
            default: return FACING_WEST;
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction dir = state.get(Properties.HORIZONTAL_FACING);
        switch (dir) {
            case NORTH: return FACING_NORTH;
            case SOUTH: return FACING_SOUTH;
            case EAST: return FACING_EAST;
            default: return FACING_WEST;
        }
    }


    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        int i = state.get(LEVEL_4);
        if (!world.isClient && entity.isOnFire() && i != 0) {
            entity.extinguish();
            this.onFireCollision(state, world, pos);
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);
    }

    protected void onFireCollision(BlockState state, World world, BlockPos pos) {
        KitchenSink.decrementFluidLevel(state, world, pos);
    }

    public static void decrementFluidLevel(BlockState state, World world, BlockPos pos) {
        int i = state.get(LEVEL_4) - 1;
        world.setBlockState(pos, state.with(LEVEL_4, i));
    }

    public boolean isFull(BlockState state) {
        return state.get(LEVEL_4) == 3;
    }

    protected double getFluidHeight(BlockState state) {
        return (6.0 + (double) state.get(LEVEL_4).intValue() * 3.0) / 16.0;
    }

    protected static boolean canFillWithPrecipitation(World world, Biome.Precipitation precipitation) {
        if (precipitation == Biome.Precipitation.RAIN) {
            return world.getRandom().nextFloat() < 0.05f;
        }
        if (precipitation == Biome.Precipitation.SNOW) {
            return world.getRandom().nextFloat() < 0.1f;
        }
        return false;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return state.get(LEVEL_4);
    }


    @Override
    public void rainTick(World world, BlockPos pos) {
        if (world.random.nextInt(20) != 1) {
            return;
        }
        float f = world.getBiome(pos).getTemperature(pos);
        if (f < 0.15f) {
            return;
        }
        BlockState blockState = world.getBlockState(pos);
        if (blockState.get(LEVEL_4) < 4) {
            world.setBlockState(pos, blockState.cycle(LEVEL_4), 2);
        }
    }


    public int getFlammability(BlockState state, BlockView world, BlockPos pos, Direction face) {
        if (state.getMaterial() == Material.WOOD || state.getMaterial() == Material.WOOL) {
            return 20;
        }
        return 0;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new SinkBlockEntity();
    }
}
