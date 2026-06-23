package com.liangmu.arcvaluecalc.network;

import com.liangmu.arcvaluecalc.client.ClientValueCache;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public final class ReloadValuesMessage {
    public static void encode(ReloadValuesMessage message, FriendlyByteBuf buffer) {
    }

    public static ReloadValuesMessage decode(FriendlyByteBuf buffer) {
        return new ReloadValuesMessage();
    }

    public static void handle(ReloadValuesMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientValueCache::clearServerValues);
        contextSupplier.get().setPacketHandled(true);
    }
}
