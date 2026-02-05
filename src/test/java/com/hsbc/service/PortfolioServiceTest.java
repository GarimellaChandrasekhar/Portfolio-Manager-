package com.hsbc.service;

import com.hsbc.entity.Holding;
import com.hsbc.repo.HoldingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private HoldingRepository holdingRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    @Test
    void shouldFetchAndSaveHoldings() {

        // Arrange
        Holding h1 = new Holding();
        h1.setId(1L);
        h1.setSymbol("AAPL");

        Holding h2 = new Holding();
        h2.setId(2L);
        h2.setSymbol("GOOG");

        List<Holding> holdings = List.of(h1, h2);

        when(holdingRepository.findByPortfolioId(1L))
                .thenReturn(holdings);

        when(holdingRepository.saveAll(holdings))
                .thenReturn(holdings);

        // Act
        List<Holding> result =
                portfolioService.updatePrices(1L);

        // Assert
        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).getSymbol());
        assertEquals("GOOG", result.get(1).getSymbol());

        // Verify interactions
        verify(holdingRepository).findByPortfolioId(1L);
        verify(holdingRepository).saveAll(holdings);
    }
}
