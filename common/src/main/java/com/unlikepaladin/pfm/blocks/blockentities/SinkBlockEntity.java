package com.unlikepaladin.pfm.blocks.blockentities;

import com.unlikepaladin.pfm.blocks.BasicToilet;
import com.unlikepaladin.pfm.blocks.KitchenSink;
import com.unlikepaladin.pfm.blocks.Microwave;
import com.unlikepaladin.pfm.registry.BlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class SinkBlockEntity extends BlockEntity implements Tickable {
    public SinkBlockEntity() {
        super(BlockEntities.SINK_BLOCK_ENTITY);
    }
    private int sinkTimer = 0;
    private boolean isFilling = false;
    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return super.toInitialChunkDataNbt();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("sinkTimer", sinkTimer);
        nbt.putBoolean("isFilling", isFilling);
        return nbt;
    }

    @Override
    public void fromTag(BlockState state, NbtCompound nbt) {
        sinkTimer = nbt.getInt("sinkTimer");
        isFilling = nbt.getBoolean("isFilling");
        super.fromTag(state, nbt);
    }

    public void setSinkTimer(int sinkTimer) {
        this.sinkTimer = sinkTimer;
    }

    public void setFilling(boolean isFilling) {
        if (isFilling){
            world.playSound(null, pos, SoundEvents.BLOCK_WATER_AMBIENT, SoundCategory.BLOCKS, 0.7f, 1.0f);
        }
        this.isFilling = isFilling;
    }

    public static void tick(World world, BlockPos pos, BlockState state, SinkBlockEntity blockEntity) {
        if (blockEntity.isFilling) {
            if (blockEntity.sinkTimer >= 30) {
                blockEntity.setSinkTimer(0);
                blockEntity.setFilling(false);
            } else {
                if (world.isClient) {
                    KitchenSink.spawnParticles(blockEntity.getCachedState().get(Properties.HORIZONTAL_FACING), blockEntity.world, blockEntity.getPos());
                }
                blockEntity.sinkTimer++;
            }
        }
    }

    @Override
    public void tick() {
        if (this.isFilling) {
            if (this.sinkTimer >= 30) {
                this.setSinkTimer(0);
                this.setFilling(false);
            } else {
                if (world.isClient) {
                    KitchenSink.spawnParticles(this.getCachedState().get(Properties.HORIZONTAL_FACING), this.world, this.getPos());
                }
                this.sinkTimer++;
            }
        }
    }
}
