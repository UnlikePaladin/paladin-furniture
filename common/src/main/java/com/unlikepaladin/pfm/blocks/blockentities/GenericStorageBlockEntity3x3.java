package com.unlikepaladin.pfm.blocks.blockentities;

import com.unlikepaladin.pfm.blocks.ClassicNightstand;
import com.unlikepaladin.pfm.blocks.KitchenCabinet;
import com.unlikepaladin.pfm.blocks.KitchenDrawer;
import com.unlikepaladin.pfm.blocks.KitchenWallDrawerSmall;
import com.unlikepaladin.pfm.registry.BlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class GenericStorageBlockEntity3x3 extends LootableContainerBlockEntity {
    public GenericStorageBlockEntity3x3() {
        super(BlockEntities.KITCHEN_DRAWER_SMALL_BLOCK_ENTITY);
    }
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(9, ItemStack.EMPTY);

        protected void onContainerOpen(World world, BlockPos pos, BlockState state) {
            if (state.getBlock() instanceof KitchenDrawer || state.getBlock() instanceof KitchenCabinet || state.getBlock() instanceof ClassicNightstand){
                GenericStorageBlockEntity3x3.this.playSound(state, SoundEvents.BLOCK_BARREL_OPEN);
                GenericStorageBlockEntity3x3.this.setOpen(state, true);
            }
        }

        protected void onContainerClose(World world, BlockPos pos, BlockState state) {
            if (state.getBlock() instanceof KitchenDrawer || state.getBlock() instanceof KitchenCabinet || state.getBlock() instanceof ClassicNightstand) {
                GenericStorageBlockEntity3x3.this.playSound(state, SoundEvents.BLOCK_BARREL_CLOSE);
                GenericStorageBlockEntity3x3.this.setOpen(state, false);
            }
        }

    @Override
    public int size() {
        return 9;
    }

    @Override
    protected DefaultedList<ItemStack> getInvStackList() {
        return this.inventory;
    }

    @Override
    protected void setInvStackList(DefaultedList<ItemStack> list) {
        this.inventory = list;
    }

    @Override
    public void onOpen(PlayerEntity player) {
        if (!this.removed && !player.isSpectator()) {
            this.onContainerOpen( this.getWorld(), this.getPos(), this.getCachedState());
        }
    }

    @Override
    public void onClose(PlayerEntity player) {
        if (!this.removed && !player.isSpectator()) {
            this.onContainerClose(this.getWorld(), this.getPos(), this.getCachedState());
        }
    }


    @Override
    public void fromTag(BlockState state, NbtCompound nbt) {
        super.fromTag(state, nbt);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.deserializeLootTable(nbt)) {
            Inventories.readNbt(nbt, this.inventory);
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (!this.serializeLootTable(nbt)) {
            Inventories.writeNbt(nbt, this.inventory);
        }
        return nbt;
    }

    protected Text getContainerName() {
        if (this.getCachedState().getBlock() instanceof KitchenWallDrawerSmall)
            return new TranslatableText("container.pfm.drawer_small");
        else
            return new TranslatableText("container.pfm.small_storage");
    }

    void setOpen(BlockState state, boolean open) {
        this.world.setBlockState(this.getPos(), state.with(Properties.OPEN, open), 3);
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new Generic3x3ContainerScreenHandler(syncId, playerInventory, this);
    }

    void playSound(BlockState state, SoundEvent soundEvent) {
        Vec3i vec3i = state.get(Properties.HORIZONTAL_FACING).getVector();
        double d = (double)this.pos.getX() + 0.5 + (double)vec3i.getX() / 2.0;
        double e = (double)this.pos.getY() + 0.5 + (double)vec3i.getY() / 2.0;
        double f = (double)this.pos.getZ() + 0.5 + (double)vec3i.getZ() / 2.0;
        this.world.playSound(null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.5f, this.world.random.nextFloat() * 0.1f + 0.9f);
    }
}

