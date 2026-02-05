package com.hsbc.service;

import com.hsbc.enums.AssetType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AllocationEngineTest {

    private AllocationEngine allocationEngine;

    @BeforeEach
    void setUp() {
        allocationEngine = new AllocationEngine();
    }

    // ==================== Time Horizon Tests ====================

    @Test
    void shouldAllocateForShortTermOneYear() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(1, "MEDIUM");

        assertEquals(60.0, allocation.get(AssetType.STOCK));
        assertEquals(40.0, allocation.get(AssetType.GOLD));
    }

    @Test
    void shouldAllocateForShortTermTwoYears() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(2, "MEDIUM");

        assertEquals(60.0, allocation.get(AssetType.STOCK));
        assertEquals(40.0, allocation.get(AssetType.GOLD));
    }

    @Test
    void shouldAllocateBoundaryAtThreeYears() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(3, "MEDIUM");

        assertEquals(60.0, allocation.get(AssetType.STOCK));
        assertEquals(40.0, allocation.get(AssetType.GOLD));
    }

    @Test
    void shouldAllocateForMediumTermFourYears() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(4, "MEDIUM");

        assertEquals(70.0, allocation.get(AssetType.STOCK));
        assertEquals(30.0, allocation.get(AssetType.GOLD));
    }

    @Test
    void shouldAllocateForMediumTermFiveYears() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(5, "MEDIUM");

        assertEquals(70.0, allocation.get(AssetType.STOCK));
        assertEquals(30.0, allocation.get(AssetType.GOLD));
    }

    @Test
    void shouldAllocateBoundaryAtSevenYears() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(7, "MEDIUM");

        assertEquals(70.0, allocation.get(AssetType.STOCK));
        assertEquals(30.0, allocation.get(AssetType.GOLD));
    }

    @Test
    void shouldAllocateForLongTermEightYears() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(8, "MEDIUM");

        assertEquals(80.0, allocation.get(AssetType.STOCK));
        assertEquals(20.0, allocation.get(AssetType.GOLD));
    }

    @Test
    void shouldAllocateForLongTermTenYears() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(10, "MEDIUM");

        assertEquals(80.0, allocation.get(AssetType.STOCK));
        assertEquals(20.0, allocation.get(AssetType.GOLD));
    }

    @Test
    void shouldAllocateForVeryLongTermThirtyYears() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(30, "MEDIUM");

        assertEquals(80.0, allocation.get(AssetType.STOCK));
        assertEquals(20.0, allocation.get(AssetType.GOLD));
    }

    // ==================== Risk Level Tests ====================

    @Test
    void shouldAllocateLowRiskShortTerm() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(2, "LOW");

        assertEquals(50.0, allocation.get(AssetType.STOCK));
        assertEquals(50.0, allocation.get(AssetType.GOLD));
    }

    @Test
    void shouldAllocateLowRiskMediumTerm() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(5, "LOW");

        assertEquals(60.0, allocation.get(AssetType.STOCK));
        assertEquals(40.0, allocation.get(AssetType.GOLD));
    }

    @Test
    void shouldAllocateLowRiskLongTerm() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(10, "LOW");

        assertEquals(70.0, allocation.get(AssetType.STOCK));
        assertEquals(30.0, allocation.get(AssetType.GOLD));
    }

    @Test
    void shouldAllocateHighRiskShortTerm() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(2, "HIGH");

        assertEquals(70.0, allocation.get(AssetType.STOCK));
        assertEquals(30.0, allocation.get(AssetType.GOLD));
    }

    @Test
    void shouldAllocateHighRiskMediumTerm() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(5, "HIGH");

        assertEquals(80.0, allocation.get(AssetType.STOCK));
        assertEquals(20.0, allocation.get(AssetType.GOLD));
    }

    @Test
    void shouldAllocateHighRiskLongTerm() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(10, "HIGH");

        assertEquals(90.0, allocation.get(AssetType.STOCK));
        assertEquals(10.0, allocation.get(AssetType.GOLD));
    }

    @Test
    void shouldAllocateMediumRiskShortTerm() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(2, "MEDIUM");

        assertEquals(60.0, allocation.get(AssetType.STOCK));
        assertEquals(40.0, allocation.get(AssetType.GOLD));
    }

    @Test
    void shouldAllocateMediumRiskMediumTerm() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(5, "MEDIUM");

        assertEquals(70.0, allocation.get(AssetType.STOCK));
        assertEquals(30.0, allocation.get(AssetType.GOLD));
    }

    @Test
    void shouldAllocateMediumRiskLongTerm() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(10, "MEDIUM");

        assertEquals(80.0, allocation.get(AssetType.STOCK));
        assertEquals(20.0, allocation.get(AssetType.GOLD));
    }

    // ==================== Case Sensitivity Tests ====================

    @Test
    void shouldHandleLowercaseRiskLevel() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(5, "low");

        assertEquals(60.0, allocation.get(AssetType.STOCK));
        assertEquals(40.0, allocation.get(AssetType.GOLD));
    }

    @Test
    void shouldHandleUppercaseRiskLevel() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(5, "HIGH");

        assertEquals(80.0, allocation.get(AssetType.STOCK));
        assertEquals(20.0, allocation.get(AssetType.GOLD));
    }

    @Test
    void shouldHandleMixedCaseRiskLevel() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(5, "MeDiUm");

        assertEquals(70.0, allocation.get(AssetType.STOCK));
        assertEquals(30.0, allocation.get(AssetType.GOLD));
    }

    // ==================== Edge Cases ====================

    @Test
    void shouldHandleZeroTimeHorizon() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(0, "MEDIUM");

        // Time horizon 0 falls into <=3 bracket
        assertEquals(60.0, allocation.get(AssetType.STOCK));
        assertEquals(40.0, allocation.get(AssetType.GOLD));
    }

    @Test
    void shouldHandleVeryLargeTimeHorizon() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(100, "MEDIUM");

        // Very long term should use long-term allocation
        assertEquals(80.0, allocation.get(AssetType.STOCK));
        assertEquals(20.0, allocation.get(AssetType.GOLD));
    }

    @Test
    void shouldHandleUnknownRiskLevel() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(5, "UNKNOWN");

        // Default case - no risk adjustment
        assertEquals(70.0, allocation.get(AssetType.STOCK));
        assertEquals(30.0, allocation.get(AssetType.GOLD));
    }

    @Test
    void shouldHandleNullRiskLevel() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(5, null);

        // Default case - no risk adjustment
        assertEquals(70.0, allocation.get(AssetType.STOCK));
        assertEquals(30.0, allocation.get(AssetType.GOLD));
    }

    @Test
    void shouldHandleEmptyRiskLevel() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(5, "");

        // Default case - no risk adjustment
        assertEquals(70.0, allocation.get(AssetType.STOCK));
        assertEquals(30.0, allocation.get(AssetType.GOLD));
    }

    // ==================== Validation Tests ====================

    @Test
    void shouldReturnMapWithTwoAssetTypes() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(5, "MEDIUM");

        assertEquals(2, allocation.size());
        assertTrue(allocation.containsKey(AssetType.STOCK));
        assertTrue(allocation.containsKey(AssetType.GOLD));
    }

    @Test
    void shouldEnsureAllocationsAddUpTo100Percent() {
        Map<AssetType, Double> allocation =
                allocationEngine.calculateAllocation(5, "HIGH");

        double total = allocation.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        assertEquals(100.0, total, 0.01);
    }

    @Test
    void shouldNeverHaveNegativeAllocations() {
        // Test extreme cases
        Map<AssetType, Double> allocation1 =
                allocationEngine.calculateAllocation(1, "HIGH");

        assertTrue(allocation1.get(AssetType.STOCK) >= 0);
        assertTrue(allocation1.get(AssetType.GOLD) >= 0);

        Map<AssetType, Double> allocation2 =
                allocationEngine.calculateAllocation(30, "LOW");

        assertTrue(allocation2.get(AssetType.STOCK) >= 0);
        assertTrue(allocation2.get(AssetType.GOLD) >= 0);
    }

    // ==================== Comprehensive Combination Tests ====================

    @Test
    void shouldAllocateAllCombinationsCorrectly() {
        // Short term + LOW
        var result1 = allocationEngine.calculateAllocation(1, "LOW");
        assertEquals(50.0, result1.get(AssetType.STOCK));
        assertEquals(50.0, result1.get(AssetType.GOLD));

        // Short term + HIGH
        var result2 = allocationEngine.calculateAllocation(3, "HIGH");
        assertEquals(70.0, result2.get(AssetType.STOCK));
        assertEquals(30.0, result2.get(AssetType.GOLD));

        // Medium term + LOW
        var result3 = allocationEngine.calculateAllocation(5, "LOW");
        assertEquals(60.0, result3.get(AssetType.STOCK));
        assertEquals(40.0, result3.get(AssetType.GOLD));

        // Medium term + HIGH
        var result4 = allocationEngine.calculateAllocation(7, "HIGH");
        assertEquals(80.0, result4.get(AssetType.STOCK));
        assertEquals(20.0, result4.get(AssetType.GOLD));

        // Long term + LOW
        var result5 = allocationEngine.calculateAllocation(15, "LOW");
        assertEquals(70.0, result5.get(AssetType.STOCK));
        assertEquals(30.0, result5.get(AssetType.GOLD));

        // Long term + HIGH
        var result6 = allocationEngine.calculateAllocation(20, "HIGH");
        assertEquals(90.0, result6.get(AssetType.STOCK));
        assertEquals(10.0, result6.get(AssetType.GOLD));
    }
}