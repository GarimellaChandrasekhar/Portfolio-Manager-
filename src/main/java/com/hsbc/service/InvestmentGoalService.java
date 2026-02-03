
package com.hsbc.service;

import com.hsbc.dto.GoalRequest;
import com.hsbc.entity.InvestmentGoal;
import com.hsbc.repo.InvestmentGoalRepository;
import org.springframework.stereotype.Service;

@Service
public class InvestmentGoalService {

    private final InvestmentGoalRepository repository;

    public InvestmentGoalService(InvestmentGoalRepository repository) {
        this.repository = repository;
    }

    public InvestmentGoal createGoal(GoalRequest request) {

        InvestmentGoal goal = InvestmentGoal.builder()
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .timeHorizon(request.getTimeHorizon())
                .riskLevel(request.getRiskLevel())
                .monthlyInvestment(request.getMonthlyInvestment())
                .build();

        return repository.save(goal);
    }
}

