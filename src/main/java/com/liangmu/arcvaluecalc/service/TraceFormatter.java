package com.liangmu.arcvaluecalc.service;

import com.liangmu.arcvaluecalc.config.ArcValueConfig;
import com.liangmu.arcvaluecalc.model.ValueTrace;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class TraceFormatter {
    private TraceFormatter() {
    }

    public static List<Component> chatLines(ValueTrace trace) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("参考价格来源").withStyle(ChatFormatting.GOLD));
        append(lines, trace, 0);
        return lines;
    }

    public static String oneLine(ValueTrace trace) {
        if (trace.missing()) {
            return trace.label() + " 无参考价格";
        }
        String value = trace.value() == null ? "?" : ValueFormatter.display(trace.value()) + ArcValueConfig.VALUE_UNIT.get();
        String rule = trace.ruleId() == null || trace.ruleId().isBlank() ? "" : " [" + trace.ruleId() + "]";
        return trace.label() + " x" + trace.count() + " = " + value + " " + trace.source() + rule;
    }

    private static void append(List<Component> lines, ValueTrace trace, int depth) {
        MutableComponent line = Component.literal("  ".repeat(Math.max(0, depth)) + "- " + oneLine(trace));
        if (trace.missing()) {
            line.withStyle(ChatFormatting.RED);
        } else if (trace.cycle() || trace.truncated()) {
            line.withStyle(ChatFormatting.YELLOW);
        } else if (depth == 0) {
            line.withStyle(ChatFormatting.AQUA);
        }
        lines.add(line);
        if (trace.cycle()) {
            lines.add(Component.literal("  ".repeat(depth + 1) + "(循环引用，已停止展开)").withStyle(ChatFormatting.YELLOW));
            return;
        }
        if (trace.truncated()) {
            lines.add(Component.literal("  ".repeat(depth + 1) + "(达到最大深度，已截断)").withStyle(ChatFormatting.YELLOW));
            return;
        }
        for (ValueTrace child : trace.children()) {
            append(lines, child, depth + 1);
        }
    }
}
