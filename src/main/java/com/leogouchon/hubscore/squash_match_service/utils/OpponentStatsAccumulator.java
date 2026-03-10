package com.leogouchon.hubscore.squash_match_service.utils;

import com.leogouchon.hubscore.player_service.entity.Players;
import com.leogouchon.hubscore.squash_common.type.PlayerRankOpponentStats;
import lombok.Getter;

public class OpponentStatsAccumulator {
    @Getter
    private final Players opponent;
    @Getter
    private int wins;
    private int losses;
    private int pointsScored;
    private int pointsReceived;

    public OpponentStatsAccumulator(Players opponent) {
        this.opponent = opponent;
    }

    public void recordMatch(boolean hasWon, int scored, int received) {
        if (hasWon) {
            wins++;
        } else {
            losses++;
        }
        pointsScored += scored;
        pointsReceived += received;
    }

    public PlayerRankOpponentStats toOpponentStats() {
        return new PlayerRankOpponentStats(opponent, wins, losses, pointsScored, pointsReceived);
    }
}
