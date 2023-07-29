package com.unlikepaladin.pfm.compat.cookingforblockheads.forge;

import com.unlikepaladin.pfm.PaladinFurnitureMod;
import com.unlikepaladin.pfm.blocks.KitchenCounterBlock;
import com.unlikepaladin.pfm.blocks.KitchenSinkBlock;
import com.unlikepaladin.pfm.blocks.KitchenWallCounterBlock;
import com.unlikepaladin.pfm.blocks.StoveBlock;
import com.unlikepaladin.pfm.blocks.forge.StoveBlockImpl;
import com.unlikepaladin.pfm.compat.cookingforblockheads.forge.menu.StoveBlockEntityRendererBalm;
import com.unlikepaladin.pfm.compat.cookingforblockheads.forge.menu.StoveScreenHandlerBalm;
import com.unlikepaladin.pfm.compat.cookingforblockheads.forge.menu.StoveScreenBalm;
import com.unlikepaladin.pfm.registry.BlockEntities;
import com.unlikepaladin.pfm.registry.TriFunc;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.container.ContainerUtils;
import net.blay09.mods.cookingforblockheads.KitchenMultiBlock;
import net.blay09.mods.cookingforblockheads.item.ModItems;
import net.blay09.mods.cookingforblockheads.registry.CookingRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class PFMCookingForBlockHeadsCompat {

    public static void initBlockConnectors() {
        PaladinFurnitureMod.furnitureEntryMap.get(KitchenCounterBlock.class).getAllBlocks().forEach(KitchenMultiBlock::registerConnectorBlock);
        PaladinFurnitureMod.furnitureEntryMap.get(KitchenWallCounterBlock.class).getAllBlocks().forEach(KitchenMultiBlock::registerConnectorBlock);
        PaladinFurnitureMod.furnitureEntryMap.get(KitchenSinkBlock.class).getAllBlocks().forEach(KitchenMultiBlock::registerConnectorBlock);
    }

    public static final PFMCookingTableBlock COOKING_TABLE_BLOCK = new PFMCookingTableBlock(AbstractBlock.Settings.copy(Blocks.GRAY_CONCRETE));//PaladinFurnitureModBlocksItems.GRAY_STOVE));
    public static <T extends ScreenHandler> TriFunc<Integer, PlayerInventory, PacketByteBuf, T> getStoveScreenHandler() {
        return (integer, playerInventory, packetByteBuf) -> {
            BlockPos pos = packetByteBuf.readBlockPos();
            BlockEntity blockEntity = playerInventory.player.world.getBlockEntity(pos);
            return (T) new StoveScreenHandlerBalm(integer, playerInventory, (StoveBlockEntityBalm)blockEntity);
        };
    }

    public static <T extends ScreenHandler, J extends Screen & ScreenHandlerProvider<T>> TriFunc<T, PlayerInventory, Text,J> getStoveScreen() {
        return (t, playerInventory, text) -> (J) new StoveScreenBalm((StoveScreenHandlerBalm) t, playerInventory, text);
    }

    public static BlockEntityRendererFactory getStoveRenderer() {
        return StoveBlockEntityRendererBalm::new;
    }

    public static void openMenuScreen(World world, BlockPos pos, PlayerEntity player) {
        StoveBlockEntityBalm stove = (StoveBlockEntityBalm)world.getBlockEntity(pos);
        if (!world.isClient) {
            Balm.getNetworking().openGui(player, stove);
        }
    }

    public static BlockEntity getStoveBlockEntity(BlockPos pos, BlockState state) {
        return new StoveBlockEntityBalm(pos, state);
    }

    public static <T extends BlockEntity> BlockEntityTicker<T> getStoveTicker(World world, BlockEntityType<T> type) {
        if (world.isClient) {
            return StoveBlockImpl.checkType(type, BlockEntities.STOVE_BLOCK_ENTITY, StoveBlockEntityBalm::clientTick);
        } else {
            return StoveBlockImpl.checkType(type, BlockEntities.STOVE_BLOCK_ENTITY, StoveBlockEntityBalm::serverTick);
        }
    }

    public static ActionResult onUseStove(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getStackInHand(hand);
        if (heldItem.getItem() == ModItems.heatingUnit) {
            return ActionResult.PASS;
        } else if (hit.getSide() == Direction.UP && CookingRegistry.isToolItem(heldItem)) {
            Direction stateFacing = state.get(StoveBlock.FACING);
            double hx =  (hit.getPos().x - hit.getBlockPos().getX());
            double hz = (hit.getPos().z - hit.getBlockPos().getZ());
            switch (stateFacing) {
                case NORTH:
                    hx = 1.0 - (hit.getPos().x - hit.getBlockPos().getX());
                    hz = 1.0 - (hit.getPos().z - hit.getBlockPos().getZ());
                    break;
                case WEST:
                    hz = 1.0 - (hit.getPos().x - hit.getBlockPos().getX());
                    hx = (hit.getPos().z - hit.getBlockPos().getZ());
                    break;
                case EAST:
                    hz = (hit.getPos().x - hit.getBlockPos().getX());
                    hx = 1.0 - (hit.getPos().z - hit.getBlockPos().getZ());
            }
            int index = -1;
            if (hx < 0.5f && hz < 0.5f) {
                index = 1;
            } else if (hx >= 0.5f && hz < 0.5f) {
                index = 0;
            } else if (hx < 0.5f && hz >= 0.5f) {
                index = 3;
            } else if (hx >= 0.5f && hz >= 0.5f) {
                index = 2;
            }
            if (index != -1) {
                StoveBlockEntityBalm tileOven = (StoveBlockEntityBalm)level.getBlockEntity(pos);
                if (tileOven != null && tileOven.getToolItem(index).isEmpty()) {
                    ItemStack toolItem = heldItem.split(1);
                    tileOven.setToolItem(index, toolItem);
                }
            }
            return ActionResult.SUCCESS;
        } else {
            StoveBlockEntityBalm oven = (StoveBlockEntityBalm)level.getBlockEntity(pos);
            if (hit.getSide() == state.get(Properties.HORIZONTAL_FACING) && oven != null) {
                if (player.isSneaking()) {
                    return ActionResult.SUCCESS;
                }

                if (!heldItem.isEmpty() && oven.getSmeltingResult(heldItem) != ItemStack.EMPTY) {
                    heldItem = ContainerUtils.insertItemStacked(oven.getInputContainer(), heldItem, false);
                    player.setStackInHand(hand, heldItem);

                    return ActionResult.SUCCESS;
                } else if (!heldItem.isEmpty() && StoveBlockEntityBalm.isItemFuel(heldItem)) {
                    heldItem = ContainerUtils.insertItemStacked(oven.getFuelContainer(), heldItem, false);
                    player.setStackInHand(hand, heldItem);
                    return ActionResult.SUCCESS;
                }
            }
            if (!level.isClient) {
                Balm.getNetworking().openGui(player, oven);
            }
            return ActionResult.SUCCESS;
        }
    }
}
