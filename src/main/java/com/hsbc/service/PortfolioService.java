package com.hsbc.service;

import com.hsbc.entity.Holding;
import com.hsbc.repo.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final HoldingRepository holdingRepository;
    //private final YahooFinanceService yahooFinanceService;

    public List<Holding> updatePrices(Long portfolioId) {
        List<Holding> holdings = holdingRepository.findByPortfolioId(portfolioId);

        String symbols = holdings.stream()
                .map(Holding::getSymbol)
                .distinct()
                .reduce((a, b) -> a + "," + b)
                .orElse("");

        //Map<String, BigDecimal> prices = yahooFinanceService.fetchPrices(symbols);

//        holdings.forEach(h -> {
//            BigDecimal price = prices.get(h.getSymbol());
//            if (price != null) {
//                h.setCurrentPrice(price);
//            }
//        });

        return holdingRepository.saveAll(holdings);
    }
}
