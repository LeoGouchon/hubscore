package com.leogouchon.hubscore.kicker_match_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class GlobalStatsWithHistoryDTO {
    private UUID playerId;
    private String firstname;
    private String lastname;
    private int totalMatches;
    private int wins;
    private int losses;
    private BigDecimal winRate;
    private List<Boolean> lastMatches;

    public GlobalStatsWithHistoryDTO() {}
}
