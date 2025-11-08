package com.leogouchon.hubscore.kicker_match_service.dto;

import java.util.List;

public record SeasonalStatsDTO(String year, String quarter, int rank, int wins, int losses,
                               List<EloHistoryDTO> eloHistory) {
}
