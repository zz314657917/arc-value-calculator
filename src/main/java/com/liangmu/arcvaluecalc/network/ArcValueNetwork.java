package com.liangmu.arcvaluecalc.network;

import com.liangmu.arcvaluecalc.ArcValueCalc;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ArcValueNetwork {
    private static final String PROTOCOL = "1";
    private static SimpleChannel channel;
    private static int id;

    private ArcValueNetwork() {
    }

    public static void register() {
        channel = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(ArcValueCalc.MOD_ID, "main"))
                .networkProtocolVersion(() -> PROTOCOL)
                .clientAcceptedVersions(PROTOCOL::equals)
                .serverAcceptedVersions(PROTOCOL::equals)
                .simpleChannel();
        channel.messageBuilder(ValueRequestMessage.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(ValueRequestMessage::encode)
                .decoder(ValueRequestMessage::decode)
                .consumerMainThread(ValueRequestMessage::handle)
                .add();
        channel.messageBuilder(ValueResponseMessage.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ValueResponseMessage::encode)
                .decoder(ValueResponseMessage::decode)
                .consumerMainThread(ValueResponseMessage::handle)
                .add();
        channel.messageBuilder(ReloadValuesMessage.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ReloadValuesMessage::encode)
                .decoder(ReloadValuesMessage::decode)
                .consumerMainThread(ReloadValuesMessage::handle)
                .add();
    }

    public static void requestValue(ItemStack stack) {
        if (channel != null) {
            channel.sendToServer(new ValueRequestMessage(stack));
        }
    }

    public static void sendValue(ServerPlayer player, ValueResponseMessage message) {
        if (channel != null) {
            channel.send(PacketDistributor.PLAYER.with(() -> player), message);
        }
    }

    public static void sendReload(long generation) {
        if (channel != null) {
            channel.send(PacketDistributor.ALL.noArg(), new ReloadValuesMessage(generation));
        }
    }

    private static int nextId() {
        return id++;
    }
}
