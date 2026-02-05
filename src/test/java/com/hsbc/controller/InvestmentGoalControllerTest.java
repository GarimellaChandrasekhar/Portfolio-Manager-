package com.hsbc.controller;

import com.hsbc.dto.GoalInvestmentResponse;
import com.hsbc.dto.GoalRequest;
import com.hsbc.service.InvestmentGoalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvestmentGoalController.class)
class InvestmentGoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvestmentGoalService investmentGoalService;

    // ==================== POST /api/goals ====================

    @Test
    void shouldCreateGoalWithAllFields() throws Exception {
        GoalInvestmentResponse response = GoalInvestmentResponse.builder()
                .goalId(1L)
                .goalName("Retirement")
                .monthlyInvestment(50000.0)
                .build();

        when(investmentGoalService.createGoal(any(GoalRequest.class)))
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
                .andExpect(jsonPath("$.goalId").value(1))
                .andExpect(jsonPath("$.goalName").value("Retirement"))
                .andExpect(jsonPath("$.monthlyInvestment").value(50000.0));

        verify(investmentGoalService).createGoal(any(GoalRequest.class));
    }

    @Test
    void shouldCreateGoalForEducation() throws Exception {
        GoalInvestmentResponse response = GoalInvestmentResponse.builder()
                .goalId(2L)
                .goalName("Child Education")
                .monthlyInvestment(20000.0)
                .build();

        when(investmentGoalService.createGoal(any(GoalRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "goalName": "Child Education",
                              "targetAmount": 500000,
                              "timeHorizon": 15,
                              "riskLevel": "MEDIUM",
                              "monthlyInvestment": 20000
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goalId").value(2))
                .andExpect(jsonPath("$.goalName").value("Child Education"));

        verify(investmentGoalService).createGoal(any(GoalRequest.class));
    }

    @Test
    void shouldCreateGoalForHomePurchase() throws Exception {
        GoalInvestmentResponse response = GoalInvestmentResponse.builder()
                .goalId(3L)
                .goalName("Home Purchase")
                .monthlyInvestment(75000.0)
                .build();

        when(investmentGoalService.createGoal(any(GoalRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "goalName": "Home Purchase",
                              "targetAmount": 5000000,
                              "timeHorizon": 5,
                              "riskLevel": "LOW",
                              "monthlyInvestment": 75000
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goalName").value("Home Purchase"));

        verify(investmentGoalService).createGoal(any(GoalRequest.class));
    }

    @Test
    void shouldCreateGoalWithMinimalMonthlyInvestment() throws Exception {
        GoalInvestmentResponse response = GoalInvestmentResponse.builder()
                .goalId(4L)
                .goalName("Emergency Fund")
                .monthlyInvestment(5000.0)
                .build();

        when(investmentGoalService.createGoal(any(GoalRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "goalName": "Emergency Fund",
                              "targetAmount": 100000,
                              "timeHorizon": 2,
                              "riskLevel": "LOW",
                              "monthlyInvestment": 5000
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyInvestment").value(5000.0));

        verify(investmentGoalService).createGoal(any(GoalRequest.class));
    }

    @Test
    void shouldCreateGoalWithLongTimeHorizon() throws Exception {
        GoalInvestmentResponse response = GoalInvestmentResponse.builder()
                .goalId(5L)
                .goalName("Long Term Wealth")
                .monthlyInvestment(25000.0)
                .build();

        when(investmentGoalService.createGoal(any(GoalRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "goalName": "Long Term Wealth",
                              "targetAmount": 10000000,
                              "timeHorizon": 30,
                              "riskLevel": "HIGH",
                              "monthlyInvestment": 25000
                            }
                        """))
                .andExpect(status().isOk());

        verify(investmentGoalService).createGoal(any(GoalRequest.class));
    }

    @Test
    void shouldHandleServiceExceptionGracefully() throws Exception {
        when(investmentGoalService.createGoal(any(GoalRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "goalName": "Test Goal",
                              "targetAmount": 100000,
                              "timeHorizon": 5,
                              "riskLevel": "MEDIUM",
                              "monthlyInvestment": 10000
                            }
                        """))
                .andExpect(status().is5xxServerError());

        verify(investmentGoalService).createGoal(any(GoalRequest.class));
    }

    @Test
    void shouldCreateGoalWithDecimalValues() throws Exception {
        GoalInvestmentResponse response = GoalInvestmentResponse.builder()
                .goalId(7L)
                .goalName("Vacation Fund")
                .monthlyInvestment(4166.68)
                .build();

        when(investmentGoalService.createGoal(any(GoalRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "goalName": "Vacation Fund",
                              "targetAmount": 150000.50,
                              "timeHorizon": 3,
                              "riskLevel": "MEDIUM",
                              "monthlyInvestment": 4166.68
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyInvestment").value(4166.68));

        verify(investmentGoalService).createGoal(any(GoalRequest.class));
    }

    @Test
    void shouldCreateGoalWithShortTimeHorizon() throws Exception {
        GoalInvestmentResponse response = GoalInvestmentResponse.builder()
                .goalId(8L)
                .goalName("Short Term Savings")
                .monthlyInvestment(4200.0)
                .build();

        when(investmentGoalService.createGoal(any(GoalRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "goalName": "Short Term Savings",
                              "targetAmount": 50000,
                              "timeHorizon": 1,
                              "riskLevel": "LOW",
                              "monthlyInvestment": 4200
                            }
                        """))
                .andExpect(status().isOk());

        verify(investmentGoalService).createGoal(any(GoalRequest.class));
    }

    @Test
    void shouldVerifyControllerDelegationToService() throws Exception {
        GoalInvestmentResponse response = GoalInvestmentResponse.builder()
                .goalId(9L)
                .goalName("Test Delegation")
                .monthlyInvestment(1500.0)
                .build();

        when(investmentGoalService.createGoal(any(GoalRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "goalName": "Test Delegation",
                              "targetAmount": 100000,
                              "timeHorizon": 5,
                              "riskLevel": "MEDIUM",
                              "monthlyInvestment": 1500
                            }
                        """))
                .andExpect(status().isOk());

        verify(investmentGoalService, times(1)).createGoal(any(GoalRequest.class));
    }
}