package com.example.stockpriceprediction.repository;

import com.example.stockpriceprediction.model.StockData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockDataRepository extends JpaRepository<StockData, Long> {
    List<StockData> findBySymbol(String symbol);
}
