package com.liangmu.arcvaluecalc.network;

import com.liangmu.arcvaluecalc.client.TraceScreen;
import com.liangmu.arcvaluecalc.model.ValueTrace;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public final class TraceResponseMessage {
    private final ValueTrace trace;

    public TraceResponseMessage(ValueTrace trace) {
        this.trace = trace;
    }

    public static void encode(TraceResponseMessage message, FriendlyByteBuf buffer) {
        message.trace.write(buffer);
    }

    public static TraceResponseMessage decode(FriendlyByteBuf buffer) {
        return new TraceResponseMessage(ValueTrace.read(buffer));
    }

    public static void handle(TraceResponseMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().setScreen(new TraceScreen(message.trace)));
        contextSupplier.get().setPacketHandled(true);
    }
}
