package com.unlikepaladin.pfm.compat.cookingforblockheads.neoforge;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.mojang.datafixers.util.Pair;
import com.unlikepaladin.pfm.blocks.blockentities.CounterOvenBlockEntity;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.container.BalmContainerProvider;
import net.blay09.mods.balm.api.energy.EnergyStorage;
import net.blay09.mods.balm.api.fluid.FluidTank;
import net.blay09.mods.balm.api.provider.BalmProvider;
import net.blay09.mods.balm.api.provider.BalmProviderHolder;
import net.blay09.mods.balm.neoforge.energy.NeoForgeEnergyStorage;
import net.blay09.mods.balm.neoforge.fluid.NeoForgeFluidTank;
import net.blay09.mods.balm.neoforge.provider.NeoForgeBalmProviders;
import net.blay09.mods.cookingforblockheads.api.capability.DefaultKitchenItemProvider;
import net.blay09.mods.cookingforblockheads.api.capability.IKitchenItemProvider;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.neoforged.neoforge.common.capabilities.Capabilities;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CounterOvenBlockEntityBalm extends CounterOvenBlockEntity implements BalmContainerProvider, BalmProviderHolder, BlockEntityContract {
    private final DefaultKitchenItemProvider itemProvider;

    public CounterOvenBlockEntityBalm(BlockPos pos, BlockState state) {
        super(pos, state);
        this.itemProvider = new DefaultKitchenItemProvider(this);
    }

    @Override
    public Inventory getContainer() {
        return this;
    }

    public List<BalmProvider<?>> getProviders() {
        return Lists.newArrayList(new BalmProvider[]{new BalmProvider(IKitchenItemProvider.class, this.itemProvider)});
    }

    private boolean capabilitiesInitialized;
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (!this.capabilitiesInitialized) {
            List<BalmProviderHolder> providers = new ArrayList<>();
            this.buildProviders(providers);

            for (BalmProviderHolder providerHolder : providers) {
                Iterator var6 = providerHolder.getProviders().iterator();

                while(var6.hasNext()) {
                    BalmProvider<?> provider = (BalmProvider)var6.next();
                    this.addCapabilities(provider, this.capabilities);
                }

                var6 = providerHolder.getSidedProviders().iterator();

                while (var6.hasNext()) {
                    Pair<Direction, BalmProvider<?>> pair = (Pair) var6.next();
                    Direction direction = pair.getFirst();
                    BalmProvider<?> provider = pair.getSecond();
                    Map<Capability<?>, LazyOptional<?>> sidedCapabilities = this.sidedCapabilities.column(direction);
                    this.addCapabilities(provider, sidedCapabilities);
                }
            }

            this.capabilitiesInitialized = true;
        }

        LazyOptional<?> result = null;
        if (side != null) {
            result = this.sidedCapabilities.get(cap, side);
        }

        if (result == null) {
            result = this.capabilities.get(cap);
        }

        return result != null ? result.cast() : super.getCapability(cap, side);
    }

    private final Map<Capability<?>, LazyOptional<?>> capabilities = new HashMap<>();
    private final Table<Capability<?>, Direction, LazyOptional<?>> sidedCapabilities = HashBasedTable.create();
    private void addCapabilities(BalmProvider<?> provider, Map<Capability<?>, LazyOptional<?>> capabilities) {
        NeoForgeBalmProviders forgeProviders = (NeoForgeBalmProviders) Balm.getProviders();
        Capability<?> capability = forgeProviders.getCapability(provider.getProviderClass());
        Objects.requireNonNull(provider);
        capabilities.put(capability, LazyOptional.of(provider::getInstance));
        if (provider.getProviderClass() == Inventory.class) {
            capabilities.put(Capabilities.ITEM_HANDLER, LazyOptional.of(() -> new InvWrapper((Inventory)provider.getInstance())));
        } else if (provider.getProviderClass() == FluidTank.class) {
            capabilities.put(Capabilities.FLUID_HANDLER, LazyOptional.of(() -> new NeoForgeFluidTank((FluidTank)provider.getInstance())));
        } else if (provider.getProviderClass() == EnergyStorage.class) {
            capabilities.put(Capabilities.ENERGY, LazyOptional.of(() -> new NeoForgeEnergyStorage((EnergyStorage)provider.getInstance())));
        }
    }

    @Override
    public <T> T getProvider(Class<T> clazz) {
        NeoForgeBalmProviders forgeProviders = (NeoForgeBalmProviders)Balm.getProviders();
        Capability<?> capability = forgeProviders.getCapability(clazz);
        return (T) this.getCapability(capability).resolve().orElse(null);
    }
}