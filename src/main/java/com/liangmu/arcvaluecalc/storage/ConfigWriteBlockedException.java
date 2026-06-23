package com.liangmu.arcvaluecalc.storage;

import java.io.IOException;
import java.util.List;

public final class ConfigWriteBlockedException extends IOException {
    private final List<ConfigDiagnostic> diagnostics;

    public ConfigWriteBlockedException(List<ConfigDiagnostic> diagnostics) {
        super("配置文件存在错误，已拒绝写入。请先修复 JSON 后重试。");
        this.diagnostics = List.copyOf(diagnostics);
    }

    public List<ConfigDiagnostic> diagnostics() {
        return diagnostics;
    }
}
