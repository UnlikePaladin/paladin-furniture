package com.unlikepaladin.pfm.compat.cookingforblockheads.fabric;

import com.mojang.datafixers.util.Pair;
import com.unlikepaladin.pfm.blocks.blockentities.SinkBlockEntity;
import net.blay09.mods.balm.api.block.BalmBlockEntityContract;
import net.blay09.mods.balm.api.provider.BalmProvider;
import net.blay09.mods.balm.api.provider.BalmProviderHolder;
import net.blay09.mods.cookingforblockheads.api.capability.DefaultKitchenConnector;
import net.blay09.mods.cookingforblockheads.api.capability.IKitchenConnector;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.*;

public class SinkBlockEntityBalm extends SinkBlockEntity implements BalmProviderHolder, BalmBlockEntityContract {
    private final DefaultKitchenConnector connector;

    public SinkBlockEntityBalm(BlockPos pos, BlockState state) {
        super(pos, state);
        this.connector = new DefaultKitchenConnector();
    }


    public List<BalmProvider<?>> getProviders() {
        return List.of(new BalmProvider<>(IKitchenConnector.class, this.connector));
    }

    private final Map<Class<?>, BalmProvider<?>> providers = new HashMap<>();
    private final Map<Pair<Direction, Class<?>>, BalmProvider<?>> sidedProviders = new HashMap<>();
    private boolean providersInitialized;
    @Override
    public <T> T getProvider(Class<T> clazz) {
        if (!this.providersInitialized) {
            List<BalmProviderHolder> providers = new ArrayList<>();
            this.buildProviders(providers);

            for (BalmProviderHolder providerHolder : providers) {
                for (BalmProvider<?> provider : providerHolder.getProviders()) {
                    this.providers.put(provider.getProviderClass(), provider);
                }
                for (Pair<Direction, BalmProvider<?>> pair : providerHolder.getSidedProviders()) {
                    Direction direction = pair.getFirst();
                    BalmProvider<?> provider = pair.getSecond();
                    this.sidedProviders.put(Pair.of(direction, provider.getProviderClass()), provider);
                }
            }

            this.providersInitialized = true;
        }

        BalmProvider<?> found = this.providers.get(clazz);
        return found != null ? (T) found.getInstance() : null;
    }
}
