package com.liangmu.arcvaluecalc.network;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;

public final class ValueRequestLimiter {
    private static final long WINDOW_NANOS = 1_000_000_000L;
    private static final int MAX_REQUESTS_PER_WINDOW = 64;
    private static final Map<UUID, Window> WINDOWS = new ConcurrentHashMap<>();

    private ValueRequestLimiter() {
    }

    public static boolean allow(ServerPlayer player) {
        long now = System.nanoTime();
        Window window = WINDOWS.computeIfAbsent(player.getUUID(), ignored -> new Window(now));
        synchronized (window) {
            if (now - window.windowStartNanos >= WINDOW_NANOS) {
                window.windowStartNanos = now;
                window.count = 0;
            }
            if (window.count >= MAX_REQUESTS_PER_WINDOW) {
                return false;
            }
            window.count++;
            return true;
        }
    }

    private static final class Window {
        private long windowStartNanos;
        private int count;

        private Window(long windowStartNanos) {
            this.windowStartNanos = windowStartNanos;
        }
    }
}
