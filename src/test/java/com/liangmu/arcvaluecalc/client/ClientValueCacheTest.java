package com.liangmu.arcvaluecalc.client;

import com.liangmu.arcvaluecalc.model.MatchType;
import com.liangmu.arcvaluecalc.model.ValueKey;
import java.math.BigDecimal;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClientValueCacheTest {
    @BeforeEach
    void clearCache() {
        ClientValueCache.disconnect();
    }

    @Test
    void disconnectDoesNotCreateClientSideGenerationAheadOfServer() {
        ClientValueCache.reset(5L);
        ClientValueCache.disconnect();
        ValueKey diamond = key("minecraft:diamond");
        ClientValueCache.applyResponse(MatchType.ITEM_ONLY, diamond, diamond, new BigDecimal("0.45"), 1L);

        assertEquals(1L, ClientValueCache.generation());
        assertTrue(ClientValueCache.serverAvailable());
    }

    @Test
    void staleResponsesDoNotOverwriteNewerGenerationOrMarkServerAvailable() {
        ValueKey diamond = key("minecraft:diamond");
        ClientValueCache.reset(5L);
        ClientValueCache.applyResponse(MatchType.ITEM_ONLY, diamond, diamond, new BigDecimal("0.45"), 4L);

        assertEquals(5L, ClientValueCache.generation());
        assertTrue(!ClientValueCache.serverAvailable());
        assertTrue(ClientValueCache.getByKey(diamond).value().isEmpty());
    }

    @Test
    void itemOnlyResponseClearsExactPendingKey() {
        ValueKey exact = new ValueKey(new ResourceLocation("minecraft:diamond_sword"), "{Damage:0}");
        ValueKey itemOnly = key("minecraft:diamond_sword");
        ClientValueCache.reset(1L);
        assertTrue(ClientValueCache.markPending(exact));

        ClientValueCache.applyResponse(MatchType.ITEM_ONLY, exact, itemOnly, new BigDecimal("3.50"), 1L);

        assertEquals(ClientValueCache.Status.NOT_REQUESTED, ClientValueCache.getByKey(exact).status());
        assertEquals(ClientValueCache.Status.KNOWN, ClientValueCache.getByKey(itemOnly).status());
    }

    @Test
    void staleReloadDoesNotClearNewerGeneration() {
        ValueKey diamond = key("minecraft:diamond");
        ClientValueCache.reset(5L);
        ClientValueCache.applyResponse(MatchType.ITEM_ONLY, diamond, diamond, new BigDecimal("0.45"), 5L);

        ClientValueCache.reset(4L);

        assertEquals(5L, ClientValueCache.generation());
        assertEquals(ClientValueCache.Status.KNOWN, ClientValueCache.getByKey(diamond).status());
    }

    private ValueKey key(String id) {
        return new ValueKey(new ResourceLocation(id), (String) null);
    }
}
