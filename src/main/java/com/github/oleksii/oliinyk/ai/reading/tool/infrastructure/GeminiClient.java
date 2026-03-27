package com.github.oleksii.oliinyk.ai.reading.tool.infrastructure;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.genai.types.Part;

@Service
public class GeminiClient {

    private final Client client;

    @Value("${gemini.model:gemini-3-flash-preview}")
    private String model;

    public GeminiClient(@Value("${gemini.api-key}") String geminiKey){
        this.client = Client.builder().apiKey(geminiKey).build();
    }

    public String callGemini(byte[] fileBytes, String prompt) {
        GenerateContentResponse response = client.models.generateContent(
                model,
                Content.fromParts(
                        Part.fromBytes(fileBytes, "application/pdf"),
                        Part.fromText(prompt)
                ),
                null
        );

        return response.text() != null ? response.text() : "";
    }
}
