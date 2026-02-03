package com.hsbc.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:5400")
public class DashboardController {

    @GetMapping
    public Map<String, Object> getDashboardData() {
        return Map.of(
                "totalPortfolioValue", 152430.25,
                "totalReturn", 12.8,
                "stocksValue", 110900.20,
                "bondsValue", 25330.00,
                "cryptoValue", 11000.05,
                "cashBalance", 5200.00
        );
    }
}
