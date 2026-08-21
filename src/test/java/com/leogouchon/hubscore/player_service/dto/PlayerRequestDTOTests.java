package com.leogouchon.hubscore.player_service.dto;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerRequestDTOTests {

    @Test
    void rejectsScriptPayloadInFirstname() {
        PlayerRequestDTO dto = new PlayerRequestDTO("<script>alert(1)</script>", "Doe", List.of(java.util.UUID.randomUUID()));

        assertFalse(isValid(dto));
    }

    @Test
    void acceptsRegularPlayerNames() {
        PlayerRequestDTO dto = new PlayerRequestDTO("Jean-Luc", "O'Neil", List.of(java.util.UUID.randomUUID()));

        assertTrue(isValid(dto));
    }

    @Test
    void rejectsMissingTeamIds() {
        PlayerRequestDTO dto = new PlayerRequestDTO("John", "Doe", List.of());

        assertFalse(isValid(dto));
    }

    private boolean isValid(PlayerRequestDTO dto) {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            return validatorFactory.getValidator().validate(dto).isEmpty();
        }
    }
}
