package com.liangmu.arcvaluecalc.model;

import java.math.BigDecimal;

public final class ValueEntry {
    private final BigDecimal value;
    private final ValueSource source;

    public ValueEntry(BigDecimal value, ValueSource source) {
        this.value = value;
        this.source = source;
    }

    public BigDecimal value() {
        return value;
    }

    public ValueSource source() {
        return source;
    }
}
