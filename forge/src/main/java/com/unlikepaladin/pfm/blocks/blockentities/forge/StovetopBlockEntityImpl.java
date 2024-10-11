package com.unlikepaladin.pfm.blocks.blockentities.forge;

import com.unlikepaladin.pfm.blocks.blockentities.StoveBlockEntity;
import com.unlikepaladin.pfm.blocks.blockentities.StovetopBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventories;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StovetopBlockEntityImpl extends StovetopBlockEntity {
    public StovetopBlockEntityImpl(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbt = this.saveInitialChunkData(new NbtCompound(), registryLookup);
        Inventories.writeNbt(nbt, this.itemsBeingCooked, true, registryLookup);
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return  BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public void handleUpdateTag(NbtCompound tag, RegistryWrapper.WrapperLookup holders) {
        this.readNbt(tag, holders);
    }

    @Override
    public void onDataPacket(ClientConnection connection, BlockEntityUpdateS2CPacket pkt, RegistryWrapper.WrapperLookup lookup) {
        super.onDataPacket(connection, pkt, lookup);
        this.itemsBeingCooked.clear();
        Inventories.readNbt(pkt.getNbt(), this.itemsBeingCooked, lookup);
    }

    public static BlockEntityType.BlockEntityFactory<? extends StovetopBlockEntity> getFactory() {
        return StovetopBlockEntityImpl::new;
    }
}
