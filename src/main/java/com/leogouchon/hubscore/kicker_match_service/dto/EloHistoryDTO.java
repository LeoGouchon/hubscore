package com.leogouchon.hubscore.kicker_match_service.dto;

import java.sql.Timestamp;

public record EloHistoryDTO(
        Timestamp date,
        int elo,
        int max,
        int min,
        int firstQuartile,
        int thirdQuartile
) {
}
