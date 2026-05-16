package com.stockportfolio.app.controller;

import com.stockportfolio.app.dto.PortfolioRequest;
import com.stockportfolio.app.service.PortfolioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping
    public String listPortfolios(Model model) {
        model.addAttribute("portfolios", portfolioService.getUserPortfolios());
        model.addAttribute("portfolioRequest", new PortfolioRequest());
        model.addAttribute("currentUser", portfolioService.getCurrentUser());
        return "portfolio/list";
    }

    @PostMapping("/create")
    public String createPortfolio(@Valid @ModelAttribute PortfolioRequest request,
                                  Model model) {
        try {
            portfolioService.createPortfolio(request);
            return "redirect:/portfolio";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/portfolio";
        }
    }

    @PostMapping("/delete/{id}")
    public String deletePortfolio(@PathVariable Long id, Model model) {
        try {
            portfolioService.deletePortfolio(id);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/portfolio";
    }
}
