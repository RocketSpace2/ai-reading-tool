package com.github.oleksii.oliinyk.ai.reading.tool.application;

import com.github.oleksii.oliinyk.ai.reading.tool.infrastructure.GeminiClient;
import com.github.oleksii.oliinyk.ai.reading.tool.presentation.dto.NotesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
public class NotesService {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private GeminiClient geminiClient;

    private record NotesGenerationResult(
            String notes,
            String chosenFolder,
            String fileName,
            String objectKey,
            List<String> suggestedSearchQueries,
            List<String> recommendedSources,
            List<String> nextTopics
    ) {}

    public NotesResponse makeAndUploadNote(MultipartFile file) {
        byte[] bytes;
        try {
            bytes = file.getInputStream().readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed reading uploaded file", e);
        }

        List<String> folders = documentService.listFolders();
        String prompt = NotesPromptBuilder.build(folders);

        String response = geminiClient.callGemini(bytes, prompt);

        NotesGenerationResult result = splitSections(response);

        documentService.upload(result.notes(), result.objectKey());

        return new NotesResponse(
                result.chosenFolder(),
                result.fileName(),
                result.objectKey(),
                result.suggestedSearchQueries(),
                result.recommendedSources(),
                result.nextTopics()
        );
    }

    private NotesGenerationResult splitSections(String response) {
        int storageRecIdx = response.indexOf("STORAGE_RECOMMENDATION");
        int notesIdx = response.indexOf("NOTES");
        int learnMoreIdx = response.indexOf("LEARN_MORE");

        String storageRec = response.substring(storageRecIdx + "STORAGE_RECOMMENDATION".length(), notesIdx);
        String notes = response.substring(notesIdx + "NOTES".length(), learnMoreIdx);
        String learnMore = response.substring(learnMoreIdx + "LEARN_MORE".length());

        return new NotesGenerationResult(
                notes,
                extractValue("chosen_folder: ", storageRec),
                extractValue("suggested_filename: ", storageRec),
                extractValue("object_key: ", storageRec),
                toList(extractValue("suggested_search_queries: " , learnMore)),
                toList(extractValue("recommended_sources: " , learnMore)),
                toList(extractValue("next_topics: " , learnMore))
        );
    }

    private String extractValue(String fieldName, String rawString){
        int fieldIdx = rawString.indexOf(fieldName);
        if (fieldIdx >= 0) {
            int startIdx = fieldIdx + fieldName.length();
            int endIdx = rawString.indexOf("\n", startIdx);
            if (endIdx < 0) endIdx = rawString.length();
            return rawString.substring(startIdx, endIdx);
        }

        return "";
    }

    private List<String> toList(String strs){
        if (strs.isEmpty()) return List.of();

        strs = strs.substring(1, strs.length() - 1);
        return Arrays.stream(strs.split(",")).map(String::trim).toList();
    }
}
