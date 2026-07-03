package com.liangmu.arcvaluecalc.model;

import com.liangmu.arcvaluecalc.service.PriceParser;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;

public record ValueTrace(
        ValueKey key,
        String label,
        BigDecimal value,
        ValueSource source,
        String ruleId,
        int count,
        boolean missing,
        boolean cycle,
        boolean truncated,
        List<ValueTrace> children
) {
    public static final int MAX_CHILDREN = 32;

    public ValueTrace {
        children = children == null ? List.of() : List.copyOf(children);
    }

    public static ValueTrace missing(ValueKey key, String label, int count) {
        return new ValueTrace(key, label, null, ValueSource.NONE, "", count, true, false, false, List.of());
    }

    public static ValueTrace cycle(ValueKey key, String label, BigDecimal value, ValueSource source, int count) {
        return new ValueTrace(key, label, value, source, "", count, false, true, false, List.of());
    }

    public static ValueTrace truncated(ValueKey key, String label, BigDecimal value, ValueSource source, int count) {
        return new ValueTrace(key, label, value, source, "", count, false, false, true, List.of());
    }

    public void write(FriendlyByteBuf buffer) {
        key.write(buffer);
        buffer.writeUtf(label, 256);
        buffer.writeBoolean(value != null);
        if (value != null) {
            buffer.writeUtf(PriceParser.toPlainString(value), PriceParser.MAX_RAW_LENGTH);
        }
        buffer.writeEnum(source);
        buffer.writeUtf(ruleId == null ? "" : ruleId, 512);
        buffer.writeVarInt(count);
        buffer.writeBoolean(missing);
        buffer.writeBoolean(cycle);
        buffer.writeBoolean(truncated);
        int childCount = Math.min(children.size(), MAX_CHILDREN);
        buffer.writeVarInt(childCount);
        for (int i = 0; i < childCount; i++) {
            children.get(i).write(buffer);
        }
    }

    public static ValueTrace read(FriendlyByteBuf buffer) {
        ValueKey key = ValueKey.read(buffer);
        String label = buffer.readUtf(256);
        BigDecimal value = buffer.readBoolean() ? PriceParser.parsePrice(buffer.readUtf(PriceParser.MAX_RAW_LENGTH)) : null;
        ValueSource source = buffer.readEnum(ValueSource.class);
        String ruleId = buffer.readUtf(512);
        int count = buffer.readVarInt();
        boolean missing = buffer.readBoolean();
        boolean cycle = buffer.readBoolean();
        boolean truncated = buffer.readBoolean();
        int childCount = buffer.readVarInt();
        if (childCount < 0 || childCount > MAX_CHILDREN) {
            throw new IllegalArgumentException("invalid trace child count: " + childCount);
        }
        List<ValueTrace> children = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            children.add(read(buffer));
        }
        return new ValueTrace(key, label, value, source, ruleId, count, missing, cycle, truncated, children);
    }
}
