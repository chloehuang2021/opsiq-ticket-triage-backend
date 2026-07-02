package com.chloe.opsiq_backend.controller;

import com.chloe.opsiq_backend.service.OpenRouterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiController {

    private final OpenRouterService openRouterService;

    public AiController(OpenRouterService openRouterService) {
        this.openRouterService = openRouterService;
    }

    @GetMapping("/api/ai/test")
    public String testConnection() {
        return openRouterService.testConnection();
    }

    @GetMapping("/api/ai/analyze-test")
    public String analyzeTest() {

        // Hard-coded input keeps prompt tuning independent from the frontend
        // while the AI integration is still under development.
        return openRouterService.analyzeTicket(
                "User cannot login to payroll system",
                "The employee says the password reset link expired and payroll is due today."
        );
    }

}

