package com.leogouchon.hubscore.squash_match_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OverallStatsResponseDTO {
    private int totalMatches;
    private double averageLoserScore;
    private int closeMatchesCount;
    private int stompMatchesCount;
    private SquashMatchResponseDTO[] closestMatches;
    private SquashMatchResponseDTO[] stompestMatches;
    private SquashScoreDistributionDTO[] scoreDistribution;

    public OverallStatsResponseDTO() {

    }
}
