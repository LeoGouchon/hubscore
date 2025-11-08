package com.leogouchon.hubscore.kicker_match_service.dto;

import java.util.List;

public record OverallStatsDTO(int wins, int losses, int rank, List<EloHistoryDTO> eloHistory) {
}
