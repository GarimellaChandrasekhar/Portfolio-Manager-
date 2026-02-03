package com.hsbc.repo;


import com.hsbc.entity.InvestmentGoal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentGoalRepository extends JpaRepository<InvestmentGoal, Long> {
}
