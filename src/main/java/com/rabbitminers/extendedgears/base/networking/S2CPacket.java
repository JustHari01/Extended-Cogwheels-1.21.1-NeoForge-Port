package com.rabbitminers.extendedgears.base.networking;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

public interface S2CPacket {
    void write(FriendlyByteBuf buffer);
    @OnlyIn(Dist.CLIENT)
    void handle(Minecraft mc);
}
