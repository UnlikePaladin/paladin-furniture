package com.unlikepaladin.pfm.blocks;

import com.unlikepaladin.pfm.PaladinFurnitureMod;
import com.unlikepaladin.pfm.blocks.behavior.BathtubBehavior;
import com.unlikepaladin.pfm.entity.ChairEntity;
import com.unlikepaladin.pfm.registry.Entities;
import com.unlikepaladin.pfm.registry.Statistics;
import net.minecraft.block.*;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.unlikepaladin.pfm.blocks.SimpleStool.rotateShape;

public class BasicBathtub extends BedBlock {
    public static final IntProperty LEVEL_8 = IntProperty.of("level", 0, 8);
    private final Map<Item, BathtubBehavior> behaviorMap;

    public BasicBathtub(Settings settings, Map<Item, BathtubBehavior> map) {
        super(DyeColor.WHITE, settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH).with(LEVEL_8, 0).with(PART, BedPart.FOOT).with(OCCUPIED, false));
        this.behaviorMap = map;
        this.height = 0.05f;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(LEVEL_8);
        super.appendProperties(stateManager);
    }

    protected void onFireCollision(BlockState state, World world, BlockPos pos) {
        decrementFluidLevel(state, world, pos);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction direction = state.get(FACING);
        if (state.get(PART) == BedPart.HEAD) {
            direction = direction.getOpposite();
        }
        return world.getBlockState(pos.offset(direction)).isAir() || world.getBlockState(pos.offset(direction)).getBlock() == this;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (!world.isClient) {
            world.setBlockState(pos.offset(state.get(FACING)), this.getDefaultState().with(FACING, state.get(FACING)).with(PART, BedPart.HEAD), 3);
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onEntityLand(BlockView world, Entity entity) {
        entity.setVelocity(entity.getVelocity().multiply(1.0, 0.0, 1.0));
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockPos blockPos;
        BlockState blockState;
        BedPart bedPart;
        if (!world.isClient && player.isCreative() && (bedPart = state.get(PART)) == BedPart.FOOT && (blockState = world.getBlockState(blockPos = pos.offset(BasicBathtub.getDirectionTowardsOtherPart(bedPart, state.get(FACING))))).getBlock() == this && blockState.get(PART) == BedPart.HEAD) {
            world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 35);
            world.syncWorldEvent(player, 2001, blockPos, Block.getRawIdFromState(blockState));
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        int i = state.get(LEVEL_8);
        if (!world.isClient && entity.isOnFire() && i != 0) {
            entity.extinguish();
            this.onFireCollision(state, world, pos);
        }
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return state.get(LEVEL_8);
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
    public void rainTick(World world, BlockPos pos) {
        if (world.random.nextInt(20) != 1) {
            return;
        }
        float f = world.getBiome(pos).getTemperature(pos);
        if (f < 0.15f) {
            return;
        }
        BlockState blockState = world.getBlockState(pos);
        if (blockState.get(LEVEL_8) < 8) {
            world.setBlockState(pos, blockState.cycle(LEVEL_8), 2);
        }
    }

    public static void decrementFluidLevel(BlockState state, World world, BlockPos pos) {
        int i = state.get(LEVEL_8) - 1;
        if (i < 0) {
            i = 0;
        }
        world.setBlockState(pos, state.with(LEVEL_8, i));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction facing = PaladinFurnitureMod.getPFMConfig().doChairsFacePlayer() ? ctx.getPlayerFacing() : ctx.getPlayerFacing().getOpposite();
        return this.getDefaultState().with(Properties.HORIZONTAL_FACING, facing);
    }

    public float height;
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockPos sourcePos = pos.down().down();
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        if (state.get(LEVEL_8) < 8) {
            BlockState sourceState = world.getBlockState(sourcePos);
            if (sourceState.getFluidState().getFluid() == Fluids.WATER && !sourceState.getFluidState().isEmpty()) {
                if (sourceState.getProperties().contains(Properties.WATERLOGGED)) {
                    world.setBlockState(sourcePos, sourceState.with(Properties.WATERLOGGED, false));
                } else {
                    world.setBlockState(sourcePos, Blocks.AIR.getDefaultState());
                }
                BathtubBehavior.fillTub(world, pos, player, hand, player.getStackInHand(hand), state, SoundEvents.BLOCK_WATER_AMBIENT, false);
                return ActionResult.SUCCESS;
            }
        }
        ItemStack itemStack = player.getStackInHand(hand);
        BathtubBehavior bathtubBehavior = this.behaviorMap.get(itemStack.getItem());
        if (bathtubBehavior != null) {
            bathtubBehavior.interact(state, world, pos, player, hand, itemStack);
            return ActionResult.SUCCESS;
        }
        if (world.isNight() && world.getDimension().isBedWorking()) {
            super.onUse(state, world, pos, player, hand, hit);
            return ActionResult.SUCCESS;
        }
        return sit(state, world, pos, player, hand, hit);
    }

    public ActionResult sit(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            if (player.isSpectator() || player.isSneaking()) {
                return ActionResult.PASS;
            }
            double pz;
            double px;
            px = pos.getX() + 0.5;
            pz = pos.getZ() + 0.5;
            double py = pos.getY() + this.height;

            List<ChairEntity> active = world.getEntitiesByClass(ChairEntity.class, new Box(pos), Entity::hasPlayerRider);
            if (!active.isEmpty())
                return ActionResult.PASS;

            float yaw = state.get(FACING).getOpposite().asRotation();
            if (state.get(PART) == BedPart.FOOT) {
                yaw = state.get(FACING).asRotation();
            }
            ChairEntity entity = Entities.CHAIR.create(world);
            entity.refreshPositionAndAngles(px, py, pz, yaw, 0);
            entity.setNoGravity(true);
            entity.setSilent(true);
            entity.setInvisible(false);
            entity.setInvulnerable(true);
            entity.setAiDisabled(true);
            entity.setHeadYaw(yaw);
            entity.setBodyYaw(yaw);
            if (world.spawnEntity(entity)) {
                player.startRiding(entity, true);
                player.setHeadYaw(yaw);
                entity.setBodyYaw(yaw);
                entity.setHeadYaw(yaw);
                player.incrementStat(Statistics.USE_BATHTUB);
                return ActionResult.SUCCESS;
            }
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        BedPart tubPart = state.get(PART);
        if (direction == BasicBathtub.getDirectionTowardsOtherPart(tubPart, state.get(FACING))) {
            if (neighborState.isOf(this) && neighborState.get(PART) != tubPart) {
                return state.with(LEVEL_8, neighborState.get(LEVEL_8)).with(OCCUPIED, neighborState.get(OCCUPIED));
            }
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    private static Direction getDirectionTowardsOtherPart(BedPart part, Direction direction) {
        return part == BedPart.FOOT ? direction : direction.getOpposite();
    }

    private static final VoxelShape HEAD = VoxelShapes.combineAndSimplify(VoxelShapes.union(createCuboidShape(0, 0, 0,16, 11, 16),createCuboidShape(0.5, 11, 10,2, 12.5, 11.5),createCuboidShape(0.5, 11, 7.5,2, 14.5, 9),createCuboidShape(2, 13, 7.5,4.5, 14.5, 9),createCuboidShape(0.5, 11, 4,2, 12.5, 5.5)),createCuboidShape(3, 2, 3,16, 11, 13), BooleanBiFunction.ONLY_FIRST);
    private static final VoxelShape HEAD_NORTH = rotateShape(Direction.WEST, Direction.NORTH, HEAD);
    private static final VoxelShape HEAD_EAST = rotateShape(Direction.WEST, Direction.EAST, HEAD);
    private static final VoxelShape HEAD_SOUTH = rotateShape(Direction.WEST, Direction.SOUTH, HEAD);
    private static final VoxelShape FOOT = VoxelShapes.combineAndSimplify(createCuboidShape(0, 0, 0,16, 11, 16),createCuboidShape(0,2,3,13, 11, 13),BooleanBiFunction.ONLY_FIRST);
    private static final VoxelShape FOOT_NORTH = rotateShape(Direction.WEST, Direction.NORTH, FOOT);
    private static final VoxelShape FOOT_EAST = rotateShape(Direction.WEST, Direction.EAST, FOOT);
    private static final VoxelShape FOOT_SOUTH = rotateShape(Direction.WEST, Direction.SOUTH, FOOT);
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction facing = state.get(FACING);
        BedPart tubPart = state.get(PART);
        if (tubPart == BedPart.HEAD) {
            switch (facing) {
                case WEST: {
                    return HEAD;
                }
                case EAST: {
                    return HEAD_EAST;
                }
                case NORTH:
                {
                    return HEAD_NORTH;
                }
                case SOUTH:
                {
                    return HEAD_SOUTH;
                }
            }
        }
        else {
            switch (facing) {
                case WEST: {
                    return FOOT;
                }
                case EAST: {
                    return FOOT_EAST;
                }
                case NORTH:
                {
                    return FOOT_NORTH;
                }
                case SOUTH:
                {
                    return FOOT_SOUTH;
                }
            }
        }
        return HEAD;
    }
}
