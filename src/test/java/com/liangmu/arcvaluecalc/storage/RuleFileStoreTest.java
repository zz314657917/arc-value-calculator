package com.liangmu.arcvaluecalc.storage;

import com.liangmu.arcvaluecalc.model.RuleIngredient;
import com.liangmu.arcvaluecalc.model.ValueRule;
import com.liangmu.arcvaluecalc.model.ValueSource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RuleFileStoreTest {
    @TempDir
    Path tempDir;

    @Test
    void sanitizedGeneratedRulePathCannotEscapeRoot() {
        RuleFileStore store = new RuleFileStore(tempDir.resolve("manual"), tempDir.resolve("generated"));
        String sanitized = store.sanitize("../../outside");
        Path root = Path.of("root").normalize();
        Path resolved = root.resolve(sanitized + ".json").normalize();

        assertFalse(sanitized.contains(".."));
        assertTrue(resolved.startsWith(root));
    }

    @Test
    void generatedRulesReplaceDirectoryAfterSuccessfulWrite() throws Exception {
        Path manual = tempDir.resolve("manual");
        Path generated = tempDir.resolve("generated");
        Files.createDirectories(generated.resolve("old"));
        Files.writeString(generated.resolve("old/stale.json"), "stale", StandardCharsets.UTF_8);
        RuleFileStore store = new RuleFileStore(manual, generated);
        ValueRule rule = new ValueRule(
                "minecraft:stick",
                List.of(RuleIngredient.item(new ResourceLocation("minecraft", "planks"), 1)),
                List.of(RuleIngredient.item(new ResourceLocation("minecraft", "stick"), 4)),
                ValueSource.GENERATED_RULE
        );

        store.writeGeneratedRules(List.of(rule));

        assertFalse(Files.exists(generated.resolve("old/stale.json")));
        assertTrue(Files.exists(generated.resolve("minecraft/stick.json")));
    }
}
