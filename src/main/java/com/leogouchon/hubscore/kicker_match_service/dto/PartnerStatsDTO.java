package com.leogouchon.hubscore.kicker_match_service.dto;

import java.util.UUID;

public record PartnerStatsDTO(UUID id,
                              String firstname,
                              String lastname,
                              Long totalMatches,
                              Long wins,
                              Long loses) {
}
