package com.liangmu.arcvaluecalc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ValueFormatter {
    private ValueFormatter() {
    }

    public static String display(BigDecimal value) {
        return PriceParser.normalizeComputed(value).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
