package com.unlikepaladin.pfm.client.fabric;

import com.unlikepaladin.pfm.blocks.models.ModelHelper;
import com.unlikepaladin.pfm.blocks.models.basicTable.UnbakedBasicTableModel;
import com.unlikepaladin.pfm.blocks.models.bed.UnbakedBedModel;
import com.unlikepaladin.pfm.blocks.models.classicTable.UnbakedClassicTableModel;
import com.unlikepaladin.pfm.blocks.models.dinnerTable.UnbakedDinnerTableModel;
import com.unlikepaladin.pfm.blocks.models.logTable.UnbakedLogTableModel;
import com.unlikepaladin.pfm.blocks.models.mirror.UnbakedMirrorModel;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class PFMModelProvider implements ModelResourceProvider {
    @Override
    public @Nullable UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context) {
        if (ModelHelper.containsIdentifier(UnbakedMirrorModel.MIRROR_MODEL_IDS, resourceId)){
            return new UnbakedMirrorModel(UnbakedMirrorModel.DEFAULT_TEXTURES[2], UnbakedMirrorModel.DEFAULT_TEXTURES[0], UnbakedMirrorModel.DEFAULT_TEXTURES[1]);
        } else if (ModelHelper.containsIdentifier(UnbakedBedModel.BED_MODEL_IDS.toArray(new Identifier[0]), resourceId)){
            return new UnbakedBedModel(ModelHelper.getWoodType(resourceId), ModelHelper.getColor(resourceId), new ArrayList<>(), resourceId.getPath().contains("classic"));
        }
        else if (ModelHelper.containsIdentifier(UnbakedBasicTableModel.TABLE_MODEL_IDS.toArray(new Identifier[0]), resourceId)){
            return new UnbakedBasicTableModel(ModelHelper.getVariant(resourceId), new ArrayList<>(), ModelHelper.getBlockType(resourceId));
        }
        else if (ModelHelper.containsIdentifier(UnbakedClassicTableModel.TABLE_MODEL_IDS.toArray(new Identifier[0]), resourceId)){
            return new UnbakedClassicTableModel(ModelHelper.getVariant(resourceId), new ArrayList<>(), ModelHelper.getBlockType(resourceId));
        }
        else if (ModelHelper.containsIdentifier(UnbakedLogTableModel.TABLE_MODEL_IDS.toArray(new Identifier[0]), resourceId)){
            boolean raw = resourceId.getPath().contains("raw");
            return new UnbakedLogTableModel(ModelHelper.getVariant(resourceId), new ArrayList<>(), ModelHelper.getBlockType(resourceId), raw);
        }
        else if (ModelHelper.containsIdentifier(UnbakedDinnerTableModel.TABLE_MODEL_IDS.toArray(new Identifier[0]), resourceId)){
            System.out.println("HII");
            return new UnbakedDinnerTableModel(ModelHelper.getVariant(resourceId), new ArrayList<>(), ModelHelper.getBlockType(resourceId));
        }
        else
            return null;
    }
}
