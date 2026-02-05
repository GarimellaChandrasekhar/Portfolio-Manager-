package com.hsbc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalRequest {

    private String goalName;
    private Double targetAmount;
    private Integer timeHorizon;
    private String riskLevel;
    private Double monthlyInvestment;
}

