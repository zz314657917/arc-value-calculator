package com.liangmu.arcvaluecalc;

import com.liangmu.arcvaluecalc.service.PriceParser;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class PriceParserTest {
    @Test
    void parsesBoundedDecimalPrices() {
        assertEquals("0.45", PriceParser.parsePrice("0.4500").toPlainString());
        assertEquals("1000000000000", PriceParser.parsePrice("1000000000000").toPlainString());
    }

    @Test
    void rejectsDangerousOrOutOfRangePrices() {
        assertThrows(IllegalArgumentException.class, () -> PriceParser.parsePrice("1e100000000"));
        assertThrows(IllegalArgumentException.class, () -> PriceParser.parsePrice("-1"));
        assertThrows(IllegalArgumentException.class, () -> PriceParser.parsePrice("1.12345"));
        assertThrows(IllegalArgumentException.class, () -> PriceParser.parsePrice("1000000000001"));
        assertThrows(IllegalArgumentException.class, () -> PriceParser.parsePrice("123456789012345678901234567890123"));
    }

    @Test
    void serializesComputedValuesWithinScaleLimit() {
        assertEquals("0.3333", PriceParser.toPlainString(new BigDecimal("0.333333333333333333")));
    }
}
