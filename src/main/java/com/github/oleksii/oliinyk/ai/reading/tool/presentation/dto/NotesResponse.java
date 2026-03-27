package com.github.oleksii.oliinyk.ai.reading.tool.presentation.dto;

import java.util.List;

public record NotesResponse(
        String chosenFolder,
        String fileName,
        String objectKey,
        List<String> suggestedSearchQueries,
        List<String> recommendedSources,
        List<String> nextTopics
) {}
