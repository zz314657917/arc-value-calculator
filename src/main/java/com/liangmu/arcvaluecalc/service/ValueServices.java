package com.liangmu.arcvaluecalc.service;

import net.minecraftforge.server.ServerLifecycleHooks;

public final class ValueServices {
    private static final ValueService SERVER = new ValueService(true);
    private static final ValueService CLIENT_FALLBACK = new ValueService(false);

    private ValueServices() {
    }

    public static ValueService server() {
        return SERVER;
    }

    public static ValueService clientFallback() {
        return CLIENT_FALLBACK;
    }

    public static ValueService apiQuery() {
        return ServerLifecycleHooks.getCurrentServer() == null ? CLIENT_FALLBACK : SERVER;
    }
}
