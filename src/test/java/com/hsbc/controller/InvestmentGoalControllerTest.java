package com.hsbc.controller;

import com.hsbc.dto.GoalInvestmentResponse;
import com.hsbc.service.InvestmentGoalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvestmentGoalController.class)
class InvestmentGoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvestmentGoalService investmentGoalService;

    @Test
    void shouldCreateGoal() throws Exception {
        GoalInvestmentResponse response =
                GoalInvestmentResponse.builder()
                        .goalId(1L)
                        .goalName("Retirement")
                        .monthlyInvestment(50000.0)
                        .build();

        when(investmentGoalService.createGoal(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "goalName": "Retirement",
                              "targetAmount": 1000000,
                              "timeHorizon": 10,
                              "riskLevel": "HIGH",
                              "monthlyInvestment": 50000
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goalName").value("Retirement"));
    }
}
