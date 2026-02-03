package com.hsbc.repo;


import com.hsbc.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {

    List<Holding> findByPortfolioId(Long portfolioId);

    @Query("SELECT DISTINCT h.symbol FROM Holding h")
    List<String> findAllUniqueSymbols();

    List<Holding> findBySymbol(String symbol);
}
