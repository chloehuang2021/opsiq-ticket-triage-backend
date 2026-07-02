package com.chloe.opsiq_backend.dto;

import java.util.List;

public record AiAnalysisResponse(
        String category,
        String priority,
        String summary,
        List<String> suggestedSteps
) {
}