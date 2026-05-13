package com.stockportfolio.app.controller;

import com.stockportfolio.app.service.MarketService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/market")
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @GetMapping
    public String marketPage(Model model) {
        return "market/search";
    }

    @GetMapping("/search")
    public String searchStock(@RequestParam String symbol, Model model) {
        model.addAttribute("quote", marketService.getStockQuote(symbol));
        model.addAttribute("symbol", symbol.toUpperCase());
        return "market/search";
    }
}
