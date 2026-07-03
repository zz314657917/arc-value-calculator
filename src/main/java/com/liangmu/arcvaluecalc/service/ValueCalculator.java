package com.liangmu.arcvaluecalc.service;

import com.liangmu.arcvaluecalc.model.RuleIngredient;
import com.liangmu.arcvaluecalc.model.TraceInput;
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
                try {
                    changed |= evaluateRule(values, tagIndex, lastChanged, rule);
                } catch (RuntimeException e) {
                    ArcValueCalc.LOGGER.error("Skipping invalid value rule {}", rule.id(), e);
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

    private boolean evaluateRule(
            Map<ValueKey, ValueEntry> values,
            Map<ResourceLocation, Set<ValueKey>> tagIndex,
            List<String> lastChanged,
            ValueRule rule
    ) {
        InputTrace inputTrace = inputValue(rule.inputs(), values, tagIndex);
        if (inputTrace == null) {
            return false;
        }
        long outputCount = rule.outputs().stream().mapToLong(RuleIngredient::count).sum();
        if (outputCount <= 0) {
            return false;
        }
        BigDecimal each;
        try {
            each = inputTrace.total().divide(BigDecimal.valueOf(outputCount), MC);
            if (each.signum() > 0 && each.compareTo(MIN_POSITIVE_VALUE) < 0) {
                each = MIN_POSITIVE_VALUE;
            }
            PriceParser.validateComputed(each);
        } catch (IllegalArgumentException e) {
            ArcValueCalc.LOGGER.warn("Skipping value rule {} because calculated value is out of range", rule.id(), e);
            return false;
        }
        boolean changed = false;
        for (RuleIngredient output : rule.outputs()) {
            ValueKey outputKey = output.asKey();
            if (outputKey == null) {
                continue;
            }
            ValueEntry existing = values.get(outputKey);
            if (shouldReplace(existing, each, rule.source())) {
                values.put(outputKey, new ValueEntry(each, rule.source(), rule.id(), inputTrace.inputs()));
                changed = true;
                if (lastChanged.size() < 10) {
                    lastChanged.add(outputKey + " via " + rule.id());
                }
            }
        }
        return changed;
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

    private InputTrace inputValue(
            List<RuleIngredient> inputs,
            Map<ValueKey, ValueEntry> values,
            Map<ResourceLocation, Set<ValueKey>> tagIndex
    ) {
        BigDecimal total = BigDecimal.ZERO;
        List<TraceInput> traceInputs = new ArrayList<>();
        for (RuleIngredient input : inputs) {
            SelectedInput selected;
            if (input.isTag()) {
                selected = bestTagValue(input.tag(), values, tagIndex);
            } else if (input.isChoices()) {
                selected = bestChoiceValue(input.choices(), values);
            } else {
                ValueKey key = input.asKey();
                ValueEntry entry = values.get(key);
                selected = entry == null ? null : new SelectedInput(key, entry.value());
            }
            if (selected == null) {
                return null;
            }
            BigDecimal inputTotal = selected.value().multiply(BigDecimal.valueOf(input.count()), MC);
            traceInputs.add(new TraceInput(input, selected.key(), selected.value(), inputTotal, false));
            total = total.add(inputTotal, MC);
        }
        return new InputTrace(total, traceInputs);
    }

    private SelectedInput bestChoiceValue(List<ValueKey> choices, Map<ValueKey, ValueEntry> values) {
        SelectedInput best = null;
        for (ValueKey choice : choices) {
            ValueEntry entry = values.get(choice);
            if (entry == null) {
                continue;
            }
            if (best == null || entry.value().compareTo(best.value()) < 0) {
                best = new SelectedInput(choice, entry.value());
            }
        }
        return best;
    }

    private SelectedInput bestTagValue(
            ResourceLocation tag,
            Map<ValueKey, ValueEntry> values,
            Map<ResourceLocation, Set<ValueKey>> tagIndex
    ) {
        Set<ValueKey> keys = tagIndex.get(tag);
        if (keys == null || keys.isEmpty()) {
            return null;
        }
        SelectedInput best = null;
        for (ValueKey key : keys) {
            ValueEntry entry = values.get(key);
            if (entry == null) {
                continue;
            }
            if (best == null || entry.value().compareTo(best.value()) < 0) {
                best = new SelectedInput(key, entry.value());
            }
        }
        return best;
    }

    public record Result(Map<ValueKey, ValueEntry> values) {
    }

    private record InputTrace(BigDecimal total, List<TraceInput> inputs) {
    }

    private record SelectedInput(ValueKey key, BigDecimal value) {
    }
}
