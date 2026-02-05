package com.hsbc.service;

import com.hsbc.enums.AssetType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AllocationEngineTest {

    private final AllocationEngine allocationEngine =
            new AllocationEngine();

    @Test
    void shouldAllocateHighRiskLongTerm() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(10, "HIGH");

        assertEquals(90.0, allocation.get(AssetType.STOCK));
        assertEquals(10.0, allocation.get(AssetType.GOLD));
    }

    @Test
    void shouldAllocateLowRiskShortTerm() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(2, "LOW");

        assertEquals(50.0, allocation.get(AssetType.STOCK));
        assertEquals(50.0, allocation.get(AssetType.GOLD));
    }
}
