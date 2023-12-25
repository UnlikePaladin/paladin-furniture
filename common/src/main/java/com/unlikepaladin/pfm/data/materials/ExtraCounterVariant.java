package com.unlikepaladin.pfm.data.materials;

import com.unlikepaladin.pfm.blocks.models.ModelHelper;
import com.unlikepaladin.pfm.registry.PaladinFurnitureModBlocksItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ExtraCounterVariant extends VariantBase<ExtraCounterVariant> {
    public static ExtraCounterVariant DARK_CONCRETE = new ExtraCounterVariant(Blocks.GRAY_CONCRETE, Blocks.WHITE_CONCRETE, "dark_concrete");
    public static ExtraCounterVariant CONCRETE = new ExtraCounterVariant(Blocks.WHITE_CONCRETE, PaladinFurnitureModBlocksItems.RAW_CONCRETE, "concrete");
    public static ExtraCounterVariant SMOOTH_STONE = new ExtraCounterVariant(Blocks.WHITE_CONCRETE, Blocks.SMOOTH_STONE,"smooth_stone");
    private final String name;
    private final Block baseBlock;
    static final List<ExtraCounterVariant> DEFAULT_VARIANTS = new ArrayList<>();

    static {
        DEFAULT_VARIANTS.add(DARK_CONCRETE);
        DEFAULT_VARIANTS.add(CONCRETE);
        DEFAULT_VARIANTS.add(SMOOTH_STONE);
    }

    private final Block secondaryBlock;

    public static List<ExtraCounterVariant> values() {
        return DEFAULT_VARIANTS;
    }

    ExtraCounterVariant(Identifier identifier, Block baseBlock, Block secondaryBlock) {
        super(identifier);
        this.name = identifier.getPath();
        this.baseBlock = baseBlock;
        this.secondaryBlock = secondaryBlock;
    }
    ExtraCounterVariant(Block baseBlock, Block secondaryBlock, String name) {
        this(new Identifier("", name), baseBlock, secondaryBlock);
    }

    @Override
    public String asString() {
        return name;
    }

    @Override
    public Block getBaseBlock() {
        return baseBlock;
    }

    @Override
    public Block getSecondaryBlock() {
        return secondaryBlock;
    }

    @Override
    public boolean isNetherWood() {
        return false;
    }

    @Override
    public Material getVanillaMaterial() {
        return baseBlock.getDefaultState().getMaterial();
    }

    @Override
    public Material getBaseMaterial() {
        return baseBlock.getDefaultState().getMaterial();
    }

    @Override
    public Material getSecondaryMaterial() {
        return secondaryBlock.getDefaultState().getMaterial();
    }

    @Override
    public ExtraCounterVariant getVariantType() {
        return this;
    }

    @Override
    public boolean isVanilla() {
        return identifier.getNamespace().equals("") || identifier.getNamespace().equals("minecraft");
    }

    @Override
    public void initializeChildrenBlocks() {

    }

    @Override
    public void initializeChildrenItems() {

    }

    @Override
    public Block mainChild() {
        return DARK_CONCRETE.baseBlock;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public Identifier getTexture(BlockType type) {
        if (type == BlockType.SECONDARY)
            return ModelHelper.getTextureId(secondaryBlock);
        return ModelHelper.getTextureId(baseBlock);
    }

    @Override
    public String getPath() {
        return this.identifier.getPath();
    }
}