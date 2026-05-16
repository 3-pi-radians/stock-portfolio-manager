package com.stockportfolio.app.service;

import com.stockportfolio.app.dto.PortfolioRequest;
import com.stockportfolio.app.entity.Portfolio;
import com.stockportfolio.app.entity.User;
import com.stockportfolio.app.repository.PortfolioRepository;
import com.stockportfolio.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PortfolioService {

    private static final Logger log = LoggerFactory.getLogger(PortfolioService.class);

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    public PortfolioService(PortfolioRepository portfolioRepository, UserRepository userRepository) {
        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<Portfolio> getUserPortfolios() {
        return portfolioRepository.findByUser(getCurrentUser());
    }

    public Portfolio createPortfolio(PortfolioRequest request) {
        Portfolio portfolio = new Portfolio();
        portfolio.setName(request.getName());
        portfolio.setUser(getCurrentUser());
        log.info("Portfolio created: {} by {}", request.getName(),
                getCurrentUser().getUsername());
        return portfolioRepository.save(portfolio);
    }

    public Portfolio getPortfolioById(Long id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        if (!portfolio.getUser().getId().equals(getCurrentUser().getId())) {
            throw new RuntimeException("Access denied");
        }
        return portfolio;
    }

    @Transactional
    public void deletePortfolio(Long id) {
        Portfolio portfolio = getPortfolioById(id);
        portfolioRepository.delete(portfolio);
        log.info("Portfolio deleted: {}", id);
    }

    public List<Portfolio> getAllPortfolios() {
        return portfolioRepository.findAll();
    }
}
