package com.unlikepaladin.pfm.compat.fabric.sandwichable.blocks.blockentities;

import com.unlikepaladin.pfm.compat.fabric.sandwichable.blocks.PFMToaster;
import com.unlikepaladin.pfm.compat.fabric.sandwichable.PFMSandwichableRegistry;
import io.github.foundationgames.sandwichable.Sandwichable;
import io.github.foundationgames.sandwichable.blocks.ToasterBlock;
import io.github.foundationgames.sandwichable.items.ItemsRegistry;
import io.github.foundationgames.sandwichable.recipe.ToastingRecipe;
import io.github.foundationgames.sandwichable.util.Util;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class PFMToasterBlockEntity extends BlockEntity implements SidedInventory, BlockEntityClientSerializable, Tickable {
    private DefaultedList<ItemStack> items = DefaultedList.ofSize(2, ItemStack.EMPTY);
    private @Nullable UUID lastUser;
    private static int toastTime = 240;
    private int toastProgress = 0;
    private boolean toasting = false;
    private boolean smoking = false;
    private int smokeProgress = 0;

    private boolean currentlyPowered = false;
    private boolean previouslyPowered = false;
    private boolean updateNeighbors = false;

    public PFMToasterBlockEntity() {
        super(PFMSandwichableRegistry.IRON_TOASTER_BLOCKENTITY);
    }

    @Override
    public void fromTag(BlockState state, NbtCompound nbt) {
        super.fromTag(state, nbt);
        items = DefaultedList.ofSize(2, ItemStack.EMPTY);
        toastProgress = nbt.getInt("toastProgress");
        toasting = nbt.getBoolean("toasting");
        smokeProgress = nbt.getInt("smokeProgress");
        smoking = nbt.getBoolean("smoking");
        if (nbt.contains("lastUser")) {
            this.lastUser = nbt.getUuid("lastUser");
        } else this.lastUser = null;
        Inventories.readNbt(nbt, items);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("toastProgress", toastProgress);
        nbt.putBoolean("toasting", toasting);
        nbt.putInt("smokeProgress", smokeProgress);
        nbt.putBoolean("smoking", smoking);
        if (this.lastUser == null) {
            nbt.remove("lastUser");
        } else nbt.putUuid("lastUser", this.lastUser);
        Inventories.writeNbt(nbt, items);
        super.writeNbt(nbt);
        return nbt;
    }

    @Override
    public void fromClientTag(NbtCompound NbtCompound) {
        this.fromTag(this.getCachedState(), NbtCompound);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound NbtCompound) {
        return this.writeNbt(NbtCompound);
    }

    private void explode() {
        if(!world.isClient) {
            world.removeBlock(pos, true);
            world.createExplosion(world.getClosestPlayer(pos.getX(), pos.getZ(), 8, 10, false), pos.getX(), pos.getY(), pos.getZ(), 2.2F, true, Explosion.DestructionType.DESTROY);
        }
    }

    public Direction getToasterFacing() {
        if(this.world.getBlockState(this.pos).getBlock() instanceof ToasterBlock) {
            return this.world.getBlockState(this.pos).get(Properties.HORIZONTAL_FACING);
        }
        return Direction.NORTH;
    }

    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    public Optional<PlayerEntity> getLastUser() {
        return Optional.ofNullable(this.lastUser).map(this.world::getPlayerByUuid);
    }

    public void setLastUser(@Nullable PlayerEntity player) {
        this.lastUser = (player == null ? null : player.getUuid());
    }

    public ItemStack takeItem(@Nullable PlayerEntity player) {
        int index = !items.get(1).isEmpty() ? 1 : 0;
        ItemStack stack = items.get(index);
        items.set(index, ItemStack.EMPTY);
        updateNeighbors = true;
        this.setLastUser(player);
        return stack;
    }

    public boolean addItem(Hand hand, PlayerEntity player) {
        if(!toasting) {
            ItemStack playerItem = player.getStackInHand(hand).copy();
            playerItem.setCount(1);
            if (!items.get(0).isEmpty() && !items.get(1).isEmpty()) {
                return false;
            }
            if (!player.isCreative()) {
                player.getStackInHand(hand).decrement(1);
            }
            int index = !items.get(0).isEmpty() ? 1 : 0;
            items.set(index, playerItem);
            updateNeighbors = true;
            this.setLastUser(player);
            return true;
        } return false;
    }

    public boolean hasMetalInside() {
        return Sandwichable.METAL_ITEMS.contains(items.get(0).getItem()) || Sandwichable.METAL_ITEMS.contains(items.get(1).getItem());
    }

    private void toastItems() {
        for (int i = 0; i < 2; i++) {
            SimpleInventory inv = new SimpleInventory(items.get(i));
            Optional<ToastingRecipe> match = world.getRecipeManager().getFirstMatch(ToastingRecipe.Type.INSTANCE, inv, world);

            boolean changed = false;
            if(match.isPresent()) {
                items.set(i, match.get().getOutput().copy());
                changed = true;
            } else {
                if(items.get(i).isFood()) {
                    Item item = Sandwichable.SMALL_FOODS.contains(items.get(i).getItem()) ? ItemsRegistry.BURNT_MORSEL : ItemsRegistry.BURNT_FOOD;
                    items.set(i, new ItemStack(item, 1));
                    changed = true;
                }
            }

            if (!world.isClient() && changed) {
                ItemStack advStack = items.get(i);
                this.getLastUser().ifPresent(player -> {
                    if (player instanceof ServerPlayerEntity) {
                        Sandwichable.TOAST_ITEM.trigger((ServerPlayerEntity) player, advStack);
                    }
                });
            }
        }
    }

    public void startToasting(@Nullable PlayerEntity player) {
        if(this.world.getBlockState(this.pos).getBlock() instanceof PFMToaster) {
            this.world.setBlockState(pos, this.world.getBlockState(this.pos).with(ToasterBlock.ON, true));
        }
        world.playSound(null, pos, SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 0.5F, 0.8F);
        toastProgress = 0;
        toasting = true;
        updateNeighbors = true;
        if (player != null) this.setLastUser(player);
    }

    public void stopToasting(@Nullable PlayerEntity player) {
        if(this.world.getBlockState(this.pos).getBlock() instanceof PFMToaster) {
            this.world.setBlockState(pos, this.world.getBlockState(this.pos).with(ToasterBlock.ON, false));
        }
        world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_BELL, SoundCategory.BLOCKS, 0.8F, 4);
        toastProgress = 0;
        toasting = false;
        updateNeighbors = true;
        if (player != null) this.setLastUser(player);
    }

    public int getComparatorOutput() {
        int r = 0;
        for (int i = 0; i < 2; i++) {
            r += items.get(i).isEmpty() ? 0 : 1;
        }
        r = (int)Math.round(r * 7.5);
        return r;
    }

    public boolean isToasting() {
        return toasting;
    }

    public int getToastingProgress() {
        return this.toastProgress;
    }

    private boolean tickPitch = false;

    @Override
    public void tick() {
        int smokeTime = 80;
        if(this.updateNeighbors) {
            world.updateNeighbors(pos, world.getBlockState(pos).getBlock());
            this.updateNeighbors = false;
        }
        this.previouslyPowered = this.currentlyPowered;
        this.currentlyPowered = world.isReceivingRedstonePower(pos);
        if(this.toasting) {
            this.toastProgress++;
            if(this.toastProgress % 4 == 0 && this.toastProgress != toastTime) {
                world.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.05F, this.tickPitch ? 2.0F : 1.9F);
                this.tickPitch = !this.tickPitch;
            }
            if(this.hasMetalInside() || world.getBlockState(this.pos).get(Properties.WATERLOGGED)) {
                this.explode();
            }
        }
        if(this.toastProgress == toastTime) {
            this.stopToasting(null);
            this.toastItems();
            this.smoking = true;
        }
        if(this.smoking) {
            if(this.smokeProgress % 3 == 0) {
                world.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 0, 0.03, 0);
            }
            this.smokeProgress++;
        } if (this.smokeProgress == smokeTime) { this.smoking = false; this.smokeProgress = 0; }
        if(this.currentlyPowered && !this.previouslyPowered) {
            if(!this.toasting) { this.startToasting(null); }
        }
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[]{0, 1};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction dir) {
        return items.get(slot).isEmpty();
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return !world.getBlockState(pos).get(ToasterBlock.ON);
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return removeStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack stack = items.get(slot).copy();
        items.set(slot, ItemStack.EMPTY);
        setLastUser(null);
        Util.sync(this, world);
        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        items.set(slot, stack);
        setLastUser(null);
        Util.sync(this, world);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false;
    }

    @Override
    public void clear() {
        items.clear();
        Util.sync(this, world);
    }
}