package com.liangmu.arcvaluecalc.service;

import com.liangmu.arcvaluecalc.model.RuleIngredient;
import com.liangmu.arcvaluecalc.model.ValueEntry;
import com.liangmu.arcvaluecalc.model.ValueKey;
import com.liangmu.arcvaluecalc.model.ValueRule;
import com.liangmu.arcvaluecalc.model.ValueSource;
import com.liangmu.arcvaluecalc.ArcValueCalc;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class ValueCalculator {
    private static final MathContext MC = MathContext.DECIMAL128;
    private static final BigDecimal MIN_POSITIVE_VALUE = new BigDecimal("0.0001");

    public Result calculate(
            Map<ValueKey, BigDecimal> manualValues,
            List<ValueRule> manualRules,
            List<ValueRule> generatedRules,
            Map<ResourceLocation, Set<ValueKey>> tagIndex,
            int maxIterations
    ) {
        Map<ValueKey, ValueEntry> values = new LinkedHashMap<>();
        manualValues.forEach((key, value) -> values.put(key, new ValueEntry(PriceParser.normalizeComputed(value), ValueSource.MANUAL_VALUE)));
        List<ValueRule> allRules = new ArrayList<>(manualRules.size() + generatedRules.size());
        allRules.addAll(manualRules);
        allRules.addAll(generatedRules);
        relax(values, allRules, tagIndex, maxIterations);
        return new Result(values);
    }

    private void relax(
            Map<ValueKey, ValueEntry> values,
            List<ValueRule> rules,
            Map<ResourceLocation, Set<ValueKey>> tagIndex,
            int maxIterations
    ) {
        List<String> lastChanged = new ArrayList<>();
        for (int i = 0; i < maxIterations; i++) {
            boolean changed = false;
            lastChanged.clear();
            for (ValueRule rule : rules) {
                BigDecimal inputValue = inputValue(rule.inputs(), values, tagIndex);
                if (inputValue == null) {
                    continue;
                }
                long outputCount = rule.outputs().stream().mapToLong(RuleIngredient::count).sum();
                if (outputCount <= 0) {
                    continue;
                }
                BigDecimal each;
                try {
                    each = inputValue.divide(BigDecimal.valueOf(outputCount), MC);
                    if (each.signum() > 0 && each.compareTo(MIN_POSITIVE_VALUE) < 0) {
                        each = MIN_POSITIVE_VALUE;
                    }
                    PriceParser.validateComputed(each);
                } catch (IllegalArgumentException e) {
                    ArcValueCalc.LOGGER.warn("Skipping value rule {} because calculated value is out of range", rule.id(), e);
                    continue;
                }
                for (RuleIngredient output : rule.outputs()) {
                    ValueKey outputKey = output.asKey();
                    if (outputKey == null) {
                        continue;
                    }
                    ValueEntry existing = values.get(outputKey);
                    if (shouldReplace(existing, each, rule.source())) {
                        values.put(outputKey, new ValueEntry(each, rule.source()));
                        changed = true;
                        if (lastChanged.size() < 10) {
                            lastChanged.add(outputKey + " via " + rule.id());
                        }
                    }
                }
            }
            if (!changed) {
                return;
            }
        }
        if (!lastChanged.isEmpty()) {
            ArcValueCalc.LOGGER.warn("Value calculation reached maxIterations. Still changing: {}", lastChanged);
        }
    }

    private boolean shouldReplace(ValueEntry existing, BigDecimal value, ValueSource newSource) {
        if (existing == null) {
            return true;
        }
        int oldPriority = priority(existing.source());
        int newPriority = priority(newSource);
        if (newPriority > oldPriority) {
            return true;
        }
        return newPriority == oldPriority && value.compareTo(existing.value()) < 0;
    }

    private int priority(ValueSource source) {
        return switch (source) {
            case MANUAL_VALUE -> 3;
            case MANUAL_RULE -> 2;
            case GENERATED_RULE -> 1;
            case SERVER, NONE -> 0;
        };
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
            } else if (input.isChoices()) {
                value = bestChoiceValue(input.choices(), values);
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

    private BigDecimal bestChoiceValue(List<ValueKey> choices, Map<ValueKey, ValueEntry> values) {
        BigDecimal best = null;
        for (ValueKey choice : choices) {
            ValueEntry entry = values.get(choice);
            if (entry == null) {
                continue;
            }
            if (best == null || entry.value().compareTo(best) < 0) {
                best = entry.value();
            }
        }
        return best;
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
