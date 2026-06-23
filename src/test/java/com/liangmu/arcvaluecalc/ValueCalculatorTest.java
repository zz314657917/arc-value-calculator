package com.liangmu.arcvaluecalc;

import com.liangmu.arcvaluecalc.model.RuleIngredient;
import com.liangmu.arcvaluecalc.model.ValueEntry;
import com.liangmu.arcvaluecalc.model.ValueKey;
import com.liangmu.arcvaluecalc.model.ValueRule;
import com.liangmu.arcvaluecalc.model.ValueSource;
import com.liangmu.arcvaluecalc.service.ValueCalculator;
import com.liangmu.arcvaluecalc.service.ValueFormatter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ValueCalculatorTest {
    private final ValueKey iron = key("minecraft:iron_ingot");
    private final ValueKey nugget = key("minecraft:iron_nugget");
    private final ValueKey block = key("minecraft:iron_block");
    private final ValueKey gear = key("test:gear");
    private final ValueKey plate = key("test:plate");

    @Test
    void manualValuesWinOverRules() {
        ValueRule rule = new ValueRule(
                "iron_to_block",
                List.of(RuleIngredient.item(iron.item(), 9)),
                List.of(RuleIngredient.item(block.item(), 1)),
                ValueSource.GENERATED_RULE
        );
        Map<ValueKey, BigDecimal> manual = Map.of(
                iron, new BigDecimal("0.10"),
                block, new BigDecimal("99.00")
        );
        Map<ValueKey, ValueEntry> result = calculate(manual, List.of(), List.of(rule)).values();
        assertValueEquals("99.00", result.get(block).value());
        assertEquals(ValueSource.MANUAL_VALUE, result.get(block).source());
    }

    @Test
    void generatedRuleCalculatesOutput() {
        ValueRule rule = new ValueRule(
                "nuggets",
                List.of(RuleIngredient.item(nugget.item(), 9)),
                List.of(RuleIngredient.item(iron.item(), 1)),
                ValueSource.GENERATED_RULE
        );
        Map<ValueKey, ValueEntry> result = calculate(Map.of(nugget, new BigDecimal("0.01")), List.of(), List.of(rule)).values();
        assertValueEquals("0.09", result.get(iron).value());
    }

    @Test
    void manualRulesWinOverGeneratedRules() {
        ValueRule manualRule = new ValueRule(
                "manual",
                List.of(RuleIngredient.item(iron.item(), 2)),
                List.of(RuleIngredient.item(gear.item(), 1)),
                ValueSource.MANUAL_RULE
        );
        ValueRule generatedRule = new ValueRule(
                "generated",
                List.of(RuleIngredient.item(iron.item(), 1)),
                List.of(RuleIngredient.item(gear.item(), 1)),
                ValueSource.GENERATED_RULE
        );
        Map<ValueKey, ValueEntry> result = calculate(Map.of(iron, new BigDecimal("1.00")), List.of(manualRule), List.of(generatedRule)).values();
        assertValueEquals("2.00", result.get(gear).value());
        assertEquals(ValueSource.MANUAL_RULE, result.get(gear).source());
    }

    @Test
    void tagInputsUseCheapestKnownItem() {
        ResourceLocation tag = new ResourceLocation("forge", "ingots/iron");
        ValueRule rule = new ValueRule(
                "tag",
                List.of(RuleIngredient.tag(tag, 4)),
                List.of(RuleIngredient.item(gear.item(), 1)),
                ValueSource.GENERATED_RULE
        );
        Map<ValueKey, BigDecimal> manual = Map.of(
                iron, new BigDecimal("1.00"),
                key("other:iron_ingot"), new BigDecimal("0.50")
        );
        Map<ResourceLocation, Set<ValueKey>> tags = Map.of(tag, Set.of(iron, key("other:iron_ingot")));
        Map<ValueKey, ValueEntry> result = new ValueCalculator()
                .calculate(manual, List.of(), List.of(rule), tags, 16)
                .values();
        assertValueEquals("2.00", result.get(gear).value());
    }

    @Test
    void tagValuesCanBeExpandedAsManualValues() {
        ResourceLocation tag = new ResourceLocation("minecraft", "logs");
        Map<ValueKey, BigDecimal> manual = new java.util.LinkedHashMap<>();
        for (ValueKey key : Set.of(key("minecraft:oak_log"), key("minecraft:spruce_log"))) {
            manual.putIfAbsent(key, new BigDecimal("0.04"));
        }
        ValueRule rule = new ValueRule(
                "log_to_planks",
                List.of(RuleIngredient.item(key("minecraft:spruce_log").item(), 1)),
                List.of(RuleIngredient.item(key("minecraft:spruce_planks").item(), 4)),
                ValueSource.GENERATED_RULE
        );
        Map<ValueKey, ValueEntry> result = calculate(manual, List.of(), List.of(rule)).values();
        assertValueEquals("0.01", result.get(key("minecraft:spruce_planks")).value());
    }

    @Test
    void choicesUseCheapestKnownCandidate() {
        ValueKey copper = key("minecraft:copper_ingot");
        ValueRule rule = new ValueRule(
                "choice",
                List.of(RuleIngredient.choices(List.of(iron, copper), 1)),
                List.of(RuleIngredient.item(gear.item(), 1)),
                ValueSource.GENERATED_RULE
        );
        Map<ValueKey, BigDecimal> manual = Map.of(
                iron, new BigDecimal("1.00"),
                copper, new BigDecimal("0.30")
        );
        Map<ValueKey, ValueEntry> result = calculate(manual, List.of(), List.of(rule)).values();
        assertValueEquals("0.30", result.get(gear).value());
    }

    @Test
    void samePriorityRulesUseLowestCost() {
        ValueRule expensive = new ValueRule(
                "expensive",
                List.of(RuleIngredient.item(iron.item(), 2)),
                List.of(RuleIngredient.item(gear.item(), 1)),
                ValueSource.MANUAL_RULE
        );
        ValueRule cheap = new ValueRule(
                "cheap",
                List.of(RuleIngredient.item(iron.item(), 1)),
                List.of(RuleIngredient.item(gear.item(), 1)),
                ValueSource.MANUAL_RULE
        );
        Map<ValueKey, ValueEntry> result = calculate(Map.of(iron, new BigDecimal("1.00")), List.of(expensive, cheap), List.of()).values();
        assertValueEquals("1.00", result.get(gear).value());
        assertEquals(ValueSource.MANUAL_RULE, result.get(gear).source());
    }

    @Test
    void manualAndGeneratedRulesConvergeTogether() {
        ValueRule generated = new ValueRule(
                "generated_gear",
                List.of(RuleIngredient.item(iron.item(), 1)),
                List.of(RuleIngredient.item(gear.item(), 1)),
                ValueSource.GENERATED_RULE
        );
        ValueRule manual = new ValueRule(
                "manual_plate",
                List.of(RuleIngredient.item(gear.item(), 1)),
                List.of(RuleIngredient.item(plate.item(), 1)),
                ValueSource.MANUAL_RULE
        );
        Map<ValueKey, ValueEntry> result = calculate(Map.of(iron, new BigDecimal("1.00")), List.of(manual), List.of(generated)).values();
        assertValueEquals("1.00", result.get(plate).value());
        assertEquals(ValueSource.MANUAL_RULE, result.get(plate).source());
    }

    @Test
    void tinyPositiveOutputsUseMinimumValueInsteadOfZero() {
        ValueRule rule = new ValueRule(
                "tiny",
                List.of(RuleIngredient.item(iron.item(), 1)),
                List.of(RuleIngredient.item(gear.item(), 3)),
                ValueSource.GENERATED_RULE
        );
        Map<ValueKey, ValueEntry> result = calculate(Map.of(iron, new BigDecimal("0.0001")), List.of(), List.of(rule)).values();
        assertValueEquals("0.0001", result.get(gear).value());
    }

    @Test
    void formatterKeepsTwoDecimals() {
        assertEquals("0.10", ValueFormatter.display(new BigDecimal("0.1")));
        assertEquals("0.46", ValueFormatter.display(new BigDecimal("0.455")));
    }

    private ValueCalculator.Result calculate(Map<ValueKey, BigDecimal> manual, List<ValueRule> manualRules, List<ValueRule> generatedRules) {
        return new ValueCalculator().calculate(manual, manualRules, generatedRules, Map.of(), 16);
    }

    private ValueKey key(String id) {
        return new ValueKey(new ResourceLocation(id), (String) null);
    }

    private void assertValueEquals(String expected, BigDecimal actual) {
        assertTrue(new BigDecimal(expected).compareTo(actual) == 0, "expected " + expected + " but was " + actual);
    }
}
