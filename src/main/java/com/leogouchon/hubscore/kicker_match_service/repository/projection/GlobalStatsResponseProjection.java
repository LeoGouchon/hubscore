package com.leogouchon.hubscore.kicker_match_service.repository.projection;

import java.math.BigDecimal;
import java.util.UUID;

public interface GlobalStatsResponseProjection {
    UUID getPlayerId();
    String getFirstname();
    String getLastname();
    int getTotalMatches();
    int getWins();
    int getLosses();
    BigDecimal getWinRate();
    int getCurrentElo();
    int getRank();
}