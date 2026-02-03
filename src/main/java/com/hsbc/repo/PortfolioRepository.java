package com.hsbc.repo;

import com.hsbc.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    // Find portfolio by name (optional, useful later)
    Optional<Portfolio> findByName(String name);

    // Check if portfolio exists by name
    boolean existsByName(String name);
}
