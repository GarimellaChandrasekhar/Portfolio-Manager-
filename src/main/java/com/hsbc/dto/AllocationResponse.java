package com.hsbc.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AllocationResponse {

    private String assetType;
    private Double percentage;
    private Double sipAmount;
}
