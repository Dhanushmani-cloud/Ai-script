package com.example.aistoryboard;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.*;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

@RestController
@RequestMapping("/api")
public class AIStoryboardController {

    private static final String OPENAI_API_KEY = "YOUR_API_KEY_HERE";
    private static final String HISTORY_DIR = "script_history/";

    public AIStoryboardController() throws IOException {
        Files.createDirectories(Paths.get(HISTORY_DIR)); // Create folder if not exists
    }

    // Generate script
    @PostMapping("/generate-script")
    public String generateScript(@RequestParam String prompt, @RequestParam String characters) throws IOException {
        String fullPrompt = "Characters: " + characters + "\nScene: " + prompt + "\nGenerate a formatted script with dialogues.";
        String script = callOpenAI(fullPrompt);

        // Save history
        String filename = HISTORY_DIR + "script_" + System.currentTimeMillis() + ".txt";
        Files.write(Paths.get(filename), script.getBytes());

        return script;
    }

    // Generate AI suggestions
    @PostMapping("/generate-suggestions")
    public String generateSuggestions(@RequestParam String script) throws IOException {
        String prompt = "Here is a scene script:\n" + script + "\nSuggest alternative dialogues, plot twists, or improvements.";
        return callOpenAI(prompt);
    }

    // Download script as PDF
    @GetMapping("/download-pdf")
    public ResponseEntity<byte[]> downloadPdf(@RequestParam String scriptContent) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream content = new PDPageContentStream(document, page);

        content.setFont(PDType1Font.HELVETICA, 12);
        content.beginText();
        content.newLineAtOffset(50, 700);
        for (String line : scriptContent.split("\n")) {
            content.showText(line);
            content.newLineAtOffset(0, -15);
        }
        content.endText();
        content.close();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        document.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename("script.pdf").build());

        return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
    }

    // Load history list
    @GetMapping("/history")
    public List<String> getHistory() {
        File folder = new File(HISTORY_DIR);
        String[] files = folder.list();
        List<String> fileList = new ArrayList<>();
        if (files != null) {
            Arrays.sort(files, Comparator.reverseOrder());
            Collections.addAll(fileList, files);
        }
        return fileList;
    }

    // Load a saved script
    @GetMapping("/load-script")
    public String loadScript(@RequestParam String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(HISTORY_DIR + filename)));
    }

    // Call OpenAI API
    private String callOpenAI(String prompt) throws IOException {
        String apiUrl = "https://api.openai.com/v1/chat/completions";

        String jsonInput = "{ \"model\":\"gpt-4\", \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}] }";

        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + OPENAI_API_KEY);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonInput.getBytes("utf-8"));
        }

        StringBuilder result = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                result.append(line);
            }
        }

        return result.toString();
    }
}
