package com.liangmu.arcvaluecalc;

import com.liangmu.arcvaluecalc.model.ValueKey;
import com.liangmu.arcvaluecalc.network.ValueRequestMessage;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

final class ValueRequestMessageTest {
    @Test
    void rejectsOversizedRequestNbtDuringDecode() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeResourceLocation(new ResourceLocation("minecraft", "diamond_sword"));
        buffer.writeBoolean(true);
        buffer.writeUtf("a".repeat(ValueKey.MAX_NBT_CHARS + 1), ValueKey.MAX_NBT_CHARS + 1);

        assertThrows(Exception.class, () -> ValueRequestMessage.decode(buffer));
    }

    @Test
    void valueKeyRejectsOversizedNbtBeforePlainStringExpansionPaths() {
        String oversized = "a".repeat(ValueKey.MAX_NBT_CHARS + 1);

        assertThrows(IllegalArgumentException.class, () -> new ValueKey(new ResourceLocation("minecraft", "diamond_sword"), oversized));
    }
}
