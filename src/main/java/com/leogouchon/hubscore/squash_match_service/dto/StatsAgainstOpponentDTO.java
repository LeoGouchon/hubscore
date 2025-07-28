package com.leogouchon.hubscore.squash_match_service.dto;

import com.leogouchon.hubscore.player_service.entity.Players;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatsAgainstOpponentDTO {
    private Players opponentPlayer;
    private int totalMatches;
    private int wins;
    private int losses;
    private double averageLostScore;
    private int closeWonCount;
    private int closeLostCount;
    private int stompWonCount;
    private int stompLostCount;

    public StatsAgainstOpponentDTO() {
    }

    public StatsAgainstOpponentDTO(Players opponentPlayer, int totalMatches, int wins, int losses, double averageLostScore, int closeWonCount, int closeLostCount, int stompWonCount, int stompLostCount) {
        this.opponentPlayer = opponentPlayer;
        this.totalMatches = totalMatches;
        this.wins = wins;
        this.losses = losses;
        this.averageLostScore = averageLostScore;
        this.closeWonCount = closeWonCount;
        this.closeLostCount = closeLostCount;
        this.stompWonCount = stompWonCount;
        this.stompLostCount = stompLostCount;
    }
}
