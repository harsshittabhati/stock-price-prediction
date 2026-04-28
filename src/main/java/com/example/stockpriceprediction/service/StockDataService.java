package com.example.stockpriceprediction.service;

import com.example.stockpriceprediction.model.StockData;
import com.example.stockpriceprediction.repository.StockDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockDataService {

    private final StockDataRepository stockDataRepository;

    @Autowired
    public StockDataService(StockDataRepository stockDataRepository) {
        this.stockDataRepository = stockDataRepository;
    }

    // Get all stocks
    public List<StockData> getAllStockData() {
        return stockDataRepository.findAll();
    }

    // Get stocks by symbol
    public List<StockData> getStockDataBySymbol(String symbol) {
        return stockDataRepository.findBySymbol(symbol);
    }

    // Save stock data
    public StockData saveStockData(StockData stockData) {
        // Calculate prediction before saving
        double predictedPrice = predictNextPrice(stockData);
        stockData.setPredictedPrice(predictedPrice);
        return stockDataRepository.save(stockData);
    }

    // Delete stock data
    public void deleteStockData(Long id) {
        stockDataRepository.deleteById(id);
    }

    // AI Prediction Logic (Moving Average)
    public double predictNextPrice(StockData stockData) {
        List<StockData> historicalData = stockDataRepository
                .findBySymbol(stockData.getSymbol());

        if (historicalData.isEmpty()) {
            // No history - return close price as prediction
            return stockData.getClosePrice();
        }

        // Calculate average of last 5 closing prices
        int count = Math.min(5, historicalData.size());
        double sum = 0;

        for (int i = historicalData.size() - count; 
             i < historicalData.size(); i++) {
            sum += historicalData.get(i).getClosePrice();
        }

        double movingAverage = sum / count;

        // Apply simple trend factor
        double latestClose = stockData.getClosePrice();
        double trendFactor = latestClose / movingAverage;

        // Predicted price = moving average * trend factor
        return Math.round(movingAverage * trendFactor * 100.0) / 100.0;
    }

    // Get latest stock by symbol
    public StockData getLatestStock(String symbol) {
        List<StockData> data = stockDataRepository.findBySymbol(symbol);
        if (data.isEmpty()) return null;
        return data.get(data.size() - 1);
    }
}