package com.liangmu.arcvaluecalc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ValueFormatter {
    private static final int DISPLAY_SCALE = 2;

    private ValueFormatter() {
    }

    public static String display(BigDecimal value) {
        return roundForDisplay(value).toPlainString();
    }

    public static boolean shouldDisplay(BigDecimal value) {
        return roundForDisplay(value).compareTo(BigDecimal.ZERO) > 0;
    }

    private static BigDecimal roundForDisplay(BigDecimal value) {
        return PriceParser.normalizeComputed(value).setScale(DISPLAY_SCALE, RoundingMode.HALF_UP);
    }
}
