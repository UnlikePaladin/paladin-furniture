package com.unlikepaladin.pfm.registry.fabric;

import com.unlikepaladin.pfm.PaladinFurnitureMod;
import com.unlikepaladin.pfm.recipes.FreezingRecipe;
import com.unlikepaladin.pfm.recipes.FurnitureRecipe;
import com.unlikepaladin.pfm.recipes.FurnitureSerializer;
import com.unlikepaladin.pfm.registry.RecipeTypes;
import net.minecraft.recipe.CookingRecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class RecipeRegistryFabric {
    public static void registerRecipes() {
        RecipeTypes.FREEZING_RECIPE = Registry.register(Registries.RECIPE_TYPE, PaladinFurnitureMod.MOD_ID + ":freezing",  new RecipeType<FreezingRecipe>() {
            @Override
            public String toString() {return "freezing";}
        });
        RecipeTypes.FREEZING_RECIPE_SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(PaladinFurnitureMod.MOD_ID, "freezing"), new CookingRecipeSerializer<>(FreezingRecipe::new, 200));
        RecipeTypes.FURNITURE_RECIPE = Registry.register(Registries.RECIPE_TYPE, RecipeTypes.FURNITURE_ID,  new RecipeType<FurnitureRecipe>() {
            @Override
            public String toString() {return "furniture";}
        });
        RecipeTypes.FURNITURE_SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(PaladinFurnitureMod.MOD_ID,"furniture"), new FurnitureSerializer());
    }
}
