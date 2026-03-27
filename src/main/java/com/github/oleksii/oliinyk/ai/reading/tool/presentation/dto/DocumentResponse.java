package com.github.oleksii.oliinyk.ai.reading.tool.presentation.dto;

public record DocumentResponse(
        String objectKey,
        String documentName,
        String contentType,
        Long sizeBytes
) {}
