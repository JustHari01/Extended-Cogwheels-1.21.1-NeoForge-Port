package com.rabbitminers.extendedgears;

import com.rabbitminers.extendedgears.cogwheels.DynamicCogwheelRenderer;
import com.rabbitminers.extendedgears.cogwheels.materials.CogwheelMaterialManager;
import com.rabbitminers.extendedgears.registry.ExtendedCogwheelsPartials;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class ExtendedCogwheelsClient {
    public static void onClientSetup(FMLClientSetupEvent event) {
        init();
    }

    public static void init() {
        ExtendedCogwheelsPartials.init();
        CogwheelMaterialManager.setupClientCogwheelMaterials();
        DynamicCogwheelRenderer.registerVisualizers();
    }
}
