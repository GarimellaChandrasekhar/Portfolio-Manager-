package com.hsbc.dto;

import com.hsbc.entity.Holding;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
class HoldingResponse {
    private Long id;
    private String symbol;
    private String name;
    private Holding.AssetType assetType;
    private BigDecimal quantity;
    private BigDecimal purchasePrice;
    private LocalDate purchaseDate;
    private BigDecimal currentPrice;
    private BigDecimal investmentValue;
    private BigDecimal currentValue;
    private BigDecimal profitLoss;
    private BigDecimal profitLossPercentage;
    private LocalDateTime lastUpdated;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class PriceUpdateResponse {
    private String symbol;
    private BigDecimal price;
    private LocalDateTime timestamp;
    private String status;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class PortfolioSummary {
    private Long portfolioId;
    private String portfolioName;
    private BigDecimal totalInvestment;
    private BigDecimal currentValue;
    private BigDecimal totalProfitLoss;
    private BigDecimal totalProfitLossPercentage;
    private AssetAllocation assetAllocation;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class AssetAllocation {
    private BigDecimal stockPercentage;
    private BigDecimal mutualFundPercentage;
    private BigDecimal goldPercentage;
    private BigDecimal stockValue;
    private BigDecimal mutualFundValue;
    private BigDecimal goldValue;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ChartDataPoint {
    private String date;
    private BigDecimal value;
    private String label;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class PerformanceChartData {
    private String symbol;
    private java.util.List<ChartDataPoint> data;
}