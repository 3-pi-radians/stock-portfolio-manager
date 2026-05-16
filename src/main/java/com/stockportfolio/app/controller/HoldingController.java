package com.stockportfolio.app.controller;

import com.stockportfolio.app.dto.HoldingRequest;
import com.stockportfolio.app.entity.Holding;
import com.stockportfolio.app.service.HoldingService;
import com.stockportfolio.app.service.MarketService;
import com.stockportfolio.app.service.PortfolioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Controller
@RequestMapping("/portfolio/{portfolioId}/holdings")
public class HoldingController {

    private final HoldingService holdingService;
    private final PortfolioService portfolioService;
    private final MarketService marketService;

    public HoldingController(HoldingService holdingService,
                             PortfolioService portfolioService,
                             MarketService marketService) {
        this.holdingService = holdingService;
        this.portfolioService = portfolioService;
        this.marketService = marketService;
    }

    @GetMapping
    public String listHoldings(@PathVariable Long portfolioId, Model model) {
        List<Holding> holdings = holdingService.getHoldings(portfolioId);

        List<Map<String, Object>> enriched = new ArrayList<>();
        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalCurrent = BigDecimal.ZERO;

        for (Holding h : holdings) {
            Map<String, Object> item = new HashMap<>();
            item.put("holding", h);
            Map<String, Object> quote = marketService.getStockQuote(h.getStockSymbol());
            if (quote.containsKey("currentPrice")) {
                BigDecimal current = new BigDecimal(quote.get("currentPrice").toString());
                BigDecimal invested = h.getBuyPrice().multiply(BigDecimal.valueOf(h.getQuantity()));
                BigDecimal currentVal = current.multiply(BigDecimal.valueOf(h.getQuantity()));
                BigDecimal pnl = currentVal.subtract(invested);
                item.put("currentPrice", current);
                item.put("pnl", pnl.setScale(2, RoundingMode.HALF_UP));
                item.put("currentValue", currentVal.setScale(2, RoundingMode.HALF_UP));
                totalInvested = totalInvested.add(invested);
                totalCurrent = totalCurrent.add(currentVal);
            }
            enriched.add(item);
        }

        model.addAttribute("enrichedHoldings", enriched);
        model.addAttribute("portfolio", portfolioService.getPortfolioById(portfolioId));
        model.addAttribute("holdingRequest", new HoldingRequest());
        model.addAttribute("totalInvested", totalInvested.setScale(2, RoundingMode.HALF_UP));
        model.addAttribute("totalCurrent", totalCurrent.setScale(2, RoundingMode.HALF_UP));
        model.addAttribute("totalPnl",
                totalCurrent.subtract(totalInvested).setScale(2, RoundingMode.HALF_UP));
        model.addAttribute("transactions",
                holdingService.getTransactions(portfolioId));
        return "holdings/list";
    }

    @PostMapping("/add")
    public String addHolding(@PathVariable Long portfolioId,
                             @Valid @ModelAttribute HoldingRequest request,
                             Model model) {
        try {
            holdingService.addHolding(portfolioId, request);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/portfolio/" + portfolioId + "/holdings";
    }

    @PostMapping("/remove/{holdingId}")
    public String removeHolding(@PathVariable Long portfolioId,
                                @PathVariable Long holdingId,
                                Model model) {
        try {
            holdingService.removeHolding(portfolioId, holdingId);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/portfolio/" + portfolioId + "/holdings";
    }
}
