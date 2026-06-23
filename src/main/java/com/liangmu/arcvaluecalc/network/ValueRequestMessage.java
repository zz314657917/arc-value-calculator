package com.liangmu.arcvaluecalc.network;

import com.liangmu.arcvaluecalc.model.ValueKey;
import com.liangmu.arcvaluecalc.service.ValueService;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public final class ValueRequestMessage {
    private static final int MAX_REQUEST_NBT_CHARS = 8192;
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
        if (player != null && isAcceptable(message.stack) && ValueRequestLimiter.allow(player)) {
            try {
                ValueKey requestedKey = ValueKey.exact(message.stack);
                ArcValueNetwork.sendValue(player, ValueResponseMessage.fromLookup(requestedKey, ValueService.get().lookup(message.stack)));
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed or unregistered client stacks.
            }
        }
        context.setPacketHandled(true);
    }

    private static boolean isAcceptable(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getTag() == null || stack.getTag().toString().length() <= MAX_REQUEST_NBT_CHARS;
    }
}
