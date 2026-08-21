package com.leogouchon.hubscore.common.dto;

import java.util.Map;

public record ApiErrorResponseDTO(
        String message,
        Map<String, String> errors
) {
    public ApiErrorResponseDTO(String message) {
        this(message, Map.of());
    }
}
