package com.liangmu.arcvaluecalc.storage;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RuleFileStoreTest {
    @Test
    void sanitizedGeneratedRulePathCannotEscapeRoot() {
        RuleFileStore store = new RuleFileStore();
        String sanitized = store.sanitize("../../outside");
        Path root = Path.of("root").normalize();
        Path resolved = root.resolve(sanitized + ".json").normalize();

        assertFalse(sanitized.contains(".."));
        assertTrue(resolved.startsWith(root));
    }
}
