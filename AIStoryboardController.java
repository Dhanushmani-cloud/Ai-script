package com.example.aistoryboard;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")  // Prevent CORS issues
public class AIStoryboardController {

    // POST method to generate script
    @PostMapping("/generate-script")
    public Map<String, String> generateScript(@RequestBody Map<String, String> request) {

        String prompt = request.get("prompt");
        String characters = request.get("characters");

        // Simple AI-style formatted script (Demo Logic)
        String generatedScript =
                "TITLE: Generated Film Scene\n\n" +
                "CHARACTERS: " + characters + "\n\n" +
                "SCENE:\n" +
                prompt + "\n\n" +
                "DIALOGUE:\n" +
                characters.split(",")[0].trim() + ": What is happening here?\n" +
                characters.split(",")[1].trim() + ": Something unexpected is about to begin.";

        Map<String, String> response = new HashMap<>();
        response.put("script", generatedScript);

        return response;
    }
}

