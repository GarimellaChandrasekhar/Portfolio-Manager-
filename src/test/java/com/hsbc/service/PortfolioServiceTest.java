package com.hsbc.service;

import com.hsbc.entity.Holding;
import com.hsbc.entity.Portfolio;
import com.hsbc.repo.HoldingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private HoldingRepository holdingRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    // ==================== Basic Update Tests ====================

    @Test
    void shouldFetchAndSaveHoldings() {
        // Arrange
        Holding h1 = new Holding();
        h1.setId(1L);
        h1.setSymbol("AAPL");
        h1.setName("Apple Inc.");
        h1.setQuantity(new BigDecimal("10.0"));

        Holding h2 = new Holding();
        h2.setId(2L);
        h2.setSymbol("GOOG");
        h2.setName("Alphabet Inc.");
        h2.setQuantity(new BigDecimal("5.0"));

        List<Holding> holdings = Arrays.asList(h1, h2);

        when(holdingRepository.findByPortfolioId(1L))
                .thenReturn(holdings);
        when(holdingRepository.saveAll(holdings))
                .thenReturn(holdings);

        // Act
        List<Holding> result = portfolioService.updatePrices(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).getSymbol());
        assertEquals("GOOG", result.get(1).getSymbol());

        verify(holdingRepository).findByPortfolioId(1L);
        verify(holdingRepository).saveAll(holdings);
    }

    @Test
    void shouldHandleSingleHolding() {
        // Arrange
        Holding holding = new Holding();
        holding.setId(1L);
        holding.setSymbol("MSFT");
        holding.setName("Microsoft Corp.");
        holding.setQuantity(new BigDecimal("20.0"));

        List<Holding> holdings = Collections.singletonList(holding);

        when(holdingRepository.findByPortfolioId(2L))
                .thenReturn(holdings);
        when(holdingRepository.saveAll(holdings))
                .thenReturn(holdings);

        // Act
        List<Holding> result = portfolioService.updatePrices(2L);

        // Assert
        assertEquals(1, result.size());
        assertEquals("MSFT", result.get(0).getSymbol());

        verify(holdingRepository).findByPortfolioId(2L);
        verify(holdingRepository).saveAll(holdings);
    }

    @Test
    void shouldHandleEmptyPortfolio() {
        // Arrange
        List<Holding> emptyHoldings = Collections.emptyList();

        when(holdingRepository.findByPortfolioId(3L))
                .thenReturn(emptyHoldings);
        when(holdingRepository.saveAll(emptyHoldings))
                .thenReturn(emptyHoldings);

        // Act
        List<Holding> result = portfolioService.updatePrices(3L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(holdingRepository).findByPortfolioId(3L);
        verify(holdingRepository).saveAll(emptyHoldings);
    }

    // ==================== Symbol Aggregation Tests ====================

    @Test
    void shouldConcatenateMultipleSymbols() {
        // Arrange
        Holding h1 = new Holding();
        h1.setSymbol("AAPL");

        Holding h2 = new Holding();
        h2.setSymbol("GOOGL");

        Holding h3 = new Holding();
        h3.setSymbol("MSFT");

        List<Holding> holdings = Arrays.asList(h1, h2, h3);

        when(holdingRepository.findByPortfolioId(4L))
                .thenReturn(holdings);
        when(holdingRepository.saveAll(holdings))
                .thenReturn(holdings);

        // Act
        List<Holding> result = portfolioService.updatePrices(4L);

        // Assert - verify the service processes multiple symbols
        assertEquals(3, result.size());
        verify(holdingRepository).findByPortfolioId(4L);
    }

    @Test
    void shouldHandleDuplicateSymbols() {
        // Arrange
        Holding h1 = new Holding();
        h1.setSymbol("AAPL");

        Holding h2 = new Holding();
        h2.setSymbol("AAPL");

        Holding h3 = new Holding();
        h3.setSymbol("GOOGL");

        List<Holding> holdings = Arrays.asList(h1, h2, h3);

        when(holdingRepository.findByPortfolioId(5L))
                .thenReturn(holdings);
        when(holdingRepository.saveAll(holdings))
                .thenReturn(holdings);

        // Act
        List<Holding> result = portfolioService.updatePrices(5L);

        // Assert - service should handle duplicate symbols
        assertEquals(3, result.size());
        verify(holdingRepository).findByPortfolioId(5L);
    }

    @Test
    void shouldHandleSingleSymbol() {
        // Arrange
        Holding holding = new Holding();
        holding.setSymbol("TSLA");

        List<Holding> holdings = Collections.singletonList(holding);

        when(holdingRepository.findByPortfolioId(6L))
                .thenReturn(holdings);
        when(holdingRepository.saveAll(holdings))
                .thenReturn(holdings);

        // Act
        List<Holding> result = portfolioService.updatePrices(6L);

        // Assert
        assertEquals(1, result.size());
        assertEquals("TSLA", result.get(0).getSymbol());
    }

    // ==================== Different Asset Types Tests ====================

    @Test
    void shouldHandleStockHoldings() {
        // Arrange
        Holding stock = new Holding();
        stock.setId(1L);
        stock.setSymbol("AAPL");
        stock.setAssetType(Holding.AssetType.STOCK);

        List<Holding> holdings = Collections.singletonList(stock);

        when(holdingRepository.findByPortfolioId(7L))
                .thenReturn(holdings);
        when(holdingRepository.saveAll(holdings))
                .thenReturn(holdings);

        // Act
        List<Holding> result = portfolioService.updatePrices(7L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(Holding.AssetType.STOCK, result.get(0).getAssetType());
    }

    @Test
    void shouldHandleMutualFundHoldings() {
        // Arrange
        Holding mutualFund = new Holding();
        mutualFund.setId(1L);
        mutualFund.setSymbol("VFIAX");
        mutualFund.setAssetType(Holding.AssetType.MUTUAL_FUND);

        List<Holding> holdings = Collections.singletonList(mutualFund);

        when(holdingRepository.findByPortfolioId(8L))
                .thenReturn(holdings);
        when(holdingRepository.saveAll(holdings))
                .thenReturn(holdings);

        // Act
        List<Holding> result = portfolioService.updatePrices(8L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(Holding.AssetType.MUTUAL_FUND, result.get(0).getAssetType());
    }

    @Test
    void shouldHandleGoldHoldings() {
        // Arrange
        Holding gold = new Holding();
        gold.setId(1L);
        gold.setSymbol("GOLD");
        gold.setAssetType(Holding.AssetType.GOLD);

        List<Holding> holdings = Collections.singletonList(gold);

        when(holdingRepository.findByPortfolioId(9L))
                .thenReturn(holdings);
        when(holdingRepository.saveAll(holdings))
                .thenReturn(holdings);

        // Act
        List<Holding> result = portfolioService.updatePrices(9L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(Holding.AssetType.GOLD, result.get(0).getAssetType());
    }

    @Test
    void shouldHandleMixedAssetTypes() {
        // Arrange
        Holding stock = new Holding();
        stock.setSymbol("AAPL");
        stock.setAssetType(Holding.AssetType.STOCK);

        Holding mutualFund = new Holding();
        mutualFund.setSymbol("VFIAX");
        mutualFund.setAssetType(Holding.AssetType.MUTUAL_FUND);

        Holding gold = new Holding();
        gold.setSymbol("GOLD");
        gold.setAssetType(Holding.AssetType.GOLD);

        List<Holding> holdings = Arrays.asList(stock, mutualFund, gold);

        when(holdingRepository.findByPortfolioId(10L))
                .thenReturn(holdings);
        when(holdingRepository.saveAll(holdings))
                .thenReturn(holdings);

        // Act
        List<Holding> result = portfolioService.updatePrices(10L);

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(h -> h.getAssetType() == Holding.AssetType.STOCK));
        assertTrue(result.stream().anyMatch(h -> h.getAssetType() == Holding.AssetType.MUTUAL_FUND));
        assertTrue(result.stream().anyMatch(h -> h.getAssetType() == Holding.AssetType.GOLD));
    }

    // ==================== Large Portfolio Tests ====================

    @Test
    void shouldHandleLargePortfolio() {
        // Arrange
        List<Holding> holdings = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            Holding holding = new Holding();
            holding.setId((long) i);
            holding.setSymbol("STOCK" + i);
            holding.setName("Company " + i);
            holding.setQuantity(new BigDecimal("10.0"));
            holding.setAssetType(Holding.AssetType.STOCK);
            holdings.add(holding);
        }

        when(holdingRepository.findByPortfolioId(11L))
                .thenReturn(holdings);
        when(holdingRepository.saveAll(holdings))
                .thenReturn(holdings);

        // Act
        List<Holding> result = portfolioService.updatePrices(11L);

        // Assert
        assertEquals(50, result.size());
        verify(holdingRepository).findByPortfolioId(11L);
        verify(holdingRepository).saveAll(holdings);
    }

    // ==================== Repository Interaction Tests ====================

    @Test
    void shouldCallRepositoryWithCorrectPortfolioId() {
        // Arrange
        when(holdingRepository.findByPortfolioId(123L))
                .thenReturn(Collections.emptyList());
        when(holdingRepository.saveAll(any()))
                .thenReturn(Collections.emptyList());

        // Act
        portfolioService.updatePrices(123L);

        // Assert
        verify(holdingRepository).findByPortfolioId(123L);
        verify(holdingRepository, never()).findByPortfolioId(122L);
        verify(holdingRepository, never()).findByPortfolioId(124L);
    }

    @Test
    void shouldCallSaveAllExactlyOnce() {
        // Arrange
        List<Holding> holdings = Collections.singletonList(new Holding());

        when(holdingRepository.findByPortfolioId(15L))
                .thenReturn(holdings);
        when(holdingRepository.saveAll(holdings))
                .thenReturn(holdings);

        // Act
        portfolioService.updatePrices(15L);

        // Assert
        verify(holdingRepository, times(1)).saveAll(holdings);
    }

    @Test
    void shouldPassCorrectListToSaveAll() {
        // Arrange
        Holding h1 = new Holding();
        h1.setId(1L);

        Holding h2 = new Holding();
        h2.setId(2L);

        List<Holding> holdings = Arrays.asList(h1, h2);

        ArgumentCaptor<List<Holding>> captor = ArgumentCaptor.forClass(List.class);

        when(holdingRepository.findByPortfolioId(16L))
                .thenReturn(holdings);
        when(holdingRepository.saveAll(captor.capture()))
                .thenReturn(holdings);

        // Act
        portfolioService.updatePrices(16L);

        // Assert
        List<Holding> savedHoldings = captor.getValue();
        assertEquals(2, savedHoldings.size());
        assertEquals(1L, savedHoldings.get(0).getId());
        assertEquals(2L, savedHoldings.get(1).getId());
    }

    // ==================== Different Portfolio IDs Tests ====================

    @Test
    void shouldHandleDifferentPortfolioIds() {
        // Test portfolio 1
        when(holdingRepository.findByPortfolioId(1L))
                .thenReturn(Collections.emptyList());
        when(holdingRepository.saveAll(any()))
                .thenReturn(Collections.emptyList());

        portfolioService.updatePrices(1L);
        verify(holdingRepository).findByPortfolioId(1L);

        // Test portfolio 100
        when(holdingRepository.findByPortfolioId(100L))
                .thenReturn(Collections.emptyList());

        portfolioService.updatePrices(100L);
        verify(holdingRepository).findByPortfolioId(100L);
    }

    // ==================== Return Value Tests ====================

    @Test
    void shouldReturnSavedHoldingsList() {
        // Arrange
        Holding original = new Holding();
        original.setId(1L);
        original.setSymbol("AAPL");

        Holding saved = new Holding();
        saved.setId(1L);
        saved.setSymbol("AAPL");
        saved.setCurrentPrice(new BigDecimal("150.0"));

        List<Holding> originalList = Collections.singletonList(original);
        List<Holding> savedList = Collections.singletonList(saved);

        when(holdingRepository.findByPortfolioId(17L))
                .thenReturn(originalList);
        when(holdingRepository.saveAll(originalList))
                .thenReturn(savedList);

        // Act
        List<Holding> result = portfolioService.updatePrices(17L);

        // Assert
        assertSame(savedList, result);
        assertNotSame(originalList, result);
    }

    @Test
    void shouldReturnNonNullList() {
        // Arrange
        when(holdingRepository.findByPortfolioId(18L))
                .thenReturn(Collections.emptyList());
        when(holdingRepository.saveAll(any()))
                .thenReturn(Collections.emptyList());

        // Act
        List<Holding> result = portfolioService.updatePrices(18L);

        // Assert
        assertNotNull(result);
    }

    // ==================== Special Characters in Symbols Tests ====================

    @Test
    void shouldHandleSymbolsWithSpecialCharacters() {
        // Arrange
        Holding h1 = new Holding();
        h1.setSymbol("BRK.B");

        Holding h2 = new Holding();
        h2.setSymbol("BF.B");

        List<Holding> holdings = Arrays.asList(h1, h2);

        when(holdingRepository.findByPortfolioId(19L))
                .thenReturn(holdings);
        when(holdingRepository.saveAll(holdings))
                .thenReturn(holdings);

        // Act
        List<Holding> result = portfolioService.updatePrices(19L);

        // Assert
        assertEquals(2, result.size());
        assertEquals("BRK.B", result.get(0).getSymbol());
        assertEquals("BF.B", result.get(1).getSymbol());
    }

    // ==================== Null Safety Tests ====================

    @Test
    void shouldHandleNullSymbolsGracefully() {
        // Arrange
        Holding h1 = new Holding();
        h1.setSymbol(null);

        Holding h2 = new Holding();
        h2.setSymbol("GOOGL");

        List<Holding> holdings = Arrays.asList(h1, h2);

        when(holdingRepository.findByPortfolioId(20L))
                .thenReturn(holdings);
        when(holdingRepository.saveAll(holdings))
                .thenReturn(holdings);

        // Act
        List<Holding> result = portfolioService.updatePrices(20L);

        // Assert - should not throw exception
        assertEquals(2, result.size());
    }

    // ==================== Integration Flow Tests ====================

    @Test
    void shouldCompleteFullUpdateFlow() {
        // Arrange
        Portfolio portfolio = new Portfolio();
        portfolio.setId(21L);

        Holding holding = new Holding();
        holding.setId(1L);
        holding.setPortfolio(portfolio);
        holding.setSymbol("NVDA");
        holding.setName("NVIDIA Corporation");
        holding.setQuantity(new BigDecimal("10.0"));
        holding.setPurchasePrice(new BigDecimal("400.0"));
        holding.setAssetType(Holding.AssetType.STOCK);

        List<Holding> holdings = Collections.singletonList(holding);

        when(holdingRepository.findByPortfolioId(21L))
                .thenReturn(holdings);
        when(holdingRepository.saveAll(holdings))
                .thenReturn(holdings);

        // Act
        List<Holding> result = portfolioService.updatePrices(21L);

        // Assert - verify complete flow
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("NVDA", result.get(0).getSymbol());
        assertEquals(new BigDecimal("400.0"), result.get(0).getPurchasePrice());

        verify(holdingRepository).findByPortfolioId(21L);
        verify(holdingRepository).saveAll(holdings);
        verifyNoMoreInteractions(holdingRepository);
    }
}