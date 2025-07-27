package com.leogouchon.hubscore.squash_match_service.dto;

import com.leogouchon.hubscore.squash_match_service.entity.SquashMatches;
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

    public OverallStatsResponseDTO() {
    }

    public OverallStatsResponseDTO(int totalMatches, double averageLoserScore, int closeMatchesCount, int stompMatchesCount, SquashMatchResponseDTO[] closestMatches, SquashMatchResponseDTO[] stompestMatches) {
        this.totalMatches = totalMatches;
        this.averageLoserScore = averageLoserScore;
        this.closeMatchesCount = closeMatchesCount;
        this.stompMatchesCount = stompMatchesCount;
        this.closestMatches = closestMatches;
        this.stompestMatches = stompestMatches;
    }
}
