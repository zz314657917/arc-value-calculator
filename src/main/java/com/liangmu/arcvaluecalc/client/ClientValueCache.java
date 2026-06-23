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
    private static final long UNKNOWN_GENERATION = -1L;
    private static final long PENDING_TIMEOUT_NANOS = 10_000_000_000L;
    private static final Map<ValueKey, CacheEntry> SERVER_VALUES = new LinkedHashMap<>(128, 0.75F, true);
    private static long generation = UNKNOWN_GENERATION;
    private static boolean serverAvailable;

    private ClientValueCache() {
    }

    public static synchronized void reset(long newGeneration) {
        if (generation != UNKNOWN_GENERATION && newGeneration < generation) {
            return;
        }
        SERVER_VALUES.clear();
        generation = newGeneration;
        serverAvailable = false;
    }

    public static synchronized void disconnect() {
        SERVER_VALUES.clear();
        generation = UNKNOWN_GENERATION;
        serverAvailable = false;
    }

    public static synchronized long generation() {
        return generation == UNKNOWN_GENERATION ? 0L : generation;
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

    static synchronized Lookup getByKey(ValueKey key) {
        CacheEntry entry = SERVER_VALUES.get(key);
        return entry == null ? Lookup.notRequested() : entry.toLookup();
    }

    public static synchronized boolean markPending(ItemStack stack) {
        return markPending(ValueKey.exact(stack));
    }

    static synchronized boolean markPending(ValueKey key) {
        CacheEntry current = SERVER_VALUES.get(key);
        if (current != null && current.status != Status.NOT_REQUESTED && !current.isExpiredPending()) {
            return false;
        }
        put(key, new CacheEntry(Status.PENDING, null, generation(), System.nanoTime()));
        return true;
    }

    public static synchronized void applyResponse(MatchType matchType, ValueKey requestedKey, ValueKey resolvedKey, BigDecimal value, long responseGeneration) {
        if (generation != UNKNOWN_GENERATION && responseGeneration < generation) {
            return;
        }
        generation = responseGeneration;
        serverAvailable = true;
        if (!requestedKey.equals(resolvedKey)) {
            CacheEntry requestedEntry = SERVER_VALUES.get(requestedKey);
            if (requestedEntry != null && requestedEntry.status == Status.PENDING) {
                SERVER_VALUES.remove(requestedKey);
            }
        }
        Status status = matchType == MatchType.MISSING ? Status.KNOWN_MISSING : Status.KNOWN;
        put(resolvedKey, new CacheEntry(status, value, responseGeneration, System.nanoTime()));
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

    private record CacheEntry(Status status, BigDecimal value, long generation, long updatedNanos) {
        private Lookup toLookup() {
            return new Lookup(status, Optional.ofNullable(value));
        }

        private boolean isExpiredPending() {
            return status == Status.PENDING && System.nanoTime() - updatedNanos >= PENDING_TIMEOUT_NANOS;
        }
    }
}
