package com.unlikepaladin.pfm.registry.dynamic.neoforge;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Lifecycle;
import com.unlikepaladin.pfm.PaladinFurnitureMod;
import com.unlikepaladin.pfm.blocks.AbstractSittableBlock;
import com.unlikepaladin.pfm.blocks.SimpleBedBlock;
import com.unlikepaladin.pfm.registry.PaladinFurnitureModBlocksItems;
import com.unlikepaladin.pfm.registry.dynamic.LateBlockRegistry;
import com.unlikepaladin.pfm.registry.neoforge.BlockItemRegistryImpl;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Supplier;

public class LateBlockRegistryImpl {

    public static Map<String, Block> blocks = new LinkedHashMap<>();
    public static Map<String, Supplier<Item>> items = new LinkedHashMap<>();
    public static void registerLateItem(String itemName, Supplier<Item> itemSup, Pair<String, ItemGroup> group) {
        items.put(itemName, itemSup);
        BlockItemRegistryImpl.itemNameToGroup.put(itemName, group);
    }

    public static <T extends Block> T registerLateBlock(String blockId, Supplier<T> blockSup, boolean registerItem, Pair<String, ItemGroup> group) {
        T block = blockSup.get();
        if (registerItem) {
            PaladinFurnitureModBlocksItems.BLOCKS.add(block);
            registerBlockItemPlatformSpecific(blockId, block, group);
        }
        blocks.put(blockId, block);
        return block;
    }

    public static void registerBlockItemPlatformSpecific(String itemName, Block block, Pair<String, ItemGroup> group) {
        if (AbstractSittableBlock.isWoodBased(block.getDefaultState())) {
            registerLateItem(itemName, () -> new BlockItem(block, new Item.Settings()) {
                @Override
                public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                    return 300;
                }
            }, group);
        }
        registerLateItem(itemName, () -> new BlockItem(block, new Item.Settings()), group);
    }


    public static void registerBlocks(Registry<Block> blockRegisterEvent) {
        try {
            LateBlockRegistry.registerBlocks();
        } catch (InvocationTargetException | InstantiationException |
                 IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
        blocks.forEach((blockName, block) -> Registry.register(Registries.BLOCK, new Identifier(PaladinFurnitureMod.MOD_ID, blockName), block));

        Set<BlockState> originalBedStates = Registries.POINT_OF_INTEREST_TYPE.get(PointOfInterestTypes.HOME).blockStates();
        Set<BlockState> addedBedStates = Arrays.stream(PaladinFurnitureModBlocksItems.getBeds()).flatMap(block -> block.getStateManager().getStates().stream().filter(state -> state.get(SimpleBedBlock.PART) == BedPart.HEAD)).collect(ImmutableSet.toImmutableSet());
        Set<BlockState> newBedStates = new HashSet<>();
        newBedStates.addAll(originalBedStates);
        newBedStates.addAll(addedBedStates);
        PointOfInterestType homePOI = Registries.POINT_OF_INTEREST_TYPE.get(PointOfInterestTypes.HOME);
        homePOI.blockStates = ImmutableSet.copyOf(newBedStates);
    }

    public static void registerItems(Registry<Item> itemIForgeRegistry) {
        items.forEach((itemName, itemSup) -> {
            Item item = itemSup.get();
            Registry.register(Registries.ITEM, new Identifier(PaladinFurnitureMod.MOD_ID, itemName), item);
            if (!PaladinFurnitureModBlocksItems.ITEM_GROUP_LIST_MAP.containsKey(BlockItemRegistryImpl.itemNameToGroup.get(itemName))) {
                PaladinFurnitureModBlocksItems.ITEM_GROUP_LIST_MAP.put(BlockItemRegistryImpl.itemNameToGroup.get(itemName), new ArrayList<>());
            }
            PaladinFurnitureModBlocksItems.ITEM_GROUP_LIST_MAP.get(BlockItemRegistryImpl.itemNameToGroup.get(itemName)).add(item);
        });
    }

    public static <T extends Block> T registerLateBlockClassic(String blockId, T block, boolean registerItem, Pair<String, ItemGroup> group) {
        if (registerItem) {
            PaladinFurnitureModBlocksItems.BLOCKS.add(block);
            registerBlockItemPlatformSpecific(blockId, block, group);
        }
        blocks.put(blockId, block);
        return block;
    }
}
