package com.liangmu.arcvaluecalc.network;

import com.liangmu.arcvaluecalc.client.ClientValueCache;
import com.liangmu.arcvaluecalc.model.ValueKey;
import java.math.BigDecimal;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public final class ValueResponseMessage {
    private final ItemStack stack;
    private final BigDecimal value;

    public ValueResponseMessage(ItemStack stack, BigDecimal value) {
        this.stack = stack.copy();
        this.value = value;
    }

    public static void encode(ValueResponseMessage message, FriendlyByteBuf buffer) {
        buffer.writeItem(message.stack);
        buffer.writeBoolean(message.value != null);
        if (message.value != null) {
            buffer.writeUtf(message.value.toPlainString());
        }
    }

    public static ValueResponseMessage decode(FriendlyByteBuf buffer) {
        ItemStack stack = buffer.readItem();
        BigDecimal value = buffer.readBoolean() ? new BigDecimal(buffer.readUtf()) : null;
        return new ValueResponseMessage(stack, value);
    }

    public static void handle(ValueResponseMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ClientValueCache.markServerAvailable();
            ClientValueCache.putServerValue(ValueKey.itemOnly(message.stack), message.value);
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
