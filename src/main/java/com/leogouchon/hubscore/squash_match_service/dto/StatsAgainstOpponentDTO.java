package com.leogouchon.hubscore.squash_match_service.dto;

import com.leogouchon.hubscore.player_service.entity.Players;

public record StatsAgainstOpponentDTO(
        Players opponentPlayer,
        int totalMatches,
        int wins,
        int losses,
        double averageLostScore,
        int closeWonCount,
        int closeLostCount,
        int stompWonCount,
        int stompLostCount
) {
}
