package com.leogouchon.hubscore.kicker_match_service.dto;

import java.util.UUID;

public record OpponentStatsDTO(UUID id, String firstname, String lastname, Long wins, Long loses) {
}
