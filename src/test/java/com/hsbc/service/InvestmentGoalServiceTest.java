package com.hsbc.service;

import com.hsbc.dto.GoalInvestmentResponse;
import com.hsbc.dto.GoalRequest;
import com.hsbc.entity.InvestmentGoal;
import com.hsbc.enums.AssetType;
import com.hsbc.repo.GoalInvestmentrepo;
import com.hsbc.repo.InvestmentGoalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    void shouldCreateGoalAndAllocations() {
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
                .monthlyInvestment(50000.0)
                .build();

        when(goalRepo.save(any()))
                .thenReturn(savedGoal);

        when(allocationEngine.calculateAllocation(10, "HIGH"))
                .thenReturn(Map.of(
                        AssetType.STOCK, 80.0,
                        AssetType.GOLD, 20.0
                ));

        GoalInvestmentResponse response =
                investmentGoalService.createGoal(request);

        assertEquals("Retirement", response.getGoalName());
        assertEquals(2, response.getAllocations().size());

        verify(goalRepo).save(any());
        verify(allocationRepo, times(2)).save(any());
    }
}

