package com.hsbc.repo;

import com.hsbc.entity.GoalInvestmentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalInvestmentrepo extends JpaRepository<GoalInvestmentAllocation,Long> {
}
