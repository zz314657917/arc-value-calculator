package com.liangmu.arcvaluecalc;

import com.liangmu.arcvaluecalc.model.ValueKey;
import com.liangmu.arcvaluecalc.service.PriceParser;
import com.liangmu.arcvaluecalc.storage.ConfigWriteBlockedException;
import com.liangmu.arcvaluecalc.storage.LoadResult;
import com.liangmu.arcvaluecalc.storage.ValueFileStore;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ValueFileStoreTest {
    @TempDir
    Path tempDir;

    @Test
    void badManualEntryDoesNotDiscardLaterValidEntriesOrAllowOverwrite() throws Exception {
        Path itemValues = tempDir.resolve("item_values.json");
        Path tagValues = tempDir.resolve("tag_values.json");
        Files.writeString(itemValues, """
                [
                  {"item":"minecraft:stone","value":"0.02"},
                  {"item":"minecraft:diamond","value":"1e100000000"},
                  {"item":"minecraft:gold_ingot","value":"0.20"}
                ]
                """, StandardCharsets.UTF_8);

        ValueFileStore store = new ValueFileStore(itemValues, tagValues);
        LoadResult<ValueKey, BigDecimal> result = store.loadManualValues();

        assertTrue(result.hasErrors());
        assertTrue(result.values().containsKey(key("minecraft:stone")));
        assertTrue(result.values().containsKey(key("minecraft:gold_ingot")));
        assertThrows(ConfigWriteBlockedException.class, () -> store.setManualValue(key("minecraft:iron_ingot"), PriceParser.parsePrice("0.11")));
        assertTrue(Files.readString(itemValues, StandardCharsets.UTF_8).contains("minecraft:gold_ingot"));
    }

    @Test
    void missingDefaultSeedIsNotMergedBackIntoExistingFile() throws Exception {
        Path itemValues = tempDir.resolve("item_values.json");
        Path tagValues = tempDir.resolve("tag_values.json");
        Files.writeString(itemValues, "[]", StandardCharsets.UTF_8);

        ValueFileStore store = new ValueFileStore(itemValues, tagValues);
        LoadResult<ValueKey, BigDecimal> result = store.loadManualValues();

        assertFalse(result.values().containsKey(key("minecraft:diamond")));
        assertFalse(Files.readString(itemValues, StandardCharsets.UTF_8).contains("minecraft:diamond"));
    }

    @Test
    void saveCreatesBackupBeforeReplacingExistingFile() throws Exception {
        Path itemValues = tempDir.resolve("item_values.json");
        Path tagValues = tempDir.resolve("tag_values.json");
        Files.writeString(itemValues, "[{\"item\":\"minecraft:stone\",\"value\":\"0.02\"}]", StandardCharsets.UTF_8);

        ValueFileStore store = new ValueFileStore(itemValues, tagValues);
        store.setManualValue(key("minecraft:iron_ingot"), PriceParser.parsePrice("0.11"));

        assertTrue(Files.exists(tempDir.resolve("item_values.json.bak")));
        assertTrue(Files.readString(itemValues, StandardCharsets.UTF_8).contains("minecraft:iron_ingot"));
    }

    private ValueKey key(String id) {
        return new ValueKey(new ResourceLocation(id), (String) null);
    }
}
