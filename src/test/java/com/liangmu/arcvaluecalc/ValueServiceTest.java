package com.liangmu.arcvaluecalc.service;

import com.liangmu.arcvaluecalc.model.ValueKey;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ValueServiceTest {
    @Test
    void tagValuesDoNotOverrideExplicitItemValues() throws Exception {
        ValueKey oakLog = key("minecraft:oak_log");
        ValueKey spruceLog = key("minecraft:spruce_log");
        ResourceLocation logs = new ResourceLocation("minecraft", "logs");
        ResourceLocation cheapLogs = new ResourceLocation("test", "cheap_logs");
        Map<ValueKey, BigDecimal> itemValues = new LinkedHashMap<>();
        itemValues.put(oakLog, new BigDecimal("1.00"));
        Map<ResourceLocation, BigDecimal> tagValues = new LinkedHashMap<>();
        tagValues.put(logs, new BigDecimal("0.04"));
        tagValues.put(cheapLogs, new BigDecimal("0.02"));
        Map<ResourceLocation, Set<ValueKey>> tagIndex = Map.of(
                logs, Set.of(oakLog, spruceLog),
                cheapLogs, Set.of(spruceLog)
        );

        expandTagValues(itemValues, tagValues, tagIndex);

        assertEquals(0, new BigDecimal("1.00").compareTo(itemValues.get(oakLog)));
        assertEquals(0, new BigDecimal("0.02").compareTo(itemValues.get(spruceLog)));
    }

    private void expandTagValues(
            Map<ValueKey, BigDecimal> itemValues,
            Map<ResourceLocation, BigDecimal> tagValues,
            Map<ResourceLocation, Set<ValueKey>> tagIndex
    ) {
        ValueService.expandTagValues(itemValues, tagValues, tagIndex);
    }

    private ValueKey key(String id) {
        return new ValueKey(new ResourceLocation(id), (String) null);
    }
}
