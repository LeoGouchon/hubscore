package com.leogouchon.hubscore.kicker_match_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeasonStatsResponseDTO {
    private int year;
    private int quarter;
    private int nbMatches;
    private int nbPlayers;

    public SeasonStatsResponseDTO() {}
}
