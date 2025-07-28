package com.leogouchon.hubscore.squash_match_service.dto;

import com.leogouchon.hubscore.player_service.entity.Players;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerStatsResponseDTO {
    private Players player;
    private int totalMatches;
    private int wins;
    private int losses;
    private double averageLostScore;
    private int closeWonCount;
    private int closeLostCount;
    private int stompWonCount;
    private int stompLostCount;
    private StatsAgainstOpponentDTO[] statsAgainstOpponents;

    public PlayerStatsResponseDTO() {}
}
