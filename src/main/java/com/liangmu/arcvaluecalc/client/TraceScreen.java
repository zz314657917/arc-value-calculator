package com.liangmu.arcvaluecalc.client;

import com.liangmu.arcvaluecalc.config.ArcValueConfig;
import com.liangmu.arcvaluecalc.model.ValueKey;
import com.liangmu.arcvaluecalc.model.ValueSource;
import com.liangmu.arcvaluecalc.model.ValueTrace;
import com.liangmu.arcvaluecalc.service.ValueFormatter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

public final class TraceScreen extends Screen {
    private static final int ROW_HEIGHT = 22;
    private static final int ICON_SIZE = 16;
    private static final int TREE_LEFT = 18;
    private static final int INDENT = 20;
    private static final int TEXT_GAP = 6;
    private final ValueTrace trace;
    private final List<Row> rows = new ArrayList<>();
    private int scroll;
    private ItemStack hoveredStack = ItemStack.EMPTY;

    public TraceScreen(ValueTrace trace) {
        super(Component.literal("参考价格来源"));
        this.trace = trace;
        flatten(trace, 0);
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("关闭"), button -> onClose())
                .bounds(width / 2 - 40, height - 28, 80, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, 12, 0xFFE3C46B);
        int top = 34;
        int bottom = height - 38;
        int y = top - scroll;
        hoveredStack = ItemStack.EMPTY;
        for (Row row : rows) {
            if (y >= top - ROW_HEIGHT && y <= bottom) {
                renderRow(graphics, row, y, mouseX, mouseY);
            }
            y += ROW_HEIGHT;
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        if (!hoveredStack.isEmpty()) {
            graphics.renderTooltip(font, hoveredStack, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int maxScroll = Math.max(0, rows.size() * ROW_HEIGHT - (height - 76));
        scroll = Math.max(0, Math.min(maxScroll, scroll - (int) (delta * ROW_HEIGHT * 2)));
        return true;
    }

    private void flatten(ValueTrace node, int depth) {
        rows.add(new Row(node, depth, stackFor(node.key())));
        if (node.cycle() || node.truncated()) {
            return;
        }
        for (ValueTrace child : node.children()) {
            flatten(child, depth + 1);
        }
    }

    private void renderRow(GuiGraphics graphics, Row row, int y, int mouseX, int mouseY) {
        int iconX = TREE_LEFT + row.depth() * INDENT;
        int iconY = y + 2;
        drawConnector(graphics, row.depth(), iconX, y);
        graphics.renderItem(row.stack(), iconX, iconY);
        graphics.renderItemDecorations(font, row.stack(), iconX, iconY);
        int textX = iconX + ICON_SIZE + TEXT_GAP;
        int maxTextWidth = Math.max(24, width - textX - 12);
        graphics.drawString(font, fit(rowText(row.trace()), maxTextWidth), textX, y + 2, color(row.trace()), false);
        String detail = detailText(row.trace());
        if (!detail.isBlank()) {
            graphics.drawString(font, fit(detail, maxTextWidth), textX, y + 12, detailColor(row.trace()), false);
        }
        if (mouseX >= iconX && mouseX < iconX + ICON_SIZE && mouseY >= iconY && mouseY < iconY + ICON_SIZE) {
            hoveredStack = row.stack();
        }
    }

    private void drawConnector(GuiGraphics graphics, int depth, int iconX, int y) {
        if (depth <= 0) {
            return;
        }
        int lineColor = 0x665F6470;
        int centerY = y + 10;
        int parentX = iconX - INDENT + ICON_SIZE / 2;
        int branchX = iconX - 6;
        graphics.fill(parentX, y, parentX + 1, y + ROW_HEIGHT, lineColor);
        graphics.fill(parentX, centerY, branchX, centerY + 1, lineColor);
    }

    private ItemStack stackFor(ValueKey key) {
        Item item = ForgeRegistries.ITEMS.getValue(key.item());
        ItemStack stack = new ItemStack(item == null ? Items.BARRIER : item);
        if (key.nbt() == null) {
            return stack;
        }
        try {
            stack.setTag(TagParser.parseTag(key.nbt()));
        } catch (Exception ignored) {
            return stack;
        }
        return stack;
    }

    private String rowText(ValueTrace trace) {
        if (trace.missing()) {
            return trace.label() + " x" + trace.count() + " = 无参考价格";
        }
        String value = trace.value() == null ? "?" : ValueFormatter.display(trace.value()) + ArcValueConfig.VALUE_UNIT.get();
        return trace.label() + " x" + trace.count() + " = " + value;
    }

    private String detailText(ValueTrace trace) {
        if (trace.cycle()) {
            return "循环引用，已停止展开";
        }
        if (trace.truncated()) {
            return "达到最大深度，已截断";
        }
        if (trace.missing()) {
            return "";
        }
        String rule = trace.ruleId() == null || trace.ruleId().isBlank() ? "" : " · " + trace.ruleId();
        String nbt = trace.key().hasNbt() ? " · NBT" : "";
        return sourceLabel(trace.source()) + nbt + rule;
    }

    private String sourceLabel(ValueSource source) {
        return switch (source) {
            case MANUAL_VALUE -> "固定物品价";
            case MANUAL_RULE -> "手写规则";
            case GENERATED_RULE -> "配方推导";
            case SERVER -> "服务端价格";
            case NONE -> "无来源";
        };
    }

    private int color(ValueTrace trace) {
        if (trace.missing()) {
            return ChatFormatting.RED.getColor();
        }
        if (trace.truncated() || trace.cycle()) {
            return ChatFormatting.YELLOW.getColor();
        }
        if (trace == this.trace) {
            return 0xFF8FD7FF;
        }
        return 0xFFE6E0D0;
    }

    private int detailColor(ValueTrace trace) {
        return trace.cycle() || trace.truncated() ? ChatFormatting.YELLOW.getColor() : 0xFF9AA4B2;
    }

    private String fit(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        return font.plainSubstrByWidth(text, Math.max(0, maxWidth - font.width("..."))) + "...";
    }

    private record Row(ValueTrace trace, int depth, ItemStack stack) {
    }
}
