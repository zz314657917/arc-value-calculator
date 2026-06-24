package com.liangmu.arcvaluecalc;

import com.liangmu.arcvaluecalc.network.ArcValueNetwork;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class NetworkCompatibilityTest {
    @Test
    void protocolVersionChangesWhenPacketShapeChanges() throws Exception {
        Field field = ArcValueNetwork.class.getDeclaredField("PROTOCOL");
        field.setAccessible(true);
        assertEquals("3", field.get(null));
    }
}
