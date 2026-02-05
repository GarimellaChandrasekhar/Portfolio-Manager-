package com.hsbc.controller;

import com.hsbc.dto.HoldingRequest;
import com.hsbc.entity.Holding;
import com.hsbc.entity.Portfolio;
import com.hsbc.repo.HoldingRepository;
import com.hsbc.repo.PortfolioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HoldingController.class)
class HoldingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HoldingRepository holdingRepository;

    @MockBean
    private PortfolioRepository portfolioRepository;

    // ==================== POST /api/holdings/{portfolioId} ====================

    @Test
    void shouldAddHoldingWithAllFields() throws Exception {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(1L);

        Holding savedHolding = new Holding();
        savedHolding.setId(1L);
        savedHolding.setPortfolio(portfolio);
        savedHolding.setSymbol("AAPL");
        savedHolding.setName("Apple Inc.");
        savedHolding.setQuantity(new BigDecimal("10.0"));
        savedHolding.setPurchasePrice(new BigDecimal("150.0"));
        savedHolding.setAssetType(Holding.AssetType.STOCK);
        savedHolding.setPurchaseDate(LocalDate.of(2024, 1, 15));

        when(portfolioRepository.findById(1L))
                .thenReturn(Optional.of(portfolio));
        when(holdingRepository.save(any(Holding.class)))
                .thenReturn(savedHolding);

        mockMvc.perform(post("/api/holdings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "symbol": "AAPL",
                              "name": "Apple Inc.",
                              "quantity": 10.0,
                              "purchasePrice": 150.0,
                              "assetType": "STOCK",
                              "purchaseDate": "2024-01-15"
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.name").value("Apple Inc."));

        verify(portfolioRepository).findById(1L);
        verify(holdingRepository).save(any(Holding.class));
    }

    @Test
    void shouldAddHoldingWithoutPurchaseDateUsesCurrentDate() throws Exception {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(1L);

        Holding savedHolding = new Holding();
        savedHolding.setId(1L);
        savedHolding.setPortfolio(portfolio);
        savedHolding.setSymbol("GOOGL");
        savedHolding.setName("Google");
        savedHolding.setQuantity(new BigDecimal("5.0"));
        savedHolding.setPurchasePrice(new BigDecimal("2800.0"));
        savedHolding.setAssetType(Holding.AssetType.STOCK);
        savedHolding.setPurchaseDate(LocalDate.now());

        when(portfolioRepository.findById(1L))
                .thenReturn(Optional.of(portfolio));
        when(holdingRepository.save(any(Holding.class)))
                .thenReturn(savedHolding);

        mockMvc.perform(post("/api/holdings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "symbol": "GOOGL",
                              "name": "Google",
                              "quantity": 5.0,
                              "purchasePrice": 2800.0,
                              "assetType": "STOCK"
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("GOOGL"));

        verify(portfolioRepository).findById(1L);
        verify(holdingRepository).save(any(Holding.class));
    }

    @Test
    void shouldThrowExceptionWhenPortfolioNotFoundOnAdd() throws Exception {
        when(portfolioRepository.findById(999L))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/holdings/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "symbol": "AAPL",
                              "name": "Apple Inc.",
                              "quantity": 10.0,
                              "purchasePrice": 150.0,
                              "assetType": "STOCK"
                            }
                        """))
                .andExpect(status().is5xxServerError());

        verify(portfolioRepository).findById(999L);
        verify(holdingRepository, never()).save(any(Holding.class));
    }

    @Test
    void shouldAddHoldingWithMutualFundAssetType() throws Exception {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(1L);

        Holding savedHolding = new Holding();
        savedHolding.setId(2L);
        savedHolding.setPortfolio(portfolio);
        savedHolding.setSymbol("VFIAX");
        savedHolding.setName("Vanguard 500 Index Fund");
        savedHolding.setQuantity(new BigDecimal("100.0"));
        savedHolding.setPurchasePrice(new BigDecimal("350.0"));
        savedHolding.setAssetType(Holding.AssetType.MUTUAL_FUND);

        when(portfolioRepository.findById(1L))
                .thenReturn(Optional.of(portfolio));
        when(holdingRepository.save(any(Holding.class)))
                .thenReturn(savedHolding);

        mockMvc.perform(post("/api/holdings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "symbol": "VFIAX",
                              "name": "Vanguard 500 Index Fund",
                              "quantity": 100.0,
                              "purchasePrice": 350.0,
                              "assetType": "MUTUAL_FUND"
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assetType").value("MUTUAL_FUND"));

        verify(portfolioRepository).findById(1L);
        verify(holdingRepository).save(any(Holding.class));
    }

    @Test
    void shouldAddHoldingWithGoldAssetType() throws Exception {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(1L);

        Holding savedHolding = new Holding();
        savedHolding.setId(3L);
        savedHolding.setPortfolio(portfolio);
        savedHolding.setSymbol("GOLD");
        savedHolding.setName("Gold ETF");
        savedHolding.setQuantity(new BigDecimal("50.0"));
        savedHolding.setPurchasePrice(new BigDecimal("180.0"));
        savedHolding.setAssetType(Holding.AssetType.GOLD);

        when(portfolioRepository.findById(1L))
                .thenReturn(Optional.of(portfolio));
        when(holdingRepository.save(any(Holding.class)))
                .thenReturn(savedHolding);

        mockMvc.perform(post("/api/holdings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "symbol": "GOLD",
                              "name": "Gold ETF",
                              "quantity": 50.0,
                              "purchasePrice": 180.0,
                              "assetType": "GOLD"
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assetType").value("GOLD"));

        verify(portfolioRepository).findById(1L);
        verify(holdingRepository).save(any(Holding.class));
    }

    // ==================== PUT /api/holdings/{holdingId} ====================

    @Test
    void shouldUpdateHoldingWithAllFields() throws Exception {
        Holding existingHolding = new Holding();
        existingHolding.setId(1L);
        existingHolding.setSymbol("AAPL");
        existingHolding.setName("Apple");
        existingHolding.setQuantity(new BigDecimal("10.0"));
        existingHolding.setPurchasePrice(new BigDecimal("150.0"));
        existingHolding.setAssetType(Holding.AssetType.STOCK);

        Holding updatedHolding = new Holding();
        updatedHolding.setId(1L);
        updatedHolding.setSymbol("AAPL");
        updatedHolding.setName("Apple Inc. Updated");
        updatedHolding.setQuantity(new BigDecimal("20.0"));
        updatedHolding.setPurchasePrice(new BigDecimal("160.0"));
        updatedHolding.setAssetType(Holding.AssetType.MUTUAL_FUND);

        when(holdingRepository.findById(1L))
                .thenReturn(Optional.of(existingHolding));
        when(holdingRepository.save(any(Holding.class)))
                .thenReturn(updatedHolding);

        mockMvc.perform(put("/api/holdings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "Apple Inc. Updated",
                              "quantity": 20.0,
                              "purchasePrice": 160.0,
                              "assetType": "MUTUAL_FUND"
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Apple Inc. Updated"));

        verify(holdingRepository).findById(1L);
        verify(holdingRepository).save(any(Holding.class));
    }

    @Test
    void shouldUpdateHoldingWithPartialFields() throws Exception {
        Holding existingHolding = new Holding();
        existingHolding.setId(1L);
        existingHolding.setSymbol("AAPL");
        existingHolding.setName("Apple");
        existingHolding.setQuantity(new BigDecimal("10.0"));
        existingHolding.setPurchasePrice(new BigDecimal("150.0"));
        existingHolding.setAssetType(Holding.AssetType.STOCK);

        when(holdingRepository.findById(1L))
                .thenReturn(Optional.of(existingHolding));
        when(holdingRepository.save(any(Holding.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/holdings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "quantity": 15.0
                            }
                        """))
                .andExpect(status().isOk());

        verify(holdingRepository).findById(1L);
        verify(holdingRepository).save(any(Holding.class));
    }

    @Test
    void shouldNotUpdateHoldingWhenNameIsBlank() throws Exception {
        Holding existingHolding = new Holding();
        existingHolding.setId(1L);
        existingHolding.setSymbol("AAPL");
        existingHolding.setName("Apple");
        existingHolding.setQuantity(new BigDecimal("10.0"));

        when(holdingRepository.findById(1L))
                .thenReturn(Optional.of(existingHolding));
        when(holdingRepository.save(any(Holding.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/holdings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "   ",
                              "quantity": 15.0
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Apple"));

        verify(holdingRepository).findById(1L);
        verify(holdingRepository).save(any(Holding.class));
    }

    @Test
    void shouldNotUpdateHoldingWhenNameIsNull() throws Exception {
        Holding existingHolding = new Holding();
        existingHolding.setId(1L);
        existingHolding.setSymbol("AAPL");
        existingHolding.setName("Apple");
        existingHolding.setQuantity(new BigDecimal("10.0"));

        when(holdingRepository.findById(1L))
                .thenReturn(Optional.of(existingHolding));
        when(holdingRepository.save(any(Holding.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/holdings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "quantity": 15.0
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Apple"));

        verify(holdingRepository).findById(1L);
        verify(holdingRepository).save(any(Holding.class));
    }

    @Test
    void shouldThrowExceptionWhenHoldingNotFoundOnUpdate() throws Exception {
        when(holdingRepository.findById(999L))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/holdings/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "quantity": 20.0
                            }
                        """))
                .andExpect(status().is5xxServerError());

        verify(holdingRepository).findById(999L);
        verify(holdingRepository, never()).save(any(Holding.class));
    }

    @Test
    void shouldUpdateOnlyNameField() throws Exception {
        Holding existingHolding = new Holding();
        existingHolding.setId(1L);
        existingHolding.setName("Old Name");
        existingHolding.setQuantity(new BigDecimal("10.0"));
        existingHolding.setPurchasePrice(new BigDecimal("100.0"));

        when(holdingRepository.findById(1L))
                .thenReturn(Optional.of(existingHolding));
        when(holdingRepository.save(any(Holding.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/holdings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "New Name"
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));

        verify(holdingRepository).findById(1L);
        verify(holdingRepository).save(any(Holding.class));
    }

    @Test
    void shouldUpdateOnlyPurchasePriceField() throws Exception {
        Holding existingHolding = new Holding();
        existingHolding.setId(1L);
        existingHolding.setName("Apple");
        existingHolding.setQuantity(new BigDecimal("10.0"));
        existingHolding.setPurchasePrice(new BigDecimal("100.0"));

        when(holdingRepository.findById(1L))
                .thenReturn(Optional.of(existingHolding));
        when(holdingRepository.save(any(Holding.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/holdings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "purchasePrice": 200.0
                            }
                        """))
                .andExpect(status().isOk());

        verify(holdingRepository).findById(1L);
        verify(holdingRepository).save(any(Holding.class));
    }

    @Test
    void shouldUpdateOnlyAssetTypeField() throws Exception {
        Holding existingHolding = new Holding();
        existingHolding.setId(1L);
        existingHolding.setName("Apple");
        existingHolding.setAssetType(Holding.AssetType.STOCK);

        when(holdingRepository.findById(1L))
                .thenReturn(Optional.of(existingHolding));
        when(holdingRepository.save(any(Holding.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/holdings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "assetType": "MUTUAL_FUND"
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assetType").value("MUTUAL_FUND"));

        verify(holdingRepository).findById(1L);
        verify(holdingRepository).save(any(Holding.class));
    }

    // ==================== DELETE /api/holdings/{holdingId} ====================

    @Test
    void shouldDeleteHoldingSuccessfully() throws Exception {
        when(holdingRepository.existsById(1L))
                .thenReturn(true);

        mockMvc.perform(delete("/api/holdings/1"))
                .andExpect(status().isOk());

        verify(holdingRepository).existsById(1L);
        verify(holdingRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenHoldingNotFoundOnDelete() throws Exception {
        when(holdingRepository.existsById(999L))
                .thenReturn(false);

        mockMvc.perform(delete("/api/holdings/999"))
                .andExpect(status().is5xxServerError());

        verify(holdingRepository).existsById(999L);
        verify(holdingRepository, never()).deleteById(anyLong());
    }

    // ==================== GET /api/holdings/{portfolioId} ====================

    @Test
    void shouldGetHoldingsForPortfolio() throws Exception {
        Holding holding1 = new Holding();
        holding1.setId(1L);
        holding1.setSymbol("AAPL");
        holding1.setName("Apple Inc.");
        holding1.setQuantity(new BigDecimal("10.0"));

        Holding holding2 = new Holding();
        holding2.setId(2L);
        holding2.setSymbol("GOOGL");
        holding2.setName("Google");
        holding2.setQuantity(new BigDecimal("5.0"));

        List<Holding> holdings = Arrays.asList(holding1, holding2);

        when(holdingRepository.findByPortfolioId(1L))
                .thenReturn(holdings);

        mockMvc.perform(get("/api/holdings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$[1].symbol").value("GOOGL"));

        verify(holdingRepository).findByPortfolioId(1L);
    }

    @Test
    void shouldReturnEmptyListWhenNoHoldingsFound() throws Exception {
        when(holdingRepository.findByPortfolioId(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/holdings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(holdingRepository).findByPortfolioId(1L);
    }

    @Test
    void shouldGetHoldingsWithDifferentAssetTypes() throws Exception {
        Holding stock = new Holding();
        stock.setId(1L);
        stock.setSymbol("AAPL");
        stock.setAssetType(Holding.AssetType.STOCK);

        Holding mutualFund = new Holding();
        mutualFund.setId(2L);
        mutualFund.setSymbol("VFIAX");
        mutualFund.setAssetType(Holding.AssetType.MUTUAL_FUND);

        Holding gold = new Holding();
        gold.setId(3L);
        gold.setSymbol("GOLD");
        gold.setAssetType(Holding.AssetType.GOLD);

        List<Holding> holdings = Arrays.asList(stock, mutualFund, gold);

        when(holdingRepository.findByPortfolioId(1L))
                .thenReturn(holdings);

        mockMvc.perform(get("/api/holdings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].assetType").value("STOCK"))
                .andExpect(jsonPath("$[1].assetType").value("MUTUAL_FUND"))
                .andExpect(jsonPath("$[2].assetType").value("GOLD"));

        verify(holdingRepository).findByPortfolioId(1L);
    }
}