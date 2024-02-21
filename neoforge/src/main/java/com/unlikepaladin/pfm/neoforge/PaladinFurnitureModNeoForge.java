package com.unlikepaladin.pfm.neoforge;

import com.google.common.base.Suppliers;
import com.unlikepaladin.pfm.PaladinFurnitureMod;
import com.unlikepaladin.pfm.client.PathPackRPWrapper;
import com.unlikepaladin.pfm.config.PaladinFurnitureModConfig;
import com.unlikepaladin.pfm.registry.dynamic.neoforge.LateBlockRegistryNeoForge;
import com.unlikepaladin.pfm.registry.neoforge.*;
import net.minecraft.resource.*;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import com.unlikepaladin.pfm.runtime.PFMDataGenerator;
import com.unlikepaladin.pfm.runtime.PFMRuntimeResources;
import net.minecraft.SharedConstants;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.text.Text;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


@Mod(PaladinFurnitureMod.MOD_ID)
public class PaladinFurnitureModNeoForge extends PaladinFurnitureMod {
    public static PaladinFurnitureModConfig pfmConfig;
    public PaladinFurnitureModNeoForge(IEventBus modEventBus) {
        pfmConfig = new PaladinFurnitureModConfig(FMLPaths.CONFIGDIR.get());
        try {
            pfmConfig.initialize();
        } catch (IOException e) {
            GENERAL_LOGGER.error("Failed to initialize Paladin's Furniture configuration, default values will be used instead");
            GENERAL_LOGGER.error("", e);
        }
        this.commonInit();
        modEventBus.register(EntityRegistryNeoForge.class);
        modEventBus.register(BlockItemRegistryNeoForge.class);
        modEventBus.register(StatisticsRegistryNeoForge.class);
        modEventBus.register(ScreenHandlerRegistryNeoForge.class);
        modEventBus.register(RecipeRegistryNeoForge.class);
        modEventBus.register(BlockEntityRegistryNeoForge.class);
        modEventBus.register(SoundRegistryNeoForge.class);
        modEventBus.addListener(NetworkRegistryNeoForge::register);
        LateBlockRegistryNeoForge.addDynamicBlockRegistration(modEventBus);
        PaladinFurnitureMod.isClient = FMLEnvironment.dist == Dist.CLIENT;
        NeoForge.EVENT_BUS.addListener(NetworkRegistryNeoForge::onServerJoin);
        modEventBus.addListener(ItemGroupRegistryNeoForge::registerItemGroups);
        modEventBus.addListener(ItemGroupRegistryNeoForge::addToVanillaItemGroups);
        modEventBus.addListener(PaladinFurnitureModNeoForge::generateResources);
    }

    @SubscribeEvent
    public static void generateResources(AddPackFindersEvent event) {
        int data = SharedConstants.getGameVersion().getResourceVersion(ResourceType.SERVER_DATA);
        int resource = SharedConstants.getGameVersion().getResourceVersion(ResourceType.CLIENT_RESOURCES);
        if (event.getPackType() == ResourceType.CLIENT_RESOURCES) {
            PackResourceMetadata packResourceMetadata = new PackResourceMetadata(Text.literal("Runtime Generated Assets for PFM"), SharedConstants.getGameVersion().getResourceVersion(ResourceType.CLIENT_RESOURCES), Optional.empty());
            ResourcePackProfile.PackFactory packFactory = new ResourcePackProfile.PackFactory() {
                @Override
                public ResourcePack open(String name) {
                    return new PathPackRPWrapper(Suppliers.memoize(() -> {
                        if (!PFMDataGenerator.areAssetsRunning())
                            PFMRuntimeResources.prepareAndRunAssetGen(false);
                        return PFMRuntimeResources.ASSETS_PACK;}), packResourceMetadata);
                }

                @Override
                public ResourcePack openWithOverlays(String name, ResourcePackProfile.Metadata metadata) {
                    return this.open(name);
                }
            };
            ResourcePackProfile.Metadata metadata = new ResourcePackProfile.Metadata(Text.literal("Runtime Generated Assets for PFM"), ResourcePackCompatibility.COMPATIBLE, FeatureFlags.DEFAULT_ENABLED_FEATURES, List.of(), false);
            event.addRepositorySource(profileAdder -> {
                profileAdder.accept(ResourcePackProfile.of("pfm-asset-resources", Text.literal("PFM Assets"), true,  packFactory, metadata, ResourcePackProfile.InsertionPosition.BOTTOM, false, ResourcePackSource.NONE));
            });
        } else if (event.getPackType() == ResourceType.SERVER_DATA) {
            PackResourceMetadata packResourceMetadata = new PackResourceMetadata(Text.literal("Runtime Generated Data for PFM"), SharedConstants.getGameVersion().getResourceVersion(ResourceType.SERVER_DATA), Optional.empty());
            ResourcePackProfile.PackFactory packFactory = new ResourcePackProfile.PackFactory() {
                @Override
                public ResourcePack open(String name) {
                    return new PathPackRPWrapper(Suppliers.memoize(() -> {
                        if (!PFMDataGenerator.isDataRunning())
                            PFMRuntimeResources.prepareAndRunDataGen(false);
                        return PFMRuntimeResources.DATA_PACK;}), packResourceMetadata);
                }

                @Override
                public ResourcePack openWithOverlays(String name, ResourcePackProfile.Metadata metadata) {
                    return this.open(name);
                }
            };
            ResourcePackProfile.Metadata metadata = new ResourcePackProfile.Metadata(Text.literal("Runtime Generated Data for PFM"), ResourcePackCompatibility.COMPATIBLE, FeatureFlags.DEFAULT_ENABLED_FEATURES, List.of(), false);
            event.addRepositorySource(profileAdder -> {
                profileAdder.accept(ResourcePackProfile.of("pfm-data-resources", Text.literal("PFM Data"), true,  packFactory, metadata, ResourcePackProfile.InsertionPosition.BOTTOM, false, ResourcePackSource.NONE));
            });
        }
    }
}
