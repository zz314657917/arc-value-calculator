package com.liangmu.arcvaluecalc;

import com.liangmu.arcvaluecalc.model.MatchType;
import com.liangmu.arcvaluecalc.model.ValueKey;
import com.liangmu.arcvaluecalc.network.ValueResponseMessage;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

final class ValueResponseMessageTest {
    @Test
    void rejectsInvalidNetworkPrice() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeEnum(MatchType.EXACT);
        new ValueKey(new ResourceLocation("minecraft", "diamond"), (String) null).write(buffer);
        new ValueKey(new ResourceLocation("minecraft", "diamond"), (String) null).write(buffer);
        buffer.writeLong(1L);
        buffer.writeBoolean(true);
        buffer.writeUtf("1e100000000");

        assertThrows(IllegalArgumentException.class, () -> ValueResponseMessage.decode(buffer));
    }
}
