package com.github.oleksii.oliinyk.ai.reading.tool.presentation.dto;

import java.util.List;

public record FolderDocumentsResponse(
        String folderName,
        List<DocumentResponse> files
) {}
