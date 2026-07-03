package com.liangmu.arcvaluecalc;

import com.google.gson.JsonParser;
import com.liangmu.arcvaluecalc.model.RuleIngredient;
import com.liangmu.arcvaluecalc.model.ValueKey;
import com.liangmu.arcvaluecalc.model.ValueRule;
import com.liangmu.arcvaluecalc.model.ValueSource;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void rejectsOversizedInputNbtBeforeCalculation() {
        String oversized = "{display:{Name:'" + "a".repeat(ValueKey.MAX_NBT_CHARS + 1) + "'}}";
        var json = JsonParser.parseString("""
                {
                  "input": [{"item": "minecraft:stick", "nbt": "%s"}],
                  "output": [{"item": "minecraft:diamond"}]
                }
                """.formatted(oversized.replace("\\", "\\\\").replace("\"", "\\\""))).getAsJsonObject();

        assertThrows(IllegalArgumentException.class, () -> ValueRule.fromJson("oversized_nbt", json, ValueSource.MANUAL_RULE));
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
    void ruleIngredientFromValueKeyPreservesNbt() {
        ValueKey key = new ValueKey(new ResourceLocation("minecraft", "diamond_sword"), "{Damage:0}");

        RuleIngredient ingredient = RuleIngredient.item(key, 2);

        assertEquals(key, ingredient.asKey());
        assertEquals(2, ingredient.count());
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
