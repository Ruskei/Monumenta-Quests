package com.jupiterr.gem.config;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigInitializer {
    public static void initConfig() {
        Path questsDir = FabricLoader.getInstance().getConfigDir().resolve("mmquests.json");
        if (Files.notExists(questsDir)) {
            ModContainer container = FabricLoader.getInstance().getModContainer("gem").get();
            Path path = container.findPath("mmquests.json").get();

            try {
                Files.copy(path, questsDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Path puzzlesDir = FabricLoader.getInstance().getConfigDir().resolve("mmpuzzles.json");
        if (Files.notExists(puzzlesDir)) {
            ModContainer container = FabricLoader.getInstance().getModContainer("gem").get();
            Path path = container.findPath("mmpuzzles.json").get();

            try {
                Files.copy(path, puzzlesDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
