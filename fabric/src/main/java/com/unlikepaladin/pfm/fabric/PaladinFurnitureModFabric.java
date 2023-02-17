package com.unlikepaladin.pfm.fabric;

import com.unlikepaladin.pfm.PaladinFurnitureMod;
import com.unlikepaladin.pfm.compat.PaladinFurnitureModConfig;
import com.unlikepaladin.pfm.compat.fabric.MissingDependencyScreen;
import com.unlikepaladin.pfm.compat.fabric.PaladinFurnitureModConfigImpl;
import com.unlikepaladin.pfm.compat.fabric.sandwichable.PFMSandwichableRegistry;
import com.unlikepaladin.pfm.registry.*;
import com.unlikepaladin.pfm.registry.fabric.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.impl.itemgroup.FabricItemGroupBuilderImpl;
import net.fabricmc.fabric.impl.itemgroup.ItemGroupHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

import static com.unlikepaladin.pfm.PaladinFurnitureMod.MOD_ID;

public class PaladinFurnitureModFabric extends PaladinFurnitureMod implements ModInitializer {

    public static final Identifier FURNITURE_DYED_ID = new Identifier("pfm:furniture_dyed");
    public static SoundEvent FURNITURE_DYED_EVENT = SoundEvent.of(FURNITURE_DYED_ID);

    public static final Logger GENERAL_LOGGER = LogManager.getLogger();

    public static ConfigHolder<PaladinFurnitureModConfigImpl> pfmConfig;
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        ServerLifecycleEvents.SERVER_STARTED.register((server) ->
        {
            String reason;
            String missingMod;
            if (FabricLoader.getInstance().isModLoaded("sandwichable") && !FabricLoader.getInstance().isModLoaded("advanced_runtime_resource_pack")) {
                reason = "To use Sandwichable and Paladin's Furniture Mod together you will need to install ARRP. Please download it, place it on your mods folder, then re-launch the game.";
                missingMod = "ARRP";
            } else {
                return;
            }
            String url = "https://www.curseforge.com/minecraft/mc-mods/arrp/files/3529149";
            MissingDependencyScreen.Screen(reason, url);
            server.shutdown();
            throw new RuntimeException("Missing Dependency for Paladin's furniture mod:" + missingMod);
        });
        BlockItemRegistryFabric.registerBlocks();

        PaladinFurnitureMod.DYE_KITS = FabricItemGroup.builder(new Identifier(MOD_ID, "dye_kits"))
                .displayName(Text.translatable("itemGroup.pfm.dye_kits"))
                .icon(() -> new ItemStack(PaladinFurnitureModBlocksItems.DYE_KIT_RED))
                .entries((enabledFeatures, stacks, operatorEnabled) -> {
                    stacks.add(new ItemStack(PaladinFurnitureModBlocksItems.DYE_KIT_RED));
                    stacks.add(new ItemStack(PaladinFurnitureModBlocksItems.DYE_KIT_ORANGE));
                    stacks.add(new ItemStack(PaladinFurnitureModBlocksItems.DYE_KIT_YELLOW));
                    stacks.add(new ItemStack(PaladinFurnitureModBlocksItems.DYE_KIT_GREEN));
                    stacks.add(new ItemStack(PaladinFurnitureModBlocksItems.DYE_KIT_LIME));
                    stacks.add(new ItemStack(PaladinFurnitureModBlocksItems.DYE_KIT_CYAN));
                    stacks.add(new ItemStack(PaladinFurnitureModBlocksItems.DYE_KIT_BLUE));
                    stacks.add(new ItemStack(PaladinFurnitureModBlocksItems.DYE_KIT_LIGHT_BLUE));
                    stacks.add(new ItemStack(PaladinFurnitureModBlocksItems.DYE_KIT_PURPLE));
                    stacks.add(new ItemStack(PaladinFurnitureModBlocksItems.DYE_KIT_MAGENTA));
                    stacks.add(new ItemStack(PaladinFurnitureModBlocksItems.DYE_KIT_PINK));
                    stacks.add(new ItemStack(PaladinFurnitureModBlocksItems.DYE_KIT_BROWN));
                    stacks.add(new ItemStack(PaladinFurnitureModBlocksItems.DYE_KIT_WHITE));
                    stacks.add(new ItemStack(PaladinFurnitureModBlocksItems.DYE_KIT_GRAY));
                    stacks.add(new ItemStack(PaladinFurnitureModBlocksItems.DYE_KIT_LIGHT_GRAY));
                    stacks.add(new ItemStack(PaladinFurnitureModBlocksItems.DYE_KIT_BLACK));
                })
                .build();

        PaladinFurnitureMod.FURNITURE_GROUP = FabricItemGroup.builder(new Identifier(MOD_ID, "furniture"))
                .displayName(Text.translatable("itemGroup.pfm.furniture"))
                .icon(() -> new ItemStack(PaladinFurnitureModBlocksItems.OAK_CHAIR))
                .entries((enabledFeatures, stacks, operatorEnabled) -> {
                            PaladinFurnitureModBlocksItems.PFM_TAB_ITEMS.forEach(item -> stacks.add(new ItemStack(item)));
                        }
                ).build();

        EntityRegistryFabric.registerEntities();
        if (FabricLoader.getInstance().isModLoaded("sandwichable") && FabricLoader.getInstance().isModLoaded("advanced_runtime_resource_pack")) {
            PFMSandwichableRegistry.register();
        }
        if (FabricLoader.getInstance().isModLoaded("cloth-config2")) {
            pfmConfig = AutoConfig.register(PaladinFurnitureModConfigImpl.class, Toml4jConfigSerializer::new);
        }
        this.commonInit();
        StatisticsRegistryFabric.registerStatistics();
        SoundRegistryFabric.registerSounds();
        BlockEntityRegistryFabric.registerBlockEntities();
        NetworkRegistryFabric.registerPackets();
        ScreenHandlerRegistryFabric.registerScreenHandlers();
        RecipeRegistryFabric.registerRecipes();
    }


}
