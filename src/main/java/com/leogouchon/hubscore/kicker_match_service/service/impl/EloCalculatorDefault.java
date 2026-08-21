package com.leogouchon.hubscore.kicker_match_service.service.impl;

import com.leogouchon.hubscore.kicker_match_service.service.EloCalculatorService;
import com.leogouchon.hubscore.player_service.entity.Players;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class EloCalculatorDefault implements EloCalculatorService {
    private static final int MAX_SCORE_DIFFERENCE = 20;

    public int getInitialELo() {
        return 1500;
    }

    public int calculateDeltaElo(int k, double actualScore, double expectedScore) {
        return (int) Math.round(k * (actualScore - expectedScore));
    }

    public double averageElo(
            Players player1,
            Players player2,
            Map<UUID, Integer> currentElos
    ) {
        UUID id1 = player1.getId();
        Integer elo1 = currentElos.get(id1);

        if (elo1 == null) {
            throw new IllegalArgumentException("Missing elo for player " + id1);
        }

        if (player2 == null) {
            return elo1;
        }

        UUID id2 = player2.getId();
        Integer elo2 = currentElos.get(id2);

        if (elo2 == null) {
            throw new IllegalArgumentException("Missing elo for player " + id2);
        }

        return (elo1 + elo2) / 2.0;
    }

    public int calculateK(int scoreDiff) {
        int clamped = Math.clamp(scoreDiff, 1, MAX_SCORE_DIFFERENCE);

        double k = 15 * Math.log(clamped + 1.0);

        return (int) Math.round(k);
    }

    public double getScore(int scoreA, int scoreB) {
        if (scoreA == scoreB) {
            return 0.5;
        }

        int diff = Math.abs(scoreA - scoreB);
        int maxScore = Math.max(scoreA, scoreB);

        double normalized = (double) diff / maxScore;

        double curve = Math.pow(normalized, 0.1);

        double factor = 0.7 + curve * 0.3;

        return scoreA > scoreB ? factor : 1 - factor;
    }

    public double expectedResult(double eloTeamA, double eloTeamB) {
        return 1.0 / (1.0 + Math.pow(10, (eloTeamB - eloTeamA) / 600.0)); // https://en.wikipedia.org/wiki/Elo_rating_system#cite_note-29
    }
}
