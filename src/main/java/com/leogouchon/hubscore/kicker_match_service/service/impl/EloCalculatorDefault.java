package com.leogouchon.hubscore.kicker_match_service.service.impl;

import com.leogouchon.hubscore.kicker_match_service.service.EloCalculatorService;
import com.leogouchon.hubscore.player_service.entity.Players;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class EloCalculatorDefault implements EloCalculatorService {
    public int getInitialELo() {
        return 1500;
    }

    public int calculateDeltaElo(int K, double actualScore, double expectedScore) {
        return (int) Math.round(K * (actualScore - expectedScore));
    }

    public double averageElo(Players player1, Players player2, Map<UUID, Integer> currentElos) {
        if (player2 != null) {
            return (double) (currentElos.get(player1.getId()) + currentElos.get(player2.getId())) / 2;
        }
        return currentElos.get(player1.getId());
    }

    public int calculateK(int scoreDiff) {
        int MAX_SCORE_DIFFERENCE = 20;
        int clamped = Math.max(1, Math.min(scoreDiff, MAX_SCORE_DIFFERENCE));

        double k = 15 * Math.log(clamped + 1);

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

    public double exceptedResult(double eloTeamA, double eloTeamB) {
        return  1 / (1 + Math.pow(10, (eloTeamB - eloTeamA) / 600.0)); // https://en.wikipedia.org/wiki/Elo_rating_system#cite_note-29
    }
}
