package com.hsbc.controller;

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

import java.util.List;
import java.util.Optional;

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

    @Test
    void shouldAddHolding() throws Exception {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(1L);

        when(portfolioRepository.findById(1L))
                .thenReturn(Optional.of(portfolio));

        when(holdingRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/holdings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "symbol": "AAPL",
                              "name": "Apple",
                              "quantity": 10,
                              "purchasePrice": 150,
                              "assetType": "STOCK"
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("AAPL"));
    }

    @Test
    void shouldGetHoldings() throws Exception {
        Holding holding = new Holding();
        holding.setSymbol("AAPL");

        when(holdingRepository.findByPortfolioId(1L))
                .thenReturn(List.of(holding));

        mockMvc.perform(get("/api/holdings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("AAPL"));
    }

    @Test
    void shouldDeleteHolding() throws Exception {
        when(holdingRepository.existsById(1L))
                .thenReturn(true);

        mockMvc.perform(delete("/api/holdings/1"))
                .andExpect(status().isOk());

        verify(holdingRepository).deleteById(1L);
    }
}
