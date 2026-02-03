package com.hsbc.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "investment_goal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "goal_name", nullable = false)
    private String goalName;

    @Column(name = "target_amount", nullable = false)
    private Double targetAmount;

    @Column(name = "time_horizon", nullable = false)
    private Integer timeHorizon; // in years

    @Column(name = "risk_level", nullable = false)
    private String riskLevel; // LOW, MEDIUM, HIGH

    @Column(name = "monthly_investment")
    private Double monthlyInvestment;
}


