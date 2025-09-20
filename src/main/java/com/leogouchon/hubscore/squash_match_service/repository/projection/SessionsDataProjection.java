package com.leogouchon.hubscore.squash_match_service.repository.projection;

import java.util.UUID;

public interface SessionsDataProjection {
    int getTotalMatches();
    int getWins();
    int getLosses();
    int getPointsScored();
    int getPointsConceded();
    String getPlayerName();
    UUID getPlayerId();
    long getDayUnix();
}
