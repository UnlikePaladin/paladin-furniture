package com.unlikepaladin.pfm.blocks.blockentities.forge;

import com.unlikepaladin.pfm.blocks.blockentities.TrashcanBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;

import java.util.function.Supplier;

public class TrashcanBlockEntityImpl extends TrashcanBlockEntity {
    public TrashcanBlockEntityImpl(BlockEntityType<? extends TrashcanBlockEntity> trashcanBlockEntity) {
        super(trashcanBlockEntity);
    }

    public TrashcanBlockEntityImpl() {
        super();
    }


    @Nullable
    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return new BlockEntityUpdateS2CPacket(this.pos, 13, this.toInitialChunkDataNbt());
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = super.toInitialChunkDataNbt();
        Inventories.writeNbt(nbt, this.inventory);
        return nbt;
    }

    @Override
    public void handleUpdateTag(BlockState state, NbtCompound tag) {
        this.fromTag(state,tag);
    }

    @Override
    public void onDataPacket(ClientConnection net, BlockEntityUpdateS2CPacket pkt) {
        super.onDataPacket(net, pkt);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.readNbt(pkt.getNbt(), this.inventory);
    }

    public static Supplier<? extends TrashcanBlockEntity> getFactory() {
        return TrashcanBlockEntityImpl::new;
    }

}
