package com.hsbc.controller;

import com.hsbc.dto.StockRecommendationResponse;
import com.hsbc.entity.Holding;
import com.hsbc.repo.HoldingRepository;
import com.hsbc.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendation")
@RequiredArgsConstructor
public class RecommendationController {

    private final GeminiService geminiService;
    private final HoldingRepository holdingRepository;

    // ✅ Test Gemini
    @GetMapping("/test")
    public String testGemini() {
        return geminiService.testGemini();
    }

    // ✅ Portfolio Recommendation
    @GetMapping("/{portfolioId}")
    public StockRecommendationResponse recommend(
            @PathVariable Long portfolioId
    ) {

        List<Holding> holdings =
                holdingRepository.findByPortfolioId(portfolioId);

        String result =
                geminiService.recommendPortfolio(holdings);

        return new StockRecommendationResponse(result);
    }
}
