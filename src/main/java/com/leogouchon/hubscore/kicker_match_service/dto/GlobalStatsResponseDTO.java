package com.leogouchon.hubscore.kicker_match_service.dto;

import java.math.BigDecimal;
import java.util.UUID;

public interface GlobalStatsResponseDTO {
    UUID getPlayerId();
    String getFirstname();
    String getLastname();
    int getTotalMatches();
    int getWins();
    int getLosses();
    BigDecimal getWinRate();
}