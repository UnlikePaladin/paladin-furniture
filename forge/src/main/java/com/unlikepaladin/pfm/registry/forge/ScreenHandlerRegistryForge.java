package com.unlikepaladin.pfm.registry.forge;

import com.unlikepaladin.pfm.PaladinFurnitureMod;
import com.unlikepaladin.pfm.client.PaladinFurnitureModClient;
import com.unlikepaladin.pfm.menus.*;
import com.unlikepaladin.pfm.registry.ScreenHandlerIDs;
import com.unlikepaladin.pfm.registry.ScreenHandlerRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.stat.StatType;
import net.minecraft.util.Identifier;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "pfm", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ScreenHandlerRegistryForge {

    @SubscribeEvent
    public static void registerScreenHandlers(RegistryEvent.Register<ScreenHandlerType<?>> event) {
        ScreenHandlerRegistry.registerScreenHandlers();
        event.getRegistry().registerAll(
            ScreenHandlerRegistryImpl.screenHandlerTypeList.toArray(new ScreenHandlerType[0])
        );
    }
}
