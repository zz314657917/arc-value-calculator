package com.liangmu.arcvaluecalc.client;

import com.liangmu.arcvaluecalc.model.MatchType;
import com.liangmu.arcvaluecalc.model.ValueKey;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;

public final class ClientValueCache {
    private static final int MAX_ENTRIES = 4096;
    private static final Map<ValueKey, CacheEntry> SERVER_VALUES = new LinkedHashMap<>(128, 0.75F, true);
    private static long generation;
    private static boolean serverAvailable;

    private ClientValueCache() {
    }

    public static synchronized void reset(long newGeneration) {
        SERVER_VALUES.clear();
        generation = Math.max(generation + 1, newGeneration);
        serverAvailable = false;
    }

    public static synchronized void disconnect() {
        SERVER_VALUES.clear();
        generation++;
        serverAvailable = false;
    }

    public static synchronized long generation() {
        return generation;
    }

    public static synchronized boolean serverAvailable() {
        return serverAvailable;
    }

    public static synchronized Lookup getServerValue(ItemStack stack) {
        CacheEntry exact = SERVER_VALUES.get(ValueKey.exact(stack));
        if (exact != null && exact.status != Status.PENDING) {
            return exact.toLookup();
        }
        CacheEntry itemOnly = SERVER_VALUES.get(ValueKey.itemOnly(stack));
        if (itemOnly != null) {
            return itemOnly.toLookup();
        }
        if (exact != null) {
            return exact.toLookup();
        }
        return Lookup.notRequested();
    }

    public static synchronized boolean markPending(ItemStack stack) {
        ValueKey key = ValueKey.exact(stack);
        CacheEntry current = SERVER_VALUES.get(key);
        if (current != null && current.status != Status.NOT_REQUESTED) {
            return false;
        }
        put(key, new CacheEntry(Status.PENDING, null, generation));
        return true;
    }

    public static synchronized void applyResponse(MatchType matchType, ValueKey resolvedKey, BigDecimal value, long responseGeneration) {
        serverAvailable = true;
        if (responseGeneration < generation) {
            return;
        }
        generation = responseGeneration;
        Status status = matchType == MatchType.MISSING ? Status.KNOWN_MISSING : Status.KNOWN;
        put(resolvedKey, new CacheEntry(status, value, responseGeneration));
    }

    private static void put(ValueKey key, CacheEntry entry) {
        SERVER_VALUES.put(key, entry);
        while (SERVER_VALUES.size() > MAX_ENTRIES) {
            ValueKey eldest = SERVER_VALUES.keySet().iterator().next();
            SERVER_VALUES.remove(eldest);
        }
    }

    public enum Status {
        NOT_REQUESTED,
        PENDING,
        KNOWN,
        KNOWN_MISSING
    }

    public record Lookup(Status status, Optional<BigDecimal> value) {
        public static Lookup notRequested() {
            return new Lookup(Status.NOT_REQUESTED, Optional.empty());
        }
    }

    private record CacheEntry(Status status, BigDecimal value, long generation) {
        private Lookup toLookup() {
            return new Lookup(status, Optional.ofNullable(value));
        }
    }
}
