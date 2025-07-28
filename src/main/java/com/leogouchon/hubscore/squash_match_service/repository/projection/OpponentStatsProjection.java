package com.leogouchon.hubscore.squash_match_service.repository.projection;

import com.leogouchon.hubscore.squash_match_service.dto.SquashMatchResponseDTO;

import java.util.UUID;

public interface OpponentStatsProjection {
    UUID getOpponentId();

    String getOpponentFirstname();
    String getOpponentLastname();

    int getTotalMatches();

    int getWins();

    int getLosses();

    Double getAverageScoreWhenLost();

    int getCloseWonCount();
    int getCloseLostCount();

    int getStompsWonCount();
    int getStompsLostCount();
}
