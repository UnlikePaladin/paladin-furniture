package com.unlikepaladin.pfm.runtime.assets;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.unlikepaladin.pfm.PaladinFurnitureMod;
import com.unlikepaladin.pfm.blocks.*;
import com.unlikepaladin.pfm.data.materials.VariantBase;
import com.unlikepaladin.pfm.data.materials.WoodVariant;
import com.unlikepaladin.pfm.data.materials.WoodVariantRegistry;
import com.unlikepaladin.pfm.mixin.PFMLanguageManagerAccessor;
import com.unlikepaladin.pfm.registry.QuadFunc;
import com.unlikepaladin.pfm.runtime.PFMDataGen;
import com.unlikepaladin.pfm.runtime.PFMRuntimeResources;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.LanguageDefinition;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.client.resource.metadata.LanguageResourceMetadata;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.resource.*;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PFMLangProvider {

    public void run() {
        try (PFMResourceManager resourceManager = new PFMResourceManager(ResourceType.CLIENT_RESOURCES, PFMRuntimeResources.RESOURCE_PACK_LIST)) {
            loadLanguages(resourceManager);
            resourceManager.close();
        }
        catch (Exception e) {
            PFMDataGen.LOGGER.info(e);
        };
        try(BufferedWriter writer = IOUtils.buffer(new FileWriter(new File(PFMRuntimeResources.createDirIfNeeded(PFMRuntimeResources.getAssetsDirectory().resolve("lang")).toFile(), "en_us.json"))))
        {
            writer.write("{\n");
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(BasicChairBlock.class).getVariantToBlockMap(), writer, "block.pfm.basic_chair", PFMLangProvider::simpleStrippedFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(BasicChairBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.basic_chair", PFMLangProvider::simpleStrippedFurnitureTranslation);

            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(DinnerChairBlock.class).getVariantToBlockMap(), writer, "block.pfm.dinner_chair", PFMLangProvider::simpleStrippedFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(DinnerChairBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.dinner_chair", PFMLangProvider::simpleStrippedFurnitureTranslation);

            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(ClassicChairBlock.class).getVariantToBlockMap(), writer, "block.pfm.classic_chair", PFMLangProvider::simpleStrippedFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(ClassicChairBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.classic_chair", PFMLangProvider::simpleStrippedFurnitureTranslation);

            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(ModernChairBlock.class).getVariantToBlockMap(), writer, "block.pfm.modern_chair", PFMLangProvider::simpleStrippedFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(ModernChairBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.modern_chair", PFMLangProvider::simpleStrippedFurnitureTranslation);

            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(BasicTableBlock.class).getVariantToBlockMap(), writer, "block.pfm.table_basic", PFMLangProvider::simpleStrippedFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(BasicTableBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.table_basic", PFMLangProvider::simpleStrippedFurnitureTranslation);

            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(ClassicTableBlock.class).getVariantToBlockMap(), writer, "block.pfm.table_classic", PFMLangProvider::simpleStrippedFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(ClassicTableBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.table_classic", PFMLangProvider::simpleStrippedFurnitureTranslation);

            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(LogTableBlock.class).getVariantToBlockMap(), writer, "block.pfm.table_log", PFMLangProvider::logTableFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(LogTableBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.table_log", PFMLangProvider::logTableFurnitureTranslation);

            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(RawLogTableBlock.class).getVariantToBlockMap(), writer, "block.pfm.table_log", PFMLangProvider::logTableFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(RawLogTableBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.table_log", PFMLangProvider::logTableFurnitureTranslation);

            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(DinnerTableBlock.class).getVariantToBlockMap(), writer, "block.pfm.table_dinner", PFMLangProvider::simpleStrippedFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(DinnerTableBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.table_dinner", PFMLangProvider::simpleStrippedFurnitureTranslation);

            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(ModernDinnerTableBlock.class).getVariantToBlockMap(), writer, "block.pfm.table_modern_dinner", PFMLangProvider::simpleStrippedFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(ModernDinnerTableBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.table_modern_dinner", PFMLangProvider::simpleStrippedFurnitureTranslation);

            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(LogStoolBlock.class).getVariantToBlockMap(), writer, "block.pfm.log_stool", PFMLangProvider::logTableFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(LogStoolBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.log_stool", PFMLangProvider::logTableFurnitureTranslation);

            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(SimpleStoolBlock.class).getVariantToBlockMap(), writer, "block.pfm.simple_stool", PFMLangProvider::simpleStrippedFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(SimpleStoolBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.simple_stool", PFMLangProvider::simpleStrippedFurnitureTranslation);

            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(ClassicStoolBlock.class).getVariantToBlockMap(), writer, "block.pfm.classic_stool", PFMLangProvider::simpleStrippedFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(ClassicStoolBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.classic_stool", PFMLangProvider::simpleStrippedFurnitureTranslation);

            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(ModernStoolBlock.class).getVariantToBlockMap(), writer, "block.pfm.modern_stool", PFMLangProvider::simpleStrippedFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(ModernStoolBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.modern_stool", PFMLangProvider::simpleStrippedFurnitureTranslation);

            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(ClassicNightstandBlock.class).getVariantToBlockMap(), writer, "block.pfm.classic_nightstand", PFMLangProvider::simpleStrippedFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(ClassicNightstandBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.classic_nightstand", PFMLangProvider::simpleStrippedFurnitureTranslation);

            generateTranslationForBedMap(PaladinFurnitureMod.furnitureEntryMap.get(SimpleBedBlock.class).getVariantToBlockMapList(), writer, "block.pfm.simple_bed", PFMLangProvider::bedFurnitureTranslation);
            generateTranslationForBedMap(PaladinFurnitureMod.furnitureEntryMap.get(SimpleBedBlock.class).getVariantToBlockMapList(), writer, "block.pfm.simple_bed", PFMLangProvider::bedFurnitureTranslation);

            generateTranslationForBedMap(PaladinFurnitureMod.furnitureEntryMap.get(ClassicBedBlock.class).getVariantToBlockMapList(), writer, "block.pfm.classic_bed", PFMLangProvider::bedFurnitureTranslation);
            generateTranslationForBedMap(PaladinFurnitureMod.furnitureEntryMap.get(ClassicBedBlock.class).getVariantToBlockMapList(), writer, "block.pfm.classic_bed", PFMLangProvider::bedFurnitureTranslation);

            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(SimpleBunkLadderBlock.class).getVariantToBlockMap(), writer, "block.pfm.simple_bunk_ladder", PFMLangProvider::simpleFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(SimpleBunkLadderBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.simple_bunk_ladder", PFMLangProvider::simpleFurnitureTranslation);

            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(KitchenCounterBlock.class).getVariantToBlockMap(), writer, "block.pfm.kitchen_counter", PFMLangProvider::simpleStrippedFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(KitchenCounterBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.kitchen_counter", PFMLangProvider::simpleStrippedFurnitureTranslation);

            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(KitchenDrawerBlock.class).getVariantToBlockMap(), writer, "block.pfm.kitchen_drawer", PFMLangProvider::simpleStrippedFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(KitchenDrawerBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.kitchen_drawer", PFMLangProvider::simpleStrippedFurnitureTranslation);

            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(KitchenCabinetBlock.class).getVariantToBlockMap(), writer, "block.pfm.kitchen_cabinet", PFMLangProvider::simpleStrippedFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(KitchenCabinetBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.kitchen_cabinet", PFMLangProvider::simpleStrippedFurnitureTranslation);

            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(KitchenSinkBlock.class).getVariantToBlockMap(), writer, "block.pfm.kitchen_sink", PFMLangProvider::simpleStrippedFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(KitchenSinkBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.kitchen_sink", PFMLangProvider::simpleStrippedFurnitureTranslation);

            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(KitchenCounterOvenBlock.class).getVariantToBlockMap(), writer, "block.pfm.kitchen_counter_oven", PFMLangProvider::simpleStrippedFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(KitchenCounterOvenBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.kitchen_counter_oven", PFMLangProvider::simpleStrippedFurnitureTranslation);

            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(KitchenWallCounterBlock.class).getVariantToBlockMap(), writer, "block.pfm.kitchen_wall_counter", PFMLangProvider::simpleStrippedFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(KitchenWallCounterBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.kitchen_wall_counter", PFMLangProvider::simpleStrippedFurnitureTranslation);

            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(KitchenWallDrawerBlock.class).getVariantToBlockMap(), writer, "block.pfm.kitchen_wall_drawer", PFMLangProvider::simpleStrippedFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(KitchenWallDrawerBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.kitchen_wall_drawer", PFMLangProvider::simpleStrippedFurnitureTranslation);

            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(KitchenWallDrawerSmallBlock.class).getVariantToBlockMap(), writer, "block.pfm.kitchen_wall_small_drawer", PFMLangProvider::simpleStrippedFurnitureTranslation);
            generateTranslationForVariantBlockMap(PaladinFurnitureMod.furnitureEntryMap.get(KitchenWallDrawerSmallBlock.class).getVariantToBlockMapNonBase(), writer, "block.pfm.kitchen_wall_small_drawer", PFMLangProvider::simpleStrippedFurnitureTranslation);

            generateTranslationForLampBlock(writer);
            writer.write("    \"pfm.dummy.entry\": \"dummy entry\"\n");
            writer.write("}");
        }
        catch(IOException e)
        {
            PFMDataGen.LOGGER.error("Writer exception: " + e);
            e.printStackTrace();
        }
    }

    public static String simpleStrippedFurnitureTranslation(Block block, String furnitureKey, String strippedKey, String translatedVariantName) {
        return translate(furnitureKey, strippedKey, translatedVariantName);
    }

    public static String logTableFurnitureTranslation(Block block, String furnitureKey, String strippedKey, String translatedVariantName) {
        String rawFix = block.getTranslationKey().contains("raw") ? translate("block.type.raw") : "";
        String extraLogKey = block.getTranslationKey().contains("stem") ? translate("block.type.stem") : block.getTranslationKey().contains("natural") ? translate("block.type.natural") : translate("block.type.log");
        return translate(furnitureKey, rawFix + (rawFix.isBlank() ? "" : " ") + strippedKey, translatedVariantName, extraLogKey);
    }

    public static String simpleFurnitureTranslation(Block block, String furnitureKey, String strippedKey, String translatedVariantName) {
        return translate(furnitureKey, translatedVariantName);
    }

    public static String bedFurnitureTranslation(Block block, String furnitureKey, String strippedKey, String translatedVariantName) {
        String color = block instanceof SimpleBedBlock ? translate("color.minecraft."+((SimpleBedBlock) block).getPFMColor().asString()) : "";
        return translate(furnitureKey, translatedVariantName, color);
    }

    private static Map<String, LanguageDefinition> loadAvailableLanguages(Stream<ResourcePack> packs) {
        HashMap<String, LanguageDefinition> map = Maps.newHashMap();
        packs.forEach(pack -> {
            try {
                LanguageResourceMetadata languageResourceMetadata = pack.parseMetadata(LanguageResourceMetadata.SERIALIZER);
                if (languageResourceMetadata != null) {
                    languageResourceMetadata.definitions().forEach(map::putIfAbsent);
                }
            }
            catch (IOException | RuntimeException exception) {
                PFMDataGen.LOGGER.warn("Unable to parse language metadata section of resourcepack: {}", pack.getName(), exception);
            }
        });
        return ImmutableMap.copyOf(map);
    }
    private Map<String, LanguageDefinition> languageDefs = ImmutableMap.of("en_us", PFMLanguageManagerAccessor.getEnglish_Us());
    private static volatile Language language = Language.getInstance();


    public void loadLanguages(ResourceManager manager) {
        this.languageDefs = loadAvailableLanguages(manager.streamResourcePacks());
        List<String> list = new ArrayList(2);
        boolean bl = true;
        list.add("en_us");
        LanguageDefinition enUSDefinition = this.languageDefs.getOrDefault(LanguageManager.DEFAULT_LANGUAGE_CODE, PFMLanguageManagerAccessor.getEnglish_Us());
        String selectedDef = ((PFMLanguageManagerAccessor) MinecraftClient.getInstance().getLanguageManager()).getCurrentLanguageCode();
        if (!selectedDef.equals("en_us")) {
            LanguageDefinition languageDefinition = this.languageDefs.getOrDefault(selectedDef, enUSDefinition);
            if (languageDefinition != null) {
                list.add(selectedDef);
                bl = languageDefinition.rightToLeft();
            }
        }
        TranslationStorage translationStorage = TranslationStorage.load(manager, list, bl);
        language = translationStorage;
        Language.setInstance(translationStorage);
    }


    public static String translate(String key, Object ... args) {
        String string = language.get(key);
        try {
            return String.format(string, args);
        }
        catch (IllegalFormatException illegalFormatException) {
            PFMDataGen.LOGGER.error("Format error: " + string + "\n" + illegalFormatException);
            return "Format error: " + string;
        }
    }

    public void generateTranslationForLampBlock(BufferedWriter writer) {
        for (WoodVariant variant : WoodVariantRegistry.getVariants()) {
            for (DyeColor color : DyeColor.values()) {
                try {
                    String translatedVariantName = getTranslatedVariantName(variant);
                    String translatedColor = translate("color.minecraft."+color.getName());
                    String translatedFurnitureName = StringUtils.normalizeSpace(translate("block.pfm.basic_lamp", translatedColor, translatedVariantName));
                    writer.write(String.format("    \"%1$s\": \"%2$s\",", String.format("block.pfm.basic_%s_%s_lamp", color.asString(), variant.asString()), translatedFurnitureName));
                    writer.write("\n");
                } catch (IOException e) {
                    PFMDataGen.LOGGER.error("Writer exception: " + e);
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public String getTranslatedVariantName(VariantBase<?> variant) {
        AtomicReference<String> variantName = new AtomicReference<>(translate(variant.getSecondaryBlock().getTranslationKey()));
        String baseBlockName = translate(variant.getBaseBlock().getTranslationKey());
        List<String> common = findCommonWords(variantName.get(), baseBlockName);
        variantName.set("");
        if (variant == WoodVariantRegistry.getVariantFromVanillaWoodType(BoatEntity.Type.BAMBOO)) {
            variantName.set(variantName.get().replace("Block of", ""));
        }
        variantName.set(String.join(" ", common));
        return variantName.get();
    }

    public void generateTranslationForBedMap(HashMap<VariantBase<?>, ? extends List<?>> variantBaseHashMap, BufferedWriter writer, String furnitureKey, QuadFunc<Block, String, String, String, String> blockStringStringStringStringQuadFunc) {
        variantBaseHashMap.forEach((variant, list) -> {
            list.forEach(block1 -> {
                Block block = (Block) block1;
                if (variant instanceof WoodVariant) {
                    String translatedVariantName = getTranslatedVariantName(variant);
                    String strippedKey = block.getTranslationKey().contains("stripped") ? translate("block.type.stripped") : "";
                    String translatedFurnitureName = StringUtils.normalizeSpace(blockStringStringStringStringQuadFunc.apply(block, furnitureKey, strippedKey, translatedVariantName));
                    try {
                        writer.write(String.format("    \"%1$s\": \"%2$s\",", block.getTranslationKey(), translatedFurnitureName));
                        writer.write("\n");
                    } catch (IOException e) {
                        PFMDataGen.LOGGER.error("Writer exception: " + e);
                        throw new RuntimeException(e);
                    }
                }
            });
        });

    }

    public void generateTranslationForVariantBlockMap(Map<VariantBase<?>, ? extends Block> variantBaseHashMap, BufferedWriter writer, String furnitureKey, QuadFunc<Block, String, String, String, String> blockStringStringStringStringQuadFunc) {
        variantBaseHashMap.forEach((variant, block) -> {
            if (variant instanceof WoodVariant) {
                String translatedVariantName = getTranslatedVariantName(variant);
                String strippedKey = block.getTranslationKey().contains("stripped") ? translate("block.type.stripped") : "";
                String translatedFurnitureName = StringUtils.normalizeSpace(blockStringStringStringStringQuadFunc.apply(block, furnitureKey, strippedKey, translatedVariantName));
                try {
                    writer.write(String.format("    \"%1$s\": \"%2$s\",", block.getTranslationKey(), translatedFurnitureName));
                    writer.write("\n");
                } catch (IOException e) {
                    PFMDataGen.LOGGER.error("Writer exception: " + e);
                    throw new RuntimeException(e);
                }
            }
        });

    }

    public static List<String> findCommonWords(String input1,String input2) {
        String[] words1 = input1.trim().split("\\s+");
        String[] words2 = input2.trim().split("\\s+");
        List<String>list1 = new ArrayList<>(Arrays.asList(words1));
        List<String>list2 = Arrays.asList(words2);
        list1.retainAll(list2);
        return list1;
    }

    public String getName() {
        return "PFM Lang";
    }

    private static final class PFMResourceManager implements ResourceManager,
            AutoCloseable {
        private LifecycledResourceManager activeManager;
        private final ResourceType type;

        public PFMResourceManager(ResourceType type, List<ResourcePack> packs) {
            this.type = type;
            this.activeManager = new LifecycledResourceManagerImpl(type, packs);
        }

        @Override
        public void close() {
            this.activeManager.close();
        }

        @Override
        public Optional<Resource> getResource(Identifier identifier) {
            return this.activeManager.getResource(identifier);
        }

        @Override
        public Set<String> getAllNamespaces() {
            return this.activeManager.getAllNamespaces();
        }

        @Override
        public List<Resource> getAllResources(Identifier id) {
            return this.activeManager.getAllResources(id);
        }

        @Override
        public Map<Identifier, List<Resource>> findAllResources(String startingPath, Predicate<Identifier> allowedPathPredicate) {
            return this.activeManager.findAllResources(startingPath, allowedPathPredicate);
        }

        @Override
        public Map<Identifier, Resource> findResources(String startingPath, Predicate<Identifier> pathPredicate) {
            return this.activeManager.findResources(startingPath, pathPredicate);
        }

        @Override
        public Stream<ResourcePack> streamResourcePacks() {
            return this.activeManager.streamResourcePacks();
        }
    }

}
