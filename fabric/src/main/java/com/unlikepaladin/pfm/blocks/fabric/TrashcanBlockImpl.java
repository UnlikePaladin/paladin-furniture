package com.unlikepaladin.pfm.blocks.fabric;

import com.unlikepaladin.pfm.blocks.blockentities.fabric.TrashcanBlockEntityImpl;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class TrashcanBlockImpl {
    public static BlockEntity getBlockEntity() {
        return new TrashcanBlockEntityImpl();
    }

    public static void openScreen(PlayerEntity player, BlockState state, World world, BlockPos pos) {
        NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);
        if (screenHandlerFactory != null) {
            //With this call the server will request the client to open the appropriate Screenhandler
            player.openHandledScreen(screenHandlerFactory);
        }
    }
}
