package com.github.oleksii.oliinyk.ai.reading.tool.presentation.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String cause,
        String path
) {
    public ErrorResponse(
            int status,
            String error,
            String message,
            String cause,
            String path
    ){
        this(
             LocalDateTime.now(),
             status,
             error,
             message,
             cause,
             path
        );
    }
}
