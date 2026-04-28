package com.example.stockpriceprediction.controller;

import com.example.stockpriceprediction.model.StockData;
import com.example.stockpriceprediction.service.StockDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockApiController {

    private final StockDataService stockDataService;

    @Autowired
    public StockApiController(StockDataService stockDataService) {
        this.stockDataService = stockDataService;
    }

    // Get all stocks
    @GetMapping
    public List<StockData> getAllStocks() {
        return stockDataService.getAllStockData();
    }

    // Get stocks by symbol
    @GetMapping("/{symbol}")
    public List<StockData> getStocksBySymbol(@PathVariable String symbol) {
        return stockDataService.getStockDataBySymbol(symbol);
    }

    // Get latest stock by symbol
    @GetMapping("/{symbol}/latest")
    public ResponseEntity<StockData> getLatestStock(@PathVariable String symbol) {
        StockData stock = stockDataService.getLatestStock(symbol);
        if (stock == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stock);
    }

    // Add new stock data
    @PostMapping
    public ResponseEntity<StockData> addStock(@RequestBody StockData stockData) {
        StockData saved = stockDataService.saveStockData(stockData);
        return ResponseEntity.ok(saved);
    }

    // Delete stock data
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStock(@PathVariable Long id) {
        stockDataService.deleteStockData(id);
        return ResponseEntity.ok("Stock deleted successfully!");
    }

    // Get prediction for a symbol
    @GetMapping("/{symbol}/predict")
    public ResponseEntity<Double> getPrediction(@PathVariable String symbol) {
        StockData latest = stockDataService.getLatestStock(symbol);
        if (latest == null) {
            return ResponseEntity.notFound().build();
        }
        double prediction = stockDataService.predictNextPrice(latest);
        return ResponseEntity.ok(prediction);
    }
}