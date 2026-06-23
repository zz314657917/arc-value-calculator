package com.liangmu.arcvaluecalc;

import com.google.gson.JsonParser;
import com.liangmu.arcvaluecalc.model.ValueRule;
import com.liangmu.arcvaluecalc.model.ValueSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

final class ValueRuleValidationTest {
    @Test
    void rejectsInvalidNbtInsteadOfWideningRule() {
        var json = JsonParser.parseString("""
                {
                  "input": [{"item": "minecraft:stick", "nbt": "{"}],
                  "output": [{"item": "minecraft:diamond"}]
                }
                """).getAsJsonObject();

        assertThrows(IllegalArgumentException.class, () -> ValueRule.fromJson("bad_nbt", json, ValueSource.MANUAL_RULE));
    }

    @Test
    void rejectsZeroCount() {
        var json = JsonParser.parseString("""
                {
                  "input": [{"item": "minecraft:stick", "count": 0}],
                  "output": [{"item": "minecraft:diamond"}]
                }
                """).getAsJsonObject();

        assertThrows(IllegalArgumentException.class, () -> ValueRule.fromJson("bad_count", json, ValueSource.MANUAL_RULE));
    }

    @Test
    void rejectsEmptyOutputs() {
        var json = JsonParser.parseString("""
                {
                  "input": [{"item": "minecraft:stick"}],
                  "output": []
                }
                """).getAsJsonObject();

        assertThrows(IllegalArgumentException.class, () -> ValueRule.fromJson("empty_output", json, ValueSource.MANUAL_RULE));
    }
}
