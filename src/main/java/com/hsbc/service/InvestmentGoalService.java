package com.hsbc.service;

import com.hsbc.dto.AllocationResponse;
import com.hsbc.dto.GoalInvestmentResponse;
import com.hsbc.dto.GoalRequest;
import com.hsbc.entity.GoalInvestmentAllocation;
import com.hsbc.entity.InvestmentGoal;
import com.hsbc.enums.AssetType;
import com.hsbc.repo.GoalInvestmentrepo;
import com.hsbc.repo.InvestmentGoalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class InvestmentGoalService {

    private final InvestmentGoalRepository goalRepo;
    private final GoalInvestmentrepo allocationRepo;
    private final AllocationEngine allocationEngine;

    public InvestmentGoalService(
            InvestmentGoalRepository goalRepo,
            GoalInvestmentrepo allocationRepo,
            AllocationEngine allocationEngine
    ) {
        this.goalRepo = goalRepo;
        this.allocationRepo = allocationRepo;
        this.allocationEngine = allocationEngine;
    }

    @Transactional
    public GoalInvestmentResponse createGoal(GoalRequest request) {

        InvestmentGoal goal = InvestmentGoal.builder()
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .timeHorizon(request.getTimeHorizon())
                .riskLevel(request.getRiskLevel())
                .monthlyInvestment(request.getMonthlyInvestment())
                .build();

        InvestmentGoal savedGoal = goalRepo.save(goal);

        Map<AssetType, Double> allocationMap =
                allocationEngine.calculateAllocation(
                        request.getTimeHorizon(),
                        request.getRiskLevel()
                );

        List<AllocationResponse> allocationResponses = new ArrayList<>();

        allocationMap.forEach((assetType, percentage) -> {

            double sipAmount =
                    (percentage / 100) * request.getMonthlyInvestment();

            allocationRepo.save(
                    GoalInvestmentAllocation.builder()
                            .goal(savedGoal)
                            .assetType(assetType)
                            .percentage(percentage)
                            .monthlySipAmount(sipAmount)
                            .build()
            );

            allocationResponses.add(
                    AllocationResponse.builder()
                            .assetType(assetType.name())
                            .percentage(percentage)
                            .sipAmount(sipAmount)
                            .build()
            );
        });

        return GoalInvestmentResponse.builder()
                .goalId(savedGoal.getId())
                .goalName(savedGoal.getGoalName())
                .monthlyInvestment(savedGoal.getMonthlyInvestment())
                .allocations(allocationResponses)
                .build();
    }
}


