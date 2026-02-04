package com.hsbc.service;

import com.hsbc.enums.AssetType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AllocationEngine {

    public Map<AssetType, Double> calculateAllocation(
            int timeHorizon,
            String riskLevel
    ) {
        double stockPercent;
        double goldPercent;

        // Base allocation by time horizon
        if (timeHorizon <= 3) {
            stockPercent = 60;
            goldPercent = 40;
        } else if (timeHorizon <= 7) {
            stockPercent = 70;
            goldPercent = 30;
        } else {
            stockPercent = 80;
            goldPercent = 20;
        }

        // Risk adjustment
        switch (riskLevel.toUpperCase()) {
            case "LOW" -> {
                stockPercent -= 10;
                goldPercent += 10;
            }
            case "HIGH" -> {
                stockPercent += 10;
                goldPercent -= 10;
            }
        }

        Map<AssetType, Double> allocation = new HashMap<>();
        allocation.put(AssetType.STOCK, stockPercent);
        allocation.put(AssetType.GOLD, goldPercent);

        return allocation;
    }
}
