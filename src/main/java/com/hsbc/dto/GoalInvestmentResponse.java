package com.hsbc.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class GoalInvestmentResponse {

    private Long goalId;
    private String goalName;
    private Double monthlyInvestment;
    private List<AllocationResponse> allocations;
}
