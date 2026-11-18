package com.rabbitminers.extendedgears.cogwheels.materials;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import com.rabbitminers.extendedgears.registry.ExtendedCogwheelsPartials;
import com.simibubi.create.AllPartialModels;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@OnlyIn(Dist.CLIENT)
public record ClientCogwheelMaterial(Map<ResourceLocation, ResourceLocation> textures, PartialModel smallModel, PartialModel largeModel) {
    public static ClientCogwheelMaterial fromMaterial(CogwheelMaterial material) {
        Map<ResourceLocation, ResourceLocation> textures = new HashMap<>();
        for (Entry<ResourceLocation, ResourceLocation> entry : material.textures.entrySet())
            textures.put(entry.getKey(), entry.getValue());
        PartialModel smallModel = material.chunky ? ExtendedCogwheelsPartials.CHUNKY_COGWHEEL :
                AllPartialModels.SHAFTLESS_COGWHEEL;
        PartialModel largeModel = material.chunky ? ExtendedCogwheelsPartials.LARGE_CHUNKY_COGWHEEL :
                AllPartialModels.SHAFTLESS_LARGE_COGWHEEL;
        return new ClientCogwheelMaterial(textures, smallModel, largeModel);
    }

    public PartialModel getModel(boolean large) {
        return large ? largeModel : smallModel;
    }
}
