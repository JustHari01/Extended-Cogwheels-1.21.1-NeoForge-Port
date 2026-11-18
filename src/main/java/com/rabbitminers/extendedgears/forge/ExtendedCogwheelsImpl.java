package com.rabbitminers.extendedgears.forge;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ExtendedCogwheelsImpl {
    public static void registerConfig(ModConfig.Type type, ForgeConfigSpec spec) {
        ModLoadingContext.get().registerConfig(type, spec);
    }

    public static Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
