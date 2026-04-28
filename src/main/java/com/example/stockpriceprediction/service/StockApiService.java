package com.example.stockpriceprediction.service;

import com.example.stockpriceprediction.model.StockData;
import com.example.stockpriceprediction.repository.StockDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StockApiService {

    @Value("${alphavantage.api.key}")
    private String apiKey;

    private final StockDataRepository stockDataRepository;
    private final StockDataService stockDataService;
    private final RestTemplate restTemplate;

    @Autowired
    public StockApiService(StockDataRepository stockDataRepository,
                       StockDataService stockDataService) {
        this.stockDataRepository = stockDataRepository;
        this.stockDataService = stockDataService;
        this.restTemplate = new RestTemplate();
    }

    // Fetch and save stock data from Alpha Vantage
    public List<StockData> fetchAndSaveStockData(String symbol) {
        String url = "https://www.alphavantage.co/query"
                + "?function=TIME_SERIES_DAILY"
                + "&symbol=" + symbol
                + "&outputsize=compact"
                + "&apikey=" + apiKey;

        try {
            // Call Alpha Vantage API
            Map<String, Object> response = restTemplate
                    .getForObject(url, Map.class);

            if (response == null || 
                !response.containsKey("Time Series (Daily)")) {
                return new ArrayList<>();
            }

            // Get company name from metadata
            Map<String, String> metadata = 
                    (Map<String, String>) response.get("Meta Data");
            String companySymbol = metadata != null ? 
                    metadata.get("2. Symbol") : symbol;

            // Get time series data
            Map<String, Map<String, String>> timeSeries =
                    (Map<String, Map<String, String>>) 
                    response.get("Time Series (Daily)");

            List<StockData> savedStocks = new ArrayList<>();
            int count = 0;

            // Save last 10 days of data
            for (Map.Entry<String, Map<String, String>> entry 
                    : timeSeries.entrySet()) {

                if (count >= 10) break;

                String dateStr = entry.getKey();
                Map<String, String> values = entry.getValue();

                // Check if data already exists
                LocalDate date = LocalDate.parse(dateStr);
                List<StockData> existing = 
                        stockDataRepository.findBySymbol(symbol);

                boolean alreadyExists = existing.stream()
                        .anyMatch(s -> s.getDate().equals(date));

                if (!alreadyExists) {
                    StockData stock = new StockData();
                    stock.setSymbol(symbol.toUpperCase());
                    stock.setCompanyName(companySymbol);
                    stock.setDate(date);
                    stock.setOpenPrice(Double
                            .parseDouble(values.get("1. open")));
                    stock.setHighPrice(Double
                            .parseDouble(values.get("2. high")));
                    stock.setLowPrice(Double
                            .parseDouble(values.get("3. low")));
                    stock.setClosePrice(Double
                            .parseDouble(values.get("4. close")));
                    stock.setVolume(Long
                            .parseLong(values.get("5. volume")));

                    savedStocks.add(stockDataService.saveStockData(stock));
                }
                count++;
            }

            return savedStocks;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}