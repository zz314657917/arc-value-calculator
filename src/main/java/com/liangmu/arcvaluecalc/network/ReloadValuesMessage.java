package com.liangmu.arcvaluecalc.network;

import com.liangmu.arcvaluecalc.client.ClientValueCache;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public final class ReloadValuesMessage {
    private final long generation;

    public ReloadValuesMessage(long generation) {
        this.generation = generation;
    }

    public static void encode(ReloadValuesMessage message, FriendlyByteBuf buffer) {
        buffer.writeLong(message.generation);
    }

    public static ReloadValuesMessage decode(FriendlyByteBuf buffer) {
        return new ReloadValuesMessage(buffer.readLong());
    }

    public static void handle(ReloadValuesMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientValueCache.reset(message.generation));
        contextSupplier.get().setPacketHandled(true);
    }
}
