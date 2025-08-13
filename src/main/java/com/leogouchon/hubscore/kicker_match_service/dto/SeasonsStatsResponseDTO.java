package com.leogouchon.hubscore.kicker_match_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SeasonsStatsResponseDTO {
    private int nbSeasons;
    private int totalMatches;
    private int totalPlayers;
    private List<SeasonStatsResponseDTO> seasonsStats;

    public SeasonsStatsResponseDTO() {}

    public SeasonsStatsResponseDTO(int nbSeasons, int totalMatches, int totalPlayers, List<SeasonStatsResponseDTO> seasonsStats) {
        this.nbSeasons = nbSeasons;
        this.totalMatches = totalMatches;
        this.totalPlayers = totalPlayers;
        this.seasonsStats = seasonsStats;
    }
}
