package com.hsbc.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class HoldingRequest {
    public String symbol;
    public String name;
    public BigDecimal quantity;
    public BigDecimal purchasePrice;
    public String assetType;
    public LocalDate purchaseDate;

}
