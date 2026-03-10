package com.leogouchon.hubscore.squash_common.type;

import com.leogouchon.hubscore.player_service.entity.Players;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerRankOpponentStats {
    private Players opponent;
    private int wins;
    private int losses;
    private int pointsScored;
    private int pointsReceived;

    public PlayerRankOpponentStats() {
    }

    public PlayerRankOpponentStats(Players opponent, int wins, int losses, int pointsScored, int pointsReceived) {
        this.opponent = opponent;
        this.wins = wins;
        this.losses = losses;
        this.pointsScored = pointsScored;
        this.pointsReceived = pointsReceived;
    }
}
