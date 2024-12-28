package com.unlikepaladin.pfm.runtime.data;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.JsonOps;
import com.unlikepaladin.pfm.PaladinFurnitureMod;
import com.unlikepaladin.pfm.blocks.*;
import com.unlikepaladin.pfm.blocks.models.ModelHelper;
import com.unlikepaladin.pfm.data.FurnitureBlock;
import com.unlikepaladin.pfm.data.materials.VariantBase;
import com.unlikepaladin.pfm.data.materials.WoodVariant;
import com.unlikepaladin.pfm.data.materials.WoodVariantRegistry;
import com.unlikepaladin.pfm.items.PFMComponents;
import com.unlikepaladin.pfm.registry.PaladinFurnitureModBlocksItems;
import com.unlikepaladin.pfm.runtime.PFMGenerator;
import com.unlikepaladin.pfm.runtime.PFMProvider;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataWriter;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.*;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.*;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class PFMRecipeProvider extends PFMProvider {

    public PFMRecipeProvider(PFMGenerator parent) {
        super(parent);
        parent.setProgress("Generating Recipes");
    }

    // Create a registry wrapper lookup without dynamic entries such as biomes as they don't exist yet
    private static RegistryWrapper.WrapperLookup createWrapperLookup() {
        RegistryBuilder builder = new RegistryBuilder();
        return builder.createWrapperLookup(DynamicRegistryManager.of(Registries.REGISTRIES));
    }

    public CompletableFuture<?> run(DataWriter writer) {
        Path path = getParent().getOutput();
        Set<RegistryKey<Recipe<?>>> set = Sets.newHashSet();
        RegistryWrapper.WrapperLookup lookup = createWrapperLookup();
        generateRecipes(new RecipeExporter() {
            @Override
            public void accept(RegistryKey<Recipe<?>> recipeId, Recipe<?> recipe, @Nullable AdvancementEntry advancementEntry) {
                if (!set.add(recipeId)) {
                    getParent().getLogger().error("Duplicate recipe " + recipeId);
                    throw new IllegalStateException("Duplicate recipe " + recipeId);
                }
                if (recipe == null) {
                    getParent().getLogger().error("Recipe Json Provider is null");
                    throw new IllegalStateException("Recipe Json Provider is null");
                } else {
                    RegistryOps<JsonElement> ops = lookup.getOps(JsonOps.INSTANCE);
                    saveRecipe(Recipe.CODEC.encodeStart(ops, recipe).getOrThrow(IllegalStateException::new), path.resolve("data/" + recipeId.getValue().getNamespace() + "/recipe/" + recipeId.getValue().getPath() + ".json"));
                    if (advancementEntry != null) {
                        saveRecipeAdvancement(Advancement.CODEC.encodeStart(ops, advancementEntry.value()).getOrThrow(IllegalStateException::new), path.resolve("data/" + recipeId.getValue().getNamespace() + "/advancement/" + advancementEntry.id().getPath() + ".json"));
                    }
                }
            }

            @Override
            @SuppressWarnings("Deprecated")
            public Advancement.Builder getAdvancementBuilder() {
                return Advancement.Builder.createUntelemetered().parent(CraftingRecipeJsonBuilder.ROOT);
            }

            @Override
            public void addRootAdvancement() {

            }
        });
        return CompletableFuture.allOf();
    }

    public String getName() {
        return "PFM Recipes";
    }

    private void saveRecipe(JsonElement json, Path path) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8))){
            Files.createDirectories(path.getParent());
            Files.createFile(path);
            jsonWriter.setSerializeNulls(false);
            jsonWriter.setIndent("  ");
            JsonHelper.writeSorted(jsonWriter, json, JSON_KEY_SORTING_COMPARATOR);
            jsonWriter.flush();
            Files.write(path, byteArrayOutputStream.toByteArray(), StandardOpenOption.WRITE);
            byteArrayOutputStream.close();
        }
        catch (Exception exception) {
            getParent().getLogger().error("Couldn't save {}", path, exception);
        }
    }

    private void saveRecipeAdvancement(JsonElement json, Path path) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8))){
            Files.createDirectories(path.getParent());
            Files.createFile(path);
            jsonWriter.setSerializeNulls(false);
            jsonWriter.setIndent("  ");
            JsonHelper.writeSorted(jsonWriter, json, JSON_KEY_SORTING_COMPARATOR);
            jsonWriter.flush();
            Files.write(path, byteArrayOutputStream.toByteArray(), StandardOpenOption.WRITE);
            byteArrayOutputStream.close();
        }
        catch (Exception exception) {
            getParent().getLogger().error("Couldn't save {}", path, exception);
        }
    }
    @ExpectPlatform
    protected static Identifier getId(Block block) {
        throw new AssertionError();    
    }
    protected void generateRecipes(RecipeExporter exporter) {
        List<Identifier> generatedRecipes = new ArrayList<>();
        PaladinFurnitureMod.furnitureEntryMap.get(BasicChairBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerBasicChairRecipe(block.asItem(), Ingredient.ofItems(variantBase.getSecondaryBlock()), Ingredient.ofItems(variantBase.getBaseBlock()), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(BasicChairBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerBasicChairRecipe(block.asItem(), Ingredient.ofItems(variantBase.getBaseBlock()), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(DinnerChairBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerDinnerChairRecipe(block.asItem(), Ingredient.ofItems(variantBase.getSecondaryBlock()), Ingredient.ofItems(variantBase.getBaseBlock()), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(DinnerChairBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerDinnerChairRecipe(block.asItem(), Ingredient.ofItems(variantBase.getBaseBlock()), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(ClassicChairBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerClassicChairRecipe(block.asItem(), Ingredient.ofItems(variantBase.getSecondaryBlock()), Ingredient.ofItems(variantBase.getBaseBlock()), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(ClassicChairBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerClassicChairRecipe(block.asItem(), Ingredient.ofItems(variantBase.getBaseBlock()), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        FurnitureBlock[] froggyChairs = FroggyChairBlock.streamFroggyChair().toList().toArray(new FurnitureBlock[0]);
        for (FurnitureBlock froggyChair : froggyChairs) {
            if (!generatedRecipes.contains(getId(froggyChair.getBlock()))) {
                offerFroggyChairRecipe(froggyChair.getBlock(), Ingredient.ofItems(froggyChair.getFroggyChairMaterial().asItem()), exporter);
                generatedRecipes.add(getId(froggyChair.getBlock()));
            }
        }
        FurnitureBlock[] woolClassicChairs = ClassicChairDyeableBlock.streamWoodDyeableChair().toList().toArray(new FurnitureBlock[0]);
        for (FurnitureBlock classicChair : woolClassicChairs) {
            if (!generatedRecipes.contains(getId(classicChair.getBlock()))) {
                offerClassicChairRecipe(classicChair.getBlock(), Ingredient.ofItems(Items.OAK_LOG), Ingredient.ofItems(classicChair.getArmChairMaterial()), exporter);
                generatedRecipes.add(getId(classicChair.getBlock()));
            }
        }
        PaladinFurnitureMod.furnitureEntryMap.get(ModernChairBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerModernChairRecipe(block.asItem(), Ingredient.ofItems(variantBase.getSecondaryBlock()), Ingredient.ofItems(variantBase.getBaseBlock()), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(ModernChairBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerModernChairRecipe(block.asItem(), Ingredient.ofItems(variantBase.getBaseBlock()), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        FurnitureBlock[] armChairs = ArmChairColoredBlock.streamArmChairColored().toList().toArray(new FurnitureBlock[0]);
        for (FurnitureBlock armChair : armChairs) {
            if (!generatedRecipes.contains(getId(armChair.getBlock()))) {
                offerArmChairRecipe(armChair.getBlock(), Ingredient.ofItems(Items.OAK_LOG), Ingredient.ofItems(armChair.getArmChairMaterial().asItem()), exporter);
                generatedRecipes.add(getId(armChair.getBlock()));
            }
        }
        FurnitureBlock[] simpleSofas = SimpleSofaBlock.streamSimpleSofas().toList().toArray(new FurnitureBlock[0]);
        for (FurnitureBlock sofa : simpleSofas) {
            if (!generatedRecipes.contains(getId(sofa.getBlock()))) {
                offerSimpleSofaRecipe(sofa.getBlock(), Ingredient.ofItems(Items.OAK_LOG), Ingredient.ofItems(sofa.getArmChairMaterial().asItem()), exporter);
                generatedRecipes.add(getId(sofa.getBlock()));
            }
        }
        FurnitureBlock[] leatherArmChairs =  ArmChairBlock.streamArmChairs().toList().toArray(new FurnitureBlock[0]);
        for (FurnitureBlock armChair : leatherArmChairs) {
            if (!generatedRecipes.contains(getId(armChair.getBlock()))) {
                offerArmChairRecipe(armChair.getBlock(), Ingredient.ofItems(Items.OAK_LOG), Ingredient.ofItems(armChair.getArmChairMaterial().asItem()), exporter);
                generatedRecipes.add(getId(armChair.getBlock()));
            }
        }
        PaladinFurnitureMod.furnitureEntryMap.get(BasicTableBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerBasicTableRecipe(block.asItem(), Ingredient.ofItems(variantBase.getSecondaryBlock()), Ingredient.ofItems(variantBase.getBaseBlock()), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(BasicTableBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerBasicTableRecipe(block.asItem(), Ingredient.ofItems(variantBase.getBaseBlock()), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(ClassicTableBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerClassicTableRecipe(block.asItem(), Ingredient.ofItems(variantBase.getSecondaryBlock()), Ingredient.ofItems(variantBase.getBaseBlock()), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(ClassicTableBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerClassicTableRecipe(block.asItem(), Ingredient.ofItems(variantBase.getBaseBlock()), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(LogTableBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                if (block.toString().contains("raw")){
                    offerLogTableRecipe(block.asItem(), Ingredient.ofItems(variantBase.getSecondaryBlock()), Ingredient.ofItems(variantBase.getSecondaryBlock()), exporter);
                } else {
                    offerLogTableRecipe(block.asItem(), Ingredient.ofItems(variantBase.getSecondaryBlock()), Ingredient.ofItems(variantBase.getBaseBlock()), exporter);
                }
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(LogTableBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                if (block.toString().contains("raw") && block.toString().contains("stripped")){
                    offerLogTableRecipe(block.asItem(), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), exporter);
                } else {
                    offerLogTableRecipe(block.asItem(), Ingredient.ofItems(variantBase.getBaseBlock()), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), exporter);
                }
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(RawLogTableBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerLogTableRecipe(block.asItem(), Ingredient.ofItems(variantBase.getSecondaryBlock()), Ingredient.ofItems(variantBase.getSecondaryBlock()), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(RawLogTableBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerLogTableRecipe(block.asItem(), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(DinnerTableBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerDinnerTableRecipe(block.asItem(), Ingredient.ofItems(variantBase.getSecondaryBlock()), Ingredient.ofItems(variantBase.getBaseBlock()), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(DinnerTableBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerDinnerTableRecipe(block.asItem(), Ingredient.ofItems(variantBase.getBaseBlock()), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(ModernDinnerTableBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerModernDinnerTableRecipe(block.asItem(), Ingredient.ofItems(variantBase.getSecondaryBlock()), Ingredient.ofItems(variantBase.getBaseBlock()), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(ModernDinnerTableBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerModernDinnerTableRecipe(block.asItem(), Ingredient.ofItems(variantBase.getBaseBlock()), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(ClassicNightstandBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerClassicNightStandRecipe(block.asItem(), Ingredient.ofItems(variantBase.getSecondaryBlock()), Ingredient.ofItems(variantBase.getBaseBlock()), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(ClassicNightstandBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerClassicNightStandRecipe(block.asItem(), Ingredient.ofItems(variantBase.getBaseBlock()), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(SimpleBedBlock.class).getVariantToBlockMapList().forEach((variantBase, blockList) -> {
            blockList.forEach(block -> {
                if (!generatedRecipes.contains(getId(block))) {
                    offerSimpleBedRecipe(block.asItem(), Ingredient.ofItems(variantBase.getBaseBlock()), Ingredient.ofItems(getVanillaBed(block)), exporter);
                    generatedRecipes.add(getId(block));
                }
            });
        });
        PaladinFurnitureMod.furnitureEntryMap.get(ClassicBedBlock.class).getVariantToBlockMapList().forEach((variantBase, blockList) -> {
            blockList.forEach(block -> {
                if (!generatedRecipes.contains(getId(block))) {
                    offerClassicBedRecipe(block.asItem(), Ingredient.ofItems(variantBase.getBaseBlock()), Ingredient.ofItems(getVanillaBed(block)), Ingredient.ofItems((Block) variantBase.getChild("fence")), exporter);
                    generatedRecipes.add(getId(block));
                }
            });
        });
        PaladinFurnitureMod.furnitureEntryMap.get(SimpleBunkLadderBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerSimpleBunkLadderRecipe(block.asItem(), Ingredient.ofItems(variantBase.getBaseBlock()), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(LogStoolBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerLogStoolRecipe(block.asItem(), Ingredient.ofItems(variantBase.getSecondaryBlock()), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(SimpleStoolBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerSimpleStoolRecipe(block.asItem(), Ingredient.ofItems(variantBase.getSecondaryBlock()), Ingredient.ofItems(variantBase.getBaseBlock()), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(SimpleStoolBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerSimpleStoolRecipe(block.asItem(), Ingredient.ofItems(variantBase.getBaseBlock()), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(ClassicStoolBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerClassicStoolRecipe(block.asItem(), Ingredient.ofItems(variantBase.getSecondaryBlock()), Ingredient.ofItems(variantBase.getBaseBlock()), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(ClassicStoolBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerClassicStoolRecipe(block.asItem(), Ingredient.ofItems(variantBase.getBaseBlock()), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(ModernStoolBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerModernStoolRecipe(block.asItem(), Ingredient.ofItems(variantBase.getSecondaryBlock()), Ingredient.ofItems(variantBase.getBaseBlock()), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(ModernStoolBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerModernStoolRecipe(block.asItem(), Ingredient.ofItems(variantBase.getBaseBlock()), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), exporter);
                generatedRecipes.add(getId(block));
            }
        });

        PaladinFurnitureMod.furnitureEntryMap.get(KitchenCounterBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                Pair<Block, Block> materials = getCounterMaterials(variantBase);
                offerCounterRecipe(block.asItem(), Ingredient.ofItems(materials.getLeft()), Ingredient.ofItems(materials.getRight()), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(KitchenCounterBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerCounterRecipe(block.asItem(), Ingredient.ofItems(variantBase.getBaseBlock()), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(KitchenSinkBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                Pair<Block, Block> materials = getCounterMaterials(variantBase);
                offerKitchenSinkRecipe(block.asItem(), Ingredient.ofItems(materials.getLeft()), Ingredient.ofItems(materials.getRight()), Ingredient.ofItems(Items.BUCKET), Ingredient.ofItems(Items.IRON_INGOT), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(KitchenSinkBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerKitchenSinkRecipe(block.asItem(), Ingredient.ofItems(variantBase.getBaseBlock()), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), Ingredient.ofItems(Items.BUCKET), Ingredient.ofItems(Items.IRON_INGOT), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(KitchenDrawerBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                Pair<Block, Block> materials = getCounterMaterials(variantBase);
                offerCounterApplianceRecipe(block.asItem(), Ingredient.ofItems(materials.getLeft()), Ingredient.ofItems(materials.getRight()), Ingredient.ofItems(Items.CHEST), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(KitchenDrawerBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerCounterApplianceRecipe(block.asItem(), Ingredient.ofItems(variantBase.getBaseBlock()), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), Ingredient.ofItems(Items.CHEST), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(KitchenCounterOvenBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                Pair<Block, Block> materials = getCounterMaterials(variantBase);
                offerCounterApplianceRecipe(block.asItem(), Ingredient.ofItems(materials.getLeft()), Ingredient.ofItems(materials.getRight()), Ingredient.ofItems(Items.FURNACE), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(KitchenCounterOvenBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerCounterApplianceRecipe(block.asItem(), Ingredient.ofItems(variantBase.getBaseBlock()), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), Ingredient.ofItems(Items.FURNACE), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(KitchenWallCounterBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                Pair<Block, Block> materials = getCounterMaterials(variantBase);
                offerCounterRecipe(block.asItem(), Ingredient.ofItems(materials.getLeft()), Ingredient.ofItems(materials.getLeft()), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(KitchenWallCounterBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerCounterRecipe(block.asItem(), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(KitchenWallDrawerBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                Pair<Block, Block> materials = getCounterMaterials(variantBase);
                offerWallDrawerRecipe(block.asItem(), Ingredient.ofItems(materials.getRight()), Ingredient.ofItems(materials.getLeft()), Ingredient.ofItems(Items.CHEST), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(KitchenWallDrawerBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerWallDrawerRecipe(block.asItem(), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), Ingredient.ofItems(variantBase.getBaseBlock()), Ingredient.ofItems(Items.CHEST), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(KitchenWallDrawerSmallBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                Pair<Block, Block> materials = getCounterMaterials(variantBase);
                offerWallDrawerSmallRecipe(block.asItem(), Ingredient.ofItems(materials.getRight()), Ingredient.ofItems(materials.getLeft()), Ingredient.ofItems(Items.CHEST), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(KitchenWallDrawerSmallBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerWallDrawerSmallRecipe(block.asItem(), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), Ingredient.ofItems(Items.CHEST), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(KitchenCabinetBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                Pair<Block, Block> materials = getCounterMaterials(variantBase);
                offerCabinetRecipe(block.asItem(), Ingredient.ofItems(materials.getRight()), Ingredient.ofItems(materials.getRight()), Ingredient.ofItems(Items.CHEST), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(KitchenCabinetBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerCabinetRecipe(block.asItem(), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), Ingredient.ofItems(Items.CHEST), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        FurnitureBlock[] herringbonePlanks = HerringbonePlankBlock.streamPlanks().toList().toArray(new FurnitureBlock[0]);
        for (FurnitureBlock herringbonePlank : herringbonePlanks) {
            if (!generatedRecipes.contains(getId(herringbonePlank.getBlock()))) {
                offerHerringbonePlanks(herringbonePlank.getBlock(), herringbonePlank.getSlab().asItem(), exporter);
            }
        }
        FurnitureBlock[] fridges = FridgeBlock.streamFridges().toList().toArray(new FurnitureBlock[0]);
        for (FurnitureBlock fridge : fridges) {
            if (!generatedRecipes.contains(getId(fridge.getBlock()))) {
                offerFridgeRecipe(fridge.getBlock(), Ingredient.ofItems(fridge.getFridgeMaterial().asItem()), Ingredient.ofItems(Items.CHEST), exporter);
                generatedRecipes.add(getId(fridge.getBlock()));
            }
        }
        FurnitureBlock[] freezers = FreezerBlock.streamFreezers().toList().toArray(new FurnitureBlock[0]);
        for (FurnitureBlock freezer : freezers) {
            if (!generatedRecipes.contains(getId(freezer.getBlock()))) {
                offerFreezerRecipe(freezer.getBlock(), Ingredient.ofItems(freezer.getFridgeMaterial().asItem()), exporter);
                generatedRecipes.add(getId(freezer.getBlock()));
            }
        }
        FurnitureBlock[] microwaves = MicrowaveBlock.streamMicrowaves().toList().toArray(new FurnitureBlock[0]);
        for (FurnitureBlock microwave : microwaves) {
            if (!generatedRecipes.contains(getId(microwave.getBlock()))) {
                offerMicrowaveRecipe(microwave.getBlock(),  Ingredient.ofItems(microwave.getFridgeMaterial().asItem()), Ingredient.ofItems(Items.FURNACE), exporter);
                generatedRecipes.add(getId(microwave.getBlock()));
            }
        }
        FurnitureBlock[] rangeHoods = KitchenRangeHoodBlock.streamOvenRangeHoods().toList().toArray(new FurnitureBlock[0]);
        for (FurnitureBlock rangeHood : rangeHoods) {
            if (!generatedRecipes.contains(getId(rangeHood.getBlock()))) {
                offerRangeHoodRecipe(rangeHood.getBlock(),  Ingredient.ofItems(rangeHood.getFridgeMaterial().asItem()), Ingredient.ofItems(Items.REDSTONE_LAMP), exporter);
                generatedRecipes.add(getId(rangeHood.getBlock()));
            }
        }
        FurnitureBlock[] stoves = StoveBlock.streamStoves().toList().toArray(new FurnitureBlock[0]);
        for (FurnitureBlock stove : stoves) {
            if (!generatedRecipes.contains(getId(stove.getBlock()))) {
                offerStoveRecipe(stove.getBlock(),  Ingredient.ofItems(stove.getFridgeMaterial().asItem()), Ingredient.ofItems(Items.FURNACE), exporter);
                generatedRecipes.add(getId(stove.getBlock()));
            }
        }
        FurnitureBlock[] ironStove = IronStoveBlock.streamIronStoves().toList().toArray(new FurnitureBlock[0]);
        for (FurnitureBlock stove : ironStove) {
            if (!generatedRecipes.contains(getId(stove.getBlock()))) {
                offerStoveRecipe(stove.getBlock(),  Ingredient.ofItems(stove.getFridgeMaterial().asItem()), Ingredient.ofItems(Items.FURNACE), exporter);
                generatedRecipes.add(getId(stove.getBlock()));
            }
        }
        KitchenStovetopBlock[] stovetopBlocks = KitchenStovetopBlock.streamKitchenStovetop().toList().toArray(new KitchenStovetopBlock[0]);
        for (KitchenStovetopBlock stove : stovetopBlocks) {
            if (!generatedRecipes.contains(getId(stove))) {
                offerStovetopRecipe(stove, Ingredient.ofItems(Items.IRON_INGOT), Ingredient.ofItems(Blocks.GRAY_CONCRETE), exporter);
                generatedRecipes.add(getId(stove));
            }
        }
        FurnitureBlock[] plates = PlateBlock.streamPlates().toList().toArray(new FurnitureBlock[0]);
        for (FurnitureBlock plate : plates) {
            if (!generatedRecipes.contains(getId(plate.getBlock()))) {
                offerPlateRecipe(plate.getBlock(), Ingredient.ofItems(plate.getPlateMaterial()), Ingredient.ofItems(Items.ITEM_FRAME), Ingredient.ofItems(plate.getPlateDecoration()), exporter);
                generatedRecipes.add(getId(plate.getBlock()));
            }
        }
        FurnitureBlock[] cutleries = CutleryBlock.streamCutlery().toList().toArray(new FurnitureBlock[0]);
        for (FurnitureBlock cutlery : cutleries) {
            if (!generatedRecipes.contains(getId(cutlery.getBlock()))) {
                offerCutleryRecipe(cutlery.getBlock(), Ingredient.ofItems(cutlery.getCutleryMaterial()), exporter);
                generatedRecipes.add(getId(cutlery.getBlock()));
            }
        }
        PaladinFurnitureMod.furnitureEntryMap.get(PendantBlock.class).getAllBlocks().forEach((block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                Block hang = Blocks.LIGHT_GRAY_CONCRETE;
                Block base;
                if (block == PaladinFurnitureModBlocksItems.GLASS_MODERN_PENDANT)
                    base = Blocks.WHITE_STAINED_GLASS;
                else if (block == PaladinFurnitureModBlocksItems.WHITE_MODERN_PENDANT)
                    base = Blocks.WHITE_CONCRETE;
                else
                    base = Blocks.GRAY_CONCRETE;
                offerPendantRecipe(block.asItem(), Ingredient.ofItems(base), Ingredient.ofItems(hang), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        if (!generatedRecipes.contains(Registries.ITEM.getId(PaladinFurnitureModBlocksItems.LIGHT_SWITCH_ITEM))) {
            FurnitureRecipeJsonFactory.create(PaladinFurnitureModBlocksItems.LIGHT_SWITCH_ITEM.getDefaultStack().copyWithCount(6)).input(Blocks.WHITE_CONCRETE, 6).input(Blocks.LIGHT_GRAY_CONCRETE, 2).input(Items.REDSTONE).offerTo(exporter, Identifier.of("pfm", PaladinFurnitureModBlocksItems.LIGHT_SWITCH_ITEM.getTranslationKey().replace("block.pfm.", "")));
            generatedRecipes.add(Registries.ITEM.getId(PaladinFurnitureModBlocksItems.LIGHT_SWITCH_ITEM));
        }

        FurnitureBlock[] basicToilets = BasicToiletBlock.streamBasicToilet().toList().toArray(new FurnitureBlock[0]);
        for (FurnitureBlock toilet : basicToilets) {
            if (!generatedRecipes.contains(getId(toilet.getBlock()))) {
                offerToiletRecipe(toilet.getBlock(), Ingredient.ofItems(Items.STONE_BUTTON), Ingredient.ofItems(Blocks.QUARTZ_BLOCK), exporter);
                generatedRecipes.add(getId(toilet.getBlock()));
            }
        }

        if (!generatedRecipes.contains(getId(PaladinFurnitureModBlocksItems.WALL_TOILET_PAPER))) {
            offerWallToiletPaperRecipe(PaladinFurnitureModBlocksItems.WALL_TOILET_PAPER,  Ingredient.ofItems(Blocks.STONE), exporter);
            generatedRecipes.add(getId(PaladinFurnitureModBlocksItems.WALL_TOILET_PAPER));
        }

        if (!generatedRecipes.contains(getId(PaladinFurnitureModBlocksItems.BASIC_SINK))) {
            offerSinkRecipe(PaladinFurnitureModBlocksItems.BASIC_SINK, Ingredient.ofItems(Blocks.QUARTZ_BLOCK), exporter);
            generatedRecipes.add(getId(PaladinFurnitureModBlocksItems.BASIC_SINK));
        }

        if (!generatedRecipes.contains(getId(PaladinFurnitureModBlocksItems.BASIC_BATHTUB))) {
            offerBathtubRecipe(PaladinFurnitureModBlocksItems.BASIC_BATHTUB, Ingredient.ofItems(Blocks.QUARTZ_BLOCK), exporter);
            generatedRecipes.add(getId(PaladinFurnitureModBlocksItems.BASIC_BATHTUB));
        }

        if (!generatedRecipes.contains(getId(PaladinFurnitureModBlocksItems.BASIC_SHOWER_HEAD))) {
            offerShowerHeadRecipe(PaladinFurnitureModBlocksItems.BASIC_SHOWER_HEAD, Ingredient.ofItems(Items.WATER_BUCKET), exporter);
            offerShowerHandleRecipe(PaladinFurnitureModBlocksItems.BASIC_SHOWER_HANDLE_ITEM.getDefaultStack(), Ingredient.ofItems(Blocks.LEVER), exporter);
            generatedRecipes.add(getId(PaladinFurnitureModBlocksItems.BASIC_SHOWER_HEAD));
            generatedRecipes.add(Registries.ITEM.getId(PaladinFurnitureModBlocksItems.BASIC_SHOWER_HANDLE_ITEM));
        }

        if (!generatedRecipes.contains(getId(PaladinFurnitureModBlocksItems.MESH_TRASHCAN))) {
            FurnitureRecipeJsonFactory.create(PaladinFurnitureModBlocksItems.MESH_TRASHCAN, 1).input(Items.IRON_INGOT, 1).input(Items.ENDER_PEARL, 1).input(Blocks.IRON_BARS, 4).offerTo(exporter, Identifier.of("pfm", PaladinFurnitureModBlocksItems.MESH_TRASHCAN.asItem().getTranslationKey().replace("block.pfm.", "")));
            generatedRecipes.add(getId(PaladinFurnitureModBlocksItems.MESH_TRASHCAN));
        }
        if (!generatedRecipes.contains(getId(PaladinFurnitureModBlocksItems.TRASHCAN))) {
            FurnitureRecipeJsonFactory.create(PaladinFurnitureModBlocksItems.TRASHCAN, 1).input(Items.IRON_INGOT, 1).input(Items.ENDER_PEARL, 1).input(Blocks.IRON_BARS, 4).offerTo(exporter, Identifier.of("pfm", PaladinFurnitureModBlocksItems.TRASHCAN.asItem().getTranslationKey().replace("block.pfm.", "")));
            generatedRecipes.add(getId(PaladinFurnitureModBlocksItems.TRASHCAN));
        }

        FurnitureBlock[] showerTowels = ShowerTowelBlock.streamShowerTowels().toList().toArray(new FurnitureBlock[0]);
        for (FurnitureBlock towel : showerTowels) {
            if (!generatedRecipes.contains(getId(towel.getBlock()))) {
                offerShowerTowelRecipe(towel.getBlock(),  Ingredient.ofItems(towel.getWoolColor()), exporter);
                generatedRecipes.add(getId(towel.getBlock()));
            }
        }

        FurnitureBlock[] mirrors = MirrorBlock.streamMirrorBlocks().toList().toArray(new FurnitureBlock[0]);
        for (FurnitureBlock mirror : mirrors) {
            if (!generatedRecipes.contains(getId(mirror.getBlock()))) {
                offerMirrorRecipe(mirror.getBlock(), Ingredient.ofItems(mirror.getBaseMaterial()), exporter);
                generatedRecipes.add(getId(mirror.getBlock()));
            }
        }

        offerLampRecipes(exporter);

        PaladinFurnitureMod.furnitureEntryMap.get(BasicCoffeeTableBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerBasicCoffeeTableRecipe(block.asItem(), Ingredient.ofItems(variantBase.getSecondaryBlock()), Ingredient.ofItems(variantBase.getBaseBlock()), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(BasicCoffeeTableBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerBasicCoffeeTableRecipe(block.asItem(), Ingredient.ofItems(variantBase.getBaseBlock()), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), exporter);
                generatedRecipes.add(getId(block));
            }
        });

        PaladinFurnitureMod.furnitureEntryMap.get(ModernCoffeeTableBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerModernCoffeeTableRecipe(block.asItem(), Ingredient.ofItems(variantBase.getSecondaryBlock()), Ingredient.ofItems(variantBase.getBaseBlock()), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(ModernCoffeeTableBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerModernCoffeeTableRecipe(block.asItem(), Ingredient.ofItems(variantBase.getBaseBlock()), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), exporter);
                generatedRecipes.add(getId(block));
            }
        });

        PaladinFurnitureMod.furnitureEntryMap.get(ClassicCoffeeTableBlock.class).getVariantToBlockMap().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerClassicCoffeeTableRecipe(block.asItem(), Ingredient.ofItems(variantBase.getSecondaryBlock()), Ingredient.ofItems(variantBase.getBaseBlock()), exporter);
                generatedRecipes.add(getId(block));
            }
        });
        PaladinFurnitureMod.furnitureEntryMap.get(ClassicCoffeeTableBlock.class).getVariantToBlockMapNonBase().forEach((variantBase, block) -> {
            if (!generatedRecipes.contains(getId(block))) {
                offerClassicCoffeeTableRecipe(block.asItem(), Ingredient.ofItems(variantBase.getBaseBlock()), Ingredient.ofItems((Block)variantBase.getChild("stripped_log")), exporter);
                generatedRecipes.add(getId(block));
            }
        });

        PaladinFurnitureMod.pfmModCompatibilities.forEach(pfmModCompatibility -> pfmModCompatibility.generateRecipes(exporter));
    }

    public static void offerLampRecipes(RecipeExporter exporter) {
        for (WoodVariant variant : WoodVariantRegistry.getVariants()) {
            for (DyeColor color : DyeColor.values()) {
                ItemStack stack = new ItemStack(PaladinFurnitureModBlocksItems.BASIC_LAMP);
                stack.set(PFMComponents.VARIANT_COMPONENT, variant.identifier);
                stack.set(PFMComponents.COLOR_COMPONENT, color);
                FurnitureRecipeJsonFactory.create(stack).input(ModelHelper.getWoolColor(color.asString()), 3).input((Block)variant.getChild("stripped_log"), 2).offerTo(exporter, Identifier.of("pfm", String.format("basic_%s_%s_lamp", color.asString(), variant.asString())));
            }
        }
    }

    public static Pair<Block, Block> getCounterMaterials(VariantBase<?> variantBase) {
        Block counterTop = variantBase.getSecondaryBlock();
        Block counterBase = variantBase.getBaseBlock();

        switch (variantBase.identifier.getPath()) {
            case "granite" -> {
                counterTop = Blocks.POLISHED_GRANITE;
                counterBase = Blocks.WHITE_TERRACOTTA;
            }
            case "calcite", "netherite" -> {
                Block temp = counterBase;
                counterBase = counterTop;
                counterTop = temp;
            }
            case "andesite" -> {
                counterTop = Blocks.POLISHED_ANDESITE;
                counterBase = Blocks.STRIPPED_OAK_LOG;
            }
            case "deepslate" -> {
                counterTop = Blocks.POLISHED_DEEPSLATE;
                counterBase = Blocks.DARK_OAK_PLANKS;
            }
            case "blackstone" -> {
                counterTop = Blocks.POLISHED_BLACKSTONE;
                counterBase = Blocks.CRIMSON_PLANKS;
            }
        }
        return new Pair<>(counterBase, counterTop);
    }
    public Block getVanillaBed(Block block) {
        if (block instanceof SimpleBedBlock){
            String color = ((SimpleBedBlock) block).getPFMColor().getName();
            return Registries.BLOCK.get(Identifier.of("minecraft:" + color + "_bed"));
        }
        return null;
    }

    public static void offerBasicChairRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient baseMaterial, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 4).group("chairs").criterion(getCriterionNameFromOutput(output), conditionsFromIngredient(baseMaterial)).input(legMaterial, 2).input(baseMaterial, 4).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerFroggyChairRecipe(ItemConvertible output, Ingredient baseMaterial, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 4).group("chairs").criterion("has_concrete", conditionsFromIngredient(baseMaterial)).input(baseMaterial, 6).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerDinnerChairRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient baseMaterial, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 4).group("chairs").criterion(getCriterionNameFromOutput(output), conditionsFromIngredient(baseMaterial)).input(legMaterial, 3).input(baseMaterial, 3).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerClassicChairRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient baseMaterial, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 4).group("chairs").criterion(getCriterionNameFromOutput(output), conditionsFromIngredient(baseMaterial)).input(legMaterial, 4).input(baseMaterial, 2).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerModernChairRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient baseMaterial, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 4).group("chairs").criterion(getCriterionNameFromOutput(output), conditionsFromIngredient(baseMaterial)).input(legMaterial, 3).input(baseMaterial,3).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }
    public static void offerArmChairRecipe(ItemConvertible output, Ingredient baseMaterial, Ingredient legMaterial, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 2).group("chairs").criterion("has_wool", conditionsFromIngredient(baseMaterial)).input(legMaterial, 4).input(baseMaterial, 2).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerSimpleSofaRecipe(ItemConvertible output, Ingredient baseMaterial, Ingredient legMaterial, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 2).group("chairs").criterion("has_wool", conditionsFromIngredient(baseMaterial)).input(legMaterial, 2).input(baseMaterial, 4).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerBasicTableRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient baseMaterial, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 4).group("tables").criterion(getCriterionNameFromOutput(output), conditionsFromIngredient(baseMaterial)).input(legMaterial, 5).input(baseMaterial, 3).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerBasicCoffeeTableRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient baseMaterial, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 4).group("tables").criterion(getCriterionNameFromOutput(output), conditionsFromIngredient(baseMaterial)).input(legMaterial, 3).input(baseMaterial, 3).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerModernCoffeeTableRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient baseMaterial, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 4).group("tables").criterion(getCriterionNameFromOutput(output), conditionsFromIngredient(baseMaterial)).input(legMaterial, 4).input(baseMaterial, 3).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerClassicCoffeeTableRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient baseMaterial, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 4).group("tables").criterion(getCriterionNameFromOutput(output), conditionsFromIngredient(baseMaterial)).input(legMaterial, 2).input(baseMaterial, 3).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerClassicTableRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient baseMaterial, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 4).group("tables").criterion(getCriterionNameFromOutput(output), conditionsFromIngredient(baseMaterial)).input(legMaterial, 4).input(baseMaterial, 3).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerLogTableRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient baseMaterial, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 4).group("tables").criterion(getCriterionNameFromOutput(output, "has_log"), conditionsFromIngredient(baseMaterial)).input(legMaterial, 2).input(baseMaterial, 3).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerHerringbonePlanks(ItemConvertible output, Item baseMaterial, RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(createWrapperLookup().getOrThrow(RegistryKeys.ITEM), RecipeCategory.BUILDING_BLOCKS, output, 4).input('X', baseMaterial).pattern("XX").pattern("XX").criterion("has_wood_slabs", conditionsFromItem(baseMaterial)).offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE ,Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", ""))));
    }

    public static void offerDinnerTableRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient baseMaterial, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 4).group("tables").criterion(getCriterionNameFromOutput(output), conditionsFromIngredient(baseMaterial)).input(legMaterial, 3).input(baseMaterial, 3).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerModernDinnerTableRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient baseMaterial, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 4).group("tables").criterion(getCriterionNameFromOutput(output), conditionsFromIngredient(baseMaterial)).input(legMaterial, 5).input(baseMaterial, 3).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerClassicNightStandRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient baseMaterial, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 4).group("bedroom").criterion(getCriterionNameFromOutput(output), conditionsFromIngredient(baseMaterial)).input(legMaterial, 6).input(baseMaterial, 1).input(Blocks.CHEST, 1).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static String getCriterionNameFromOutput(ItemConvertible output) {
        return getCriterionNameFromOutput(output, "");
    }
    public static String getCriterionNameFromOutput(ItemConvertible output, String type) {
        if (Block.getBlockFromItem(output.asItem()) == null || Block.getBlockFromItem(output.asItem()) == Blocks.AIR || !PaladinFurnitureMod.furnitureEntryMap.containsKey(Block.getBlockFromItem(output.asItem()).getClass())) {
            return getItemPath(output);
        }
        if (PaladinFurnitureMod.furnitureEntryMap.get(Block.getBlockFromItem(output.asItem()).getClass()).getVariantFromEntry(Block.getBlockFromItem(output.asItem())) instanceof WoodVariant) {
            return type.isEmpty() ? "has_planks" : type;
        }
        else return getItemPath(output);
    }

    public static void offerSimpleBedRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient baseBed, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 1).group("bedroom").criterion("has_bed", conditionsFromIngredient(baseBed)).input(legMaterial,5).input(baseBed, 1).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerClassicBedRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient baseBed, Ingredient fence, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 1).group("bedroom").criterion("has_bed", conditionsFromIngredient(baseBed)).input(legMaterial, 3).input(baseBed, 1).input(fence, 2).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerSimpleBunkLadderRecipe(ItemConvertible output, Ingredient base, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 4).group("bedroom").input(base, 1).criterion("has_planks", conditionsFromIngredient(base)).input(Ingredient.ofItems(Items.STICK), 6).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerLogStoolRecipe(ItemConvertible output, Ingredient legMaterial, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 4).group("stools").criterion("has_log", conditionsFromIngredient(legMaterial)).input(legMaterial).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerSimpleStoolRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient baseMaterial, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 4).group("stools").criterion("has_planks", conditionsFromIngredient(baseMaterial)).input(legMaterial, 2).input(baseMaterial, 3).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerClassicStoolRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient baseMaterial, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 4).group("stools").criterion("has_planks", conditionsFromIngredient(baseMaterial)).input(legMaterial, 3).input(baseMaterial, 2).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerModernStoolRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient baseMaterial, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 4).group("stools").criterion("has_planks", conditionsFromIngredient(baseMaterial)).input(legMaterial, 1).input(baseMaterial, 3).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerCounterRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient baseMaterial, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 6).group("kitchen").criterion("has_planks", conditionsFromIngredient(baseMaterial)).input(baseMaterial, 6).input(legMaterial, 3).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }
    public static void offerCounterApplianceRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient baseMaterial, Ingredient appliance, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 1).group("kitchen").criterion("has_planks", conditionsFromIngredient(baseMaterial)).input(baseMaterial, 5).input(legMaterial, 3).input(appliance).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerKitchenSinkRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient baseMaterial, Ingredient center, Ingredient ingot, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 1).group("kitchen").criterion("has_planks", conditionsFromIngredient(baseMaterial)).input(baseMaterial, 5).input(legMaterial, 2).input(ingot).input(center).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerWallDrawerRecipe(ItemConvertible output, Ingredient baseMaterial, Ingredient legMaterial, Ingredient appliace, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 1).group("kitchen").criterion("has_planks", conditionsFromIngredient(baseMaterial)).input(legMaterial, 6).input(baseMaterial, 2).input(appliace).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerWallDrawerSmallRecipe(ItemConvertible output, Ingredient baseMaterial, Ingredient legMaterial, Ingredient appliance, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 3).group("kitchen").criterion("has_planks", conditionsFromIngredient(baseMaterial)).input(legMaterial, 3).input(baseMaterial, 2).input(appliance).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerCabinetRecipe(ItemConvertible output, Ingredient baseMaterial, Ingredient legMaterial, Ingredient chest, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 1).group("kitchen").criterion("has_planks", conditionsFromIngredient(baseMaterial)).input(legMaterial,6).input(baseMaterial,2).input(chest).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerFridgeRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient storage, RecipeExporter exporter) {
        if (output.asItem().toString().contains("xbox")) {
            FurnitureRecipeJsonFactory.create(output, 1).group("kitchen").criterion("has_" + getItemPath(legMaterial), conditionsFromIngredient(legMaterial)).input(legMaterial, 6).input(storage, 1).input(Ingredient.ofItems(Items.REDSTONE)).input(Ingredient.ofItems(Items.WHITE_CONCRETE)).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
        }
        else {
            FurnitureRecipeJsonFactory.create(output, 1).group("kitchen").criterion("has_" + getItemPath(legMaterial), conditionsFromIngredient(legMaterial)).input(legMaterial, 7).input(storage).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
        }
    }

    public static void offerFreezerRecipe(ItemConvertible output, Ingredient legMaterial, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 1).group("kitchen").criterion("has_" + getItemPath(legMaterial), conditionsFromIngredient(legMaterial)).input(legMaterial, 7).input(Ingredient.ofItems(Items.REDSTONE), 2).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerMicrowaveRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient storage, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 1).group("kitchen").criterion("has_" + getItemPath(legMaterial), conditionsFromIngredient(legMaterial)).input(legMaterial ,3).input(storage).input(Ingredient.ofItems(Items.REDSTONE)).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerRangeHoodRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient secondMaterial, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 1).group("kitchen").criterion("has_" + getItemPath(legMaterial), conditionsFromIngredient(legMaterial)).input(legMaterial, 4).input(secondMaterial).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerStoveRecipe(ItemConvertible output, Ingredient legMaterial, Ingredient storage, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 1).group("kitchen").criterion("has_" + getItemPath(legMaterial), conditionsFromIngredient(legMaterial)).input(legMaterial, 8).input(storage).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerStovetopRecipe(ItemConvertible output, Ingredient base, Ingredient material, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 1).group("kitchen").criterion("has_" + getItemPath(base), conditionsFromIngredient(base)).input(base, 6).input(material, 2).input(Ingredient.ofItems(Items.FLINT_AND_STEEL)).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerPlateRecipe(ItemConvertible output, Ingredient base, Ingredient frame, Ingredient decoration, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 4).group("kitchen").criterion("has_" + getItemPath(base), conditionsFromIngredient(base)).input(base, 4).input(frame).input(decoration, 4).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }
    public static void offerCutleryRecipe(ItemConvertible output, Ingredient base, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 4).group("kitchen").criterion("has_" + getItemPath(base), conditionsFromIngredient(base)).input(base, 4).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerPendantRecipe(ItemConvertible output, Ingredient base,Ingredient hang, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 4).group("lighting").criterion("has_" + getItemPath(base), conditionsFromIngredient(base)).input(base, 2).input(hang, 2).input(PaladinFurnitureModBlocksItems.SIMPLE_LIGHT).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerToiletRecipe(ItemConvertible output, Ingredient base, Ingredient material, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 1).group("bathroom").criterion("has_" + getItemPath(material), conditionsFromIngredient(material)).input(base).input(material, 4).input(Ingredient.ofItems(Items.BUCKET)).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerWallToiletPaperRecipe(ItemConvertible output, Ingredient base, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 1).group("bathroom").criterion("has_" + getItemPath(Items.PAPER), conditionsFromItem(Items.PAPER)).input(base, 1).input(Items.PAPER, 8).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerSinkRecipe(ItemConvertible output, Ingredient base, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 1).group("bathroom").criterion("has_" + getItemPath(base), conditionsFromIngredient(base)).input(base, 3).input(Items.STONE_BUTTON, 2).input(Items.IRON_INGOT, 1).input(Items.BUCKET, 1).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerBathtubRecipe(ItemConvertible output, Ingredient base, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 1).group("bathroom").criterion("has_" + getItemPath(base), conditionsFromIngredient(base)).input(base, 5).input(Items.STONE_BUTTON, 2).input(Items.BUCKET, 1).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerShowerHeadRecipe(ItemConvertible output, Ingredient base, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 1).group("bathroom").criterion("has_" + getItemPath(base), conditionsFromIngredient(base)).input(base, 1).input(Items.REDSTONE, 1).input(Items.IRON_INGOT, 1).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerShowerHandleRecipe(ItemStack output, Ingredient base, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output).group("bathroom").criterion("has_" + getItemPath(base), conditionsFromIngredient(base)).input(base, 2).input(Items.REDSTONE, 1).input(Items.IRON_INGOT, 1).offerTo(exporter, Identifier.of("pfm", output.getItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerShowerTowelRecipe(ItemConvertible output, Ingredient base, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 2).group("bathroom").criterion("has_" + getItemPath(base), conditionsFromIngredient(base)).input(base,4).input(Ingredient.ofItems(Items.LIGHT_GRAY_CONCRETE), 2).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    public static void offerMirrorRecipe(ItemConvertible output, Ingredient base, RecipeExporter exporter) {
        FurnitureRecipeJsonFactory.create(output, 2).group("bathroom").criterion("has_" + getItemPath(base), conditionsFromIngredient(base)).input(base,3).input(Ingredient.ofItems(Items.GLASS), 2).offerTo(exporter, Identifier.of("pfm", output.asItem().getTranslationKey().replace("block.pfm.", "")));
    }

    private static AdvancementCriterion<InventoryChangedCriterion.Conditions> conditionsFromItem(NumberRange.IntRange count, ItemConvertible item) {
        return conditionsFromItemPredicates(ItemPredicate.Builder.create().items(createWrapperLookup().getOrThrow(RegistryKeys.ITEM), item).count(count).build());
    }

    public static AdvancementCriterion<InventoryChangedCriterion.Conditions> conditionsFromItem(ItemConvertible item) {
        return conditionsFromItemPredicates(ItemPredicate.Builder.create().items(createWrapperLookup().getOrThrow(RegistryKeys.ITEM), item).build());
    }

    public static AdvancementCriterion<InventoryChangedCriterion.Conditions> conditionsFromIngredient(Ingredient item) {
        List<Item> items = new ArrayList<>();
        for (RegistryEntry<Item> item1:
                item.getMatchingItems()) {
            if (items.contains(item1.value()))
                continue;
            items.add(item1.value());
        }
        return conditionsFromItemPredicates(ItemPredicate.Builder.create().items(null, items.toArray(new Item[0])).build());
    }

    private static AdvancementCriterion<InventoryChangedCriterion.Conditions> conditionsFromTag(TagKey<Item> tag) {
        return conditionsFromItemPredicates(ItemPredicate.Builder.create().tag(createWrapperLookup().getOrThrow(RegistryKeys.ITEM), tag).build());
    }

    public static AdvancementCriterion<InventoryChangedCriterion.Conditions> conditionsFromPredicates(ItemPredicate.Builder... predicates) {
        return conditionsFromItemPredicates(Arrays.stream(predicates).map(ItemPredicate.Builder::build).toArray(ItemPredicate[]::new));
    }

    public static AdvancementCriterion<InventoryChangedCriterion.Conditions> conditionsFromItemPredicates(ItemPredicate... predicates) {
        return Criteria.INVENTORY_CHANGED.create(new InventoryChangedCriterion.Conditions(Optional.empty(), InventoryChangedCriterion.Conditions.Slots.ANY, List.of(predicates)));
    }

    private static String getItemPath(Ingredient item) {
        List<RegistryEntry<Item>> n = item.getMatchingItems();
        if (!n.isEmpty()) {
            return Registries.ITEM.getId(n.getFirst().value()).getPath();
        } else {
            return item.toString();
        }
    }
    private static String getItemPath(ItemConvertible item) {
        return Registries.ITEM.getId(item.asItem()).getPath();
    }
}
