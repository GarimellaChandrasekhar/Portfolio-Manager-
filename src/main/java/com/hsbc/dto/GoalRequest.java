package com.hsbc.dto;

import lombok.Data;

@Data
public class GoalRequest {

    private String goalName;
    private Double targetAmount;
    private Integer timeHorizon;
    private String riskLevel;
    private Double monthlyInvestment;
}

