package com.unlikepaladin.pfm.blocks.blockentities.fabric;

import com.unlikepaladin.pfm.blocks.blockentities.StovetopBlockEntity;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class StovetopBlockEntityImpl extends StovetopBlockEntity implements BlockEntityClientSerializable {
    public StovetopBlockEntityImpl() {
        super();
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        fromTag(this.getCachedState(), tag);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        return writeNbt(tag);
    }
}
