package com.portfolio.controller;

import com.hsbc.entity.Holding;
import com.hsbc.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
@CrossOrigin
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping("/{portfolioId}/refresh")
    public List<Holding> refreshPrices(@PathVariable Long portfolioId) {
        return portfolioService.updatePrices(portfolioId);
    }
}
