package com.leogouchon.hubscore.kicker_match_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SeasonalStatsDTO {
    private int year;
    private int quarter;
    private Long wins;
    private Long losses;
    private List<EloHistoryDTO> eloHistory;

    public SeasonalStatsDTO(int year, int quarter, Long wins, Long losses) {
        this.year = year;
        this.quarter = quarter;
        this.wins = wins;
        this.losses = losses;
    }
}