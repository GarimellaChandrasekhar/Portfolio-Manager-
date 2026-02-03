package com.hsbc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class YahooFinanceService {

    private final WebClient webClient = WebClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    public Map<String, BigDecimal> fetchPrices(String symbolsCsv) {
        String url = "https://query1.finance.yahoo.com/v7/finance/quote?symbols=" + symbolsCsv;

        String response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        Map<String, BigDecimal> prices = new HashMap<>();

        try {
            JsonNode root = mapper.readTree(response);
            JsonNode results = root.path("quoteResponse").path("result");

            for (JsonNode node : results) {
                String symbol = node.path("symbol").asText();
                BigDecimal price = node.path("regularMarketPrice").decimalValue();
                prices.put(symbol, price);
            }
        } catch (Exception e) {
            throw new RuntimeException("Yahoo Finance parsing error", e);
        }

        return prices;
    }
}
