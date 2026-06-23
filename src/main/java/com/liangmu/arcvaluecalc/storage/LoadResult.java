package com.liangmu.arcvaluecalc.storage;

import java.util.List;
import java.util.Map;

public record LoadResult<K, V>(Map<K, V> values, List<ConfigDiagnostic> diagnostics) {
    public boolean hasErrors() {
        return !diagnostics.isEmpty();
    }
}
