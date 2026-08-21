package com.leogouchon.hubscore.kicker_match_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OverallStatsDTO {
    private Long wins;
    private Long losses;
    private List<EloHistoryDTO> eloHistory;

    public OverallStatsDTO(Long wins, Long losses) {
        this.wins = wins;
        this.losses = losses;
    }
}
