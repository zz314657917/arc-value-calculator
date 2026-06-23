package com.liangmu.arcvaluecalc.tooltip;

import com.liangmu.arcvaluecalc.api.ArcValueApi;
import com.liangmu.arcvaluecalc.client.ClientValueCache;
import com.liangmu.arcvaluecalc.config.ArcValueConfig;
import com.liangmu.arcvaluecalc.network.ArcValueNetwork;
import com.liangmu.arcvaluecalc.service.ValueFormatter;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import com.liangmu.arcvaluecalc.ArcValueCalc;

@EventBusSubscriber(modid = ArcValueCalc.MOD_ID, value = Dist.CLIENT)
public final class ValueTooltipHandler {
    private static final Set<String> REQUESTED = ConcurrentHashMap.newKeySet();

    private ValueTooltipHandler() {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (!ArcValueConfig.SHOW_TOOLTIP.get()) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }
        Optional<BigDecimal> value = Optional.empty();
        if (ArcValueConfig.PREFER_SERVER_VALUES.get() && ClientValueCache.serverAvailable()) {
            value = ClientValueCache.getServerValue(stack);
        }
        if (value.isEmpty()) {
            value = ArcValueApi.getValue(stack);
        }
        if (ArcValueConfig.PREFER_SERVER_VALUES.get()) {
            requestServerValueOnce(stack);
        }
        if (value.isPresent()) {
            event.getToolTip().add(Component.translatable(
                    "arcvaluecalc.tooltip.value",
                    ValueFormatter.display(value.get()),
                    ArcValueConfig.VALUE_UNIT.get()
            ));
        } else if (ArcValueConfig.SHOW_UNKNOWN.get()) {
            event.getToolTip().add(Component.translatable("arcvaluecalc.tooltip.no_value").withStyle(ChatFormatting.RED));
        }
    }

    private static void requestServerValueOnce(ItemStack stack) {
        String key = stack.getItem().toString() + "|" + (stack.getTag() == null ? "" : stack.getTag());
        if (REQUESTED.add(key)) {
            ArcValueNetwork.requestValue(stack);
        }
    }
}
