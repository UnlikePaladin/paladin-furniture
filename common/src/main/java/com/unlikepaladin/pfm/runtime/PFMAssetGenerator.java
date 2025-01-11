package com.unlikepaladin.pfm.runtime;

import com.google.common.base.Stopwatch;
import com.mojang.bridge.game.PackType;
import com.unlikepaladin.pfm.PaladinFurnitureMod;
import com.unlikepaladin.pfm.client.screens.PFMGeneratingOverlay;
import com.unlikepaladin.pfm.runtime.assets.PFMBlockstateModelProvider;
import com.unlikepaladin.pfm.runtime.assets.PFMLangProvider;
import com.unlikepaladin.pfm.runtime.data.PFMMCMetaProvider;
import com.unlikepaladin.pfm.utilities.PFMFileUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.data.DataCache;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class PFMAssetGenerator extends PFMGenerator {
    public static boolean FROZEN = false;

    public PFMAssetGenerator(Path output, boolean logOrDebug) {
        super(output, logOrDebug, LogManager.getLogger("PFM-Asset-Generation"));
    }

    public void run() throws IOException {
        if (!FROZEN) {
            setAssetsRunning(true);
            log("Packs:");
            for (ResourcePack pack : PFMRuntimeResources.RESOURCE_PACK_LIST) {
                log("\tPack {}", pack.getName());
                for (String namespace : pack.getNamespaces(ResourceType.CLIENT_RESOURCES)) {
                    log("\t\tNamespace {}", namespace);
                }
            }
            FROZEN = true;

            Path modListPath = output.resolve("modsList");
            Path hashPath = output.resolve("dataHash");
            if (!modListPath.toFile().isFile()) {
                Files.deleteIfExists(modListPath);
                Files.createFile(modListPath);
            }
            if (!hashPath.toFile().isFile()) {
                Files.deleteIfExists(hashPath);
                Files.createFile(hashPath);
            }
            List<String> hashToCompare = hashDirectory(output.toFile(), false);
            List<String> oldHash = Files.readAllLines(hashPath);
            List<String> modList = Files.readAllLines(modListPath);
            if (!hashToCompare.toString().equals(oldHash.toString()) || !modList.toString().replace("[", "").replace("]", "").equals(PaladinFurnitureMod.getVersionMap().toString())) {
                List<PFMProvider> providers = new ArrayList<>();
                //MinecraftClient.getInstance().setOverlay(new PFMGeneratingOverlay(MinecraftClient.getInstance().getOverlay(), this, MinecraftClient.getInstance(), true));
                getLogger().info("Starting PFM Asset Generation");
                PFMFileUtil.deleteDir(output.toFile());
                PFMRuntimeResources.createDirIfNeeded(output);
                DataCache dataCache = new DataCache(this.output, "cache");
                dataCache.ignore(this.output.resolve("version.json"));
                Stopwatch stopwatch = Stopwatch.createStarted();


                PFMMCMetaProvider metaProvider = new PFMMCMetaProvider(this);
                metaProvider.setInfo(new PFMMCMetaProvider.PackInfo(PackType.RESOURCE, "PFM-Assets"));
                providers.add(metaProvider);
                providers.add(new PFMBlockstateModelProvider(this));
                providers.add(new PFMLangProvider(this));
                this.setTotalCount(providers.size());

                MinecraftClient client = MinecraftClient.getInstance();
                PFMGeneratingOverlay overlay = new PFMGeneratingOverlay(client.getOverlay(), this, client, true);
                client.setOverlay(overlay);
                boolean allDone = false;

                ExecutorService executor = Executors.newFixedThreadPool(providers.size());
                List<? extends Future<?>> futures = providers.stream()
                        .map(provider -> executor.submit(() -> provider.run(dataCache)))
                        .toList();

                while (!allDone) {
                    allDone = futures.stream().allMatch(Future::isDone);

                    int completedTasks = (int) futures.stream().filter(Future::isDone).count();
                    this.setCount(completedTasks);
                    long i = Util.getMeasuringTimeNano();
                    client.gameRenderer.render(1, i, false);
                }
                executor.shutdown();

                getLogger().info("Asset providers took: {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
                dataCache.write();
                this.createPackIcon();
                Files.deleteIfExists(hashPath);
                Files.createFile(hashPath);
                List<String> newDataHash = hashDirectory(output.toFile(), false);
                Files.writeString(PFMRuntimeResources.createDirIfNeeded(hashPath), newDataHash.toString().replace("[", "").replace("]", ""), StandardOpenOption.APPEND);

                Files.deleteIfExists(modListPath);
                Files.createFile(modListPath);
                Files.writeString(PFMRuntimeResources.createDirIfNeeded(modListPath), PaladinFurnitureMod.getVersionMap().toString().replace("[", "").replace("]", ""), StandardOpenOption.APPEND);
            } else {
                log("Data Hash and Mod list matched, skipping generation");
            }
            setAssetsRunning(false);
        }
    }
}
