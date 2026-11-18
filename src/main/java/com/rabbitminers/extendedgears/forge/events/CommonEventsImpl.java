package com.rabbitminers.extendedgears.forge.events;

import com.rabbitminers.extendedgears.base.events.CommonEvents;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import org.jetbrains.annotations.Nullable;

public class CommonEventsImpl {
    public static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(MaterialReloadListener.INSTANCE);
    }

    public static void onDatapackSync(OnDatapackSyncEvent event) {
        @Nullable ServerPlayer player = event.getPlayer();
        CommonEvents.onDatapackSyncEvent(player);
    }
}
