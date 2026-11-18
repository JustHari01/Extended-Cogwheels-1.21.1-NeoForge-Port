package com.rabbitminers.extendedgears.forge;

import com.rabbitminers.extendedgears.ExtendedCogwheels;
import com.rabbitminers.extendedgears.ExtendedCogwheelsClient;
import com.rabbitminers.extendedgears.config.ExtendedCogwheelsConfig;
import com.rabbitminers.extendedgears.registry.forge.ExtendedCogwheelsCreativeModeTabsImpl;
import com.rabbitminers.extendedgears.forge.events.CommonEventsImpl;
import com.rabbitminers.extendedgears.registry.ExtendedCogwheelsPackets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(ExtendedCogwheels.MOD_ID)
public class ExtendedCogwheelsForge {
    public ExtendedCogwheelsForge(IEventBus eventBus, ModContainer modContainer) {
        ExtendedCogwheelsCreativeModeTabsImpl.register(eventBus);

        ExtendedCogwheels.init();
        ExtendedCogwheels.registrate().registerEventListeners(eventBus);
        ExtendedCogwheelsConfig.register(modContainer);
        eventBus.addListener(ExtendedCogwheelsConfig::onLoad);
        eventBus.addListener(ExtendedCogwheelsConfig::onReload);
        eventBus.addListener(this::registerPayloads);
        NeoForge.EVENT_BUS.addListener(CommonEventsImpl::addReloadListeners);
        NeoForge.EVENT_BUS.addListener(CommonEventsImpl::onDatapackSync);

        if (FMLEnvironment.dist == Dist.CLIENT)
            eventBus.addListener(ExtendedCogwheelsClient::onClientSetup);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        ExtendedCogwheelsPackets.registerPayloads(event);
    }
}
