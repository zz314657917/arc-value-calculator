package com.liangmu.arcvaluecalc.model;

import java.math.BigDecimal;
import java.util.List;

public final class ValueEntry {
    private final BigDecimal value;
    private final ValueSource source;
    private final String ruleId;
    private final List<TraceInput> inputs;

    public ValueEntry(BigDecimal value, ValueSource source) {
        this(value, source, "", List.of());
    }

    public ValueEntry(BigDecimal value, ValueSource source, String ruleId, List<TraceInput> inputs) {
        this.value = value;
        this.source = source;
        this.ruleId = ruleId == null ? "" : ruleId;
        this.inputs = inputs == null ? List.of() : List.copyOf(inputs);
    }

    public BigDecimal value() {
        return value;
    }

    public ValueSource source() {
        return source;
    }

    public String ruleId() {
        return ruleId;
    }

    public List<TraceInput> inputs() {
        return inputs;
    }
}
