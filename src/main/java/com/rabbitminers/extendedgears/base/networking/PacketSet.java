package com.rabbitminers.extendedgears.base.networking;

import com.rabbitminers.extendedgears.ExtendedCogwheels;
import com.rabbitminers.extendedgears.registry.ExtendedCogwheelsPackets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class PacketSet {
    private static final int MAX_PAYLOAD_BYTES = 1024 * 1024;

    public final String id;
    public final int version;

    private final List<Function<FriendlyByteBuf, S2CPacket>> s2cPackets;
    private final Object2IntMap<Class<? extends S2CPacket>> s2cTypes;

    private PacketSet(String id, int version,
                      List<Function<FriendlyByteBuf, S2CPacket>> s2cPackets,
                      Object2IntMap<Class<? extends S2CPacket>> s2cTypes) {
        this.id = id;
        this.version = version;
        this.s2cPackets = List.copyOf(s2cPackets);
        this.s2cTypes = new Object2IntOpenHashMap<>(s2cTypes);
    }

    public static Builder builder(String id, int version) {
        return new Builder(id, version).s2c(CheckVersionPacket.class, CheckVersionPacket::new);
    }

    public void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Integer.toString(version));
        registrar.playToClient(S2CPayload.TYPE, S2CPayload.STREAM_CODEC,
                (payload, context) -> handleS2CPayload(payload));
    }

    public void sendTo(ServerPlayer player, S2CPacket packet) {
        PacketDistributor.sendToPlayer(player, createPayload(packet));
    }

    public void sendToAll(S2CPacket packet) {
        PacketDistributor.sendToAllPlayers(createPayload(packet));
    }

    private S2CPayload createPayload(S2CPacket packet) {
        int packetId = s2cTypes.getOrDefault(packet.getClass(), -1);
        if (packetId < 0)
            throw new IllegalArgumentException("Cannot send an unregistered S2C packet: " + packet);

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeVarInt(packetId);
        packet.write(buffer);

        if (buffer.readableBytes() > MAX_PAYLOAD_BYTES)
            throw new IllegalArgumentException("S2C payload exceeds the 1 MiB NeoForge limit");

        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        buffer.release();
        return new S2CPayload(bytes);
    }

    @Internal
    private void handleS2CPayload(S2CPayload payload) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.copiedBuffer(payload.bytes()));
        int packetId = buffer.readVarInt();
        if (packetId < 0 || packetId >= s2cPackets.size()) {
            ExtendedCogwheels.LOGGER.error("Invalid S2C packet {}, ignoring", packetId);
            buffer.release();
            return;
        }

        S2CPacket packet = s2cPackets.get(packetId).apply(buffer);
        Minecraft.getInstance().execute(() -> {
            try {
                packet.handle(Minecraft.getInstance());
            } finally {
                buffer.release();
            }
        });
    }

    public void onPlayerJoin(ServerPlayer player) {
        sendTo(player, new CheckVersionPacket(version));
    }

    public record S2CPayload(byte[] bytes) implements CustomPacketPayload {
        public static final Type<S2CPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(ExtendedCogwheels.MOD_ID, "s2c"));

        public static final StreamCodec<ByteBuf, S2CPayload> STREAM_CODEC = StreamCodec.of(
                (buffer, payload) -> {
                    if (payload.bytes.length > MAX_PAYLOAD_BYTES)
                        throw new IllegalArgumentException("S2C payload exceeds the 1 MiB NeoForge limit");
                    ByteBufCodecs.VAR_INT.encode(buffer, payload.bytes.length);
                    buffer.writeBytes(payload.bytes);
                },
                buffer -> {
                    int length = ByteBufCodecs.VAR_INT.decode(buffer);
                    if (length < 0 || length > MAX_PAYLOAD_BYTES || length > buffer.readableBytes())
                        throw new IllegalArgumentException("Invalid S2C payload size: " + length);
                    byte[] bytes = new byte[length];
                    buffer.readBytes(bytes);
                    return new S2CPayload(bytes);
                });

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record CheckVersionPacket(int serverVersion) implements S2CPacket {
        public CheckVersionPacket(FriendlyByteBuf buffer) {
            this(buffer.readVarInt());
        }

        @Override
        public void write(FriendlyByteBuf buffer) {
            buffer.writeVarInt(serverVersion);
        }

        @Override
        public void handle(Minecraft minecraft) {
            if (ExtendedCogwheelsPackets.PACKETS.version == serverVersion)
                return;
            Component error = Component.literal("Extended Cogwheels on the client uses a different network format than the server.")
                    .append(" Use the same mod version on both sides.");
            if (minecraft.getConnection() != null)
                minecraft.getConnection().onDisconnect(new DisconnectionDetails(error));
        }
    }

    public static final class Builder {
        private final String id;
        private final int version;
        private final List<Function<FriendlyByteBuf, S2CPacket>> s2cPackets = new ArrayList<>();
        private final Object2IntMap<Class<? extends S2CPacket>> s2cTypes = new Object2IntOpenHashMap<>();

        private Builder(String id, int version) {
            this.id = id;
            this.version = version;
        }

        public Builder s2c(Class<? extends S2CPacket> type, Function<FriendlyByteBuf, S2CPacket> factory) {
            int index = s2cPackets.size();
            s2cPackets.add(factory);
            s2cTypes.put(type, index);
            return this;
        }

        public PacketSet build() {
            return new PacketSet(id, version, s2cPackets, s2cTypes);
        }
    }
}
