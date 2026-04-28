package com.example.stockpriceprediction.controller;
import com.example.stockpriceprediction.service.StockApiService;
import com.example.stockpriceprediction.model.StockData;
import com.example.stockpriceprediction.service.StockDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.stockpriceprediction.service.CsvService;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class StockWebController {

    private final StockDataService stockDataService;
    private final StockApiService stockApiService;
    private final CsvService csvService;

@Autowired
public StockWebController(StockDataService stockDataService,
                        StockApiService stockApiService,
                        CsvService csvService) {
    this.stockDataService = stockDataService;
    this.stockApiService = stockApiService;
    this.csvService = csvService;
}

    // Home page - show all stocks
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("stocks", stockDataService.getAllStockData());
        return "index";
    }

    // Search page - search by symbol
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String symbol, 
                         Model model) {
        if (symbol != null && !symbol.isEmpty()) {
            model.addAttribute("stocks", 
                stockDataService.getStockDataBySymbol(symbol));
            model.addAttribute("symbol", symbol);

            // Add prediction
            StockData latest = stockDataService.getLatestStock(symbol);
            if (latest != null) {
                double prediction = stockDataService.predictNextPrice(latest);
                model.addAttribute("prediction", prediction);
                model.addAttribute("latestStock", latest);
            }
        }
        return "search";
    }

    // Add stock page
    @GetMapping("/add")
    public String addStockForm(Model model) {
        model.addAttribute("stockData", new StockData());
        return "add";
    }

    // Save new stock
    @PostMapping("/add")
    public String saveStock(@ModelAttribute StockData stockData) {
        stockDataService.saveStockData(stockData);
        return "redirect:/";
    }

    // Dashboard page
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stocks", stockDataService.getAllStockData());
        model.addAttribute("totalStocks", 
            stockDataService.getAllStockData().size());
        return "dashboard";
    }

    // Delete stock
    @GetMapping("/delete/{id}")
    public String deleteStock(@PathVariable Long id) {
        stockDataService.deleteStockData(id);
        return "redirect:/";
    }
    // Fetch live stock data from Alpha Vantage
    @PostMapping("/fetch")
    public String fetchStockData(@RequestParam String symbol) {
        stockApiService.fetchAndSaveStockData(symbol.toUpperCase());
        return "redirect:/";
    }
    // Show CSV upload page
    @GetMapping("/upload")
    public String uploadPage() {
        return "upload";
    }

    // Handle CSV upload
    @PostMapping("/upload")
    public String uploadCsv(@RequestParam("file") MultipartFile file,
                        @RequestParam String symbol,
                        @RequestParam String companyName) {
        csvService.parseCsvAndSave(file, symbol, companyName);
        return "redirect:/";
    }
}