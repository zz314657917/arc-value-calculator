package com.liangmu.arcvaluecalc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

public final class PriceParser {
    public static final int MAX_RAW_LENGTH = 32;
    public static final int MAX_SCALE = 4;
    public static final BigDecimal MAX_VALUE = new BigDecimal("1000000000000");
    private static final Pattern PRICE_PATTERN = Pattern.compile("\\d{1,15}(?:\\.\\d{1,4})?");

    private PriceParser() {
    }

    public static BigDecimal parsePrice(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("价格格式无效");
        }
        String normalized = raw.trim();
        if (normalized.length() > MAX_RAW_LENGTH || !PRICE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("价格格式无效");
        }
        BigDecimal value = new BigDecimal(normalized);
        validateInputValue(value);
        return strip(value);
    }

    public static String toPlainString(BigDecimal value) {
        BigDecimal normalized = normalizeComputed(value);
        return normalized.toPlainString();
    }

    public static void validateComputed(BigDecimal value) {
        validateComputedValue(value);
    }

    public static BigDecimal normalizeComputed(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("价格格式无效");
        }
        BigDecimal scaled = value.setScale(MAX_SCALE, RoundingMode.HALF_UP);
        validateInputValue(scaled);
        return strip(scaled);
    }

    private static void validateInputValue(BigDecimal value) {
        if (value.signum() < 0
                || value.precision() > 19
                || value.scale() > MAX_SCALE
                || value.compareTo(MAX_VALUE) > 0) {
            throw new IllegalArgumentException("价格超出允许范围");
        }
    }

    private static void validateComputedValue(BigDecimal value) {
        if (value == null
                || value.signum() < 0
                || value.compareTo(MAX_VALUE) > 0) {
            throw new IllegalArgumentException("价格超出允许范围");
        }
    }

    private static BigDecimal strip(BigDecimal value) {
        BigDecimal stripped = value.stripTrailingZeros();
        if (stripped.scale() < 0) {
            return stripped.setScale(0);
        }
        return stripped;
    }
}
