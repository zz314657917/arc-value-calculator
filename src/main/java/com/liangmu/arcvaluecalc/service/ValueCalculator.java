package com.liangmu.arcvaluecalc.service;

import com.liangmu.arcvaluecalc.model.RuleIngredient;
import com.liangmu.arcvaluecalc.model.ValueEntry;
import com.liangmu.arcvaluecalc.model.ValueKey;
import com.liangmu.arcvaluecalc.model.ValueRule;
import com.liangmu.arcvaluecalc.model.ValueSource;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class ValueCalculator {
    private static final MathContext MC = MathContext.DECIMAL128;

    public Result calculate(
            Map<ValueKey, BigDecimal> manualValues,
            List<ValueRule> manualRules,
            List<ValueRule> generatedRules,
            Map<ResourceLocation, Set<ValueKey>> tagIndex,
            int maxIterations
    ) {
        Map<ValueKey, ValueEntry> values = new LinkedHashMap<>();
        manualValues.forEach((key, value) -> values.put(key, new ValueEntry(value, ValueSource.MANUAL_VALUE)));
        relax(values, manualRules, tagIndex, maxIterations, true);
        relax(values, generatedRules, tagIndex, maxIterations, false);
        return new Result(values);
    }

    private void relax(
            Map<ValueKey, ValueEntry> values,
            List<ValueRule> rules,
            Map<ResourceLocation, Set<ValueKey>> tagIndex,
            int maxIterations,
            boolean overwriteGenerated
    ) {
        for (int i = 0; i < maxIterations; i++) {
            boolean changed = false;
            for (ValueRule rule : rules) {
                BigDecimal inputValue = inputValue(rule.inputs(), values, tagIndex);
                if (inputValue == null) {
                    continue;
                }
                int outputCount = rule.outputs().stream().mapToInt(RuleIngredient::count).sum();
                if (outputCount <= 0) {
                    continue;
                }
                BigDecimal each = inputValue.divide(BigDecimal.valueOf(outputCount), MC);
                for (RuleIngredient output : rule.outputs()) {
                    ValueKey outputKey = output.asKey();
                    if (outputKey == null) {
                        continue;
                    }
                    ValueEntry existing = values.get(outputKey);
                    if (existing != null && existing.source() == ValueSource.MANUAL_VALUE) {
                        continue;
                    }
                    if (existing != null && existing.source() == ValueSource.MANUAL_RULE && rule.source() == ValueSource.GENERATED_RULE) {
                        continue;
                    }
                    if (existing == null || overwriteGenerated || each.compareTo(existing.value()) < 0) {
                        values.put(outputKey, new ValueEntry(each, rule.source()));
                        changed = true;
                    }
                }
            }
            if (!changed) {
                return;
            }
        }
    }

    private BigDecimal inputValue(
            List<RuleIngredient> inputs,
            Map<ValueKey, ValueEntry> values,
            Map<ResourceLocation, Set<ValueKey>> tagIndex
    ) {
        BigDecimal total = BigDecimal.ZERO;
        for (RuleIngredient input : inputs) {
            BigDecimal value;
            if (input.isTag()) {
                value = bestTagValue(input.tag(), values, tagIndex);
            } else {
                ValueEntry entry = values.get(input.asKey());
                value = entry == null ? null : entry.value();
            }
            if (value == null) {
                return null;
            }
            total = total.add(value.multiply(BigDecimal.valueOf(input.count()), MC), MC);
        }
        return total;
    }

    private BigDecimal bestTagValue(
            ResourceLocation tag,
            Map<ValueKey, ValueEntry> values,
            Map<ResourceLocation, Set<ValueKey>> tagIndex
    ) {
        Set<ValueKey> keys = tagIndex.get(tag);
        if (keys == null || keys.isEmpty()) {
            return null;
        }
        BigDecimal best = null;
        for (ValueKey key : keys) {
            ValueEntry entry = values.get(key);
            if (entry == null) {
                continue;
            }
            if (best == null || entry.value().compareTo(best) < 0) {
                best = entry.value();
            }
        }
        return best;
    }

    public record Result(Map<ValueKey, ValueEntry> values) {
    }
}
