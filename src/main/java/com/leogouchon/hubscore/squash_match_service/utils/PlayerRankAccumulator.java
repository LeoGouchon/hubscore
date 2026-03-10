package com.leogouchon.hubscore.squash_match_service.utils;

import com.leogouchon.hubscore.player_service.entity.Players;
import com.leogouchon.hubscore.squash_common.type.PlayerRank;
import com.leogouchon.hubscore.squash_common.type.PlayerRankOpponentStats;
import lombok.Getter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerRankAccumulator {
    @Getter
    private final Players player;
    private int wins;
    private int losses;
    @Getter
    private int totalPointsScored;
    private int totalPointsReceived;
    private final Map<UUID, OpponentStatsAccumulator> opponents = new HashMap<>();

    public PlayerRankAccumulator(Players player) {
        this.player = player;
    }

    public void recordMatch(Players opponent, int pointsScored, int pointsReceived) {
        totalPointsScored += pointsScored;
        totalPointsReceived += pointsReceived;

        boolean hasWon = pointsScored > pointsReceived;
        if (hasWon) {
            wins++;
        } else {
            losses++;
        }

        opponents
                .computeIfAbsent(opponent.getId(), ignored -> new OpponentStatsAccumulator(opponent))
                .recordMatch(hasWon, pointsScored, pointsReceived);
    }

    public double getVictoryRate() {
        int totalMatches = wins + losses;
        if (totalMatches == 0) {
            return 0D;
        }
        return (double) wins / totalMatches;
    }

    public int getWinsAgainst(UUID opponentId) {
        OpponentStatsAccumulator stats = opponents.get(opponentId);
        return stats == null ? 0 : stats.getWins();
    }

    public PlayerRank toPlayerRank() {
        List<PlayerRankOpponentStats> statsAgainstPlayers = opponents.values().stream()
                .sorted(Comparator.comparing(stats -> stats.getOpponent().getFirstname(), String.CASE_INSENSITIVE_ORDER))
                .map(OpponentStatsAccumulator::toOpponentStats)
                .toList();

        return new PlayerRank(player, wins, losses, totalPointsScored, totalPointsReceived, statsAgainstPlayers);
    }
}
