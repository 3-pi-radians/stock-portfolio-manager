package com.stockportfolio.auth.controller;

import com.stockportfolio.auth.dto.LoginRequest;
import com.stockportfolio.auth.dto.RegisterRequest;
import com.stockportfolio.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest request,
                           Model model) {
        try {
            authService.register(request);
            return "redirect:/auth/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginRequest request,
                        HttpServletResponse response,
                        Model model) {
        try {
            String token = authService.login(request);
            response.addHeader("Set-Cookie",
                    "jwt=" + token + "; Path=/; HttpOnly; Max-Age=86400; SameSite=Lax");
            return "redirect:/portfolio";
        } catch (Exception e) {
            model.addAttribute("error", "Invalid username or password");
            return "auth/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        response.addHeader("Set-Cookie",
                "jwt=; Path=/; HttpOnly; Max-Age=0; SameSite=Lax");
        return "redirect:/auth/login";
    }
}
