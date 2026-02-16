package com.example.aistoryboard;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AIStoryboardController {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .build();

    @PostMapping("/generate-script")
    public ResponseEntity<Map<String, String>> generateScript(@RequestBody Map<String, String> request) {

        String prompt = request.get("prompt");
        String characters = request.get("characters");

        if (prompt == null || characters == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid input"));
        }

        String fullPrompt = "Create a professional movie script scene.\n\n" +
                "Characters: " + characters + "\n\n" +
                "Scene Description: " + prompt + "\n\n" +
                "Write dialogues and proper screenplay format.";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", fullPrompt));
        requestBody.put("messages", messages);

        try {
            Map response = webClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + openaiApiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List choices = (List) response.get("choices");
            Map firstChoice = (Map) choices.get(0);
            Map message = (Map) firstChoice.get("message");

            String aiScript = message.get("content").toString();

            return ResponseEntity.ok(Map.of("script", aiScript));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "AI generation failed: " + e.getMessage()));
        }
    }
}

