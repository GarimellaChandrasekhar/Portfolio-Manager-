package com.hsbc.controller;

import com.hsbc.entity.Holding;
//import com.hsbc.service.HoldingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HoldingController.class)
class HoldingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HoldingService holdingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnHoldingsByPortfolio() throws Exception {
        Holding h = new Holding();
        h.setId(1L);
        h.setSymbol("TCS");
        h.setName("Tata Consultancy Services");
        h.setQuantity(BigDecimal.ONE);
        h.setPurchasePrice(BigDecimal.valueOf(3000));

        Mockito.when(holdingService.getHoldingsByPortfolio(1L))
                .thenReturn(List.of(h));

        mockMvc.perform(get("/api/holdings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("TCS"));
    }

    @Test
    void shouldDeleteHolding() throws Exception {
        Mockito.doNothing().when(holdingService).deleteHolding(1L);

        mockMvc.perform(delete("/api/holdings/1"))
                .andExpect(status().isNoContent());
    }
}
