package com.liangmu.arcvaluecalc.network;

import com.liangmu.arcvaluecalc.model.ValueKey;
import com.liangmu.arcvaluecalc.service.ValueServices;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public final class ValueRequestMessage {
    private final ValueKey requestedKey;

    public ValueRequestMessage(ValueKey requestedKey) {
        this.requestedKey = requestedKey;
    }

    public static void encode(ValueRequestMessage message, FriendlyByteBuf buffer) {
        message.requestedKey.write(buffer);
    }

    public static ValueRequestMessage decode(FriendlyByteBuf buffer) {
        return new ValueRequestMessage(ValueKey.read(buffer));
    }

    public static void handle(ValueRequestMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null && ValueRequestLimiter.allow(player)) {
            try {
                ArcValueNetwork.sendValue(player, ValueResponseMessage.fromLookup(message.requestedKey, ValueServices.server().lookup(message.requestedKey)));
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed client keys.
            }
        }
        context.setPacketHandled(true);
    }
}
