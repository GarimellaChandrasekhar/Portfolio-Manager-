package com.hsbc.repo;


import com.hsbc.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    Optional<PriceHistory> findBySymbolAndPriceDate(String symbol, LocalDate priceDate);

    List<PriceHistory> findBySymbolOrderByPriceDateDesc(String symbol);

    @Query("SELECT ph FROM PriceHistory ph WHERE ph.symbol = :symbol " +
            "AND ph.priceDate >= :startDate AND ph.priceDate <= :endDate " +
            "ORDER BY ph.priceDate ASC")
    List<PriceHistory> findBySymbolAndDateRange(@Param("symbol") String symbol,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);
}
