package com.leogouchon.hubscore.kicker_match_service.dto;

import java.util.List;
import java.util.UUID;

public record PlayerStatsResponseDTO(UUID id,
                                     String firstname,
                                     String lastname,
                                     List<PartnerStatsDTO> statsPerPartner,
                                     List<OpponentStatsDTO> statsPerOpponent,
                                     List<SeasonalStatsDTO> seasonalStats,
                                     OverallStatsDTO allTimeStats
) {
}
