package com.liangmu.arcvaluecalc.network;

import com.liangmu.arcvaluecalc.service.ValueService;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public final class ValueRequestMessage {
    private final ItemStack stack;

    public ValueRequestMessage(ItemStack stack) {
        this.stack = stack.copy();
    }

    public static void encode(ValueRequestMessage message, FriendlyByteBuf buffer) {
        buffer.writeItem(message.stack);
    }

    public static ValueRequestMessage decode(FriendlyByteBuf buffer) {
        return new ValueRequestMessage(buffer.readItem());
    }

    public static void handle(ValueRequestMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            ArcValueNetwork.sendValue(player, ValueResponseMessage.fromLookup(ValueService.get().lookup(message.stack)));
        }
        context.setPacketHandled(true);
    }
}
