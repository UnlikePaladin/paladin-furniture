package com.unlikepaladin.pfm;

import com.unlikepaladin.pfm.blocks.behavior.SinkBehavior;
import com.unlikepaladin.pfm.blocks.blockentities.*;
import com.unlikepaladin.pfm.menus.AbstractFreezerScreenHandler;
import com.unlikepaladin.pfm.menus.FreezerScreenHandler;
import com.unlikepaladin.pfm.menus.IronStoveScreenHandler;
import com.unlikepaladin.pfm.menus.StoveScreenHandler;
import com.unlikepaladin.pfm.recipes.FreezingRecipe;
import com.unlikepaladin.pfm.registry.BlockItemRegistry;
import com.unlikepaladin.pfm.registry.EntityRegistry;
import com.unlikepaladin.pfm.registry.StatisticsRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CookingRecipeSerializer;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PaladinFurnitureMod implements ModInitializer {

	public static final String MOD_ID = "pfm";
	public static final Identifier Player_Chair_Screen = new Identifier(MOD_ID, "player_chair_screen");
	//public static final ScreenHandlerType<PlayerChairScreenHandler> Player_Chair_Screen_Handler = ScreenHandlerRegistry.registerExtended(id("pfm"), PlayerChairScreenHandler::new);

	public static BlockEntityType<DrawerBlockEntity> DRAWER_BLOCK_ENTITY;
	public static BlockEntityType<FridgeBlockEntity> FRIDGE_BLOCK_ENTITY;
	public static BlockEntityType<FreezerBlockEntity> FREEZER_BLOCK_ENTITY;
	public static BlockEntityType<StoveBlockEntity> STOVE_BLOCK_ENTITY;
	public static BlockEntityType<IronStoveBlockEntity> IRON_STOVE_BLOCK_ENTITY;

	public static final Identifier FREEZER = new Identifier(MOD_ID, "freezer_block_entity");

	public static final Identifier FURNITURE_DYED_ID = new Identifier("pfm:furniture_dyed");
	public static SoundEvent FURNITURE_DYED_EVENT = new SoundEvent(FURNITURE_DYED_ID);
	public static ScreenHandlerType<AbstractFreezerScreenHandler> FREEZER_SCREEN_HANDLER;
	public static ScreenHandlerType<StoveScreenHandler> STOVE_SCREEN_HANDLER;
	public static ScreenHandlerType<IronStoveScreenHandler> IRON_STOVE_SCREEN_HANDLER;



	public static final Logger GENERAL_LOGGER = LogManager.getLogger();



	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
	public static final ItemGroup FURNITURE_GROUP = FabricItemGroupBuilder.build(
			new Identifier(MOD_ID, "furniture"),
			() -> new ItemStack(BlockItemRegistry.OAK_CHAIR));

	public static final ItemGroup DYE_KITS = FabricItemGroupBuilder.create(
			new Identifier(MOD_ID, "dye_kits"))
			.icon(() -> new ItemStack(BlockItemRegistry.DYE_KIT_RED))
			.appendItems(stacks -> {
		stacks.add(new ItemStack(BlockItemRegistry.DYE_KIT_RED));
		stacks.add(new ItemStack(BlockItemRegistry.DYE_KIT_ORANGE));
		stacks.add(new ItemStack(BlockItemRegistry.DYE_KIT_YELLOW));
		stacks.add(new ItemStack(BlockItemRegistry.DYE_KIT_GREEN));
		stacks.add(new ItemStack(BlockItemRegistry.DYE_KIT_LIME));
		stacks.add(new ItemStack(BlockItemRegistry.DYE_KIT_CYAN));
		stacks.add(new ItemStack(BlockItemRegistry.DYE_KIT_BLUE));
		stacks.add(new ItemStack(BlockItemRegistry.DYE_KIT_LIGHT_BLUE));
		stacks.add(new ItemStack(BlockItemRegistry.DYE_KIT_PURPLE));
		stacks.add(new ItemStack(BlockItemRegistry.DYE_KIT_MAGENTA));
		stacks.add(new ItemStack(BlockItemRegistry.DYE_KIT_PINK));
		stacks.add(new ItemStack(BlockItemRegistry.DYE_KIT_BLACK));
		stacks.add(new ItemStack(BlockItemRegistry.DYE_KIT_WHITE));
		stacks.add(new ItemStack(BlockItemRegistry.DYE_KIT_GRAY));
		stacks.add(new ItemStack(BlockItemRegistry.DYE_KIT_LIGHT_GRAY));
		stacks.add(new ItemStack(BlockItemRegistry.DYE_KIT_BLACK));
	})
			.build();
	public static RecipeType<FreezingRecipe> FREEZING_RECIPE;
	public static RecipeSerializer<FreezingRecipe> FREEZING_RECIPE_SERIALIZER;

	//System.out.println("Hello Fabric world!");

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		EntityRegistry.registerEntities();
		SinkBehavior.registerBehavior();
		BlockItemRegistry.register();
		StatisticsRegistry.register();
		DRAWER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, MOD_ID + ":drawer_block_entity", FabricBlockEntityTypeBuilder.create(DrawerBlockEntity::new, BlockItemRegistry.OAK_KITCHEN_DRAWER).build(null));
		FRIDGE_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, MOD_ID + ":fridge_block_entity", FabricBlockEntityTypeBuilder.create(FridgeBlockEntity::new, BlockItemRegistry.WHITE_FRIDGE).build(null));
		FREEZER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, MOD_ID + ":freezer_block_entity", FabricBlockEntityTypeBuilder.create(FreezerBlockEntity::new, BlockItemRegistry.WHITE_FREEZER).build(null));
		STOVE_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, MOD_ID + ":stove_block_entity", FabricBlockEntityTypeBuilder.create(StoveBlockEntity::new, BlockItemRegistry.SIMPLE_STOVE).build(null));
		IRON_STOVE_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, MOD_ID + ":iron_stove_block_entity", FabricBlockEntityTypeBuilder.create(IronStoveBlockEntity::new, BlockItemRegistry.IRON_STOVE).build(null));

		FREEZING_RECIPE = Registry.register(Registry.RECIPE_TYPE, MOD_ID + ":freezing",  new RecipeType<FreezingRecipe>() {
			@Override
			public String toString() {return "freezing";}
		});
		FREEZING_RECIPE_SERIALIZER = Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(MOD_ID, "freezing"), new CookingRecipeSerializer<>(FreezingRecipe::new, 200));

		FREEZER_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(FREEZER, FreezerScreenHandler::new);
		STOVE_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(MOD_ID, "stove_block_entity"), StoveScreenHandler::new);
		IRON_STOVE_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(MOD_ID, "iron_stove_block_entity"), IronStoveScreenHandler::new);

	}
}
