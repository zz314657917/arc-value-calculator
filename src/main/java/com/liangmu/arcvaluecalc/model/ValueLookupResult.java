package com.liangmu.arcvaluecalc.model;

import java.math.BigDecimal;
import java.util.Optional;

public record ValueLookupResult(MatchType matchType, ValueKey resolvedKey, BigDecimal value, long generation) {
    public static ValueLookupResult missing(ValueKey requestedKey, long generation) {
        return new ValueLookupResult(MatchType.MISSING, requestedKey, null, generation);
    }

    public Optional<BigDecimal> optionalValue() {
        return Optional.ofNullable(value);
    }
}
