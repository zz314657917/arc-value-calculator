package com.liangmu.arcvaluecalc.client;

import com.liangmu.arcvaluecalc.model.ValueKey;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.item.ItemStack;

public final class ClientValueCache {
    private static final Map<ValueKey, BigDecimal> SERVER_VALUES = new ConcurrentHashMap<>();
    private static volatile boolean serverAvailable;

    private ClientValueCache() {
    }

    public static void clearServerValues() {
        SERVER_VALUES.clear();
        serverAvailable = false;
    }

    public static void putServerValue(ValueKey key, BigDecimal value) {
        serverAvailable = true;
        if (value != null) {
            SERVER_VALUES.put(key, value);
        }
    }

    public static void markServerAvailable() {
        serverAvailable = true;
    }

    public static Optional<BigDecimal> getServerValue(ItemStack stack) {
        BigDecimal exact = SERVER_VALUES.get(ValueKey.exact(stack));
        if (exact != null) {
            return Optional.of(exact);
        }
        return Optional.ofNullable(SERVER_VALUES.get(ValueKey.itemOnly(stack)));
    }

    public static boolean serverAvailable() {
        return serverAvailable;
    }
}
