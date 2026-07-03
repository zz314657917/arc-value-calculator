package com.liangmu.arcvaluecalc.model;

import java.math.BigDecimal;

public record TraceInput(RuleIngredient ingredient, ValueKey selectedKey, BigDecimal unitValue, BigDecimal totalValue, boolean missing) {
}
