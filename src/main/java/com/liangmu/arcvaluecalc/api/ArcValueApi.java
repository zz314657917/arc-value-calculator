package com.liangmu.arcvaluecalc.api;

import com.liangmu.arcvaluecalc.model.ValueKey;
import com.liangmu.arcvaluecalc.model.ValueSource;
import com.liangmu.arcvaluecalc.service.ValueServices;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;

public final class ArcValueApi {
    private ArcValueApi() {
    }

    public static Optional<BigDecimal> getValue(ItemStack stack) {
        return ValueServices.apiQuery().getValue(stack);
    }

    public static Optional<BigDecimal> getClientFallbackValue(ItemStack stack) {
        return ValueServices.clientFallback().getValue(stack);
    }

    public static Optional<BigDecimal> getServerValue(ItemStack stack) {
        return ValueServices.server().getValue(stack);
    }

    public static ValueSource getSource(ItemStack stack) {
        return ValueServices.apiQuery().getSource(stack);
    }

    public static ValueSource getClientFallbackSource(ItemStack stack) {
        return ValueServices.clientFallback().getSource(stack);
    }

    public static ValueSource getServerSource(ItemStack stack) {
        return ValueServices.server().getSource(stack);
    }

    public static Map<ValueKey, BigDecimal> snapshot() {
        return ValueServices.apiQuery().snapshot();
    }

    public static Map<ValueKey, BigDecimal> clientFallbackSnapshot() {
        return ValueServices.clientFallback().snapshot();
    }

    public static Map<ValueKey, BigDecimal> serverSnapshot() {
        return ValueServices.server().snapshot();
    }
}
