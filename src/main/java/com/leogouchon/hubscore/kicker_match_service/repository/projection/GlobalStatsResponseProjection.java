package com.leogouchon.hubscore.kicker_match_service.repository.projection;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

public interface GlobalStatsResponseProjection {
    UUID getPlayerId();
    String getFirstname();
    String getLastname();
    int getTotalMatches();
    int getWins();
    int getLosses();
    BigDecimal getWinRate();
    Integer getCurrentElo();
    int getRank();
}