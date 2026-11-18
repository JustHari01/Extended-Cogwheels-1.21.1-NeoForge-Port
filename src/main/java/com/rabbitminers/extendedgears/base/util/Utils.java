package com.rabbitminers.extendedgears.base.util;

import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class Utils {
    public static Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}

