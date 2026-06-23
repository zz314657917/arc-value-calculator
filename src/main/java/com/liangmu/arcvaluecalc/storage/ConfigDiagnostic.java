package com.liangmu.arcvaluecalc.storage;

import java.nio.file.Path;

public record ConfigDiagnostic(Path path, String location, String message) {
    @Override
    public String toString() {
        return path + " " + location + ": " + message;
    }
}
