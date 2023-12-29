package com.unlikepaladin.pfm.forge;

import com.google.common.base.Suppliers;
import com.mojang.bridge.game.PackType;
import com.unlikepaladin.pfm.PaladinFurnitureMod;
import com.unlikepaladin.pfm.client.PathPackRPWrapper;
import com.unlikepaladin.pfm.config.PaladinFurnitureModConfig;
import com.unlikepaladin.pfm.data.forge.PFMTagsImpl;
import com.unlikepaladin.pfm.registry.BlockItemRegistry;
import com.unlikepaladin.pfm.registry.dynamic.forge.LateBlockRegistryForge;
import com.unlikepaladin.pfm.registry.forge.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;
import com.unlikepaladin.pfm.runtime.PFMDataGenerator;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import com.unlikepaladin.pfm.runtime.PFMRuntimeResources;
import net.minecraft.SharedConstants;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.text.Text;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;


@Mod(PaladinFurnitureMod.MOD_ID)
public class PaladinFurnitureModForge extends PaladinFurnitureMod {
    public static PaladinFurnitureModConfig pfmConfig;
    public PaladinFurnitureModForge() {
        pfmConfig = new PaladinFurnitureModConfig(FMLPaths.CONFIGDIR.get().resolve("pfm.properties"));
        try {
            pfmConfig.initialize();
        } catch (IOException e) {
            GENERAL_LOGGER.error("Failed to initialize Paladin's Furniture configuration, default values will be used instead");
            GENERAL_LOGGER.error("", e);
        }
        this.commonInit();
        MinecraftForge.EVENT_BUS.register(EntityRegistryForge.class);
        MinecraftForge.EVENT_BUS.register(BlockItemRegistryForge.class);
        MinecraftForge.EVENT_BUS.register(StatisticsRegistryForge.class);
        MinecraftForge.EVENT_BUS.register(ScreenHandlerRegistryForge.class);
        MinecraftForge.EVENT_BUS.register(RecipeRegistryForge.class);
        MinecraftForge.EVENT_BUS.register(BlockEntityRegistryForge.class);
        MinecraftForge.EVENT_BUS.register(SoundRegistryForge.class);
        MinecraftForge.EVENT_BUS.register(NetworkRegistryForge.class);
        NetworkRegistryForge.registerPackets();
        LateBlockRegistryForge.addDynamicBlockRegistration();
        PaladinFurnitureMod.isClient = FMLEnvironment.dist == Dist.CLIENT;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ItemGroupRegistryForge::registerItemGroups);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ItemGroupRegistryForge::addToVanillaItemGroups);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(PaladinFurnitureModForge::generateResources);
    }

    @SubscribeEvent
    public static void generateResources(AddPackFindersEvent event) {
        if (event.getPackType() == ResourceType.CLIENT_RESOURCES) {
            PackResourceMetadata packResourceMetadata = new PackResourceMetadata(Text.literal("Runtime Generated Assets for PFM"), SharedConstants.getGameVersion().getPackVersion(PackType.RESOURCE));
            ResourcePackProfile.PackFactory packFactory = name -> new PathPackRPWrapper(Suppliers.memoize(() -> {
                if (!PFMDataGenerator.areAssetsRunning())
                    PFMRuntimeResources.prepareAndRunAssetGen(false);
                return PFMRuntimeResources.ASSETS_PACK;}), packResourceMetadata);
            ResourcePackProfile.Metadata metadata = new ResourcePackProfile.Metadata(Text.literal("Runtime Generated Assets for PFM"), SharedConstants.getGameVersion().getPackVersion(PackType.DATA), SharedConstants.getGameVersion().getPackVersion(PackType.RESOURCE), FeatureFlags.DEFAULT_ENABLED_FEATURES, false);
            event.addRepositorySource(profileAdder -> {
                profileAdder.accept(ResourcePackProfile.of("pfm-asset-resources", Text.literal("PFM Assets"), true,  packFactory, metadata, ResourceType.CLIENT_RESOURCES, ResourcePackProfile.InsertionPosition.BOTTOM, false, ResourcePackSource.NONE));
            });
        } else if (event.getPackType() == ResourceType.SERVER_DATA) {
            PackResourceMetadata packResourceMetadata = new PackResourceMetadata(Text.literal("Runtime Generated Data for PFM"), SharedConstants.getGameVersion().getPackVersion(PackType.DATA));
            ResourcePackProfile.PackFactory packFactory = name -> new PathPackRPWrapper(Suppliers.memoize(() -> {
                if (!PFMDataGenerator.isDataRunning())
                    PFMRuntimeResources.prepareAndRunDataGen(false);
                return PFMRuntimeResources.DATA_PACK;}), packResourceMetadata);
            ResourcePackProfile.Metadata metadata = new ResourcePackProfile.Metadata(Text.literal("Runtime Generated Data for PFM"), SharedConstants.getGameVersion().getPackVersion(PackType.DATA), SharedConstants.getGameVersion().getPackVersion(PackType.RESOURCE), FeatureFlags.DEFAULT_ENABLED_FEATURES, false);
            event.addRepositorySource(profileAdder -> {
                profileAdder.accept(ResourcePackProfile.of("pfm-data-resources", Text.literal("PFM Data"), true,  packFactory, metadata, ResourceType.SERVER_DATA, ResourcePackProfile.InsertionPosition.BOTTOM, false, ResourcePackSource.NONE));
            });
        }
    }
}
