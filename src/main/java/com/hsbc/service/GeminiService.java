package com.hsbc.service;

import com.hsbc.dto.GeminiRequest;
import com.hsbc.dto.GeminiResponse;
import com.hsbc.entity.Holding;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    // ✅ Test API key
    public String testGemini() {
        return callGemini("Say hello. My Gemini wrapper is working.");
    }

    // ✅ Portfolio Diversification Recommendation
    public String recommendPortfolio(List<Holding> holdings) {

        // Convert holdings → readable portfolio text
        String portfolioText = holdings.stream()
                .map(h -> String.format(
                        "%s (%s) Qty: %s Purchase Price: %s Current Price: %s",
                        h.getSymbol(),
                        h.getAssetType(),
                        h.getQuantity(),
                        h.getPurchasePrice(),
                        h.getCurrentPrice()
                ))
                .collect(Collectors.joining("\n"));

        // 🔥 Your AI Prompt (improved but same intent)
        String prompt = """
                You are a financial portfolio advisor AI.

                Analyze the user's current portfolio and provide diversification advice.

                USER PORTFOLIO:
                %s

                Give recommendations on:

                1. Stocks or assets the user should ADD to diversify
                2. Overweight sectors or asset types
                3. Underweight / missing sectors
                4. Rebalancing suggestions
                5. Risk observations

                Keep the answer concise, structured, and practical.
                """.formatted(portfolioText);

        return callGemini(prompt);
    }

    // ✅ Core Gemini Caller
    private String callGemini(String prompt) {

        String url = apiUrl + "?key=" + apiKey;

        GeminiRequest request = new GeminiRequest(
                List.of(
                        new GeminiRequest.Content(
                                List.of(
                                        new GeminiRequest.Part(prompt)
                                )
                        )
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<GeminiRequest> entity =
                new HttpEntity<>(request, headers);

        ResponseEntity<GeminiResponse> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        entity,
                        GeminiResponse.class
                );

        return response.getBody()
                .getCandidates()
                .get(0)
                .getContent()
                .getParts()
                .get(0)
                .getText();
    }
}
