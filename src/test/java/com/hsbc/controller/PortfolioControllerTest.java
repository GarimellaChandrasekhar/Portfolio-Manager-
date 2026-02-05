package com.hsbc.controller;

import com.hsbc.entity.Holding;
import com.hsbc.entity.Portfolio;
import com.hsbc.service.PortfolioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PortfolioController.class)
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PortfolioService portfolioService;

    // ==================== POST /api/portfolio/{portfolioId}/refresh ====================

    @Test
    void shouldRefreshPricesSuccessfully() throws Exception {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(1L);

        Holding holding1 = new Holding();
        holding1.setId(1L);
        holding1.setPortfolio(portfolio);
        holding1.setSymbol("AAPL");
        holding1.setName("Apple Inc.");
        holding1.setQuantity(new BigDecimal("10.0"));
        holding1.setPurchasePrice(new BigDecimal("150.0"));
        holding1.setCurrentPrice(new BigDecimal("155.0"));
        holding1.setAssetType(Holding.AssetType.STOCK);

        Holding holding2 = new Holding();
        holding2.setId(2L);
        holding2.setPortfolio(portfolio);
        holding2.setSymbol("GOOGL");
        holding2.setName("Alphabet Inc.");
        holding2.setQuantity(new BigDecimal("5.0"));
        holding2.setPurchasePrice(new BigDecimal("2800.0"));
        holding2.setCurrentPrice(new BigDecimal("2850.0"));
        holding2.setAssetType(Holding.AssetType.STOCK);

        List<Holding> updatedHoldings = Arrays.asList(holding1, holding2);

        when(portfolioService.updatePrices(1L))
                .thenReturn(updatedHoldings);

        mockMvc.perform(post("/api/portfolio/1/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$[1].symbol").value("GOOGL"));

        verify(portfolioService).updatePrices(1L);
    }

    @Test
    void shouldRefreshPricesForSingleHolding() throws Exception {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(2L);

        Holding holding = new Holding();
        holding.setId(1L);
        holding.setPortfolio(portfolio);
        holding.setSymbol("MSFT");
        holding.setName("Microsoft Corporation");
        holding.setQuantity(new BigDecimal("20.0"));
        holding.setPurchasePrice(new BigDecimal("300.0"));
        holding.setCurrentPrice(new BigDecimal("320.0"));
        holding.setAssetType(Holding.AssetType.STOCK);

        List<Holding> updatedHoldings = Collections.singletonList(holding);

        when(portfolioService.updatePrices(2L))
                .thenReturn(updatedHoldings);

        mockMvc.perform(post("/api/portfolio/2/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].symbol").value("MSFT"));

        verify(portfolioService).updatePrices(2L);
    }

    @Test
    void shouldReturnEmptyListWhenNoHoldingsToRefresh() throws Exception {
        when(portfolioService.updatePrices(3L))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/portfolio/3/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(portfolioService).updatePrices(3L);
    }

    @Test
    void shouldRefreshPricesForMultipleAssetTypes() throws Exception {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(4L);

        Holding stock = new Holding();
        stock.setId(1L);
        stock.setSymbol("AAPL");
        stock.setName("Apple Inc.");
        stock.setQuantity(new BigDecimal("10.0"));
        stock.setCurrentPrice(new BigDecimal("155.0"));
        stock.setAssetType(Holding.AssetType.STOCK);

        Holding mutualFund = new Holding();
        mutualFund.setId(2L);
        mutualFund.setSymbol("VFIAX");
        mutualFund.setName("Vanguard 500 Index Fund");
        mutualFund.setQuantity(new BigDecimal("50.0"));
        mutualFund.setCurrentPrice(new BigDecimal("350.0"));
        mutualFund.setAssetType(Holding.AssetType.MUTUAL_FUND);

        Holding gold = new Holding();
        gold.setId(3L);
        gold.setSymbol("GOLD");
        gold.setName("Gold ETF");
        gold.setQuantity(new BigDecimal("15.0"));
        gold.setCurrentPrice(new BigDecimal("180.0"));
        gold.setAssetType(Holding.AssetType.GOLD);

        List<Holding> updatedHoldings = Arrays.asList(stock, mutualFund, gold);

        when(portfolioService.updatePrices(4L))
                .thenReturn(updatedHoldings);

        mockMvc.perform(post("/api/portfolio/4/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].assetType").value("STOCK"))
                .andExpect(jsonPath("$[1].assetType").value("MUTUAL_FUND"))
                .andExpect(jsonPath("$[2].assetType").value("GOLD"));

        verify(portfolioService).updatePrices(4L);
    }

    @Test
    void shouldHandleServiceExceptionDuringRefresh() throws Exception {
        when(portfolioService.updatePrices(999L))
                .thenThrow(new RuntimeException("Portfolio not found"));

        mockMvc.perform(post("/api/portfolio/999/refresh"))
                .andExpect(status().is5xxServerError());

        verify(portfolioService).updatePrices(999L);
    }

    @Test
    void shouldRefreshPricesWithUpdatedTimestamps() throws Exception {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(5L);

        Holding holding = new Holding();
        holding.setId(1L);
        holding.setPortfolio(portfolio);
        holding.setSymbol("TSLA");
        holding.setName("Tesla Inc.");
        holding.setQuantity(new BigDecimal("8.0"));
        holding.setPurchasePrice(new BigDecimal("200.0"));
        holding.setCurrentPrice(new BigDecimal("250.0"));
        holding.setPurchaseDate(LocalDate.of(2024, 1, 15));
        holding.setAssetType(Holding.AssetType.STOCK);

        List<Holding> updatedHoldings = Collections.singletonList(holding);

        when(portfolioService.updatePrices(5L))
                .thenReturn(updatedHoldings);

        mockMvc.perform(post("/api/portfolio/5/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("TSLA"));

        verify(portfolioService).updatePrices(5L);
    }

    @Test
    void shouldRefreshPricesForDifferentPortfolioIds() throws Exception {
        when(portfolioService.updatePrices(10L))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/portfolio/10/refresh"))
                .andExpect(status().isOk());

        verify(portfolioService).updatePrices(10L);

        when(portfolioService.updatePrices(20L))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/portfolio/20/refresh"))
                .andExpect(status().isOk());

        verify(portfolioService).updatePrices(20L);
    }

    @Test
    void shouldRefreshPricesWithPriceIncreases() throws Exception {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(6L);

        Holding holding = new Holding();
        holding.setId(1L);
        holding.setPortfolio(portfolio);
        holding.setSymbol("NVDA");
        holding.setName("NVIDIA Corporation");
        holding.setQuantity(new BigDecimal("5.0"));
        holding.setPurchasePrice(new BigDecimal("400.0"));
        holding.setCurrentPrice(new BigDecimal("500.0"));
        holding.setAssetType(Holding.AssetType.STOCK);

        List<Holding> updatedHoldings = Collections.singletonList(holding);

        when(portfolioService.updatePrices(6L))
                .thenReturn(updatedHoldings);

        mockMvc.perform(post("/api/portfolio/6/refresh"))
                .andExpect(status().isOk());

        verify(portfolioService).updatePrices(6L);
    }

    @Test
    void shouldRefreshPricesWithPriceDecreases() throws Exception {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(7L);

        Holding holding = new Holding();
        holding.setId(1L);
        holding.setPortfolio(portfolio);
        holding.setSymbol("META");
        holding.setName("Meta Platforms Inc.");
        holding.setQuantity(new BigDecimal("15.0"));
        holding.setPurchasePrice(new BigDecimal("350.0"));
        holding.setCurrentPrice(new BigDecimal("320.0"));
        holding.setAssetType(Holding.AssetType.STOCK);

        List<Holding> updatedHoldings = Collections.singletonList(holding);

        when(portfolioService.updatePrices(7L))
                .thenReturn(updatedHoldings);

        mockMvc.perform(post("/api/portfolio/7/refresh"))
                .andExpect(status().isOk());

        verify(portfolioService).updatePrices(7L);
    }

    @Test
    void shouldVerifyServiceMethodCalledOnce() throws Exception {
        when(portfolioService.updatePrices(8L))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/portfolio/8/refresh"))
                .andExpect(status().isOk());

        verify(portfolioService, times(1)).updatePrices(8L);
        verifyNoMoreInteractions(portfolioService);
    }

    @Test
    void shouldRefreshPricesForLargePortfolio() throws Exception {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(9L);

        List<Holding> holdings = new java.util.ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Holding holding = new Holding();
            holding.setId((long) i);
            holding.setPortfolio(portfolio);
            holding.setSymbol("STOCK" + i);
            holding.setName("Company " + i);
            holding.setQuantity(new BigDecimal(10.0 * i));
            holding.setPurchasePrice(new BigDecimal(100.0 * i));
            holding.setCurrentPrice(new BigDecimal(105.0 * i));
            holding.setAssetType(Holding.AssetType.STOCK);
            holdings.add(holding);
        }

        when(portfolioService.updatePrices(9L))
                .thenReturn(holdings);

        mockMvc.perform(post("/api/portfolio/9/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[0].symbol").value("STOCK1"))
                .andExpect(jsonPath("$[9].symbol").value("STOCK10"));

        verify(portfolioService).updatePrices(9L);
    }
}