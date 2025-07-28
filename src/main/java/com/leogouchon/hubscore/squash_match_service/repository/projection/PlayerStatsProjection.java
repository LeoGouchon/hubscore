package com.leogouchon.hubscore.squash_match_service.repository.projection;

import java.util.UUID;

public interface PlayerStatsProjection {
    UUID getPlayerId();

    String getFirstname();

    String getLastname();

    int getTotalMatches();

    int getWins();

    int getLosses();

    Double getAverageLoserScore();

    int getCloseMatchesWonCount();

    int getCloseMatchesLostCount();

    int getStompMatchesWonCount();

    int getStompMatchesLostCount();
}
