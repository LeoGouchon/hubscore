package com.leogouchon.hubscore.squash_match_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SquashScoreDistributionDTO {
    private int count;
    private int winnerScore;
    private int loserScore;

    public SquashScoreDistributionDTO(int count, int winnerScore, int loserScore) {
        this.count = count;
        this.winnerScore = winnerScore;
        this.loserScore = loserScore;
    }
}
