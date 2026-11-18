package com.rabbitminers.extendedgears.config;

import com.rabbitminers.extendedgears.ExtendedCogwheels;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ExtendedCogwheelsConfig {
    private static final ModConfigSpec COMMON_SPEC;
    private static final ModConfigSpec.BooleanValue DISABLE_DATAFIXER;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        DISABLE_DATAFIXER = builder
                .comment("Disable legacy cogwheel data conversion. Only use this if no legacy cogwheels remain in the world.")
                .define("disableDatafixer", false);
        COMMON_SPEC = builder.build();
    }

    private ExtendedCogwheelsConfig() {
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
    }

    public static boolean getDisableDatafixer() {
        return DISABLE_DATAFIXER.get();
    }

    public static void onLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == COMMON_SPEC)
            ExtendedCogwheels.LOGGER.info("Loaded common configuration");
    }

    public static void onReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == COMMON_SPEC)
            ExtendedCogwheels.LOGGER.info("Reloaded common configuration");
    }
}
