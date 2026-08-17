package com.leogouchon.husbcore.kicker_match_service.dto;

import java.util.UUID;

public record OpponentStatsDTO(UUID id, String firstname, String lastname, Long wins, Long loses) {
}
