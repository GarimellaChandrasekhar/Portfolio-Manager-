package com.hsbc.dto;

import jakarta.validation.constraints.NotBlank;

public class PortfolioRequest {

    @NotBlank(message = "Portfolio name is required")
    public String name;

    public String description;
}
