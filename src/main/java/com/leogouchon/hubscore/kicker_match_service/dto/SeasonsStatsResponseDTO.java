package com.leogouchon.hubscore.kicker_match_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SeasonsStatsResponseDTO {
    private int nbSeasons;
    private List<SeasonStatsResponseDTO> seasonsStats;

    public SeasonsStatsResponseDTO() {}

    public SeasonsStatsResponseDTO(int nbSeasons, List<SeasonStatsResponseDTO> seasonsStats) {
        this.nbSeasons = nbSeasons;
        this.seasonsStats = seasonsStats;
    }
}
