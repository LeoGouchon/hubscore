package com.leogouchon.hubscore.player_service.dto;

import com.leogouchon.hubscore.player_service.entity.Players;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerResponseDTOTests {

    @Test
    void shouldKeepUnicodeCharactersFromDatabase() {
        Players player = new Players(UUID.randomUUID(), "LÃ©o", "D'HÃ©riÃ§y");

        PlayerResponseDTO response = new PlayerResponseDTO(player);

        assertEquals("LÃ©o", response.getFirstname());
        assertEquals("D'HÃ©riÃ§y", response.getLastname());
    }
}
