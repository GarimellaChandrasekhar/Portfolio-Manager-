package com.hsbc.controller;



import com.hsbc.dto.GoalRequest;
import com.hsbc.entity.InvestmentGoal;
import com.hsbc.service.InvestmentGoalService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goals")
@CrossOrigin(origins = "*")
public class InvestmentGoalController {

    private final InvestmentGoalService service;

    public InvestmentGoalController(InvestmentGoalService service) {
        this.service = service;
    }

    @PostMapping
    public InvestmentGoal createGoal(@RequestBody GoalRequest request) {
        return service.createGoal(request);
    }
}

