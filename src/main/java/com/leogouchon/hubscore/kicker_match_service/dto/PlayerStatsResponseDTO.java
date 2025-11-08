package com.leogouchon.hubscore.kicker_match_service.dto;

import java.util.List;

public record PlayerStatsResponseDTO(String id, String firstname, String lastname,
                                     List<PartnerStatsDTO> statsPerPartner, List<OpponentStatsDTO> statsPerOpponent,
                                     List<SeasonalStatsDTO> seasonalStats, OverallStatsDTO allTimeStats) {
}
