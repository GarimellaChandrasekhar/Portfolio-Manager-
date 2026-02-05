package com.hsbc.service;

import com.hsbc.dto.AllocationResponse;
import com.hsbc.dto.GoalInvestmentResponse;
import com.hsbc.dto.GoalRequest;
import com.hsbc.entity.GoalInvestmentAllocation;
import com.hsbc.entity.InvestmentGoal;
import com.hsbc.enums.AssetType;
import com.hsbc.repo.GoalInvestmentrepo;
import com.hsbc.repo.InvestmentGoalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestmentGoalServiceTest {

    @Mock
    private InvestmentGoalRepository goalRepo;

    @Mock
    private GoalInvestmentrepo allocationRepo;

    @Mock
    private AllocationEngine allocationEngine;

    @InjectMocks
    private InvestmentGoalService investmentGoalService;

    // ==================== Basic Goal Creation Tests ====================

    @Test
    void shouldCreateGoalAndAllocations() {
        // Arrange
        GoalRequest request = GoalRequest.builder()
                .goalName("Retirement")
                .targetAmount(10_000_000.0)
                .timeHorizon(10)
                .riskLevel("HIGH")
                .monthlyInvestment(50000.0)
                .build();

        InvestmentGoal savedGoal = InvestmentGoal.builder()
                .id(1L)
                .goalName("Retirement")
                .targetAmount(10_000_000.0)
                .timeHorizon(10)
                .riskLevel("HIGH")
                .monthlyInvestment(50000.0)
                .build();

        Map<AssetType, Double> allocationMap = new HashMap<>();
        allocationMap.put(AssetType.STOCK, 80.0);
        allocationMap.put(AssetType.GOLD, 20.0);

        when(goalRepo.save(any(InvestmentGoal.class)))
                .thenReturn(savedGoal);
        when(allocationEngine.calculateAllocation(10, "HIGH"))
                .thenReturn(allocationMap);

        // Act
        GoalInvestmentResponse response =
                investmentGoalService.createGoal(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getGoalId());
        assertEquals("Retirement", response.getGoalName());
        assertEquals(50000.0, response.getMonthlyInvestment());
        assertEquals(2, response.getAllocations().size());

        // Verify allocations
        List<AllocationResponse> allocations = response.getAllocations();
        boolean hasStock = allocations.stream()
                .anyMatch(a -> a.getAssetType().equals("STOCK"));
        boolean hasGold = allocations.stream()
                .anyMatch(a -> a.getAssetType().equals("GOLD"));

        assertTrue(hasStock);
        assertTrue(hasGold);

        // Verify repository calls
        verify(goalRepo).save(any(InvestmentGoal.class));
        verify(allocationEngine).calculateAllocation(10, "HIGH");
        verify(allocationRepo, times(2)).save(any(GoalInvestmentAllocation.class));
    }

    @Test
    void shouldCalculateCorrectSipAmounts() {
        // Arrange
        GoalRequest request = GoalRequest.builder()
                .goalName("Education")
                .targetAmount(1_000_000.0)
                .timeHorizon(5)
                .riskLevel("MEDIUM")
                .monthlyInvestment(10000.0)
                .build();

        InvestmentGoal savedGoal = InvestmentGoal.builder()
                .id(2L)
                .goalName("Education")
                .monthlyInvestment(10000.0)
                .build();

        Map<AssetType, Double> allocationMap = new HashMap<>();
        allocationMap.put(AssetType.STOCK, 70.0);
        allocationMap.put(AssetType.GOLD, 30.0);

        when(goalRepo.save(any(InvestmentGoal.class)))
                .thenReturn(savedGoal);
        when(allocationEngine.calculateAllocation(5, "MEDIUM"))
                .thenReturn(allocationMap);

        // Act
        GoalInvestmentResponse response =
                investmentGoalService.createGoal(request);

        // Assert
        List<AllocationResponse> allocations = response.getAllocations();

        AllocationResponse stockAllocation = allocations.stream()
                .filter(a -> a.getAssetType().equals("STOCK"))
                .findFirst()
                .orElseThrow();

        AllocationResponse goldAllocation = allocations.stream()
                .filter(a -> a.getAssetType().equals("GOLD"))
                .findFirst()
                .orElseThrow();

        // 70% of 10000 = 7000
        assertEquals(70.0, stockAllocation.getPercentage());
        assertEquals(7000.0, stockAllocation.getSipAmount(), 0.01);

        // 30% of 10000 = 3000
        assertEquals(30.0, goldAllocation.getPercentage());
        assertEquals(3000.0, goldAllocation.getSipAmount(), 0.01);
    }

    // ==================== Goal Request Field Tests ====================

    @Test
    void shouldPreserveAllGoalRequestFields() {
        // Arrange
        GoalRequest request = GoalRequest.builder()
                .goalName("Home Purchase")
                .targetAmount(5_000_000.0)
                .timeHorizon(7)
                .riskLevel("LOW")
                .monthlyInvestment(25000.0)
                .build();

        ArgumentCaptor<InvestmentGoal> goalCaptor =
                ArgumentCaptor.forClass(InvestmentGoal.class);

        InvestmentGoal savedGoal = InvestmentGoal.builder()
                .id(3L)
                .goalName("Home Purchase")
                .targetAmount(5_000_000.0)
                .timeHorizon(7)
                .riskLevel("LOW")
                .monthlyInvestment(25000.0)
                .build();

        when(goalRepo.save(goalCaptor.capture()))
                .thenReturn(savedGoal);
        when(allocationEngine.calculateAllocation(7, "LOW"))
                .thenReturn(Map.of(AssetType.STOCK, 60.0, AssetType.GOLD, 40.0));

        // Act
        investmentGoalService.createGoal(request);

        // Assert
        InvestmentGoal capturedGoal = goalCaptor.getValue();
        assertEquals("Home Purchase", capturedGoal.getGoalName());
        assertEquals(5_000_000.0, capturedGoal.getTargetAmount());
        assertEquals(7, capturedGoal.getTimeHorizon());
        assertEquals("LOW", capturedGoal.getRiskLevel());
        assertEquals(25000.0, capturedGoal.getMonthlyInvestment());
    }

    // ==================== Allocation Persistence Tests ====================

    @Test
    void shouldSaveAllocationsWithCorrectGoalReference() {
        // Arrange
        GoalRequest request = GoalRequest.builder()
                .goalName("Wealth Building")
                .targetAmount(3_000_000.0)
                .timeHorizon(15)
                .riskLevel("HIGH")
                .monthlyInvestment(15000.0)
                .build();

        InvestmentGoal savedGoal = InvestmentGoal.builder()
                .id(10L)
                .goalName("Wealth Building")
                .monthlyInvestment(15000.0)
                .build();

        ArgumentCaptor<GoalInvestmentAllocation> allocationCaptor =
                ArgumentCaptor.forClass(GoalInvestmentAllocation.class);

        when(goalRepo.save(any(InvestmentGoal.class)))
                .thenReturn(savedGoal);
        when(allocationEngine.calculateAllocation(15, "HIGH"))
                .thenReturn(Map.of(AssetType.STOCK, 90.0, AssetType.GOLD, 10.0));

        // Act
        investmentGoalService.createGoal(request);

        // Assert
        verify(allocationRepo, times(2)).save(allocationCaptor.capture());

        List<GoalInvestmentAllocation> capturedAllocations =
                allocationCaptor.getAllValues();

        // Verify all allocations reference the saved goal
        for (GoalInvestmentAllocation allocation : capturedAllocations) {
            assertEquals(10L, allocation.getGoal().getId());
            assertEquals("Wealth Building", allocation.getGoal().getGoalName());
        }
    }

    @Test
    void shouldSaveAllocationsWithCorrectPercentagesAndAmounts() {
        // Arrange
        GoalRequest request = GoalRequest.builder()
                .goalName("Test Goal")
                .targetAmount(1_000_000.0)
                .timeHorizon(5)
                .riskLevel("MEDIUM")
                .monthlyInvestment(20000.0)
                .build();

        InvestmentGoal savedGoal = InvestmentGoal.builder()
                .id(5L)
                .goalName("Test Goal")
                .monthlyInvestment(20000.0)
                .build();

        ArgumentCaptor<GoalInvestmentAllocation> allocationCaptor =
                ArgumentCaptor.forClass(GoalInvestmentAllocation.class);

        when(goalRepo.save(any(InvestmentGoal.class)))
                .thenReturn(savedGoal);
        when(allocationEngine.calculateAllocation(5, "MEDIUM"))
                .thenReturn(Map.of(AssetType.STOCK, 70.0, AssetType.GOLD, 30.0));

        // Act
        investmentGoalService.createGoal(request);

        // Assert
        verify(allocationRepo, times(2)).save(allocationCaptor.capture());

        List<GoalInvestmentAllocation> allocations =
                allocationCaptor.getAllValues();

        GoalInvestmentAllocation stockAllocation = allocations.stream()
                .filter(a -> a.getAssetType() == AssetType.STOCK)
                .findFirst()
                .orElseThrow();

        assertEquals(70.0, stockAllocation.getPercentage());
        assertEquals(14000.0, stockAllocation.getMonthlySipAmount(), 0.01);

        GoalInvestmentAllocation goldAllocation = allocations.stream()
                .filter(a -> a.getAssetType() == AssetType.GOLD)
                .findFirst()
                .orElseThrow();

        assertEquals(30.0, goldAllocation.getPercentage());
        assertEquals(6000.0, goldAllocation.getMonthlySipAmount(), 0.01);
    }

    // ==================== Different Risk Levels Tests ====================

    @Test
    void shouldHandleLowRiskGoal() {
        // Arrange
        GoalRequest request = GoalRequest.builder()
                .goalName("Conservative Goal")
                .targetAmount(500_000.0)
                .timeHorizon(3)
                .riskLevel("LOW")
                .monthlyInvestment(10000.0)
                .build();

        InvestmentGoal savedGoal = InvestmentGoal.builder()
                .id(6L)
                .goalName("Conservative Goal")
                .monthlyInvestment(10000.0)
                .build();

        when(goalRepo.save(any(InvestmentGoal.class)))
                .thenReturn(savedGoal);
        when(allocationEngine.calculateAllocation(3, "LOW"))
                .thenReturn(Map.of(AssetType.STOCK, 50.0, AssetType.GOLD, 50.0));

        // Act
        GoalInvestmentResponse response =
                investmentGoalService.createGoal(request);

        // Assert
        assertNotNull(response);
        verify(allocationEngine).calculateAllocation(3, "LOW");
    }

    @Test
    void shouldHandleHighRiskGoal() {
        // Arrange
        GoalRequest request = GoalRequest.builder()
                .goalName("Aggressive Goal")
                .targetAmount(2_000_000.0)
                .timeHorizon(20)
                .riskLevel("HIGH")
                .monthlyInvestment(8000.0)
                .build();

        InvestmentGoal savedGoal = InvestmentGoal.builder()
                .id(7L)
                .goalName("Aggressive Goal")
                .monthlyInvestment(8000.0)
                .build();

        when(goalRepo.save(any(InvestmentGoal.class)))
                .thenReturn(savedGoal);
        when(allocationEngine.calculateAllocation(20, "HIGH"))
                .thenReturn(Map.of(AssetType.STOCK, 90.0, AssetType.GOLD, 10.0));

        // Act
        GoalInvestmentResponse response =
                investmentGoalService.createGoal(request);

        // Assert
        assertNotNull(response);
        verify(allocationEngine).calculateAllocation(20, "HIGH");
    }

    // ==================== Edge Cases Tests ====================

    @Test
    void shouldHandleMinimalMonthlyInvestment() {
        // Arrange
        GoalRequest request = GoalRequest.builder()
                .goalName("Small Goal")
                .targetAmount(50_000.0)
                .timeHorizon(2)
                .riskLevel("MEDIUM")
                .monthlyInvestment(1000.0)
                .build();

        InvestmentGoal savedGoal = InvestmentGoal.builder()
                .id(8L)
                .goalName("Small Goal")
                .monthlyInvestment(1000.0)
                .build();

        when(goalRepo.save(any(InvestmentGoal.class)))
                .thenReturn(savedGoal);
        when(allocationEngine.calculateAllocation(2, "MEDIUM"))
                .thenReturn(Map.of(AssetType.STOCK, 60.0, AssetType.GOLD, 40.0));

        // Act
        GoalInvestmentResponse response =
                investmentGoalService.createGoal(request);

        // Assert
        assertNotNull(response);
        assertEquals(1000.0, response.getMonthlyInvestment());

        // Verify SIP amounts are correctly calculated for small amounts
        List<AllocationResponse> allocations = response.getAllocations();
        double totalSip = allocations.stream()
                .mapToDouble(AllocationResponse::getSipAmount)
                .sum();

        assertEquals(1000.0, totalSip, 0.01);
    }

    @Test
    void shouldHandleLargeMonthlyInvestment() {
        // Arrange
        GoalRequest request = GoalRequest.builder()
                .goalName("Large Goal")
                .targetAmount(100_000_000.0)
                .timeHorizon(25)
                .riskLevel("HIGH")
                .monthlyInvestment(500_000.0)
                .build();

        InvestmentGoal savedGoal = InvestmentGoal.builder()
                .id(9L)
                .goalName("Large Goal")
                .monthlyInvestment(500_000.0)
                .build();

        when(goalRepo.save(any(InvestmentGoal.class)))
                .thenReturn(savedGoal);
        when(allocationEngine.calculateAllocation(25, "HIGH"))
                .thenReturn(Map.of(AssetType.STOCK, 90.0, AssetType.GOLD, 10.0));

        // Act
        GoalInvestmentResponse response =
                investmentGoalService.createGoal(request);

        // Assert
        assertNotNull(response);
        assertEquals(500_000.0, response.getMonthlyInvestment());
    }

    @Test
    void shouldHandleShortTimeHorizon() {
        // Arrange
        GoalRequest request = GoalRequest.builder()
                .goalName("Quick Goal")
                .targetAmount(100_000.0)
                .timeHorizon(1)
                .riskLevel("LOW")
                .monthlyInvestment(9000.0)
                .build();

        InvestmentGoal savedGoal = InvestmentGoal.builder()
                .id(11L)
                .goalName("Quick Goal")
                .monthlyInvestment(9000.0)
                .build();

        when(goalRepo.save(any(InvestmentGoal.class)))
                .thenReturn(savedGoal);
        when(allocationEngine.calculateAllocation(1, "LOW"))
                .thenReturn(Map.of(AssetType.STOCK, 50.0, AssetType.GOLD, 50.0));

        // Act
        GoalInvestmentResponse response =
                investmentGoalService.createGoal(request);

        // Assert
        assertNotNull(response);
        verify(allocationEngine).calculateAllocation(1, "LOW");
    }

    @Test
    void shouldHandleLongTimeHorizon() {
        // Arrange
        GoalRequest request = GoalRequest.builder()
                .goalName("Long-term Goal")
                .targetAmount(20_000_000.0)
                .timeHorizon(30)
                .riskLevel("HIGH")
                .monthlyInvestment(30000.0)
                .build();

        InvestmentGoal savedGoal = InvestmentGoal.builder()
                .id(12L)
                .goalName("Long-term Goal")
                .monthlyInvestment(30000.0)
                .build();

        when(goalRepo.save(any(InvestmentGoal.class)))
                .thenReturn(savedGoal);
        when(allocationEngine.calculateAllocation(30, "HIGH"))
                .thenReturn(Map.of(AssetType.STOCK, 90.0, AssetType.GOLD, 10.0));

        // Act
        GoalInvestmentResponse response =
                investmentGoalService.createGoal(request);

        // Assert
        assertNotNull(response);
        verify(allocationEngine).calculateAllocation(30, "HIGH");
    }

    // ==================== Response Validation Tests ====================

    @Test
    void shouldReturnCompleteResponse() {
        // Arrange
        GoalRequest request = GoalRequest.builder()
                .goalName("Complete Test")
                .targetAmount(1_500_000.0)
                .timeHorizon(8)
                .riskLevel("MEDIUM")
                .monthlyInvestment(12000.0)
                .build();

        InvestmentGoal savedGoal = InvestmentGoal.builder()
                .id(13L)
                .goalName("Complete Test")
                .monthlyInvestment(12000.0)
                .build();

        when(goalRepo.save(any(InvestmentGoal.class)))
                .thenReturn(savedGoal);
        when(allocationEngine.calculateAllocation(8, "MEDIUM"))
                .thenReturn(Map.of(AssetType.STOCK, 80.0, AssetType.GOLD, 20.0));

        // Act
        GoalInvestmentResponse response =
                investmentGoalService.createGoal(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getGoalId());
        assertNotNull(response.getGoalName());
        assertNotNull(response.getMonthlyInvestment());
        assertNotNull(response.getAllocations());
        assertFalse(response.getAllocations().isEmpty());
    }

    @Test
    void shouldEnsureSipAmountsSumToMonthlyInvestment() {
        // Arrange
        GoalRequest request = GoalRequest.builder()
                .goalName("Sum Validation")
                .targetAmount(1_000_000.0)
                .timeHorizon(5)
                .riskLevel("MEDIUM")
                .monthlyInvestment(15000.0)
                .build();

        InvestmentGoal savedGoal = InvestmentGoal.builder()
                .id(14L)
                .goalName("Sum Validation")
                .monthlyInvestment(15000.0)
                .build();

        when(goalRepo.save(any(InvestmentGoal.class)))
                .thenReturn(savedGoal);
        when(allocationEngine.calculateAllocation(5, "MEDIUM"))
                .thenReturn(Map.of(AssetType.STOCK, 70.0, AssetType.GOLD, 30.0));

        // Act
        GoalInvestmentResponse response =
                investmentGoalService.createGoal(request);

        // Assert
        double totalSip = response.getAllocations().stream()
                .mapToDouble(AllocationResponse::getSipAmount)
                .sum();

        assertEquals(15000.0, totalSip, 0.01);
    }

    // ==================== Transaction Tests ====================

    @Test
    void shouldCallAllocationEngineWithCorrectParameters() {
        // Arrange
        GoalRequest request = GoalRequest.builder()
                .goalName("Parameter Test")
                .targetAmount(1_000_000.0)
                .timeHorizon(12)
                .riskLevel("LOW")
                .monthlyInvestment(8000.0)
                .build();

        InvestmentGoal savedGoal = InvestmentGoal.builder()
                .id(15L)
                .goalName("Parameter Test")
                .monthlyInvestment(8000.0)
                .build();

        when(goalRepo.save(any(InvestmentGoal.class)))
                .thenReturn(savedGoal);
        when(allocationEngine.calculateAllocation(12, "LOW"))
                .thenReturn(Map.of(AssetType.STOCK, 70.0, AssetType.GOLD, 30.0));

        // Act
        investmentGoalService.createGoal(request);

        // Assert
        verify(allocationEngine).calculateAllocation(12, "LOW");
        verifyNoMoreInteractions(allocationEngine);
    }
}