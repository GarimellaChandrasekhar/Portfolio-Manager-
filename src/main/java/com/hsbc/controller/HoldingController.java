package com.hsbc.controller;

import com.hsbc.dto.HoldingRequest;
import com.hsbc.entity.Holding;
import com.hsbc.entity.Portfolio;
import com.hsbc.repo.HoldingRepository;
import com.hsbc.repo.PortfolioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/holdings")
@RequiredArgsConstructor
@CrossOrigin
public class HoldingController {

    private final HoldingRepository holdingRepository;
    private final PortfolioRepository portfolioRepository;

    @PostMapping("/{portfolioId}")
    public Holding addHolding(
            @PathVariable Long portfolioId,
            @Valid @RequestBody HoldingRequest req
    ) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));

        Holding holding = new Holding();
        holding.setPortfolio(portfolio);
        holding.setSymbol(req.symbol);
        holding.setName(req.name);
        holding.setQuantity(req.quantity);
        holding.setPurchasePrice(req.purchasePrice);
        holding.setAssetType(Holding.AssetType.valueOf(req.assetType));
        holding.setPurchaseDate(
                req.purchaseDate != null ? req.purchaseDate : LocalDate.now()
        );

        return holdingRepository.save(holding);
    }



    @GetMapping("/{portfolioId}")
    public List<Holding> getHoldings(@PathVariable Long portfolioId) {
        return holdingRepository.findByPortfolioId(portfolioId);
    }
}
