package com.leogouchon.hubscore.squash_match_service.dto;


public record YearlyPlayerStatsDTO(
        int year,
        int totalMatches,
        int wins,
        int losses,
        double averageOpponentLostScore,
        double averagePlayerLostScore,
        int closeWonCount,
        int closeLostCount,
        int stompWonCount,
        int stompLostCount
) {
}
