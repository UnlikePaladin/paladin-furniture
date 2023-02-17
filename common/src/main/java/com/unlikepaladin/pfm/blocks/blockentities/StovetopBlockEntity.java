package com.unlikepaladin.pfm.blocks.blockentities;

import com.unlikepaladin.pfm.blocks.KitchenStovetop;
import com.unlikepaladin.pfm.PaladinFurnitureMod;
import com.unlikepaladin.pfm.blocks.KitchenStovetop;
import com.unlikepaladin.pfm.registry.BlockEntities;
import com.unlikepaladin.pfm.registry.BlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Clearable;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Random;


public class StovetopBlockEntity extends BlockEntity implements Clearable, Tickable {

    protected final DefaultedList<ItemStack> itemsBeingCooked = DefaultedList.ofSize(4, ItemStack.EMPTY);
    private final int[] cookingTimes = new int[4];
    private final int[] cookingTotalTimes = new int[4];
    public StovetopBlockEntity() {
        super(BlockEntities.STOVE_TOP_BLOCK_ENTITY);
    }
    public void litServerTick() {
        boolean bl = false;
        for (int i = 0; i < this.itemsBeingCooked.size(); ++i) {
            ItemStack itemStack = this.itemsBeingCooked.get(i);
            if (itemStack.isEmpty()) continue;
            bl = true;
            int n = i;
        if (this.cookingTimes[n] < 600){
            this.cookingTimes[n] = this.cookingTimes[n] + 2;
        }
            if (this.cookingTimes[i] < this.cookingTotalTimes[i]) continue;
            SimpleInventory inventory = new SimpleInventory(itemStack);
            ItemStack itemStack2 = world.getRecipeManager().getFirstMatch(RecipeType.CAMPFIRE_COOKING, inventory, world).map(campfireCookingRecipe -> campfireCookingRecipe.craft(inventory)).orElse(itemStack);
                if (PaladinFurnitureMod.getPFMConfig().doesFoodPopOffStove()) {
                    ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), itemStack2);
                    this.itemsBeingCooked.set(i, ItemStack.EMPTY);
                }
                else {
                    this.itemsBeingCooked.set(i, itemStack2);
                }
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
        if (bl) {
            markDirty();
        }
    }

    public void unlitServerTick() {
        boolean bl = false;
        for (int i = 0; i < this.itemsBeingCooked.size(); ++i) {
            if (this.cookingTimes[i] <= 0) continue;
            bl = true;
            this.cookingTimes[i] = MathHelper.clamp(this.cookingTimes[i] - 2, 0, this.cookingTotalTimes[i]);
        }
        if (bl) {
            markDirty();
        }
    }

    public void clientTick() {
        int i;
        Random random = world.random;
        i = getCachedState().get(KitchenStovetop.FACING).rotateYClockwise().getHorizontal();
        for (int j = 0; j < this.itemsBeingCooked.size(); ++j) {
            ItemStack stack = this.itemsBeingCooked.get(j);
            if (stack.isEmpty() || !(random.nextFloat() < 0.2f) || !world.getRecipeManager().getFirstMatch(RecipeType.CAMPFIRE_COOKING, new SimpleInventory(stack), world).isPresent()) continue;
            Direction direction = Direction.fromHorizontal(Math.floorMod(j + i, 4));
            float f = 0.2125f;
            double x = pos.getX() + 0.5 - ((direction.getOffsetX() * f) + (direction.rotateYClockwise().getOffsetX() * f));
            double y = pos.getY() + 0.2;
            double z = pos.getZ() + 0.5 - ((direction.getOffsetZ() * f) + (direction.rotateYClockwise().getOffsetZ() * f));
            for (int k = 0; k < 4; ++k) {
                if (!(random.nextFloat() < 0.9f))
                    world.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 5.0E-4, 0.0);
            }
        }
    }

    public DefaultedList<ItemStack> getItemsBeingCooked() {
        return this.itemsBeingCooked;
    }

    public Inventory getInventory(){
        SimpleInventory inventory = new SimpleInventory(itemsBeingCooked.size());
        for (int i = 0; i < itemsBeingCooked.size(); i++) {
            inventory.setStack(i, itemsBeingCooked.get(i));
        }
        return inventory;
    }

    @Override
    public void fromTag(BlockState state, NbtCompound nbt) {
        int[] is;
        super.fromTag(state, nbt);
        this.itemsBeingCooked.clear();
        Inventories.readNbt(nbt, this.itemsBeingCooked);
        if (nbt.contains("CookingTimes", 11)) {
            is = nbt.getIntArray("CookingTimes");
            System.arraycopy(is, 0, this.cookingTimes, 0, Math.min(this.cookingTotalTimes.length, is.length));
        }
        if (nbt.contains("CookingTotalTimes", 11)) {
            is = nbt.getIntArray("CookingTotalTimes");
            System.arraycopy(is, 0, this.cookingTotalTimes, 0, Math.min(this.cookingTotalTimes.length, is.length));
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        this.saveInitialChunkData(nbt);
        nbt.putIntArray("CookingTimes", this.cookingTimes);
        nbt.putIntArray("CookingTotalTimes", this.cookingTotalTimes);
        return nbt;
    }

    protected NbtCompound saveInitialChunkData(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.itemsBeingCooked, true);
        return nbt;
    }

    public ItemStack removeStack(int slot) {
        ItemStack stack = this.itemsBeingCooked.get(slot).copy();
        this.itemsBeingCooked.set(slot, ItemStack.EMPTY);
        updateListeners();
        return stack;
    }

    public Optional<CampfireCookingRecipe> getRecipeFor(ItemStack item) {
        if (this.itemsBeingCooked.stream().noneMatch(ItemStack::isEmpty)) {
            return Optional.empty();
        }
        return this.world.getRecipeManager().getFirstMatch(RecipeType.CAMPFIRE_COOKING, new SimpleInventory(item), this.world);
    }

    public boolean addItem(ItemStack item, int integer) {
        for (int i = 0; i < this.itemsBeingCooked.size(); ++i) {
            ItemStack itemStack = this.itemsBeingCooked.get(i);
            if (!itemStack.isEmpty()) continue;
            this.cookingTotalTimes[i] = integer;
            this.cookingTimes[i] = 0;
            this.itemsBeingCooked.set(i, item.split(1));
            this.updateListeners();
            return true;
        }
        return false;
    }

    private void updateListeners() {
        this.markDirty();
        this.getWorld().updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), 3);
    }

    @Override
    public void clear() {
        this.itemsBeingCooked.clear();
        updateListeners();
    }

    @Override
    public void tick() {
        BlockState state = getCachedState();
        if (world.isClient) {
            if (getCachedState().get(KitchenStovetop.LIT)) {
                clientTick();
            }
        } else {
            if (state.get(KitchenStovetop.LIT)) {
                litServerTick();
            }
            else {
                unlitServerTick();
            }
        }
    }
}

