package com.mehveen.splitledger.split;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SplitStrategyFactoryTest {

    @Test
    void providesEveryBuiltInType() {
        SplitStrategyFactory factory = new SplitStrategyFactory();
        for (SplitType type : SplitType.values()) {
            assertEquals(type, factory.forType(type).type());
        }
    }
}
