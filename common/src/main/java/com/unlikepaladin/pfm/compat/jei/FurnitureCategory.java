package com.unlikepaladin.pfm.compat.jei;

import com.unlikepaladin.pfm.PaladinFurnitureMod;
import com.unlikepaladin.pfm.recipes.FurnitureRecipe;
import com.unlikepaladin.pfm.registry.PaladinFurnitureModBlocksItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;

public class FurnitureCategory implements IRecipeCategory<FurnitureRecipe> {
    private final IDrawable BACKGROUND;
    public static final Identifier TEXTURE_GUI_VANILLA = new Identifier("pfm:textures/gui/gui_jei.png");
    public final IDrawable ICON;
    public static final Text TITLE = Text.translatable("rei.pfm.furniture");
    private final ICraftingGridHelper craftingGridHelper;
    private static final int craftOutputSlot = 9;
    private static final int craftInputSlot1 = 0;

    public FurnitureCategory(IGuiHelper guiHelper) {
        ICON = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(PaladinFurnitureModBlocksItems.WORKING_TABLE));
        this.BACKGROUND = guiHelper.createDrawable(TEXTURE_GUI_VANILLA, 0, 60, 116, 54);
        craftingGridHelper = guiHelper.createCraftingGridHelper();
    }
    public static final Identifier IDENTIFIER = new Identifier(PaladinFurnitureMod.MOD_ID, "crafting");

    @Override
    public RecipeType<FurnitureRecipe> getRecipeType() {
        return PaladinFurnitureModJEI.FURNITURE_RECIPE;
    }

    @Override
    public Text getTitle() {
        return TITLE;
    }

    @Override
    public IDrawable getBackground() {
        return BACKGROUND;
    }

    @Override
    public IDrawable getIcon() {
        return ICON;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FurnitureRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> ingredientsList = recipe.getIngredients();
        HashMap<Item, Integer> containedItems = new LinkedHashMap<>();
        for (Ingredient ingredient : ingredientsList) {
            for (ItemStack stack : ingredient.getMatchingStacks()) {
                if (!containedItems.containsKey(stack.getItem())) {
                    containedItems.put(stack.getItem(), 1);
                } else {
                    containedItems.put(stack.getItem(), containedItems.get(stack.getItem()) + 1);
                }
            }
        }
        List<Ingredient> finalList = new ArrayList<>();
        for (Map.Entry<Item, Integer> entry: containedItems.entrySet()) {
            finalList.add(Ingredient.ofStacks(new ItemStack(entry.getKey(), entry.getValue())));
        }
        finalList.sort(Comparator.comparing(o -> o.getMatchingStacks()[0].getItem().toString()));
        List<List<ItemStack>> inputLists = new ArrayList<>();
        for (Ingredient input : finalList) {
            ItemStack[] stacks = input.getMatchingStacks();
            List<ItemStack> expandedInput = List.of(stacks);
            inputLists.add(expandedInput);
        }
        ItemStack resultItem = recipe.getOutput();

        int width = getSize(inputLists.size());
        int height = width;
        craftingGridHelper.createAndSetOutputs(builder, VanillaTypes.ITEM_STACK, List.of(resultItem));
        craftingGridHelper.createAndSetInputs(builder, VanillaTypes.ITEM_STACK, inputLists, width, height);
    }

    private static int getSize(int total) {
        if (total > 4) {
            return 3;
        } else if (total > 1) {
            return 2;
        } else {
            return 1;
        }
    }
}
