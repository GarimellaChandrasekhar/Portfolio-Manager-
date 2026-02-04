package com.hsbc.entity;

import com.hsbc.enums.AssetType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "goal_investment_allocation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalInvestmentAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "goal_id", nullable = false)
    private InvestmentGoal goal;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    private AssetType assetType;

    @Column(name = "percentage", nullable = false)
    private Double percentage;

    @Column(name = "monthly_sip_amount", nullable = false)
    private Double monthlySipAmount;
}
