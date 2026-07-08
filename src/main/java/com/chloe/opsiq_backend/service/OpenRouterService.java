package com.chloe.opsiq_backend.service;

import com.chloe.opsiq_backend.dto.AiAnalysisResponse;
import com.chloe.opsiq_backend.model.ChatCompletionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.chloe.opsiq_backend.dto.AiAnalysisResponse;


@Service
public class OpenRouterService {

    @Value("${openrouter.api.key}")
    private String apiKey;

    private final RestClient restClient = RestClient.create();

    // Spring manages a shared ObjectMapper, so reuse it instead of creating our own.
    private final ObjectMapper objectMapper;

    public OpenRouterService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    //To check the connection of API
    public String testConnection() {

        String requestBody = """
                {
                  "model": "deepseek/deepseek-chat-v3-0324",
                  "messages": [
                    {
                      "role": "user",
                      "content": "Say hello in one short sentence."
                    }
                  ]
                }
                """;

        ChatCompletionResponse response = restClient.post()
                .uri("https://openrouter.ai/api/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(ChatCompletionResponse.class);

        // Return only the assistant's message instead of the full API response.
        return response.getChoices()
                .get(0)
                .getMessage()
                .getContent();
    }


    public AiAnalysisResponse analyzeTicket(String title, String description) {

        String prompt = """
        You are an experienced IT service desk analyst.

        Analyze the following support ticket.

        Return a raw JSON object only.
        Do not include markdown, code fences, explanations, or surrounding text.

        JSON schema:
        {
          "category": "string",
          "priority": "Low | Medium | High | Critical",
          "summary": "string",
          "suggestedSteps": ["string"]
        }

        Rules:
        - summary must be under 30 words.
        - suggestedSteps must include 2 to 4 concise troubleshooting steps.
        - priority should reflect business urgency and operational impact.

        Ticket title:
        %s

        Ticket description:
        %s
        """.formatted(title, description);

        String requestBody = """
                {
                  "model": "deepseek/deepseek-chat-v3-0324",
                  "max_tokens": 500,
                  "messages": [
                    {
                      "role": "user",
                      "content": %s
                    }
                  ]
                }
                """.formatted(toJsonString(prompt));

        ChatCompletionResponse response = restClient.post()
                .uri("https://openrouter.ai/api/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(ChatCompletionResponse.class);

        String content = response.getChoices()
                .get(0)
                .getMessage()
                .getContent();

        String normalizedContent = normalizeJsonResponse(content);

        try {
            return objectMapper.readValue(normalizedContent, AiAnalysisResponse.class);
        } catch (JsonProcessingException ex) {
            // Fail fast because the frontend depends on a predictable response contract.
            throw new IllegalStateException("AI returned an invalid ticket analysis response.", ex);
        }
    }


    // Models can still wrap JSON in Markdown even when the prompt says not to.
    // Normalize it once here so controller/frontend code receives clean JSON.
    private String normalizeJsonResponse(String content) {
        return content
                .replaceFirst("^```json\\s*", "")
                .replaceFirst("^```\\s*", "")
                .replaceFirst("\\s*```$", "")
                .trim();
    }

    // Escape quotes and line breaks so the prompt can be safely embedded in JSON.
    private String toJsonString(String text) {
        return "\"" + text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n") + "\"";
    }
}