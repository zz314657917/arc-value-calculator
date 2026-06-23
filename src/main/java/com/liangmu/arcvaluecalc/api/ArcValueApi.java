package com.liangmu.arcvaluecalc.api;

import com.liangmu.arcvaluecalc.model.ValueKey;
import com.liangmu.arcvaluecalc.model.ValueSource;
import com.liangmu.arcvaluecalc.service.ValueService;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;

public final class ArcValueApi {
    private ArcValueApi() {
    }

    public static Optional<BigDecimal> getValue(ItemStack stack) {
        return ValueService.get().getValue(stack);
    }

    public static ValueSource getSource(ItemStack stack) {
        return ValueService.get().getSource(stack);
    }

    public static Map<ValueKey, BigDecimal> snapshot() {
        return ValueService.get().snapshot();
    }
}
