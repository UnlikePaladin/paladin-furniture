package com.unlikepaladin.pfm.compat.rei.forge;

import com.unlikepaladin.pfm.compat.rei.FreezingCategory;
import com.unlikepaladin.pfm.compat.rei.FreezingDisplay;
import com.unlikepaladin.pfm.compat.rei.FurnitureCategory;
import com.unlikepaladin.pfm.compat.rei.FurnitureDisplay;
import com.unlikepaladin.pfm.recipes.FreezingRecipe;
import com.unlikepaladin.pfm.recipes.FurnitureRecipe;
import com.unlikepaladin.pfm.registry.RecipeTypes;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.forge.REIPlugin;

@REIPlugin
public class PaladinFurnitureModREIPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new FurnitureCategory());
        registry.addWorkstations(FurnitureDisplay.IDENTIFIER, FurnitureCategory.ICON);
        registry.add(new FreezingCategory());
        registry.addWorkstations(FreezingDisplay.IDENTIFIER, FreezingCategory.ICON);
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerFiller(FurnitureRecipe.class, FurnitureDisplay::new);
        registry.registerFiller(FreezingRecipe.class, FreezingDisplay::new);
    }

}
