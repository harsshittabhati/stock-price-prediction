package com.example.stockpriceprediction.service;

import com.example.stockpriceprediction.model.StockData;
import com.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvService {

    private final StockDataService stockDataService;

    @Autowired
    public CsvService(StockDataService stockDataService) {
        this.stockDataService = stockDataService;
    }

    public List<StockData> parseCsvAndSave(MultipartFile file, 
                                            String symbol,
                                            String companyName) {
        List<StockData> savedStocks = new ArrayList<>();

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(file.getInputStream()))) {

            String[] line;
            boolean isHeader = true;

            while ((line = reader.readNext()) != null) {
                // Skip header row
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                try {
                    StockData stock = new StockData();
                    stock.setSymbol(symbol.toUpperCase());
                    stock.setCompanyName(companyName);
                    // CSV format: Date,Open,High,Low,Close,Volume
                    stock.setDate(LocalDate.parse(line[0].trim()));
                    stock.setOpenPrice(Double.parseDouble(
                            line[1].trim()));
                    stock.setHighPrice(Double.parseDouble(
                            line[2].trim()));
                    stock.setLowPrice(Double.parseDouble(
                            line[3].trim()));
                    stock.setClosePrice(Double.parseDouble(
                            line[4].trim()));
                    stock.setVolume(Long.parseLong(
                            line[5].trim()));

                    savedStocks.add(
                            stockDataService.saveStockData(stock));

                } catch (Exception e) {
                    // Skip invalid rows
                    continue;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedStocks;
    }
}