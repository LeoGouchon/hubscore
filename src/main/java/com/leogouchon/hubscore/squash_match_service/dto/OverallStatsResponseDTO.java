package com.leogouchon.hubscore.squash_match_service.dto;

import com.leogouchon.hubscore.squash_match_service.entity.SquashMatches;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OverallStatsResponseDTO {
    private int totalMatches;
    private int averageLoserScore;
    private SquashMatchResponseDTO[] closeMatchesCount;
    private SquashMatchResponseDTO[] stompMatchesCount;

    public OverallStatsResponseDTO() {
    }

    public OverallStatsResponseDTO(int totalMatches, int averageLoserScore, SquashMatchResponseDTO[] closeMatchesCount, SquashMatchResponseDTO[] stompMatchesCount) {
        this.totalMatches = totalMatches;
        this.averageLoserScore = averageLoserScore;
        this.closeMatchesCount = closeMatchesCount;
        this.stompMatchesCount = stompMatchesCount;
    }
}
