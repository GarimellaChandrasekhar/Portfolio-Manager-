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


    // UPDATE holding
    @PutMapping("/{holdingId}")
    public Holding updateHolding(
            @PathVariable Long holdingId,
            @RequestBody HoldingRequest req
    ) {
        Holding holding = holdingRepository.findById(holdingId)
                .orElseThrow(() -> new RuntimeException("Holding not found"));

        if (req.name != null && !req.name.isBlank()) {
            holding.setName(req.name);
        }
        if (req.quantity != null) {
            holding.setQuantity(req.quantity);
        }
        if (req.purchasePrice != null) {
            holding.setPurchasePrice(req.purchasePrice);
        }
        if (req.assetType != null) {
            holding.setAssetType(Holding.AssetType.valueOf(req.assetType));
        }

        return holdingRepository.save(holding);
    }


    // DELETE holding
    @DeleteMapping("/{holdingId}")
    public void deleteHolding(@PathVariable Long holdingId) {
        if (!holdingRepository.existsById(holdingId)) {
            throw new RuntimeException("Holding not found");
        }
        holdingRepository.deleteById(holdingId);
    }

    @GetMapping("/{portfolioId}")
    public List<Holding> getHoldings(@PathVariable Long portfolioId) {
        return holdingRepository.findByPortfolioId(portfolioId);
    }
}
