package com.liangmu.arcvaluecalc.network;

import com.liangmu.arcvaluecalc.client.ClientValueCache;
import com.liangmu.arcvaluecalc.model.MatchType;
import com.liangmu.arcvaluecalc.model.ValueKey;
import com.liangmu.arcvaluecalc.model.ValueLookupResult;
import com.liangmu.arcvaluecalc.service.PriceParser;
import java.math.BigDecimal;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public final class ValueResponseMessage {
    private final MatchType matchType;
    private final ValueKey resolvedKey;
    private final BigDecimal value;
    private final long generation;

    public ValueResponseMessage(MatchType matchType, ValueKey resolvedKey, BigDecimal value, long generation) {
        this.matchType = matchType;
        this.resolvedKey = resolvedKey;
        this.value = value;
        this.generation = generation;
    }

    public static ValueResponseMessage fromLookup(ValueLookupResult result) {
        return new ValueResponseMessage(result.matchType(), result.resolvedKey(), result.value(), result.generation());
    }

    public static void encode(ValueResponseMessage message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.matchType);
        message.resolvedKey.write(buffer);
        buffer.writeLong(message.generation);
        buffer.writeBoolean(message.value != null);
        if (message.value != null) {
            buffer.writeUtf(PriceParser.toPlainString(message.value));
        }
    }

    public static ValueResponseMessage decode(FriendlyByteBuf buffer) {
        MatchType matchType = buffer.readEnum(MatchType.class);
        ValueKey resolvedKey = ValueKey.read(buffer);
        long generation = buffer.readLong();
        BigDecimal value = buffer.readBoolean() ? PriceParser.parsePrice(buffer.readUtf(PriceParser.MAX_RAW_LENGTH)) : null;
        return new ValueResponseMessage(matchType, resolvedKey, value, generation);
    }

    public static void handle(ValueResponseMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientValueCache.applyResponse(
                message.matchType,
                message.resolvedKey,
                message.value,
                message.generation
        ));
        contextSupplier.get().setPacketHandled(true);
    }
}
