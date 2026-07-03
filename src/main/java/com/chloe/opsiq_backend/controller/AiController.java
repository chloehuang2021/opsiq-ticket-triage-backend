package com.chloe.opsiq_backend.controller;

import com.chloe.opsiq_backend.dto.AiAnalysisRequest;
import com.chloe.opsiq_backend.service.OpenRouterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final OpenRouterService openRouterService;

    public AiController(OpenRouterService openRouterService) {
        this.openRouterService = openRouterService;
    }

    @GetMapping("/test")
    public String testConnection() {
        return openRouterService.testConnection();
    }

    @GetMapping("/analyze-test")
    public String analyzeTest() {
        // Hard-coded input keeps prompt tuning independent from frontend readiness.
        return openRouterService.analyzeTicket(
                "User cannot login to payroll system",
                "The employee says the password reset link expired and payroll is due today."
        );
    }

    @PostMapping("/analyze")
    public String analyzeTicket(@RequestBody AiAnalysisRequest request) {
        return openRouterService.analyzeTicket(
                request.title(),
                request.description()
        );
    }
}

